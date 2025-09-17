package org.example.service.query;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for MongoDB query operations
 * Provides common functionality for handling nested fields and array queries
 */
@Slf4j
public class MongoQueryUtils {

    // Field mapping for collections: logical/Java name -> MongoDB field name
    private static final java.util.Map<String, String> SUBMODEL_FIELD_MAP = java.util.Map.ofEntries(
        // Common fields across collections
        java.util.Map.entry("id", "_id"),
        java.util.Map.entry("idShort", "id_short"),
        java.util.Map.entry("sourceFile", "source_file"),
        java.util.Map.entry("importedAt", "imported_at"),
        
        // Submodel specific fields
        java.util.Map.entry("semanticId", "semantic_id"),
        java.util.Map.entry("supplementalSemanticIds", "supplemental_semantic_ids"),
        java.util.Map.entry("submodelId", "submodel_id"),
        java.util.Map.entry("category", "category"),
        java.util.Map.entry("description", "description"),
        java.util.Map.entry("administration", "administration"),
        java.util.Map.entry("submodelElements", "submodel_elements"),
        
        // AAS specific fields
        java.util.Map.entry("assetRef", "asset_ref"),
        java.util.Map.entry("submodelRefs", "submodel_refs"),
        
        // ConceptDescription specific fields
        java.util.Map.entry("isCaseOf", "is_case_of"),
        java.util.Map.entry("embeddedDataSpecifications", "embedded_data_specifications"),
        java.util.Map.entry("dataSpecification", "dataSpecification"),
        java.util.Map.entry("dataSpecificationContent", "dataSpecificationContent")
    );

    /**
     * Maps a logical/query field name to its MongoDB field name for Submodel collection.
     * If no mapping is found, returns the original field name.
     * 
     * @param logicalName The logical field name to map
     * @param isNestedField Whether this field is nested inside submodelElements or another nested structure
     * @return The MongoDB field name
     */
    public static String mapSubmodelFieldName(String logicalName, boolean isNestedField) {
        // If this is a nested field inside submodelElements, preserve camelCase
        // This is because MongoDB stores nested fields with the original Java property names
        if (isNestedField) {
            return logicalName; // Preserve camelCase for nested fields
        }
        
        // Only apply snake_case conversion for top-level fields
        return SUBMODEL_FIELD_MAP.getOrDefault(logicalName, logicalName);
    }
    
    /**
     * Maps a logical/query field name to its MongoDB field name for Submodel collection.
     * If no mapping is found, returns the original field name.
     * 
     * @param logicalName The logical field name to map
     * @return The MongoDB field name
     */
    public static String mapSubmodelFieldName(String logicalName) {
        // By default, assume this is not a nested field
        return mapSubmodelFieldName(logicalName, false);
    }


