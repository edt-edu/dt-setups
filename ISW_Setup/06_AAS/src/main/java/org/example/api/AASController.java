package org.example.api;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.xml.XmlDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.example.aasManager.AASManager;
import org.example.aasManager.AASOperationException;
import org.example.aasManager.AASXImporter;
import org.example.aasManager.AASXExporter;
import org.example.aasManager.DockerOptionsHandler;
import org.example.database.DockerRepository;
import org.example.database.MongoAASManager;
import org.example.database.model.MongoAssetAdministrationShell;
import org.example.database.model.SubmodelData;
import org.example.service.AASQueryService;
import org.example.service.mapper.BasyxConverterService;
import org.example.service.query.queryFile.FileQueryHelper;
import org.example.database.fileParser.FileContext;
import org.example.database.fileParser.FileContextInitializer;
import org.example.database.fileParser.FileContextInitializerRegistry;
import org.example.database.fileParser.QueryableProperty;
import org.example.database.fileParser.QueryablePropertyRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;




@RestController
@RequestMapping("/api/aas")
public class AASController {

    private static final Logger log = LoggerFactory.getLogger(AASController.class);
    private final AASManager aasManager;
    private final AASXImporter aasxImporter;
    private final AASXExporter aasxExporter;
    private final MongoAASManager mongoAASManager;
    private final DockerOptionsHandler dockerOptionsHandler;
    private final DockerRepository dockerRepository;

    // Executor service for handling container operations
    private final ExecutorService containerExecutor = Executors.newFixedThreadPool(3); // We adjust number of threads as needed
    private final AASQueryService aasQueryService;
    private final BasyxConverterService basyxConverterService;
    private final FileQueryHelper fileQueryHelper;
    private final FileContextInitializerRegistry fileContextInitializerRegistry;
    private final QueryablePropertyRegistry queryablePropertyRegistry;
   

    @Autowired
    public AASController(AASManager aasManager, AASXImporter aasxImporter, AASXExporter aasxExporter, MongoAASManager mongoAASManager,
                         AASQueryService aasQueryService,
                         BasyxConverterService basyxConverterService, DockerOptionsHandler dockerOptionsHandler, DockerRepository dockerRepository,
                         FileQueryHelper fileQueryHelper,
                         FileContextInitializerRegistry fileContextInitializerRegistry,
                         QueryablePropertyRegistry queryablePropertyRegistry) {
        this.aasManager = aasManager;
        this.aasxImporter = aasxImporter;
        this.aasxExporter = aasxExporter;
        this.mongoAASManager = mongoAASManager;
        this.aasQueryService = aasQueryService;
        this.basyxConverterService = basyxConverterService;
        this.fileQueryHelper = fileQueryHelper;
        this.fileContextInitializerRegistry = fileContextInitializerRegistry;
        this.queryablePropertyRegistry = queryablePropertyRegistry;
        
        this.dockerOptionsHandler = dockerOptionsHandler;
        this.dockerRepository = dockerRepository;
    }

    // BaSyx operations
    @PostMapping("/create")
    public ResponseEntity<String> createAAS(@RequestBody DefaultAssetAdministrationShell aas) {
        try {
            aasManager.createAAS(aas);
            // Sync to MongoDB
            mongoAASManager.saveToMongo(aas);
            return ResponseEntity.ok("AAS Created and synced to MongoDB");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while creating AAS: " + e.getMessage());
        }
    }

    @GetMapping("/{aasId:.+}")
    public ResponseEntity<AssetAdministrationShell> getAAS(@PathVariable("aasId") String aasId) {
        return getAssetAdministrationShellResponseEntity(aasId);
    }

    @GetMapping
    public ResponseEntity<AssetAdministrationShell> getAASIdURL(@RequestParam("aasId") String aasId) {
        return getAssetAdministrationShellResponseEntity(aasId);
    }

    private ResponseEntity<AssetAdministrationShell> getAssetAdministrationShellResponseEntity(@RequestParam("aasId") String aasId) {
        AssetAdministrationShell aas = aasManager.getAAS(aasId);
        if(aas == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            runOnAccessDockerFile(aas, aasId);
            return ResponseEntity.ok(aas);
        }
    }

