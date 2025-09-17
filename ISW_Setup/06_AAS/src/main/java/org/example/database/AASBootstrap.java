package org.example.database;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.example.aasManager.AASManager;
import org.example.database.model.MongoAssetAdministrationShell;
import org.example.database.model.SubmodelData;
import org.example.service.mapper.BasyxConverterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Bootstrap component that loads AAS, Submodels, and ConceptDescriptions from MongoDB
 * into BaSyx repositories when the server starts.
 */
@Component
public class AASBootstrap {
    
    private static final Logger log = LoggerFactory.getLogger(AASBootstrap.class);
    
    @Autowired
    private MongoAASManager mongoAASManager;
    
    @Autowired
    private BasyxConverterService basyxConverterService;
    
    /**
     * CommandLineRunner that loads all AAS, Submodels, and ConceptDescriptions from MongoDB
     * into BaSyx repositories when the server starts.
     */
    @Bean
    CommandLineRunner preloadAASData(AASManager aasManager) {
        return args -> {
            log.info("Starting to preload AAS data from MongoDB to BaSyx repositories");
            
            // 1. First load all ConceptDescriptions
            preloadConceptDescriptions(aasManager);
            
            // 2. Then load all Submodels
            preloadSubmodels(aasManager);
            
            // 3. Finally, load all AAS (which may reference the submodels)
            preloadAAS(aasManager);
            
            log.info("Completed preloading AAS data from MongoDB to BaSyx repositories");
        };
    }
    
    /**
     * Loads all ConceptDescriptions from MongoDB into the BaSyx ConceptDescription repository.
     */
    private void preloadConceptDescriptions(AASManager aasManager) {
        log.info("Preloading ConceptDescriptions from MongoDB");
        List<org.example.database.model.ConceptDescription> mongoConceptDescriptions = 
                mongoAASManager.getAllMongoConceptDescriptions();
        
        log.info("Found {} ConceptDescriptions in MongoDB", mongoConceptDescriptions.size());
        
        for (org.example.database.model.ConceptDescription mongoCD : mongoConceptDescriptions) {
            try {
                ConceptDescription basyxCD = basyxConverterService.convertToBasyxConceptDescription(mongoCD);
                
                if (basyxCD != null) {
                    // Check if the ConceptDescription already exists in the repository
                    try {
                        aasManager.getConceptDescription(basyxCD.getId());
                        log.debug("ConceptDescription with ID {} already exists in repository, skipping", basyxCD.getId());
                    } catch (Exception e) {
                        // ConceptDescription doesn't exist, create it
                        try {
                            aasManager.createConceptDescription(basyxCD);
                            log.info("Created ConceptDescription with ID: {}", basyxCD.getId());
                        } catch (Exception ex) {
                            log.error("Error creating ConceptDescription with ID {}: {}", basyxCD.getId(), ex.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error preloading ConceptDescription with ID {}: {}", mongoCD.getId(), e.getMessage(), e);
                // Continue with other ConceptDescriptions instead of failing the entire bootstrap
            }
        }
    }
    
    /**
     * Loads all Submodels from MongoDB into the BaSyx Submodel repository.
     */
    private void preloadSubmodels(AASManager aasManager) {
        log.info("Preloading Submodels from MongoDB");
        List<SubmodelData> mongoSubmodels = mongoAASManager.getAllMongoSubmodels();
        
        log.info("Found {} Submodels in MongoDB", mongoSubmodels.size());
        
        for (SubmodelData mongoSubmodel : mongoSubmodels) {
            try {
                Submodel basyxSubmodel = basyxConverterService.convertToBasyxSubmodel(mongoSubmodel);
                
                if (basyxSubmodel != null) {
                    // Check if the Submodel already exists in the repository
                    Submodel existingSubmodel = aasManager.getSubmodel(basyxSubmodel.getId());
                    if (existingSubmodel != null) {
                        log.debug("Submodel with ID {} already exists in repository, skipping", basyxSubmodel.getId());
                    } else {
                        // Submodel doesn't exist, create it
                        try {
                            aasManager.createSubmodel(basyxSubmodel);
                            log.info("Created Submodel with ID: {}", basyxSubmodel.getId());
                        } catch (Exception e) {
                            log.error("Error creating Submodel with ID {}: {}", basyxSubmodel.getId(), e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error preloading Submodel with ID {}: {}", mongoSubmodel.getId(), e.getMessage(), e);
                // Continue with other Submodels instead of failing the entire bootstrap
            }
        }
    }
    
    /**
     * Loads all Asset Administration Shells from MongoDB into the BaSyx AAS repository.
     * Also links submodels to AAS if they are referenced.
     */
    private void preloadAAS(AASManager aasManager) {
        log.info("Preloading Asset Administration Shells from MongoDB");
        List<MongoAssetAdministrationShell> mongoAASList = mongoAASManager.getAllMongoAAS();
        
        log.info("Found {} Asset Administration Shells in MongoDB", mongoAASList.size());
        
        for (MongoAssetAdministrationShell mongoAAS : mongoAASList) {
            try {
                AssetAdministrationShell basyxAAS = basyxConverterService.convertToBasyxAAS(mongoAAS);
                
                if (basyxAAS != null) {
                    // Check if the AAS already exists in the repository
                    AssetAdministrationShell existingAAS = aasManager.getAAS(basyxAAS.getId());
                    if (existingAAS != null) {
                        log.debug("AAS with ID {} already exists in repository, skipping", basyxAAS.getId());
                    } else {
                        // AAS doesn't exist, create it
                        try {
                            // Create the AAS - the submodel references are already included in the AAS object
                            aasManager.createAAS(basyxAAS);
                            log.info("Created AAS with ID: {}", basyxAAS.getId());
                            
                            // Log the submodels that are referenced by this AAS
                            if (mongoAAS.getSubmodelRefs() != null && !mongoAAS.getSubmodelRefs().isEmpty()) {
                                for (org.example.database.model.Reference submodelRef : mongoAAS.getSubmodelRefs()) {
                                    try {
                                        // Extract submodel ID from the reference
                                        String submodelId = null;
                                        if (submodelRef.getKeys() != null && !submodelRef.getKeys().isEmpty()) {
                                            for (org.example.database.model.Key key : submodelRef.getKeys()) {
                                                if (key.getValue() != null) {
                                                    submodelId = key.getValue();
                                                    break;
                                                }
                                            }
                                        }
                                        
                                        if (submodelId != null) {
                                            // Check if the submodel exists in the repository
                                            Submodel existingSubmodel = aasManager.getSubmodel(submodelId);
                                            if (existingSubmodel != null) {
                                                log.info("AAS {} references Submodel {}", basyxAAS.getId(), submodelId);
                                            } else {
                                                log.warn("AAS {} references Submodel {} which is not found in repository", 
                                                        basyxAAS.getId(), submodelId);
                                            }
                                        }
                                    } catch (Exception e) {
                                        log.error("Error checking Submodel reference for AAS {}: {}", 
                                                basyxAAS.getId(), e.getMessage());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("Error creating AAS with ID {}: {}", basyxAAS.getId(), e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error preloading AAS with ID {}: {}", mongoAAS.getId(), e.getMessage(), e);
                // Continue with other AAS instead of failing the entire bootstrap
            }
        }
    }
}