    /**
     * Maps a complete field path for MongoDB queries, handling nested structures correctly
     * This is a generic solution that works for all nested fields and array indices
     * 
     * @param fieldPath The original field path to map
     * @return The mapped field path suitable for MongoDB queries
     */
    public static String mapFieldPath(String fieldPath) {
        log.info("Mapping field path: {}", fieldPath);
        
        // Special case for deeply nested concept description fields
        if (fieldPath.contains("embedded_data_specifications") && 
            (fieldPath.contains("preferredName") || fieldPath.contains("definition"))) {
            log.info("Special handling for concept description field: {}", fieldPath);
            return handleConceptDescriptionField(fieldPath);
        }
        
        // Special case for deeply nested submodel fields
        if (fieldPath.contains("submodelElements")) {
            // Check for deeply nested paths that need special handling
            if (fieldPath.contains("supplementalSemanticIds") || 
                fieldPath.contains("qualifiers") || 
                fieldPath.contains("semanticId") || 
                fieldPath.contains("keys")) {
                log.info("Special handling for deeply nested submodel field: {}", fieldPath);
                return handleSubmodelField(fieldPath);
            }
        }
        
        // First normalize the path to handle array indices
        String normalizedPath = normalizeIndexedPath(fieldPath);
        
        // Split the path into parts
        String[] parts = normalizedPath.split("\\.");
        
        // Special handling for nested paths
        // In MongoDB, top-level fields use snake_case but nested fields preserve camelCase
        StringBuilder result = new StringBuilder();
        
        // These are fields that contain nested structures where we should preserve camelCase
        Set<String> nestedContainers = Set.of(
            // Submodel fields
            "submodelElements", 
            "supplementalSemanticIds", 
            "keys",
            "qualifiers",
            "semanticId",
            
            // AAS fields
            "assetRef",
            "submodelRefs",
            
            // ConceptDescription fields
            "embeddedDataSpecifications",
            "dataSpecification",
            "dataSpecificationContent",
            "definition",
            "preferredName",
            "shortName",
            "iec61360"
        );
        
        // Process each part of the path
        for (int i = 0; i < parts.length; i++) {
            // Add separator if not the first part
            if (i > 0) {
                result.append(".");
            }
            
            String part = parts[i];
            
            // Handle numeric indices (always keep as-is)
            if (part.matches("\\d+")) {
                result.append(part);
                continue;
            }
            
            // Check if this is a top-level field
            if (i == 0) {
                // Always map top-level fields to snake_case
                result.append(SUBMODEL_FIELD_MAP.getOrDefault(part, part));
                continue;
            }
            
            // Check if the previous part was a nested container or numeric index
            // If so, we're inside a nested structure and should preserve camelCase
            String previousPart = parts[i-1];
            if (nestedContainers.contains(previousPart) || previousPart.matches("\\d+")) {
                // We're inside a nested structure, preserve camelCase
                result.append(part);
            } else {
                // Not in a nested structure, apply mapping
                result.append(SUBMODEL_FIELD_MAP.getOrDefault(part, part));
            }
        }
        
        String mappedPath = result.toString();
        log.info("Mapped field path: {} -> {}", fieldPath, mappedPath);
        return mappedPath;
    }
    
    /**
     * Special handling for deeply nested fields in different collection types
     * This is a generic handler that works for all types of nested fields
     * 
     * @param fieldPath The original field path containing nested structures
     * @param collectionType The type of collection ("concept_description" or "submodel")
     * @return The properly mapped field path for MongoDB queries
     */
    private static String handleNestedField(String fieldPath, String collectionType) {
        // First normalize the path to handle array indices
        String normalizedPath = normalizeIndexedPath(fieldPath);
        
        // Split the path into parts
        String[] parts = normalizedPath.split("\\.");
        StringBuilder result = new StringBuilder();
        
        // Set of fields that should preserve camelCase in nested structures
        Set<String> preserveCamelCaseFields = Set.of(
            // Common fields
            "keys", "value", "type", "idShort",
            
            // Submodel specific fields
            "supplementalSemanticIds", "semanticId", "qualifiers",
            
            // ConceptDescription specific fields
            "dataSpecification", "dataSpecificationContent", "preferredName", 
            "definition", "shortName", "text", "language"
        );
        
        // Fields that should be mapped to snake_case
        Map<String, String> snakeCaseMapping = Map.of(
            "submodelElements", "submodel_elements",
            "embeddedDataSpecifications", "embedded_data_specifications"
        );
        
        // Process each part of the path
        for (int i = 0; i < parts.length; i++) {
            // Add separator if not the first part
            if (i > 0) {
                result.append(".");
            }
            
            String part = parts[i];
            
            // Handle numeric indices (always keep as-is)
            if (part.matches("\\d+")) {
                result.append(part);
                continue;
            }
            
            // Map top-level field
            if (i == 0) {
                result.append(SUBMODEL_FIELD_MAP.getOrDefault(part, part));
                continue;
            }
            
            // Check if this field should be mapped to snake_case
            if (snakeCaseMapping.containsKey(part)) {
                result.append(snakeCaseMapping.get(part));
            }
            // Check if this field should preserve camelCase
            else if (preserveCamelCaseFields.contains(part)) {
                result.append(part);
            }
            // For other fields, use standard mapping or preserve camelCase based on context
            else {
                // In concept descriptions, we generally preserve camelCase for nested fields
                if ("concept_description".equals(collectionType)) {
                    result.append(part);
                }
                // In submodels, we use the standard mapping for non-special fields
                else {
                    result.append(SUBMODEL_FIELD_MAP.getOrDefault(part, part));
                }
            }
        }
        
        String mappedPath = result.toString();
        log.info("Mapped {} field: {} -> {}", collectionType, fieldPath, mappedPath);
        return mappedPath;
    }
    