    private void runOnAccessDockerFile(AssetAdministrationShell aas, String aasId){
        List<Submodel> submodels = getDockerOptionsSubmodel(aas);
        if(!submodels.isEmpty()){
            List<Submodel> validSubmodels = new ArrayList<>();
            for(Submodel sm : submodels) {
                String execTrigger = dockerOptionsHandler.getOpt(sm, "executionTrigger");
                if (execTrigger.equals("onAccess")) {
                    validSubmodels.add(sm);
                }
            }

            runDockerContainersAsync(aasId, validSubmodels);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<AssetAdministrationShell>> getAllAAS(@RequestParam(defaultValue = "100") int size) {
        try {
            PaginationInfo paginationInfo = new PaginationInfo(size, null);
            CursorResult<List<AssetAdministrationShell>> result = aasManager.getAllAAS(paginationInfo);
            for(AssetAdministrationShell aas : result.getResult()){
                runOnAccessDockerFile(aas, aas.getId());
            }
            return ResponseEntity.ok(result.getResult());
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Export all AAS data as XML
     * @return XML content as application/xml response
     */
    @GetMapping(value = "/export", produces = "application/xml")
    public ResponseEntity<String> exportAllToXml() {
        try {
            log.info("Received request to export all AAS data as XML");
            String xmlContent = aasxExporter.exportAllToXml();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xmlContent);
        } catch (IOException e) {
            log.error("Failed to export AAS data as XML", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("<error>Failed to export AAS data: " + e.getMessage() + "</error>");
        }
    }

    /**
     * Export a specific AAS by idShort as XML
     * @param idShort The idShort of the AAS to export
     * @return XML content as application/xml response
     */
    @GetMapping(value = "/export/{idShort}", produces = "application/xml")
    public ResponseEntity<String> exportAASByIdShort(@PathVariable("idShort") String idShort) {
        try {
            log.info("Received request to export AAS with idShort '{}' as XML", idShort);
            String xmlContent = aasxExporter.exportAASByIdShort(idShort);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xmlContent);
        } catch (IOException e) {
            log.error("Failed to export AAS with idShort '{}' as XML: {}", idShort, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("<error>Failed to export AAS data: " + e.getMessage() + "</error>");
        }
    }

    @PutMapping("/{aasId}")
    public ResponseEntity<String> updateAAS(@PathVariable("aasId") String aasId, @RequestBody DefaultAssetAdministrationShell aas) {
        return updateAASHelper(aasId, aas);
    }

    @PutMapping
    public ResponseEntity<String> updateAASIdURL(@RequestParam("aasId") String aasId, @RequestBody DefaultAssetAdministrationShell aas) {
        return updateAASHelper(aasId, aas);
    }

    private ResponseEntity<String> updateAASHelper(@RequestParam("aasId") String aasId, @RequestBody DefaultAssetAdministrationShell aas) {
        try {
            aasManager.updateAAS(aasId, aas);
            // Sync to MongoDB
            mongoAASManager.saveToMongo(aas);
            List<Submodel> submodels = getDockerOptionsSubmodel(aas);

            if(!submodels.isEmpty()){
                List<Submodel> validSubmodels = new ArrayList<>();
                for(Submodel sm : submodels) {
                    String execTrigger = dockerOptionsHandler.getOpt(sm, "executionTrigger");
                    if (execTrigger.equals("onAccess") || execTrigger.equals("onUpdate")) {
                        validSubmodels.add(sm);
                    }
                }

                runDockerContainersAsync(aasId, validSubmodels);
            }
            return ResponseEntity.ok("AAS Updated and synced to MongoDB");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while updating AAS" + e.getMessage());
        }
    }

    private void runDockerContainersAsync(String aasId, List<Submodel> validSubmodels) {
        List<CompletableFuture<Void>> containerFutures = new ArrayList<>();
        for(Submodel sm : validSubmodels) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    dockerOptionsHandler.applyContainerOptions(sm, aasManager, dockerRepository, aasId);
                } catch (URISyntaxException e) {
                    log.error("Error while applying container options: {}", e.getMessage(), e);
                }
            }, containerExecutor);
            containerFutures.add(future);
        }

        try{
            CompletableFuture.allOf(containerFutures.toArray(new CompletableFuture[0])).join();
        }catch (CompletionException e){
            throw new AASOperationException(e.getMessage());
        }
    }

