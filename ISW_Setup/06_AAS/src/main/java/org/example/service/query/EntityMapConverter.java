package org.example.service.query;

import lombok.extern.slf4j.Slf4j;
import org.example.database.model.ConceptDescription;
import org.example.database.model.MongoAssetAdministrationShell;
import org.example.database.model.SubmodelData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts entity objects to maps with selected fields
 * Handles both direct and nested fields
 */
@Slf4j
public class EntityMapConverter {

    /**
     * Convert an entity to a map, including only the selected fields and excluding null values
     * @param entity The entity to convert
     * @param selectedFields The fields to include
     * @param selectAll Whether to include all fields
     * @return Map representation of the entity with only the requested fields
     */
    public static Map<String, Object> convertEntityToMap(Object entity, List<String> selectedFields, boolean selectAll) {
        Map<String, Object> entityMap = new HashMap<>();
        
        try {
            if (entity instanceof MongoAssetAdministrationShell) {
                convertAasToMap((MongoAssetAdministrationShell) entity, selectedFields, selectAll, entityMap);
            } else if (entity instanceof SubmodelData) {
                convertSubmodelToMap((SubmodelData) entity, selectedFields, selectAll, entityMap);
            } else if (entity instanceof ConceptDescription) {
                convertConceptDescriptionToMap((ConceptDescription) entity, selectedFields, selectAll, entityMap);
            }
        } catch (Exception e) {
            log.error("Failed to convert entity to map", e);
        }
        
        return entityMap;
    }
    
    /**
     * Convert an AAS entity to a map
     */
    private static void convertAasToMap(MongoAssetAdministrationShell aas, List<String> selectedFields, 
                                       boolean selectAll, Map<String, Object> entityMap) {
        // Always include ID
        entityMap.put("id", aas.getId());
        
        // Process all fields, both direct and nested
        if (selectAll) {
            // Include all direct fields if selectAll is true
            if (aas.getIdShort() != null) entityMap.put("idShort", aas.getIdShort());
            if (aas.getCategory() != null) entityMap.put("category", aas.getCategory());
            if (aas.getDescriptions() != null && !aas.getDescriptions().isEmpty()) entityMap.put("description", aas.getDescriptions());
            if (aas.getAssetRef() != null) entityMap.put("assetRef", aas.getAssetRef());
            if (aas.getSubmodelRefs() != null) entityMap.put("submodelRefs", aas.getSubmodelRefs());
            if (aas.getSourceFile() != null) entityMap.put("sourceFile", aas.getSourceFile());
            if (aas.getImportedAt() != null) entityMap.put("importedAt", aas.getImportedAt());
            if (aas.getAdministration() != null) entityMap.put("administration", aas.getAdministration());
        } else {
            // Process each selected field individually
            for (String field : selectedFields) {
                if (field.contains(".")) {
                    // Handle nested fields
                    EntityFieldExtractor.extractNestedFieldWithDotNotation(field, aas, entityMap);
                } else {
                    // Handle direct fields
                    switch (field) {
                        case "idShort":
                            if (aas.getIdShort() != null) entityMap.put("idShort", aas.getIdShort());
                            break;
                        case "category":
                            if (aas.getCategory() != null) entityMap.put("category", aas.getCategory());
                            break;
                        case "description":
                            if (aas.getDescriptions() != null && !aas.getDescriptions().isEmpty()) 
                                entityMap.put("description", aas.getDescriptions());
                            break;
                        case "assetRef":
                            if (aas.getAssetRef() != null) entityMap.put("assetRef", aas.getAssetRef());
                            break;
                        case "submodelRefs":
                            if (aas.getSubmodelRefs() != null) entityMap.put("submodelRefs", aas.getSubmodelRefs());
                            break;
                        case "sourceFile":
                            if (aas.getSourceFile() != null) entityMap.put("sourceFile", aas.getSourceFile());
                            break;
                        case "importedAt":
                            if (aas.getImportedAt() != null) entityMap.put("importedAt", aas.getImportedAt());
                            break;
                        case "administration":
                            if (aas.getAdministration() != null) entityMap.put("administration", aas.getAdministration());
                            break;
                    }
                }
            }
        }
    }
    