    /**
     * Special handling for deeply nested concept description fields
     * This method handles the complex structure of embedded_data_specifications fields
     * 
     * @param fieldPath The original field path containing embedded_data_specifications
     * @return The properly mapped field path for MongoDB queries
     */
    private static String handleConceptDescriptionField(String fieldPath) {
        return handleNestedField(fieldPath, "concept_description");
    }
    
    /**
     * Special handling for deeply nested submodel fields
     * This method handles complex structures like submodelElements.supplementalSemanticIds.keys.value
     * 
     * @param fieldPath The original field path containing submodelElements and nested structures
     * @return The properly mapped field path for MongoDB queries
     */
    private static String handleSubmodelField(String fieldPath) {
        return handleNestedField(fieldPath, "submodel");
    }
    
    /**
     * Process a deeply nested field path with special handling for specific nested structures
     * like supplementalSemanticIds.keys.value that need special treatment
     * 
     * @param fieldPath The field path to process
     * @return Processed field path suitable for MongoDB queries
     * @deprecated Use {@link #mapFieldPath(String)} instead, which provides more comprehensive field mapping
     */
    @Deprecated
    public static String processDeepNestedPath(String fieldPath) {
        log.info("[DEPRECATED] Using processDeepNestedPath - consider using mapFieldPath instead");
        return mapFieldPath(fieldPath);
    }
    
    /**
     * Normalize field paths with array indices to MongoDB dot notation
     * Handles both bracket notation (field[0]) and dot notation (field.0)
     * @param fieldPath The field path to normalize
     * @return Normalized field path with proper MongoDB dot notation
     */
    public static String normalizeIndexedPath(String fieldPath) {
        if (fieldPath == null) {
            return null;
        }
        
        log.debug("Normalizing field path: {}", fieldPath);
        
        // Handle quoted field names by removing quotes
        if (fieldPath.startsWith("\"") && fieldPath.endsWith("\"")) {
            fieldPath = fieldPath.substring(1, fieldPath.length() - 1);
        }
        
        // Handle bracket notation for array indices: field[0] -> field.0
        String normalized = fieldPath.replaceAll("\\[(\\d+)\\]", ".$1");
        
        // Handle any potential double dots
        normalized = normalized.replaceAll("\\.\\.", ".");
        
        // Remove any leading or trailing dots
        normalized = normalized.replaceAll("^\\.|\\.$", "");
        
        // Special handling for numeric indices in dot notation
        // This is critical for MongoDB to correctly interpret array indices
        StringBuilder finalPath = new StringBuilder();
        String[] parts = normalized.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                finalPath.append(".");
            }
            
            // If this part is a numeric index, we need to ensure MongoDB treats it as an array index
            if (parts[i].matches("\\d+")) {
                // For MongoDB, we use the special $arrayElemAt operator for explicit array access
                // But only if it's not the last part of the path
                if (i < parts.length - 1) {
                    log.debug("Found array index: {} at position {}", parts[i], i);
                }
            }
            
