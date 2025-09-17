package org.example.aasManager;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.example.database.DockerRepository;
import org.springframework.stereotype.Service;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.xml.XmlDeserializer;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Imports AASX packages and processes their contents.
 * Handles two possible XML file location patterns:
 * 1. Inside "aasx" folder as "data.xml"
 * 2. Inside a subfolder of "aasx" named after the AAS, with file named "[AAS name].aas.xml"
 */
@Service
@Slf4j
public class AASXImporter {

    // Temporary directory for extracting files
    private static final String TEMP_DIR = "temp_aasx_extract";
    
    // Standard path for the XML file in the first pattern
    private static final String STANDARD_XML_PATH = "aasx/data.xml";
    
    // Pattern for AAS subfolder XML files
    private static final Pattern AAS_SUBFOLDER_XML_PATTERN = Pattern.compile("aasx/[^/]+/[^/]+\\.aas\\.xml");

    // Executor service for handling container operations
    private final ExecutorService containerExecutor = Executors.newFixedThreadPool(3); // We adjust number of threads based on needs

    public void importAASX(InputStream aasxStream, AASManager aasManager, DockerOptionsHandler doc, DockerRepository dockerRepo) throws IOException {
        log.info("Importing AASX file");

        // Create temporary directory for extraction
        Path tempDir = createTempDir();
        try {
            // Extract all files
            Map<String, Path> extractedFiles = extractAASXContents(aasxStream, tempDir);
            
            // Find and process the AAS XML file
            Path aasXmlPath = findAASXmlFile(extractedFiles);
            
            if (aasXmlPath == null) {
                throw new IOException("No valid AAS XML file found in the AASX package");
            }
            
            // Process the AAS content
            processXMLContent(aasXmlPath, tempDir, aasManager, doc, dockerRepo);
            
        } catch (Exception e) {
            log.error("Error importing AASX file", e);
            throw new IOException("Failed to import AASX file: " + e.getMessage(), e);
        } finally {
            // Clean up the temporary directory
            cleanUpTempDir(tempDir);
        }
    }


    ////////////Helper methods//////////////


    //Creates a temporary directory for extracting AASX contents.
    private Path createTempDir() throws IOException {
        Path tempDir = Paths.get(TEMP_DIR);
        if (Files.exists(tempDir)) {
            cleanUpTempDir(tempDir);
        }
        return Files.createDirectories(tempDir);
    }