    /**
     * Convert a Submodel entity to a map
     */
    private static void convertSubmodelToMap(SubmodelData submodel, List<String> selectedFields, 
                                           boolean selectAll, Map<String, Object> entityMap) {
        // Always include ID
        entityMap.put("id", submodel.getId());
        
        // Process all fields, both direct and nested
        if (selectAll) {
            // Include all direct fields if selectAll is true
            if (submodel.getIdShort() != null) entityMap.put("idShort", submodel.getIdShort());
            if (submodel.getCategory() != null) entityMap.put("category", submodel.getCategory());
            if (submodel.getDescription() != null) entityMap.put("description", submodel.getDescription());
            if (submodel.getKind() != null) entityMap.put("kind", submodel.getKind());
            if (submodel.getSemanticId() != null) entityMap.put("semanticId", submodel.getSemanticId());
            if (submodel.getSupplementalSemanticIds() != null) entityMap.put("supplementalSemanticIds", submodel.getSupplementalSemanticIds());
            if (submodel.getSubmodelElements() != null) entityMap.put("submodelElements", submodel.getSubmodelElements());
            if (submodel.getAdministration() != null) entityMap.put("administration", submodel.getAdministration());
            if (submodel.getSourceFile() != null) entityMap.put("sourceFile", submodel.getSourceFile());
            if (submodel.getImportedAt() != null) entityMap.put("importedAt", submodel.getImportedAt());
            if (submodel.getAasRef() != null) entityMap.put("aasRef", submodel.getAasRef());
        } else {
            // Process each selected field individually
            for (String field : selectedFields) {
                if (field.contains(".")) {
                    // Handle all nested fields with a single approach
                    EntityFieldExtractor.extractNestedFieldWithDotNotation(field, submodel, entityMap);
                } else {
                    // Handle direct fields
                    switch (field) {
                        case "idShort":
                            if (submodel.getIdShort() != null) entityMap.put("idShort", submodel.getIdShort());
                            break;
                        case "category":
                            if (submodel.getCategory() != null) entityMap.put("category", submodel.getCategory());
                            break;
                        case "description":
                            if (submodel.getDescription() != null) entityMap.put("description", submodel.getDescription());
                            break;
                        case "kind":
                            if (submodel.getKind() != null) entityMap.put("kind", submodel.getKind());
                            break;
                        case "semanticId":
                            if (submodel.getSemanticId() != null) entityMap.put("semanticId", submodel.getSemanticId());
                            break;
                        case "supplementalSemanticIds":
                            if (submodel.getSupplementalSemanticIds() != null) 
                                entityMap.put("supplementalSemanticIds", submodel.getSupplementalSemanticIds());
                            break;
                        case "submodelElements":
                            if (submodel.getSubmodelElements() != null) 
                                entityMap.put("submodelElements", submodel.getSubmodelElements());
                            break;
                        case "administration":
                            if (submodel.getAdministration() != null) 
                                entityMap.put("administration", submodel.getAdministration());
                            break;
                        case "sourceFile":
                            if (submodel.getSourceFile() != null) entityMap.put("sourceFile", submodel.getSourceFile());
                            break;
                        case "importedAt":
                            if (submodel.getImportedAt() != null) entityMap.put("importedAt", submodel.getImportedAt());
                            break;
                        case "aasRef":
                            if (submodel.getAasRef() != null) entityMap.put("aasRef", submodel.getAasRef());
                            break;
                    }
                }
            }
        }
    }
    
    /**
     * Convert a ConceptDescription entity to a map
     */
    private static void convertConceptDescriptionToMap(ConceptDescription cd, List<String> selectedFields, 
                                                     boolean selectAll, Map<String, Object> entityMap) {
        // Always include ID
        entityMap.put("id", cd.getId());
        
        // Process all fields, both direct and nested
        if (selectAll) {
            // Include all direct fields if selectAll is true
            if (cd.getIdShort() != null) entityMap.put("idShort", cd.getIdShort());
            if (cd.getCategory() != null) entityMap.put("category", cd.getCategory());
            if (cd.getDescriptions() != null) entityMap.put("descriptions", cd.getDescriptions());
            if (cd.getIsCaseOf() != null) entityMap.put("isCaseOf", cd.getIsCaseOf());
            if (cd.getDataSpecifications() != null) entityMap.put("dataSpecifications", cd.getDataSpecifications());
            if (cd.getEmbeddedDataSpecifications() != null) entityMap.put("embeddedDataSpecifications", cd.getEmbeddedDataSpecifications());
           // if (cd.getIec61360Data() != null) entityMap.put("iec61360Data", cd.getIec61360Data());
            if (cd.getSourceFile() != null) entityMap.put("sourceFile", cd.getSourceFile());
            if (cd.getImportedAt() != null) entityMap.put("importedAt", cd.getImportedAt());
        } else {
            // Process each selected field individually
            for (String field : selectedFields) {
                if (field.contains(".")) {
                    // Handle all nested fields with a single approach
                    EntityFieldExtractor.extractNestedFieldWithDotNotation(field, cd, entityMap);
                } else {
                    // Handle direct fields
                    switch (field) {
                        case "idShort":
                            if (cd.getIdShort() != null) entityMap.put("idShort", cd.getIdShort());
                            break;
                        case "category":
                            if (cd.getCategory() != null) entityMap.put("category", cd.getCategory());
                            break;
                        case "descriptions":
                            if (cd.getDescriptions() != null) entityMap.put("descriptions", cd.getDescriptions());
                            break;
                        case "isCaseOf":
                            if (cd.getIsCaseOf() != null) entityMap.put("isCaseOf", cd.getIsCaseOf());
                            break;
                        case "dataSpecifications":
                            if (cd.getDataSpecifications() != null) entityMap.put("dataSpecifications", cd.getDataSpecifications());
                            break;
                        case "embeddedDataSpecifications":
                            if (cd.getEmbeddedDataSpecifications() != null) 
                                entityMap.put("embeddedDataSpecifications", cd.getEmbeddedDataSpecifications());
                            break;
                        // case "iec61360Data":
                        //     if (cd.getIec61360Data() != null) entityMap.put("iec61360Data", cd.getIec61360Data());
                        //     break;
                        case "sourceFile":
                            if (cd.getSourceFile() != null) entityMap.put("sourceFile", cd.getSourceFile());
                            break;
                        case "importedAt":
                            if (cd.getImportedAt() != null) entityMap.put("importedAt", cd.getImportedAt());
                            break;
                    }
                }
            }
        }
    }
}
