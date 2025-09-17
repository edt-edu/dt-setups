package org.example.aasManager;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.xml.XmlSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exports BaSyx objects to XML format.
 * This class provides functionality to convert Asset Administration Shells, Submodels,
 * and ConceptDescriptions back to XML format.
 */
@Service
@Slf4j
public class AASXExporter {

    private final AASManager aasManager;

    @Autowired
    public AASXExporter(AASManager aasManager) {
        this.aasManager = aasManager;
        log.info("AASXExporter initialized");
    }

    /**
     * Export all AAS data as XML
     * @return XML string representation of the entire environment
     * @throws IOException if serialization fails
     */
    public String exportAllToXml() throws IOException {
        try {
            log.info("Starting export of all AAS data to XML");
            Environment environment = createEnvironment();
            String xml = serializeToXml(environment);
            log.info("Successfully exported all AAS data to XML, size: {} bytes", xml.length());
            return xml;
        } catch (SerializationException e) {
            log.error("Error exporting AAS data to XML", e);
            throw new IOException("Failed to export AAS data as XML: " + e.getMessage(), e);
        }
    }
    
    /**
     * Export a specific AAS by idShort as XML
     * @param idShort The idShort of the AAS to export
     * @return XML string representation of the specific AAS and its related submodels and concept descriptions
     * @throws IOException if serialization fails or AAS not found
     */
    public String exportAASByIdShort(String idShort) throws IOException {
        try {
            log.info("Starting export of AAS with idShort '{}' to XML", idShort);
            
            // Find the AAS with the given idShort
            AssetAdministrationShell targetAAS = findAASByIdShort(idShort);
            if (targetAAS == null) {
                throw new IOException("AAS with idShort '" + idShort + "' not found");
            }
            
            // Create an environment with just this AAS and its related submodels and concept descriptions
            Environment environment = createEnvironmentForAAS(targetAAS);
            String xml = serializeToXml(environment);
            
            log.info("Successfully exported AAS '{}' to XML, size: {} bytes", idShort, xml.length());
            return xml;
        } catch (SerializationException e) {
            log.error("Error exporting AAS '{}' to XML", idShort, e);
            throw new IOException("Failed to export AAS data as XML: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error exporting AAS '{}' to XML", idShort, e);
            throw new IOException("Failed to export AAS data as XML: " + e.getMessage(), e);
        }
    }

    /**
     * Create an Environment object containing all AAS, Submodels, and ConceptDescriptions
     * @return Environment object with all data
     */
    private Environment createEnvironment() {
        DefaultEnvironment environment = new DefaultEnvironment();

        // Add all AAS
        List<AssetAdministrationShell> aasList = getAllAAS();
        environment.setAssetAdministrationShells(aasList);
        log.info("Added {} Asset Administration Shells to environment", aasList.size());

        // Add all Submodels
        List<Submodel> submodels = getAllSubmodels();
        environment.setSubmodels(submodels);
        log.info("Added {} Submodels to environment", submodels.size());

        // Add all ConceptDescriptions
        List<ConceptDescription> conceptDescriptions = getAllConceptDescriptions();
        environment.setConceptDescriptions(conceptDescriptions);
        log.info("Added {} ConceptDescriptions to environment", conceptDescriptions.size());

        return environment;
    }
    