    // Cleans up the temporary directory.
    private void cleanUpTempDir(Path tempDir) {
        try {
            if (Files.exists(tempDir)) {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.warn("Failed to delete temp file {}", path, e);
                            }
                        });
            }
        } catch (IOException e) {
            log.warn("Error cleaning up temporary directory", e);
        }
    }

    //Extracts the contents of an AASX package.
    private Map<String, Path> extractAASXContents(InputStream aasxStream, Path targetDir) throws IOException {
        Map<String, Path> extractedFiles = new HashMap<>();

        try (ZipInputStream zipIn = new ZipInputStream(aasxStream)) {
            ZipEntry zipEntry;
            while ((zipEntry = zipIn.getNextEntry()) != null) {
                String entryName = zipEntry.getName();
                Path filePath = targetDir.resolve(entryName);

                // Create parent directories if they don't exist
                if (filePath.getParent() != null) {
                    Files.createDirectories(filePath.getParent());
                }

                // Extract file
                if (!zipEntry.isDirectory()) {
                    Files.copy(zipIn, filePath, StandardCopyOption.REPLACE_EXISTING);
                    extractedFiles.put(entryName, filePath);
                    log.debug("Extracted file: {}", entryName);
                }

                zipIn.closeEntry();
            }
        }
        
        log.info("Extracted {} files from AASX package", extractedFiles.size());
        return extractedFiles;
    }

    //Finds the AAS XML file in the extracted files.
    private Path findAASXmlFile(Map<String, Path> extractedFiles) {
        // Case 1: Check for standard path (aasx/data.xml)
        if (extractedFiles.containsKey(STANDARD_XML_PATH)) {
            log.info("Found AAS XML file at standard path: {}", STANDARD_XML_PATH);
            return extractedFiles.get(STANDARD_XML_PATH);
        }
        
        // Case 2: Check for AAS subfolder pattern (aasx/[AAS name]/[AAS name].aas.xml)
        Optional<Map.Entry<String, Path>> aasSubfolderXml = extractedFiles.entrySet().stream()
                .filter(entry -> AAS_SUBFOLDER_XML_PATTERN.matcher(entry.getKey()).matches())
                .findFirst();
                
        if (aasSubfolderXml.isPresent()) {
            String path = aasSubfolderXml.get().getKey();
            log.info("Found AAS XML file in AAS subfolder: {}", path);
            return aasSubfolderXml.get().getValue();
        }
        
        // Fallback: Look for any XML file in the aasx folder
        Optional<Map.Entry<String, Path>> anyXmlInAasxFolder = extractedFiles.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("aasx/") && 
                                entry.getKey().toLowerCase().endsWith(".xml"))
                .findFirst();
                
        if (anyXmlInAasxFolder.isPresent()) {
            String path = anyXmlInAasxFolder.get().getKey();
            log.info("Found potential AAS XML file: {}", path);
            return anyXmlInAasxFolder.get().getValue();
        }
        
        log.warn("No AAS XML file found in the AASX package");
        return null;
    }


    //Processes the AAS content from the XML file
    private void processXMLContent(Path aasXmlPath, Path tempDir, AASManager aasManager, DockerOptionsHandler dockerOptionsService, DockerRepository dockerRepo) throws IOException {
        log.info("Parsing AAS XML file");

        XmlDeserializer parser = new XmlDeserializer();

        try(InputStream xmlStream = Files.newInputStream(aasXmlPath)){
            Environment environment = parser.read(xmlStream);
            log.info("Parsed AAS XML successfully");
            List<Submodel> submodels = new ArrayList<>();

            // Add or Update each submodel
            for(Submodel sm : environment.getSubmodels()){
                try {
                    aasManager.createSubmodel(sm);
                    log.info("Added Submodel: {}", sm.getId());
                }catch (AASOperationException e){
                    aasManager.updateSubmodel(sm.getId(), sm);
                    log.info("Updated Submodel: {}", sm.getId());
                }
                submodels.add(sm);
            }

            // Build the temp submodel→AAS map
            Map<String,String> submodelToAas = new HashMap<>();

            // Add or Update each AAS
            for(AssetAdministrationShell aas : environment.getAssetAdministrationShells()){
                try {
                    aasManager.createAAS(aas);
                    log.info("Added Asset Administration Shell: {}", aas.getId());

                    String aasIdShort = aas.getIdShort();

                    String aasId = aas.getId();

                    //Adding dockerFiles to the repository
                    addDockerFilesToRepo(aasIdShort, tempDir, dockerRepo);

                    // record mapping
                    aas.getSubmodels().forEach(ref ->
                            submodelToAas.put(ref.getKeys().getFirst().getValue(), aasId)
                    );
                }catch (AASOperationException e){
                    aasManager.updateAAS(aas.getId(), aas);
                    String aasId = aas.getId();

                    String aasIdShort = aas.getIdShort();
                    addDockerFilesToRepo(aasIdShort, tempDir, dockerRepo);

                    aas.getSubmodels().forEach(ref ->
                            submodelToAas.put(ref.getKeys().getFirst().getValue(), aasId)
                    );
                    log.info("Updated Asset Administration Shell: {}", aas.getId());
                }
            }

            // Add or Update each CD
            for(ConceptDescription cd : environment.getConceptDescriptions()){
                try {
                    aasManager.createConceptDescription(cd);
                    log.info("Added Concept Description: {}", cd.getId());
                }catch (AASOperationException e){
                    aasManager.updateConceptDescription(cd.getId(), cd);
                    log.info("Updated Concept Description: {}", cd.getId());
                }
            }

            List<CompletableFuture<Void>> containerFutures = new ArrayList<>();

            // Apply dockerOptions submodel
            submodels.stream()
                    .filter(sm -> sm.getId().contains("docker-options"))
                    .filter(sm -> {
                        String trigger = dockerOptionsService.getOpt(sm, "executionTrigger");
                        return "onInitialize".equals(trigger) || "onUpdate".equals(trigger) || "onAccess".equals(trigger);
                    })
                    .forEach(sm -> {
                        String aasId = submodelToAas.get(sm.getId());
                        if (aasId == null){
                            throw new AASOperationException("Could not find corresponding AAS for submodel");
                        }

                        // Start containers asynchronously
                        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                            try {
                                dockerOptionsService.applyContainerOptions(sm, aasManager, dockerRepo, aasId);
                            } catch (URISyntaxException e) {
                                throw new RuntimeException(e);
                            }
                        }, containerExecutor);

                        containerFutures.add(future);

                    });

            try{
                // Wait for all futures to complete before proceeding
                CompletableFuture.allOf(containerFutures.toArray(new CompletableFuture[0])).join();
            }catch (CompletionException e){
                throw new RuntimeException(e);
            }


        } catch (DeserializationException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    //Helper method to add Docker files and directories to the repository
    private void addDockerFilesToRepo(String aasIdShort, Path tempDir, DockerRepository dockerRepo) throws IOException {
        Path dockerFolder = tempDir.resolve("aasx/docker");

        if (!Files.exists(dockerFolder)) return;

        // Process all direct children of the docker folder
        Files.list(dockerFolder).forEach(path -> {
            try {
                String dockerContextName = dockerFolder.relativize(path).toString();
                System.out.println("Processing Docker context: " + path + 
                                  (Files.isDirectory(path) ? " (directory)" : " (file)"));
                
                dockerRepo.registerDockerFileForAasId(aasIdShort, dockerContextName, path);
            } catch (IOException e) {
                throw new AASOperationException("Failed to store Docker context in MongoDB: " + path, e);
            }
        });
    }

    @PreDestroy
    public void onDestroy(){
        // Close the executor services when the application shuts down
        containerExecutor.shutdown();
        try{
            if(!containerExecutor.awaitTermination(5, TimeUnit.SECONDS)){
                containerExecutor.shutdownNow();
            }
        }catch (InterruptedException e){
            containerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