    @DeleteMapping("/{aasId}")
    public ResponseEntity<String> deleteAAS(@PathVariable("aasId") String aasId) {
        try {
            aasManager.deleteAAS(aasId);
            return ResponseEntity.ok("AAS Deleted");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while deleting AAS" + e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAASIdURL(@RequestParam("aasId") String aasId) {
        try {
            aasManager.deleteAAS(aasId);
            return ResponseEntity.ok("AAS Deleted");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while deleting AAS" + e.getMessage());
        }
    }

    @PostMapping("/submodel/create")
    public ResponseEntity<String> createSubmodel(@RequestBody DefaultSubmodel submodel) {
        try {
            aasManager.createSubmodel(submodel);
            // Sync to MongoDB
            mongoAASManager.saveToMongo(submodel);
            return ResponseEntity.ok("Submodel Created and synced to MongoDB");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while creating Submodel" + e.getMessage());
        }
    }

    @PostMapping("/{submodelId}/linkWith/{aasId}")
    public ResponseEntity<String> linkSubmodel(@PathVariable("aasId") String aasId, @PathVariable("submodelId") String submodelId) {
        try {
            aasManager.linkSubmodelToAAS(submodelId, aasId);
            return ResponseEntity.ok("AAS Linked With Submodel");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while linking Submodel" + e.getMessage());
        }
    }

    @PostMapping("/{submodelId}/linkWith")
    public ResponseEntity<String> linkSubmodelIdURL(@RequestParam("aasId") String aasId, @PathVariable("submodelId") String submodelId) {
        try {
            aasManager.linkSubmodelToAAS(submodelId, aasId);
            return ResponseEntity.ok("AAS Linked With Submodel");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while linking Submodel" + e.getMessage());
        }
    }

    @PostMapping("/{submodelId}/unlinkFrom/{aasId}")
    public ResponseEntity<String> unlinkSubmodel(@PathVariable("aasId") String aasId, @PathVariable("submodelId") String submodelId) {
        try {
            aasManager.unlinkSubmodelFromAAS(submodelId, aasId);
            return ResponseEntity.ok("AAS Unlinked from Submodel");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while unlinking Submodel" + e.getMessage());
        }
    }

    @PostMapping("/{submodelId}/unlinkFrom")
    public ResponseEntity<String> unlinkSubmodelIdURL(@RequestParam("aasId") String aasId, @PathVariable("submodelId") String submodelId) {
        try {
            aasManager.unlinkSubmodelFromAAS(submodelId, aasId);
            return ResponseEntity.ok("AAS Unlinked from Submodel");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while unlinking Submodel" + e.getMessage());
        }
    }

    @GetMapping("submodel/{submodelId}")
    public ResponseEntity<Submodel> getSubmodel(@PathVariable("submodelId") String submodelId) {
        try {
            Submodel submodel = aasManager.getSubmodel(submodelId);
            if(submodel == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else {
                return ResponseEntity.ok(submodel);
            }
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //Update a submodel
    @PutMapping("/submodel/{submodelId}")
    public ResponseEntity<String> updateSubmodel(@PathVariable("submodelId") String submodelId, @RequestBody DefaultSubmodel submodel) {
        try {
            aasManager.updateSubmodel(submodelId, submodel);
            // Sync to MongoDB
            mongoAASManager.saveToMongo(submodel);
            return ResponseEntity.ok("Submodel updated and synced to MongoDB");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while updating Submodel" + e.getMessage());
        }
    }

    @GetMapping("/allSubmodels")
    public ResponseEntity<List<Submodel>> getAllSubmodels(@RequestParam(defaultValue = "100") int size) {
        try {
            PaginationInfo paginationInfo = new PaginationInfo(size, null);
            CursorResult<List<Submodel>> result = aasManager.getAllSubmodels(paginationInfo);
            return ResponseEntity.ok(result.getResult());
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("submodel/{submodelId}")
    public ResponseEntity<String> deleteSubmodel(@PathVariable("submodelId") String submodelId) {
        try {
            aasManager.deleteSubmodel(submodelId);
            return ResponseEntity.ok("Submodel Deleted");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while deleting Submodel" + e.getMessage());
        }
    }

    @PostMapping("/import")
    public ResponseEntity<String> importAAS(@RequestParam("file") MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            // Import AASX file
            aasxImporter.importAASX(in, aasManager, dockerOptionsHandler, dockerRepository);
            
            // Get all AAS and save to MongoDB
            PaginationInfo paginationInfo = new PaginationInfo(100, null);
            CursorResult<List<AssetAdministrationShell>> aasResult = aasManager.getAllAAS(paginationInfo);
            for (AssetAdministrationShell aas : aasResult.getResult()) {
                mongoAASManager.saveToMongo(aas);
                
                // Get and save all submodels referenced by this AAS
                if (aas.getSubmodels() != null) {
                    for (Reference ref : aas.getSubmodels()) {
                        String submodelId = ref.getKeys().getFirst().getValue();
                        Submodel submodel = aasManager.getSubmodel(submodelId);
                        if (submodel != null) {
                            mongoAASManager.saveToMongo(submodel);
                        }
                    }
                }
            }
            
            // Get and save all concept descriptions
            PaginationInfo cdPaginationInfo = new PaginationInfo(100, null);
            CursorResult<List<ConceptDescription>> cdResult = aasManager.getAllConceptDescriptions(cdPaginationInfo);
            for (ConceptDescription cd : cdResult.getResult()) {
                mongoAASManager.saveToMongo(cd);
            }
            
            return ResponseEntity.ok("AAS, Submodels, and ConceptDescriptions imported and saved to MongoDB");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error while importing AASX: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Import AAS data directly from an XML file
     * @param file XML file containing AAS data
     * @return Status message
     */
    @PostMapping("/import-xml")
    public ResponseEntity<String> importXML(@RequestParam("file") MultipartFile file) {
        try (InputStream xmlStream = file.getInputStream()) {
            log.info("Importing AAS data from XML file");
            
            // Parse XML content using the XmlDeserializer
            XmlDeserializer parser = new XmlDeserializer();
            Environment environment = parser.read(xmlStream);
            log.info("Parsed AAS XML successfully");
            
            List<Submodel> submodels = new ArrayList<>();
            
            // Add or Update each submodel
            for (Submodel sm : environment.getSubmodels()) {
                try {
                    aasManager.createSubmodel(sm);
                    log.info("Added Submodel: {}", sm.getId());
                } catch (AASOperationException e) {
                    aasManager.updateSubmodel(sm.getId(), sm);
                    log.info("Updated Submodel: {}", sm.getId());
                }
                submodels.add(sm);
            }
            
            // Build the temp submodel→AAS map
            Map<String, String> submodelToAas = new HashMap<>();
            
            // Add or Update each AAS
            for (AssetAdministrationShell aas : environment.getAssetAdministrationShells()) {
                try {
                    aasManager.createAAS(aas);
                    log.info("Added Asset Administration Shell: {}", aas.getId());
                    
                    String aasId = aas.getId();
                    
                    // Record mapping
                    aas.getSubmodels().forEach(ref ->
                            submodelToAas.put(ref.getKeys().getFirst().getValue(), aasId)
                    );
                } catch (AASOperationException e) {
                    aasManager.updateAAS(aas.getId(), aas);
                    String aasId = aas.getId();
                    
                    aas.getSubmodels().forEach(ref ->
                            submodelToAas.put(ref.getKeys().getFirst().getValue(), aasId)
                    );
                    log.info("Updated Asset Administration Shell: {}", aas.getId());
                }
            }
            
            // Add or Update each Concept Description
            for (ConceptDescription cd : environment.getConceptDescriptions()) {
                try {
                    aasManager.createConceptDescription(cd);
                    log.info("Added Concept Description: {}", cd.getId());
                } catch (AASOperationException e) {
                    aasManager.updateConceptDescription(cd.getId(), cd);
                    log.info("Updated Concept Description: {}", cd.getId());
                }
            }
            
            // Handle Docker options if needed
            List<CompletableFuture<Void>> containerFutures = new ArrayList<>();
            
            // Apply dockerOptions submodel
            submodels.stream()
                    .filter(sm -> sm.getId().contains("docker-options"))
                    .filter(sm -> {
                        String trigger = dockerOptionsHandler.getOpt(sm, "executionTrigger");
                        return "onInitialize".equals(trigger) || "onUpdate".equals(trigger) || "onAccess".equals(trigger);
                    })
                    .forEach(sm -> {
                        String aasId = submodelToAas.get(sm.getId());
                        if (aasId == null) {
                            throw new AASOperationException("Could not find corresponding AAS for submodel");
                        }
                        
                        // Start containers asynchronously
                        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                            try {
                                dockerOptionsHandler.applyContainerOptions(sm, aasManager, dockerRepository, aasId);
                            } catch (URISyntaxException e) {
                                throw new RuntimeException(e);
                            }
                        }, containerExecutor);
                        
                        containerFutures.add(future);
                    });
            
            try {
                // Wait for all futures to complete before proceeding
                CompletableFuture.allOf(containerFutures.toArray(new CompletableFuture[0])).join();
            } catch (CompletionException e) {
                throw new RuntimeException(e);
            }
            
            // Save all to MongoDB
            syncToMongoDB();
            
            return ResponseEntity.ok("AAS, Submodels, and ConceptDescriptions imported from XML and saved to MongoDB");
        } catch (IOException e) {
            log.error("Error importing XML file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while importing XML: " + e.getMessage());
        } catch (DeserializationException e) {
            log.error("Error deserializing XML content", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deserializing XML content: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during XML import", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @PostMapping("/mongodb/sync")
    public ResponseEntity<String> syncToMongoDB() {
        try {
            // Get all AAS from BaSyx
            PaginationInfo paginationInfo = new PaginationInfo(100, null);
            CursorResult<List<AssetAdministrationShell>> aasResult = aasManager.getAllAAS(paginationInfo);
            int aasCount = 0;
            int submodelCount = 0;
            int cdCount = 0;

            // Sync each AAS and its submodels
            for (AssetAdministrationShell aas : aasResult.getResult()) {
                mongoAASManager.saveToMongo(aas);
                aasCount++;

                // Get and sync all submodels referenced by this AAS
                if (aas.getSubmodels() != null) {
                    for (Reference ref : aas.getSubmodels()) {
                        String submodelId = ref.getKeys().getFirst().getValue();
                        Submodel submodel = aasManager.getSubmodel(submodelId);
                        if (submodel != null) {
                            mongoAASManager.saveToMongo(submodel);
                            submodelCount++;
                        }
                    }
                }
            }

            // Get and sync all concept descriptions
            PaginationInfo cdPaginationInfo = new PaginationInfo(100, null);
            CursorResult<List<ConceptDescription>> cdResult = aasManager.getAllConceptDescriptions(cdPaginationInfo);
            for (ConceptDescription cd : cdResult.getResult()) {
                mongoAASManager.saveToMongo(cd);
                cdCount++;
            }

            return ResponseEntity.ok(String.format("Sync completed. Synced %d AAS, %d Submodels, and %d ConceptDescriptions to MongoDB",
                                                  aasCount, submodelCount, cdCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error while syncing to MongoDB: " + e.getMessage());
        }
    }

    // Clear all data
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearAll() {
        try {
            // Get all AAS and delete them
            PaginationInfo paginationInfo = new PaginationInfo(100, null);
            CursorResult<List<AssetAdministrationShell>> aasResult = aasManager.getAllAAS(paginationInfo);
            for (AssetAdministrationShell aas : aasResult.getResult()) {
                aasManager.deleteAAS(aas.getId());
            }
            
            
            // Clear MongoDB data
            mongoAASManager.deleteAllMongoData();
            
            return ResponseEntity.ok("All data cleared from BaSyx and MongoDB");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error while clearing data: " + e.getMessage());
        }
    }

    // MongoDB specific operations
    @GetMapping("/mongodb/shells")
    public ResponseEntity<List<MongoAssetAdministrationShell>> getAllMongoShells() {
        return ResponseEntity.ok(mongoAASManager.getAllMongoAAS());
    }

    /**
     * Get all AAS from MongoDB and convert them to BaSyx-compatible format
     * This ensures that data retrieved from MongoDB matches the structure of objects from BaSyx repositories
     */
    @GetMapping("/mongodb/shells/basyx")
    public ResponseEntity<List<AssetAdministrationShell>> getAllMongoShellsAsBasyxAAS() {
        List<MongoAssetAdministrationShell> mongoShells = mongoAASManager.getAllMongoAAS();
        List<AssetAdministrationShell> basyxShells = mongoShells.stream()
                .map(basyxConverterService::convertToBasyxAAS)
                .toList();
        return ResponseEntity.ok(basyxShells);
    }

    @GetMapping("/mongodb/shells/{idShort}")
    public ResponseEntity<?> getMongoShellByIdShort(@PathVariable String idShort) {
        MongoAssetAdministrationShell aas = mongoAASManager.getMongoAASByIdShort(idShort);
        if (aas == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(aas);
    }

    /**
     * Get an AAS from MongoDB by idShort and convert it to BaSyx-compatible format
     * This ensures that data retrieved from MongoDB matches the structure of objects from BaSyx repositories
     */
    @GetMapping("/mongodb/shells/{idShort}/basyx")
    public ResponseEntity<?> getMongoShellByIdShortAsBasyxAAS(@PathVariable String idShort) {
        MongoAssetAdministrationShell mongoAAS = mongoAASManager.getMongoAASByIdShort(idShort);
        if (mongoAAS == null) {
            return ResponseEntity.notFound().build();
        }
        AssetAdministrationShell basyxAAS = basyxConverterService.convertToBasyxAAS(mongoAAS);
        return ResponseEntity.ok(basyxAAS);
    }

    @GetMapping("/mongodb/submodels")
    public ResponseEntity<List<SubmodelData>> getAllMongoSubmodels() {
        return ResponseEntity.ok(mongoAASManager.getAllMongoSubmodels());
    }

    /**
     * Get all Submodels from MongoDB and convert them to BaSyx-compatible format
     * This ensures that data retrieved from MongoDB matches the structure of objects from BaSyx repositories
     */
    @GetMapping("/mongodb/submodels/basyx")
    public ResponseEntity<List<Submodel>> getAllMongoSubmodelsAsBasyxSubmodels() {
        List<SubmodelData> mongoSubmodels = mongoAASManager.getAllMongoSubmodels();
        List<Submodel> basyxSubmodels = mongoSubmodels.stream()
                .map(basyxConverterService::convertToBasyxSubmodel)
                .toList();
        return ResponseEntity.ok(basyxSubmodels);
    }

    @GetMapping("/mongodb/submodels/{idShort}")
    public ResponseEntity<?> getMongoSubmodelByIdShort(@PathVariable String idShort) {
        SubmodelData submodel = mongoAASManager.getMongoSubmodelByIdShort(idShort);
        if (submodel == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(submodel);
    }

    /**
     * Get a Submodel from MongoDB by idShort and convert it to BaSyx-compatible format
     * This ensures that data retrieved from MongoDB matches the structure of objects from BaSyx repositories
     */
    @GetMapping("/mongodb/submodels/{idShort}/basyx")
    public ResponseEntity<?> getMongoSubmodelByIdShortAsBasyxSubmodel(@PathVariable String idShort) {
        SubmodelData mongoSubmodel = mongoAASManager.getMongoSubmodelByIdShort(idShort);
        if (mongoSubmodel == null) {
            return ResponseEntity.notFound().build();
        }
        Submodel basyxSubmodel = basyxConverterService.convertToBasyxSubmodel(mongoSubmodel);
        return ResponseEntity.ok(basyxSubmodel);
    }

    @GetMapping("/mongodb/stats")
    public ResponseEntity<Map<String, Long>> getMongoStats() {
        Map<String, Long> stats = new HashMap<>();
        List<MongoAssetAdministrationShell> shells = mongoAASManager.getAllMongoAAS();
        List<SubmodelData> submodels = mongoAASManager.getAllMongoSubmodels();
        List<org.example.database.model.ConceptDescription> conceptDescriptions =
            mongoAASManager.getAllMongoConceptDescriptions();

        stats.put("totalShells", (long) shells.size());
        stats.put("totalSubmodels", (long) submodels.size());
        stats.put("totalConceptDescriptions", (long) conceptDescriptions.size());

        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/mongodb/clear")
    public ResponseEntity<?> clearAllMongo() {
        mongoAASManager.deleteAllMongoData();
        return ResponseEntity.ok("All MongoDB data cleared successfully");
    }

    @GetMapping("/allCD")
    public ResponseEntity<List<ConceptDescription>> getAllCD(@RequestParam(defaultValue = "100") int size) {
        try {
            PaginationInfo paginationInfo = new PaginationInfo(size, null);
            CursorResult<List<ConceptDescription>> result = aasManager.getAllConceptDescriptions(paginationInfo);
            return ResponseEntity.ok(result.getResult());
        } catch(Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/mongodb/conceptdescriptions")
    public ResponseEntity<List<org.example.database.model.ConceptDescription>> getAllMongoConceptDescriptions() {
        return ResponseEntity.ok(mongoAASManager.getAllMongoConceptDescriptions());
    }

    /**
     * Get all ConceptDescriptions from MongoDB and convert them to BaSyx-compatible format
     * This ensures that data retrieved from MongoDB matches the structure of objects from BaSyx repositories
     */
    @GetMapping("/mongodb/conceptdescriptions/basyx")
    public ResponseEntity<List<ConceptDescription>> getAllMongoConceptDescriptionsAsBasyxCDs() {
        log.info("Retrieving all ConceptDescriptions from MongoDB and converting to BaSyx format");
        List<org.example.database.model.ConceptDescription> mongoCDs = mongoAASManager.getAllMongoConceptDescriptions();
        log.info("Retrieved {} ConceptDescriptions from MongoDB", mongoCDs.size());

        // Log details about the first few ConceptDescriptions for debugging
        if (!mongoCDs.isEmpty()) {
            for (int i = 0; i < Math.min(3, mongoCDs.size()); i++) {
                org.example.database.model.ConceptDescription mongoCD = mongoCDs.get(i);
                log.info("MongoDB ConceptDescription {}: id={}, embeddedDataSpecs={}",
                        i, mongoCD.getId(),
                        (mongoCD.getEmbeddedDataSpecifications() != null) ?
                                mongoCD.getEmbeddedDataSpecifications().size() : "null");
            }
        }

        // Convert to BaSyx format
        List<ConceptDescription> basyxCDs = new ArrayList<>();
        for (org.example.database.model.ConceptDescription mongoCD : mongoCDs) {
            ConceptDescription basyxCD = basyxConverterService.convertToBasyxConceptDescription(mongoCD);
            basyxCDs.add(basyxCD);

            // Log details about the conversion for debugging
            log.info("Converted ConceptDescription: id={}, embeddedDataSpecs={}",
                    basyxCD.getId(),
                    (basyxCD.getEmbeddedDataSpecifications() != null) ?
                            basyxCD.getEmbeddedDataSpecifications().size() : "null");
        }

        return ResponseEntity.ok(basyxCDs);
    }

    @GetMapping("/mongodb/conceptdescriptions/{idShort}")
    public ResponseEntity<?> getMongoConceptDescriptionByIdShort(@PathVariable String idShort) {
        org.example.database.model.ConceptDescription cd = mongoAASManager.getMongoConceptDescriptionByIdShort(idShort);
        if (cd == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cd);
    }

    /**
     * Get a ConceptDescription from MongoDB by idShort and convert it to BaSyx-compatible format
     * This ensures that data retrieved from MongoDB matches the structure of objects from BaSyx repositories
     */
    @GetMapping("/mongodb/conceptdescriptions/{idShort}/basyx")
    public ResponseEntity<?> getMongoConceptDescriptionByIdShortAsBasyxCD(@PathVariable String idShort) {
        org.example.database.model.ConceptDescription mongoCD = mongoAASManager.getMongoConceptDescriptionByIdShort(idShort);
        if (mongoCD == null) {
            return ResponseEntity.notFound().build();
        }
        ConceptDescription basyxCD = basyxConverterService.convertToBasyxConceptDescription(mongoCD);
        return ResponseEntity.ok(basyxCD);
    }

    /**
     * Execute a SQL query against MongoDB collections
     * The collection to query is determined from the SQL statement (FROM clause)
     * Supports SELECT, UPDATE, and DELETE operations
     * @param sqlQuery The SQL query to execute
     * @return Result of the query execution
     */
    @PostMapping("/query")
    public ResponseEntity<?> executeQuery(@RequestBody String sqlQuery) {
        log.info("Received SQL query: {}", sqlQuery);
        try {
            // Clean the SQL query to handle potential issues with URLs and markdown
            sqlQuery = sqlQuery.trim();

            // Remove any surrounding quotes if present
            if ((sqlQuery.startsWith("\"") && sqlQuery.endsWith("\"")) ||
                (sqlQuery.startsWith("'") && sqlQuery.endsWith("'"))) {
                sqlQuery = sqlQuery.substring(1, sqlQuery.length() - 1);
            }

            // Remove any markdown formatting
            sqlQuery = sqlQuery.replaceAll("\\[([^\\]]+)\\]\\(([^\\)]+)\\)", "$1");

            log.info("Cleaned SQL query: {}", sqlQuery);

            // Determine the query type based on the first word
            String queryType = sqlQuery.trim().split("\\s+")[0].toUpperCase();

            switch (queryType) {
                case "SELECT":
                    List<Map<String, Object>> selectResults = aasQueryService.executeQuery(sqlQuery);
                    return ResponseEntity.ok(selectResults);

                case "UPDATE":
                    Map<String, Object> updateResult = aasQueryService.executeUpdateQuery(sqlQuery);
                    return ResponseEntity.ok(updateResult);

                case "DELETE":
                    Map<String, Object> deleteResult = aasQueryService.executeDeleteQuery(sqlQuery);
                    return ResponseEntity.ok(deleteResult);

                default:
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "Unsupported query type: " + queryType,
                        "supportedTypes", List.of("SELECT", "UPDATE", "DELETE")
                    ));
            }
        } catch (Exception e) {
            log.error("Error executing SQL query: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Api for onDemand docker containers
    @PostMapping("/docker/start")
    public ResponseEntity<String> triggerDockerOnDemand(@RequestParam("aasId") String aasId) {
        try {
            // Find the AAS
            AssetAdministrationShell aas = aasManager.getAAS(aasId);
            List<Submodel> submodels = getDockerOptionsSubmodel(aas);
            if (submodels.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Docker options submodel not found for AAS: " + aasId);
            }

            List<Submodel> validSubmodels = new ArrayList<>();

            for (Submodel sm : submodels) {
                //We need to make sure that the executionTrigger is set to onDemand
                String execTrigger = dockerOptionsHandler.getOpt(sm, "executionTrigger");
                if (execTrigger.equals("onDemand")) {
                    validSubmodels.add(sm);
                }
            }

            runDockerContainersAsync(aasId, validSubmodels);
            return ResponseEntity.ok("Docker file with onDemand trigger executed" );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error while starting Docker container: " + e);
        }
    }

    @PostMapping("/docker/stop")
    public ResponseEntity<String> stopDockerOnDemand(@RequestParam("aasId") String aasId) {
        try {
            // Find the AAS
            AssetAdministrationShell aas = aasManager.getAAS(aasId);
            String aasIdShort = aas.getIdShort();
            List<Submodel> submodels = getDockerOptionsSubmodel(aas);

            if (submodels.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Docker options submodel not found for AAS: " + aasId);
            }

            List<Submodel> validSubmodels = new ArrayList<>();
            for (Submodel sm : submodels) {
                // We need to make sure that the terminationTrigger is set to onDemand
                String termTrigger = dockerOptionsHandler.getOpt(sm, "terminationTrigger");
                if (termTrigger.equals("onDemand")) {
                    validSubmodels.add(sm);
                }
            }

            List<CompletableFuture<Void>> containerTerminationFutures = new ArrayList<>();
            for (Submodel sm : validSubmodels) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
// Terminate the docker container
                        String dockerSource = dockerOptionsHandler.getOpt(sm, "dockerSource");
                        if (dockerSource == null) {
                            String dockerURL = dockerOptionsHandler.getOpt(sm, "dockerURL");
                            URI uri = new URI(dockerURL);
                            dockerSource = Paths.get(uri.getPath()).getFileName().toString();
                        }
                        dockerOptionsHandler.cleanupOldContainers(aasIdShort, dockerSource);
                    } catch (Exception e) {
                        throw new AASOperationException(e.getMessage());
                    }
                }, containerExecutor);
                containerTerminationFutures.add(future);
            }
            try{
                CompletableFuture.allOf(containerTerminationFutures.toArray(new CompletableFuture[0])).join();
                return ResponseEntity.ok("Docker files with onDemand trigger terminated");
            }catch (CompletionException e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error while terminating Docker container: " + e.getCause());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while stopping Docker container: " + e.getMessage());
        }
    }

    private List<Submodel> getDockerOptionsSubmodel(AssetAdministrationShell aas){
        return aas.getSubmodels().stream()
                .map(ref -> ref.getKeys().getFirst().getValue())
                .filter(id -> id.contains("docker-options"))
                .map(aasManager::getSubmodel)
                .filter(Objects::nonNull)
                .toList();
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

   

    /**
     * Test file property extractors directly
     * @param submodelIdShort The idShort of the submodel containing files to test
     * @param propertyPath Optional specific property path to extract (if null, extracts all properties)
     * @return Extracted file properties from all files in the submodel
     */
    @GetMapping("/test-file-extractors")
    public ResponseEntity<?> testFileExtractors(
            @RequestParam("submodelIdShort") String submodelIdShort,
            @RequestParam(value = "propertyPath", required = false) String propertyPath) {
        
        log.info("Testing file extractors for submodel: {}, property: {}", 
                submodelIdShort, propertyPath != null ? propertyPath : "all");
        
        try {
            // Find all files in the submodel
            Map<String, List<String>> filePathsByType = fileQueryHelper.findFilePathsForSubmodel(submodelIdShort);
            
            if (filePathsByType.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "message", "No files found in submodel: " + submodelIdShort,
                    "submodelIdShort", submodelIdShort
                ));
            }
            
            log.info("Found files by content type: {}", filePathsByType);
            
            // Results map to store extracted properties
            Map<String, Object> results = new HashMap<>();
            results.put("submodelIdShort", submodelIdShort);
            results.put("fileCount", filePathsByType.values().stream().mapToInt(List::size).sum());
            results.put("contentTypes", filePathsByType.keySet());
            
            // Process each file and extract properties
            Map<String, List<Map<String, Object>>> fileProperties = new HashMap<>();
            
            for (Map.Entry<String, List<String>> entry : filePathsByType.entrySet()) {
                String contentType = entry.getKey();
                List<String> filePaths = entry.getValue();
                
                List<Map<String, Object>> propertiesForType = new ArrayList<>();
                
                for (String filePath : filePaths) {
                    Map<String, Object> fileResult = new HashMap<>();
                    fileResult.put("filePath", filePath);
                    
                    try {
                        // Get the appropriate initializer for this content type
                        Optional<FileContextInitializer> initializerOpt = fileContextInitializerRegistry.getInitializer(contentType);
                        if (!initializerOpt.isPresent()) {
                            fileResult.put("error", "No initializer found for content type: " + contentType);
                            propertiesForType.add(fileResult);
                            continue;
                        }
                        
                        // Create a File object and initialize the context
                        File file = new File(filePath);
                        if (!file.exists()) {
                            fileResult.put("error", "File does not exist: " + filePath);
                            propertiesForType.add(fileResult);
                            continue;
                        }
                        
                        // Initialize the file context
                        try (FileContext context = initializerOpt.get().initialize(file)) {
                            Map<String, Object> extractedProperties = new HashMap<>();
                            
                            // If a specific property path is requested, extract only that property
                            if (propertyPath != null && !propertyPath.isEmpty()) {
                                QueryableProperty property = queryablePropertyRegistry.getPropertyByPath(propertyPath);
                                if (property != null && Arrays.asList(property.getSupportedContentTypes()).contains(contentType)) {
                                    try {
                                        Object value = property.extract(context);
                                        extractedProperties.put(property.getPropertyPath(), value);
                                    } catch (Exception e) {
                                        log.error("Error extracting property {} from file {}: {}", 
                                                propertyPath, filePath, e.getMessage(), e);
                                        extractedProperties.put(propertyPath, "ERROR: " + e.getMessage());
                                    }
                                } else {
                                    extractedProperties.put(propertyPath, "Property not supported for content type: " + contentType);
                                }
                            } else {
                                // Extract all properties supported for this content type
                                for (QueryableProperty property : queryablePropertyRegistry.getPropertiesForContentType(contentType)) {
                                    try {
                                        Object value = property.extract(context);
                                        extractedProperties.put(property.getPropertyPath(), value);
                                    } catch (Exception e) {
                                        log.error("Error extracting property {} from file {}: {}", 
                                                property.getPropertyPath(), filePath, e.getMessage(), e);
                                        extractedProperties.put(property.getPropertyPath(), "ERROR: " + e.getMessage());
                                    }
                                }
                            }
                            
                            fileResult.put("properties", extractedProperties);
                        }
                    } catch (Exception e) {
                        log.error("Error processing file {}: {}", filePath, e.getMessage(), e);
                        fileResult.put("error", e.getMessage());
                    }
                    
                    propertiesForType.add(fileResult);
                }
                
                fileProperties.put(contentType, propertiesForType);
            }
            
            results.put("fileProperties", fileProperties);
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Error testing file extractors: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