    /**
     * Create an Environment object containing a specific AAS and its related Submodels and ConceptDescriptions
     * @param aas The AAS to include in the environment
     * @return Environment object with the specific AAS and related data
     */
    private Environment createEnvironmentForAAS(AssetAdministrationShell aas) {
        DefaultEnvironment environment = new DefaultEnvironment();
        
        // Add the specific AAS
        List<AssetAdministrationShell> aasList = new ArrayList<>();
        aasList.add(aas);
        environment.setAssetAdministrationShells(aasList);
        log.info("Added AAS '{}' to environment", aas.getIdShort());
        
        // Get all submodels and filter to only include those referenced by this AAS
        List<String> submodelIds = aas.getSubmodels().stream()
                .map(ref -> ref.getKeys().getFirst().getValue())
                .collect(Collectors.toList());
        
        List<Submodel> allSubmodels = getAllSubmodels();
        List<Submodel> relatedSubmodels = allSubmodels.stream()
                .filter(sm -> submodelIds.contains(sm.getId()))
                .collect(Collectors.toList());
        
        environment.setSubmodels(relatedSubmodels);
        log.info("Added {} related Submodels to environment", relatedSubmodels.size());
        
        // Add all ConceptDescriptions for completeness
        // In a more sophisticated implementation, we could filter to only include relevant ones
        List<ConceptDescription> conceptDescriptions = getAllConceptDescriptions();
        environment.setConceptDescriptions(conceptDescriptions);
        log.info("Added {} ConceptDescriptions to environment", conceptDescriptions.size());
        
        return environment;
    }

    /**
     * Helper method to serialize an Environment object to XML using XmlSerializer
     * @param environment The environment to serialize
     * @return XML string representation
     * @throws SerializationException if serialization fails
     */
    private String serializeToXml(Environment environment) throws SerializationException {
        try {
            XmlSerializer serializer = new XmlSerializer();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // Using reflection to access the write method
            Method writeMethod = XmlSerializer.class.getDeclaredMethod("write", OutputStream.class, Environment.class);
            writeMethod.setAccessible(true);
            writeMethod.invoke(serializer, outputStream, environment);
            
            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error during XML serialization", e);
            throw new SerializationException("Failed to serialize environment to XML", e);
        }
    }
    
    /**
     * Helper method to write an Environment object to an output stream
     * @param environment The environment to serialize
     * @param outputStream The output stream to write to
     * @throws SerializationException if serialization fails
     */
    public void writeToOutputStream(Environment environment, OutputStream outputStream) throws SerializationException {
        try {
            XmlSerializer serializer = new XmlSerializer();
            
            // Using reflection to access the write method
            Method writeMethod = XmlSerializer.class.getDeclaredMethod("write", OutputStream.class, Environment.class);
            writeMethod.setAccessible(true);
            writeMethod.invoke(serializer, outputStream, environment);
        } catch (Exception e) {
            log.error("Error writing environment to output stream", e);
            throw new SerializationException("Failed to write environment to output stream", e);
        }
    }

    /**
     * Retrieve all Asset Administration Shells from the AAS Manager
     * @return List of all AAS objects
     */
    private List<AssetAdministrationShell> getAllAAS() {
        PaginationInfo paginationInfo = new PaginationInfo(10000, null);
        CursorResult<List<AssetAdministrationShell>> result = aasManager.getAllAAS(paginationInfo);
        return result.getResult();
    }

    /**
     * Retrieve all Submodels from the AAS Manager
     * @return List of all Submodel objects
     */
    private List<Submodel> getAllSubmodels() {
        PaginationInfo paginationInfo = new PaginationInfo(10000, null);
        CursorResult<List<Submodel>> result = aasManager.getAllSubmodels(paginationInfo);
        return result.getResult();
    }

    /**
     * Retrieve all ConceptDescriptions from the AAS Manager
     * @return List of all ConceptDescription objects
     */
    private List<ConceptDescription> getAllConceptDescriptions() {
        PaginationInfo paginationInfo = new PaginationInfo(10000, null);
        CursorResult<List<ConceptDescription>> result = aasManager.getAllConceptDescriptions(paginationInfo);
        return result.getResult();
    }
    
    /**
     * Find an AAS by its idShort
     * @param idShort The idShort to search for
     * @return The AAS if found, null otherwise
     */
    private AssetAdministrationShell findAASByIdShort(String idShort) {
        List<AssetAdministrationShell> allAAS = getAllAAS();
        return allAAS.stream()
                .filter(aas -> idShort.equals(aas.getIdShort()))
                .findFirst()
                .orElse(null);
    }
}