            finalPath.append(parts[i]);
        }
        
        normalized = finalPath.toString();
        log.debug("Normalized field path: {} -> {}", fieldPath, normalized);
        return normalized;
    }
    
    /**
     * Recursively build criteria for nested fields
     * Uses $elemMatch for array fields
     * @param parts The parts of the field path
     * @param index The current index in the parts array
     * @param value The value to match
     * @param operator The operator (eq, gt, regex)
     * @return MongoDB criteria
     */
    public static Criteria buildDeepCriteria(String[] parts, int index, Object value, String operator) {
        String current = mapSubmodelFieldName(parts[index]);
        boolean isLast = index == parts.length - 1;

        // If this is the last part, create a simple criteria
        if (isLast) {
            String fullPath = String.join(".", parts);
            log.debug("[LEAF] Building criteria for field '{}', operator '{}', value '{}'", fullPath, operator, value);
            return switch (operator) {
                case "eq" -> Criteria.where(fullPath).is(value);
                case "gt" -> Criteria.where(fullPath).gt(value);
                case "regex" -> Criteria.where(fullPath).regex(value.toString(), "i");
                default -> throw new RuntimeException("Unsupported operator: " + operator);
            };
        }

        // Always ignore numeric indices in WHERE clause: treat as array-wide match
        if (isNumeric(current)) {
            log.debug("[SKIP INDEX] Skipping numeric index '{}' in path (WHERE clause always matches any element)", current);
            return buildDeepCriteria(parts, index + 1, value, operator);
        }

        // Recursively build criteria for the next part
        Criteria next = buildDeepCriteria(parts, index + 1, value, operator);

        String nextPath = String.join(".", Arrays.copyOfRange(parts, index + 1, parts.length));
        log.debug("[RECURSE] At '{}', nextPath '{}', operator '{}', value '{}'", current, nextPath, operator, value);

        // Always use $elemMatch for array fields, never require explicit index
        if (isArrayField(current)) {
            log.debug("[ARRAY-ELEMMATCH] Wrapping '{}' in $elemMatch (index ignored, WHERE matches any element)", current);
            return Criteria.where(current).elemMatch(next);
        } else {
            String fullPath = current + "." + nextPath;
            log.debug("[DOT NOTATION] Building criteria for field '{}', operator '{}', value '{}'", fullPath, operator, value);
            return switch (operator) {
                case "eq" -> Criteria.where(fullPath).is(value);
                case "gt" -> Criteria.where(fullPath).gt(value);
                case "regex" -> Criteria.where(fullPath).regex(value.toString(), "i");
                default -> throw new RuntimeException("Unsupported operator: " + operator);
            };
        }
    }
    
    /**
     * Helper to check if a string is a numeric index (used to ignore indices in WHERE clause)
     */
    private static boolean isNumeric(String s) {
        return s != null && s.matches("\\d+");
    }

    /**
     * Check if a field is an array field based on known array prefixes.
     * Helps decide whether to wrap the query with $elemMatch.
     *
     * @param fieldName The field name to check
     * @return true if the field is an array field or contains one in the path
     */
    public static boolean isArrayField(String fieldName) {
        // Remove any numeric indices from the field name for checking
        String cleanFieldName = mapSubmodelFieldName(fieldName.replaceAll("\\.\\d+$", ""));
        
        // Known array fields in our data model (all mapped to MongoDB names)
        List<String> arrayFields = List.of(
            "submodel_elements",
            "qualifiers",
            "keys",
            "supplemental_semantic_ids",
            "embedded_data_specifications",
            "dataSpecificationContent",
            "definition",
            "preferredName",
            "shortName",
            "submodel_refs",
            "descriptions",
            "values",
            "text",
            
            // Date and time fields that might be arrays
            "imported_at",
            "timestamp",
            "dates",
            
            // Administration fields
            "administration",
            "version",
            "revision",
            
            // Semantic fields
            "semantic_id",
            "dataSpecification",
            "is_case_of",
            
            // Value-related fields
            "value_type",
            "value",
            "valueId"
        );
        
        // Check if the field itself is an array field
        if (arrayFields.contains(cleanFieldName)) {
            return true;
        }
        
        // Check if the field ends with a known array field suffix
        for (String arrayField : arrayFields) {
            if (cleanFieldName.endsWith("." + arrayField)) {
                return true;
            }
        }
        
        // Special cases for complex nested structures (mapped)
        return cleanFieldName.equals("asset_ref") || 
               cleanFieldName.startsWith("asset_ref.") || 
               cleanFieldName.contains(".asset_ref.") ||
               cleanFieldName.contains(".semantic_id.") ||
               cleanFieldName.contains(".qualifiers.") ||
               cleanFieldName.contains(".supplemental_semantic_ids.") ||
               cleanFieldName.contains(".embedded_data_specifications.") ||
               cleanFieldName.contains(".dataSpecificationContent.") ||
               cleanFieldName.contains(".dataSpecification.");
    }
    
    /**
     * Helper method to find a field in a class or its superclasses
     * @param clazz The class to search in
     * @param fieldName The field name to find
     * @return The found field or null if not found
     */
    public static java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        // Try with the original field name
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // Try with camelCase (remove underscores)
            String camelCaseField = fieldName.replace("_", "");
            try {
                return clazz.getDeclaredField(camelCaseField);
            } catch (NoSuchFieldException e2) {
                // Check superclass if exists
                Class<?> superClass = clazz.getSuperclass();
                if (superClass != null && superClass != Object.class) {
                    return findField(superClass, fieldName);
                }
                return null;
            }
        }
    }
}
