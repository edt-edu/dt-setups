package org.example.service.query.queryFile;

import org.bson.Document;
import org.example.database.MongoAASManager;
import org.example.database.fileParser.FileContext;
import org.example.database.fileParser.FileContextInitializer;
import org.example.database.fileParser.FileContextInitializerRegistry;
import org.example.database.fileParser.QueryableProperty;
import org.example.database.fileParser.QueryablePropertyRegistry;
import org.example.database.model.SubmodelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Helper class for handling file property queries
 */
@Component
public class FileQueryHelper {
    private static final Logger log = LoggerFactory.getLogger(FileQueryHelper.class);
    
    private final QueryablePropertyRegistry propertyRegistry;
    private final MongoAASManager mongoAASManager;
    private final FileContextInitializerRegistry fileContextInitializerRegistry;
    
    @Autowired
    public FileQueryHelper(QueryablePropertyRegistry propertyRegistry, MongoAASManager mongoAASManager, FileContextInitializerRegistry fileContextInitializerRegistry) {
        this.propertyRegistry = propertyRegistry;
        this.mongoAASManager = mongoAASManager;
        this.fileContextInitializerRegistry = fileContextInitializerRegistry;
    }
    
    /**
     * Check if a field path is a file property path
     * @param fieldPath The field path to check
     * @return true if the field path is a file property path, false otherwise
     */
    public boolean isFilePropertyPath(String fieldPath) {
        if (fieldPath == null) {
            log.debug("Field path is null, not a file property path");
            return false;
        }
        
        log.debug("Checking if '{}' is a file property path", fieldPath);
        
        // Try the path as-is
        if (propertyRegistry.getPropertyByPath(fieldPath) != null) {
            log.debug("Found property for path: {}", fieldPath);
            return true;
        }
        
        // Try with 'file.' prefix if it doesn't have it
        if (!fieldPath.startsWith("file.")) {
            String prefixedPath = "file." + fieldPath;
            if (propertyRegistry.getPropertyByPath(prefixedPath) != null) {
                log.debug("Found property for prefixed path: {}", prefixedPath);
                return true;
            }
            
            // Special case for nested paths like 'dimensions.length'
            // Check if any registered property ends with this path or contains it as a nested path
            for (String registeredPath : propertyRegistry.getAllPropertyPaths()) {
                // Check for exact suffix match
                if (registeredPath.endsWith("." + fieldPath)) {
                    log.debug("Found property with matching suffix: {} for path: {}", registeredPath, fieldPath);
                    return true;
                }
                
                // Check for nested path match with dots replaced by underscores
                if (fieldPath.contains(".")) {
                    String normalizedPath = fieldPath.replace(".", "_");
                    if (registeredPath.contains(normalizedPath)) {
                        log.debug("Found property with normalized nested path: {} for path: {}", registeredPath, fieldPath);
                        return true;
                    }
                }
                
                // Check for partial path match (for deeply nested properties)
                String[] pathParts = fieldPath.split("\\.");
                if (pathParts.length > 1) {
                    for (int i = 0; i < pathParts.length - 1; i++) {
                        String partialPath = String.join(".", Arrays.copyOfRange(pathParts, i, pathParts.length));
                        if (registeredPath.endsWith("." + partialPath) || registeredPath.contains("." + partialPath.replace(".", "_"))) {
                            log.debug("Found property with partial path match: {} for path: {}", registeredPath, fieldPath);
                            return true;
                        }
                    }
                }
            }
        }
        
        // Try without 'file.' prefix if it has it
        if (fieldPath.startsWith("file.")) {
            String unprefixedPath = fieldPath.substring(5); // Remove 'file.'
            if (propertyRegistry.getPropertyByPath(unprefixedPath) != null) {
                log.debug("Found property for unprefixed path: {}", unprefixedPath);
                return true;
            }
        }
        
        log.debug("No property found for path: {}", fieldPath);
        return false;
    }
    
    /**
     * Extract file-related queries from the where clause
     * @param whereClause The where clause from the query
     * @return Map of file property paths to query values with operators
     */
    public Map<String, Object> extractFileQueries(org.bson.Document whereClause) {
        Map<String, Object> fileQueries = new HashMap<>();
        
        if (whereClause == null) {
            log.debug("WHERE clause is null, returning empty file queries");
            return fileQueries;
        }
        
        log.info("Extracting file queries from WHERE clause: {}", whereClause);
        
        // Special handling for _where field which contains the actual WHERE clause in some cases
        if (whereClause.containsKey("_where")) {
            log.info("Found _where field in document, extracting from it");
            Object whereValue = whereClause.get("_where");
            if (whereValue instanceof org.bson.Document) {
                Map<String, Object> nestedQueries = extractFileQueries((org.bson.Document) whereValue);
                if (!nestedQueries.isEmpty()) {
                    log.info("Found {} file properties in _where: {}", nestedQueries.size(), nestedQueries);
                    fileQueries.putAll(nestedQueries);
                }
            }
        }
        
        // Process each condition in the where clause
        for (String key : whereClause.keySet()) {
            if (key.equals("_where") || key.equals("_select")) {
                // Already handled or not relevant for file queries
                continue;
            }
            
            // Check if this is a file property
            log.debug("Checking if key '{}' is a file property path", key);
            boolean isFileProperty = isFilePropertyPath(key);
            log.debug("Key '{}' is file property: {}", key, isFileProperty);
            
            if (isFileProperty) {
                String normalizedKey = normalizePropertyPath(key);
                Object value = whereClause.get(key);
                log.info("Found file property in WHERE clause: {} (normalized to {}) = {}", key, normalizedKey, value);
                
                // Special handling for MongoDB operators
                if (value instanceof org.bson.Document) {
                    org.bson.Document valueDoc = (org.bson.Document) value;
                    // Check if this document contains MongoDB operators
                    boolean hasOperator = false;
                    for (String opKey : valueDoc.keySet()) {
                        if (opKey.startsWith("$")) {
                            hasOperator = true;
                            break;
                        }
                    }
                    
                    if (!hasOperator) {
                        // If no MongoDB operators found, check if we need to convert SQL operators
                        // This handles cases where the SQL operator wasn't properly converted
                        if (key.contains(">") || key.contains("<") || key.contains("=")) {
                            log.info("Converting SQL operator in key: {}", key);
                            String cleanKey = key.replaceAll("[<>=]+", "").trim();
                            normalizedKey = normalizePropertyPath(cleanKey);
                            
                            org.bson.Document opDoc = new org.bson.Document();
                            if (key.contains(">=")) {
                                opDoc.put("$gte", value);
                                log.info("Converted >= operator to $gte for key: {}", cleanKey);
                            } else if (key.contains("<=")) {
                                opDoc.put("$lte", value);
                                log.info("Converted <= operator to $lte for key: {}", cleanKey);
                            } else if (key.contains(">")) {
                                opDoc.put("$gt", value);
                                log.info("Converted > operator to $gt for key: {}", cleanKey);
                            } else if (key.contains("<")) {
                                opDoc.put("$lt", value);
                                log.info("Converted < operator to $lt for key: {}", cleanKey);
                            }
                            
                            value = opDoc;
                        }
                    }
                }
                
                // Check if we already have a query for this field
                if (fileQueries.containsKey(normalizedKey)) {
                    log.info("Found multiple conditions for the same file property: {}", normalizedKey);
                    Object existingValue = fileQueries.get(normalizedKey);
                    
                    // Create an $and condition to combine multiple conditions on the same field
                    List<Document> andConditions = new ArrayList<>();
                    
                    // Add the existing condition
                    if (existingValue instanceof Document && ((Document) existingValue).containsKey("$and")) {
                        // We already have an $and condition, add to it
                        andConditions.addAll((List<Document>) ((Document) existingValue).get("$and"));
                    } else if (existingValue instanceof Document) {
                        // Add the existing document as is
                        andConditions.add((Document) existingValue);
                    } else {
                        // Create a document for the simple value
                        andConditions.add(new Document(normalizedKey, existingValue));
                    }
                    
                    // Add the new condition
                    if (value instanceof Document) {
                        andConditions.add((Document) value);
                    } else {
                        andConditions.add(new Document(normalizedKey, value));
                    }
                    
                    // Create the combined $and query
                    Document andDoc = new Document("$and", andConditions);
                    fileQueries.put(normalizedKey, andDoc);
                    log.info("Created combined $and condition for {}: {}", normalizedKey, andDoc);
                } else {
                    // First condition for this field, just add it directly
                    fileQueries.put(normalizedKey, value);
                }
            } else if (key.equals("$and") || key.equals("$or")) {
                // Handle logical operators
                log.info("Processing logical operator: {}", key);
                try {
                    List<Object> conditions = whereClause.getList(key, Object.class);
                    if (conditions != null) {
                        for (Object condObj : conditions) {
                            if (condObj instanceof org.bson.Document) {
                                org.bson.Document condition = (org.bson.Document) condObj;
                                Map<String, Object> subQueries = extractFileQueries(condition);
                                if (!subQueries.isEmpty()) {
                                    log.info("Found {} file properties in nested condition: {}", subQueries.size(), subQueries);
                                    fileQueries.putAll(subQueries);
                                }
                            } else {
                                log.warn("Expected Document in logical operator list but got: {}", condObj.getClass().getName());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing logical operator {}: {}", key, e.getMessage(), e);
                }
            } else {
                // Check if this is a property path that needs special handling
                // For example, "dimensions.length" might be registered as "file.dimensions.length"
                log.debug("Checking all registered property paths for match with {}", key);
                boolean foundMatch = false;
                
                // First try to match the key directly with registered paths
                for (String registeredPath : propertyRegistry.getAllPropertyPaths()) {
                    // Check for exact suffix match
                    if (registeredPath.endsWith("." + key)) {
                        String normalizedKey = registeredPath;
                        Object value = whereClause.get(key);
                        log.info("Found matching registered path {} for key {}", registeredPath, key);
                        log.info("Adding file property: {} (normalized to {}) = {}", key, normalizedKey, value);
                        
                        // Handle MongoDB operators for this match
                        if (value instanceof org.bson.Document) {
                            processMongoOperators(fileQueries, normalizedKey, value, key);
                        } else {
                            // Check if we already have a query for this field
                            if (fileQueries.containsKey(normalizedKey)) {
                                log.info("Found multiple conditions for the same file property: {}", normalizedKey);
                                Object existingValue = fileQueries.get(normalizedKey);
                                
                                // Create an $and condition to combine multiple conditions on the same field
                                List<Document> andConditions = new ArrayList<>();
                                
                                // Add the existing condition
                                if (existingValue instanceof Document && ((Document) existingValue).containsKey("$and")) {
                                    // We already have an $and condition, add to it
                                    andConditions.addAll((List<Document>) ((Document) existingValue).get("$and"));
                                } else if (existingValue instanceof Document) {
                                    // Add the existing document as is
                                    andConditions.add((Document) existingValue);
                                } else {
                                    // Create a document for the simple value
                                    andConditions.add(new Document(normalizedKey, existingValue));
                                }
                                
                                // Add the new condition
                                if (value instanceof Document) {
                                    andConditions.add((Document) value);
                                } else {
                                    andConditions.add(new Document(normalizedKey, value));
                                }
                                
                                // Create the combined $and query
                                Document andDoc = new Document("$and", andConditions);
                                fileQueries.put(normalizedKey, andDoc);
                                log.info("Created combined $and condition for {}: {}", normalizedKey, andDoc);
                            } else {
                                // First condition for this field, just add it directly
                                fileQueries.put(normalizedKey, value);
                            }
                        }
                        
                        foundMatch = true;
                        break;
                    }
                    
                    // Check for keys with comparison operators
                    if (key.contains(">") || key.contains("<") || key.contains("=")) {
                        String cleanKey = key.replaceAll("[<>=]+", "").trim();
                        if (registeredPath.endsWith("." + cleanKey)) {
                            String normalizedKey = registeredPath;
                            Object value = whereClause.get(key);
                            log.info("Found matching registered path {} for key with operator: {}", registeredPath, key);
                            
                            // Create MongoDB operator document
                            org.bson.Document opDoc = new org.bson.Document();
                            if (key.contains(">=")) {
                                opDoc.put("$gte", value);
                                log.info("Converted >= operator to $gte for key: {}", cleanKey);
                            } else if (key.contains("<=")) {
                                opDoc.put("$lte", value);
                                log.info("Converted <= operator to $lte for key: {}", cleanKey);
                            } else if (key.contains(">")) {
                                opDoc.put("$gt", value);
                                log.info("Converted > operator to $gt for key: {}", cleanKey);
                            } else if (key.contains("<")) {
                                opDoc.put("$lt", value);
                                log.info("Converted < operator to $lt for key: {}", cleanKey);
                            }
                            
                            fileQueries.put(normalizedKey, opDoc);
                            foundMatch = true;
                            break;
                        }
                    }
                    
                    // Check for nested path match with dots replaced by underscores
                    if (key.contains(".")) {
                        String normalizedKeyPath = key.replace(".", "_");
                        if (registeredPath.contains(normalizedKeyPath)) {
                            Object value = whereClause.get(key);
                            log.info("Found matching registered path {} for normalized key {}", registeredPath, normalizedKeyPath);
                            log.info("Adding file property: {} (normalized to {}) = {}", key, registeredPath, value);
                            fileQueries.put(registeredPath, value);
                            foundMatch = true;
                            break;
                        }
                    }
                    
                    // Check for partial path match (for deeply nested properties)
                    String[] pathParts = key.split("\\.");
                    if (pathParts.length > 1) {
                        for (int i = 0; i < pathParts.length - 1; i++) {
                            String partialPath = String.join(".", Arrays.copyOfRange(pathParts, i, pathParts.length));
                            if (registeredPath.endsWith("." + partialPath) || 
                                registeredPath.contains("." + partialPath.replace(".", "_"))) {
                                Object value = whereClause.get(key);
                                log.info("Found matching registered path {} for partial path {}", registeredPath, partialPath);
                                log.info("Adding file property: {} (normalized to {}) = {}", key, registeredPath, value);
                                fileQueries.put(registeredPath, value);
                                foundMatch = true;
                                break;
                            }
                        }
                        if (foundMatch) break;
                    }
                }
                
                if (!foundMatch) {
                    log.debug("Skipping non-file property: {}", key);
                }
            }
        }
        
        log.info("Extracted {} file properties from WHERE clause: {}", fileQueries.size(), fileQueries);
        return fileQueries;
    }
    
    /**
     * Process MongoDB operator documents and handle special cases
     * @param fileQueries The map of file queries to update
     * @param normalizedKey The normalized property key
     * @param value The query value (potentially a MongoDB document with operators)
     * @param originalKey The original key from the query
     */
    private void processMongoOperators(Map<String, Object> fileQueries, String normalizedKey, Object value, String originalKey) {
        org.bson.Document valueDoc = (org.bson.Document) value;
        
        // Check if this document contains MongoDB operators
        boolean hasOperator = false;
        for (String opKey : valueDoc.keySet()) {
            if (opKey.startsWith("$")) {
                hasOperator = true;
                break;
            }
        }
        
        if (!hasOperator) {
            // If no MongoDB operators found, check if we need to convert SQL operators
            // This handles cases where the SQL operator wasn't properly converted
            if (originalKey.contains(">") || originalKey.contains("<") || originalKey.contains("=")) {
                log.info("Converting SQL operator in key: {}", originalKey);
                String cleanKey = originalKey.replaceAll("[<>=]+", "").trim();
                
                org.bson.Document opDoc = new org.bson.Document();
                if (originalKey.contains(">=")) {
                    opDoc.put("$gte", value);
                    log.info("Converted >= operator to $gte for key: {}, value: {}", cleanKey, value);
                } else if (originalKey.contains("<=")) {
                    opDoc.put("$lte", value);
                    log.info("Converted <= operator to $lte for key: {}, value: {} (type: {})", 
                            cleanKey, value, value != null ? value.getClass().getName() : "null");
                    
                    // Additional debug for <= operator
                    if (value instanceof org.bson.Document) {
                        log.info("<= operator value is a Document: {}", value);
                    } else if (value instanceof Number) {
                        log.info("<= operator value is a Number: {} ({})", value, value.getClass().getName());
                    } else if (value instanceof String) {
                        log.info("<= operator value is a String: '{}'", value);
                        // Try to convert string to number for debugging
                        try {
                            double numValue = Double.parseDouble((String)value);
                            log.info("<= string value parsed as double: {}", numValue);
                        } catch (Exception e) {
                            log.info("<= string value could not be parsed as number: {}", e.getMessage());
                        }
                    }
                } else if (originalKey.contains(">")) {
                    opDoc.put("$gt", value);
                    log.info("Converted > operator to $gt for key: {}, value: {}", cleanKey, value);
                } else if (originalKey.contains("<")) {
                    opDoc.put("$lt", value);
                    log.info("Converted < operator to $lt for key: {}, value: {}", cleanKey, value);
                }
                
                fileQueries.put(normalizedKey, opDoc);
                return;
            }
        }
        
        // If we get here, either there are already MongoDB operators or no SQL operators to convert
        fileQueries.put(normalizedKey, value);
    }
    
    /**
     * Normalize a property path to ensure consistent format
     * @param propertyPath The property path to normalize
     * @return The normalized property path
     */
    private String normalizePropertyPath(String propertyPath) {
        if (propertyPath == null) {
            return null;
        }
        
        log.debug("Normalizing property path: {}", propertyPath);
        
        // Ensure consistent prefix handling
        if (propertyPath.startsWith("file.")) {
            log.debug("Property path already has file. prefix: {}", propertyPath);
            return propertyPath;
        } else {
            // Check if the property exists with the prefix
            String prefixedPath = "file." + propertyPath;
            if (propertyRegistry.getPropertyByPath(prefixedPath) != null) {
                log.debug("Found property with prefixed path: {}", prefixedPath);
                return prefixedPath;
            }
            
            // Special case for nested paths like 'dimensions.length'
            // Find the full registered path that ends with this property path
            for (String registeredPath : propertyRegistry.getAllPropertyPaths()) {
                // Check for exact suffix match
                if (registeredPath.endsWith("." + propertyPath)) {
                    log.debug("Found matching registered path by suffix: {} for {}", registeredPath, propertyPath);
                    return registeredPath;
                }
                
                // Check for nested path match with dots replaced by underscores
                if (propertyPath.contains(".")) {
                    String normalizedPath = propertyPath.replace(".", "_");
                    if (registeredPath.contains(normalizedPath)) {
                        log.debug("Found property with normalized nested path: {} for {}", registeredPath, propertyPath);
                        return registeredPath;
                    }
                }
                
                // Check for partial path match (for deeply nested properties)
                String[] pathParts = propertyPath.split("\\.");
                if (pathParts.length > 1) {
                    for (int i = 0; i < pathParts.length - 1; i++) {
                        String partialPath = String.join(".", Arrays.copyOfRange(pathParts, i, pathParts.length));
                        if (registeredPath.endsWith("." + partialPath) || 
                            registeredPath.contains("." + partialPath.replace(".", "_"))) {
                            log.debug("Found property with partial path match: {} for {}", registeredPath, propertyPath);
                            return registeredPath;
                        }
                    }
                }
            }
        }
        
        log.debug("No normalized path found, returning original: {}", propertyPath);
        return propertyPath;
    }
    
    /**
     * Create a query to find submodels with files that match the given file property queries
     * @param fileQueries Map of file property paths to query values
     * @return MongoDB query document to find matching submodels
     */
    public org.bson.Document createFileSubmodelQuery(Map<String, Object> fileQueries) {
        if (fileQueries.isEmpty()) {
            return null;
        }
        
        log.info("Creating file submodel query for {} file properties", fileQueries.size());
        
        // Get all content types that might be relevant
        Set<String> relevantContentTypes = new HashSet<>();
        
        for (String propertyPath : fileQueries.keySet()) {
            log.debug("Looking up property for path: {}", propertyPath);
            QueryableProperty property = propertyRegistry.getPropertyByPath(propertyPath);
            if (property != null) {
                String[] supportedTypes = property.getSupportedContentTypes();
                log.debug("Property {} supports content types: {}", propertyPath, Arrays.toString(supportedTypes));
                relevantContentTypes.addAll(Arrays.asList(supportedTypes));
            } else {
                log.warn("No property found in registry for path: {}", propertyPath);
                
                // Try to find property with similar path
                for (String registeredPath : propertyRegistry.getAllPropertyPaths()) {
                    if (registeredPath.endsWith(propertyPath) || 
                        propertyPath.endsWith(registeredPath) ||
                        registeredPath.contains(propertyPath.replace(".", "_"))) {
                        log.info("Found similar registered path: {} for {}", registeredPath, propertyPath);
                        QueryableProperty similarProperty = propertyRegistry.getPropertyByPath(registeredPath);
                        if (similarProperty != null) {
                            String[] supportedTypes = similarProperty.getSupportedContentTypes();
                            log.debug("Similar property {} supports content types: {}", registeredPath, Arrays.toString(supportedTypes));
                            relevantContentTypes.addAll(Arrays.asList(supportedTypes));
                        }
                        break;
                    }
                }
            }
        }
        
        log.info("Relevant content types for file queries: {}", relevantContentTypes);
        
        if (relevantContentTypes.isEmpty()) {
            log.warn("No relevant content types found for file queries, using all supported content types");
            // If no specific content types were found, include all supported content types
            relevantContentTypes.add("application/step");  // Add STEP file content type as fallback
            relevantContentTypes.add("*/*");  // Add wildcard as last resort
        }
        
        // Create a query to find submodels with files of these content types
        // Use $elemMatch to ensure we match ALL files that meet the criteria
        List<org.bson.Document> contentTypeConditions = new ArrayList<>();
        for (String contentType : relevantContentTypes) {
            contentTypeConditions.add(
                new org.bson.Document("submodelElements.file.contentType", contentType)
            );
        }
        
        // Create a query that will find ALL matching files, not just the first one
        org.bson.Document query = new org.bson.Document("$or", contentTypeConditions);
        log.info("Created file submodel query: {}", query);
        return query;
    }
    
    /**
     * Post-process query results to filter based on file properties and enrich with file property values
     * @param results The initial query results
     * @param fileQueries Map of file property paths to query values
     * @return Filtered results that match file property conditions, enriched with file properties
     */
    public List<Map<String, Object>> filterResultsByFileProperties(
            List<Map<String, Object>> results, 
            Map<String, Object> fileQueries) {
        // When no selected fields are provided, we'll include all properties
        return filterResultsByFileProperties(results, fileQueries, null);
    }
    
    /**
     * Post-process query results to filter based on file properties and enrich with file property values
     * @param results The initial query results
     * @param fileQueries Map of file property paths to query values
     * @param selectedFields The fields selected in the query (optional)
     * @return Filtered results that match file property conditions, enriched with file properties
     */
    public List<Map<String, Object>> filterResultsByFileProperties(
            List<Map<String, Object>> results, 
            Map<String, Object> fileQueries,
            List<String> selectedFields) {
        
        log.info("=== FILTERING RESULTS BY FILE PROPERTIES ===");
        log.info("Initial results count: {}", results != null ? results.size() : 0);
        log.info("File queries: {}", fileQueries);
        log.info("Selected fields: {}", selectedFields);
        
        if (results == null || results.isEmpty()) {
            log.info("No initial results to filter, performing file-only query");
            // If there are no initial results but we have file queries, we need to query all submodels
            // that might have matching files
            if (!fileQueries.isEmpty()) {
                log.info("Performing file-only query with file queries: {}", fileQueries);
                return performFileOnlyQuery(fileQueries, selectedFields);
            }
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> processedResults = new ArrayList<>();
        
        for (Map<String, Object> result : results) {
            String submodelId = (String) result.get("id");
            String submodelIdShort = (String) result.get("idShort");
            
            if (submodelId == null || submodelIdShort == null) {
                log.warn("Result missing id or idShort, skipping file property processing");
                processedResults.add(result);
                continue;
            }
            
            log.info("Processing submodel: {} ({})", submodelIdShort, submodelId);
            
            // Get property paths based on the selected fields
            Set<String> propertyPaths = new HashSet<>();
            
            // Make sure we have a non-null selected fields list
            List<String> effectiveSelectedFields = selectedFields != null ? selectedFields : new ArrayList<>();
            
            // If selectedFields is provided, only include those properties
            if (!effectiveSelectedFields.isEmpty()) {
                // Add all selected fields that match registered properties
                for (String field : effectiveSelectedFields) {
                    // Check if this is a file property (with or without prefix)
                    if (propertyRegistry.getPropertyByPath(field) != null) {
                        propertyPaths.add(field);
                        continue;
                    }
                    
                    // Also check with/without 'file.' prefix
                    String fieldWithPrefix = field.startsWith("file.") ? field : "file." + field;
                    String fieldWithoutPrefix = field.startsWith("file.") ? field.substring(5) : field;
                    
                    if (propertyRegistry.getPropertyByPath(fieldWithPrefix) != null) {
                        propertyPaths.add(fieldWithPrefix);
                        continue;
                    }
                    
                    if (propertyRegistry.getPropertyByPath(fieldWithoutPrefix) != null) {
                        propertyPaths.add(fieldWithoutPrefix);
                    }
                }
                
                log.info("File properties from selected fields: {}", propertyPaths);
            }
            
            // Also include any property paths from the WHERE clause
            if (!fileQueries.isEmpty()) {
                // Normalize all property paths in file queries
                Set<String> normalizedPaths = new HashSet<>();
                for (String path : fileQueries.keySet()) {
                    normalizedPaths.add(normalizePropertyPath(path));
                }
                propertyPaths.addAll(normalizedPaths);
                log.info("File properties from WHERE clause: {}", normalizedPaths);
            }
            
            try {
                // Get the complete submodel data from the database
                SubmodelData submodel = mongoAASManager.getMongoSubmodelByIdShort(submodelIdShort);
                if (submodel == null) {
                    log.warn("Submodel not found with idShort: {}", submodelIdShort);
                    // Keep the result without file properties
                    processedResults.add(result);
                    continue;
                }
                
                // Find file paths for this submodel
                Map<String, List<String>> filePathsByType = findFilePathsForSubmodelData(submodel);
                if (filePathsByType.isEmpty()) {
                    log.info("No files found in submodel {}", submodelIdShort);
                    // Only include results without files if there are no file queries
                    if (fileQueries.isEmpty()) {
                        log.info("No file queries, keeping result without file properties");
                        processedResults.add(result);
                    } else {
                        log.info("✗ Skipping result because it has no files and there are file queries");
                    }
                    continue;
                }
                
                log.info("Found files by content type: {}", filePathsByType);
                
                // If we have file queries, check if the files match
                if (!fileQueries.isEmpty()) {
                    log.info("Applying file queries: {}", fileQueries);
                    boolean matchesAllConditions = false;
                    
                    // Track which files match all queries
                    List<String> matchingFiles = new ArrayList<>();
                    Map<String, Map<String, Object>> filePropertyValues = new HashMap<>();
                    
                    // First, check each file against all queries
                    for (Map.Entry<String, List<String>> typeEntry : filePathsByType.entrySet()) {
                        String contentType = typeEntry.getKey();
                        List<String> filePaths = typeEntry.getValue();
                        
                        for (String filePath : filePaths) {
                            boolean fileMatchesAll = true;
                            Map<String, Object> extractedValues = new HashMap<>();
                            
                            // First check if the file matches all query conditions
                            for (Map.Entry<String, Object> queryEntry : fileQueries.entrySet()) {
                                String propertyPath = normalizePropertyPath(queryEntry.getKey());
                                Object queryValue = queryEntry.getValue();
                                
                                // Get the property value for this specific file
                                Object propertyValue = getPropertyValueForFile(filePath, contentType, propertyPath);
                                
                                if (propertyValue == null) {
                                    log.info("No value for property {} in file {}", propertyPath, filePath);
                                    fileMatchesAll = false;
                                    break;
                                }
                                
                                // Store the extracted value
                                extractedValues.put(propertyPath, propertyValue);
                                
                                boolean matches = matchesQueryValue(propertyValue, queryValue);
                                log.info("File {} property {} value {} matches query {}: {}", 
                                        filePath, propertyPath, propertyValue, queryValue, matches);
                                
                                if (!matches) {
                                    fileMatchesAll = false;
                                    break;
                                }
                            }
                            
                            // If the file matches all queries, extract all requested properties
                            if (fileMatchesAll) {
                                // Extract all properties from the selected fields
                                if (selectedFields != null) {
                                    for (String field : selectedFields) {
                                        // Skip fields that aren't file properties or already extracted
                                        if (extractedValues.containsKey(field) || field.equals("id") || 
                                            field.equals("idShort") || field.startsWith("submodelElements")) {
                                            continue;
                                        }
                                        
                                        // Try to extract the property
                                        try {
                                            Object propValue = getPropertyValueForFile(filePath, contentType, field);
                                            if (propValue != null) {
                                                extractedValues.put(field, propValue);
                                                log.info("Extracted additional property {} = {} from file {}", 
                                                        field, propValue, filePath);
                                            }
                                        } catch (Exception e) {
                                            log.debug("Could not extract property {} from file {}: {}", 
                                                    field, filePath, e.getMessage());
                                        }
                                    }
                                }
                            }
                            
                            // If this file matches all queries, add it to our list
                            if (fileMatchesAll) {
                                matchingFiles.add(filePath);
                                filePropertyValues.put(filePath, extractedValues);
                                matchesAllConditions = true;
                                log.info("✓ File {} matches all queries", filePath);
                            }
                        }
                    }
                    
                    if (!matchesAllConditions) {
                        log.info("✗ Submodel {} does not have any files matching all queries, skipping", submodelIdShort);
                        continue; // Skip this result if no files match all queries
                    } else {
                        log.info("✓ Submodel {} has {} matching files", submodelIdShort, matchingFiles.size());
                        
                        // For each matching file, create a separate result entry
                        if (matchingFiles.size() > 0) {
                            // Create a separate result for each matching file
                            for (int i = 0; i < matchingFiles.size(); i++) {
                                String filePath = matchingFiles.get(i);
                                Map<String, Object> fileResult = new HashMap<>(result);
                                
                                // Add the extracted property values for this file
                                Map<String, Object> values = filePropertyValues.get(filePath);
                                for (Map.Entry<String, Object> entry : values.entrySet()) {
                                    String propPath = entry.getKey();
                                    Object propValue = entry.getValue();
                                    
                                    fileResult.put(propPath, propValue);
                                    
                                    // Also add with short path if it has a prefix
                                    if (propPath.startsWith("file.")) {
                                        String shortPath = propPath.substring(5);
                                        fileResult.put(shortPath, propValue);
                                    }
                                }
                                
                                // Copy any submodelElements fields from the original result
                                if (selectedFields != null) {
                                    for (String field : selectedFields) {
                                        if (field.startsWith("submodelElements") && result.containsKey(field)) {
                                            fileResult.put(field, result.get(field));
                                            log.info("Copied submodelElements field {} to file result", field);
                                        }
                                    }
                                }
                                
                                // Only add to processed results if this is not the first file
                                // The first file will be added later through the normal enrichment
                                if (i > 0) {
                                    processedResults.add(fileResult);
                                    log.info("Added separate result for matching file: {}", filePath);
                                } else {
                                    // For the first file, update the original result
                                    result.putAll(fileResult);
                                    log.info("Updated original result with first matching file: {}", filePath);
                                }
                            }
                            
                            // For the first file, continue with normal enrichment
                            String firstFile = matchingFiles.get(0);
                            Map<String, Boolean> fileMatchesAllQueries = new HashMap<>();
                            fileMatchesAllQueries.put(firstFile, true);
                            result.put("_fileMatches", fileMatchesAllQueries);
                            result.put("_primaryFile", firstFile);
                            
                            // Add extracted property values to the first result
                            Map<String, Object> firstFileValues = filePropertyValues.get(firstFile);
                            if (firstFileValues != null) {
                                for (Map.Entry<String, Object> entry : firstFileValues.entrySet()) {
                                    String propPath = entry.getKey();
                                    Object propValue = entry.getValue();
                                    
                                    // Add the property value to the result
                                    result.put(propPath, propValue);
                                    
                                    // Also add with short path if it has a prefix
                                    if (propPath.startsWith("file.")) {
                                        String shortPath = propPath.substring(5);
                                        result.put(shortPath, propValue);
                                    }
                                }
                            }
                        } else if (matchingFiles.size() == 1) {
                            // Just one matching file, store it for enrichment
                            String matchingFile = matchingFiles.get(0);
                            Map<String, Boolean> fileMatchesAllQueries = new HashMap<>();
                            fileMatchesAllQueries.put(matchingFile, true);
                            result.put("_fileMatches", fileMatchesAllQueries);
                            result.put("_primaryFile", matchingFile);
                        }
                    }
                }
                
                // Enrich the result with all file properties
                Map<String, Object> enrichedResult = enrichResultWithFileProperties(result, filePathsByType, propertyPaths, fileQueries);
                log.info("Enriched result: {}", enrichedResult);
                processedResults.add(enrichedResult);
                
            } catch (Exception e) {
                log.error("Error processing file properties for submodel {}: {}", 
                        submodelIdShort, e.getMessage(), e);
                // Keep the result without file properties if there's an error
                processedResults.add(result);
            }
        }
        
        log.info("Final processed results count: {}", processedResults.size());
        return processedResults;
    }
    
    /**
     * Get a property value from a specific file
     * @param filePath The path to the file
     * @param contentType The content type of the file
     * @param propertyPath The property path to extract
     * @return The extracted property value, or null if not found
     */
    private Object getPropertyValueForFile(String filePath, String contentType, String propertyPath) {
        log.debug("Extracting property {} from file {}", propertyPath, filePath);
        
        // Get the file context initializer for this content type
        Optional<FileContextInitializer> initializerOpt = fileContextInitializerRegistry.getInitializer(contentType);
        if (!initializerOpt.isPresent()) {
            log.warn("No file context initializer found for content type: {}", contentType);
            return null;
        }
        
        FileContextInitializer initializer = initializerOpt.get();
        
        try {
            // Initialize the file context
            File file = new File(filePath);
            if (!file.exists()) {
                log.warn("File does not exist: {}", filePath);
                return null;
            }
            
            FileContext context = initializer.initialize(file);
            if (context == null) {
                log.warn("Failed to initialize file context for file: {}", filePath);
                return null;
            }
            
            try {
                // Get the property extractor
                QueryableProperty property = propertyRegistry.getPropertyByPath(propertyPath);
                if (property == null) {
                    log.warn("No property extractor found for path: {}", propertyPath);
                    return null;
                }
                
                // Extract the property value
                Object propertyValue = property.extract(context);
                log.debug("Extracted property {} value '{}' from file {}", 
                        propertyPath, propertyValue, filePath);
                return propertyValue;
                
            } finally {
                // Always close the context
                try {
                    context.close();
                } catch (Exception e) {
                    log.error("Error closing file context for {}: {}", filePath, e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("IOException during FileContext initialization for file {}: {}", 
                    filePath, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during FileContext initialization for file {}: {}", 
                    filePath, e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Perform a query that only filters based on file properties
     * This is used when the initial MongoDB query returns no results
     * @param fileQueries The file queries to apply
     * @param selectedFields The fields to include in the results
     * @return List of results that match the file queries
     */
    private List<Map<String, Object>> performFileOnlyQuery(Map<String, Object> fileQueries, List<String> selectedFields) {
        log.info("Performing file-only query with queries: {}", fileQueries);
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            // Get all submodels that might have files of the relevant content types
            org.bson.Document fileSubmodelQuery = createFileSubmodelQuery(fileQueries);
            if (fileSubmodelQuery == null) {
                log.warn("Could not create file submodel query");
                return results;
            }
            
            log.info("Querying for submodels with file query: {}", fileSubmodelQuery);
            
            // Use the appropriate method to find submodels
            List<SubmodelData> submodels = new ArrayList<>();
            try {
                // Try to find all submodels that have file elements
                submodels = mongoAASManager.getAllMongoSubmodels();
                log.info("Retrieved {} submodels to check for file properties", submodels.size());
            } catch (Exception e) {
                log.error("Error retrieving submodels: {}", e.getMessage(), e);
                return results;
            }
            
            // Convert submodels to result maps
            for (SubmodelData submodel : submodels) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", submodel.getId());
                result.put("idShort", submodel.getIdShort());
                results.add(result);
            }
            
            // Now filter these results using the file properties
            return filterResultsByFileProperties(results, fileQueries, selectedFields);
            
        } catch (Exception e) {
            log.error("Error performing file-only query: {}", e.getMessage(), e);
            return results;
        }
    }
    
    /**
     * Enrich a query result with file properties
     * @param result The query result to enrich
     * @param submodelId The ID of the submodel to get file properties for
     * @param submodelIdShort The idShort of the submodel (for logging)
     * @param fileQueries The file queries to apply
     * @return The enriched result
     */
    public Map<String, Object> enrichResultWithFileProperties(
            Map<String, Object> result, 
            String submodelId, 
            String submodelIdShort,
            List<FilePropertyQuery> fileQueries) {
        
        if (fileQueries.isEmpty()) {
            log.debug("No file queries to apply for submodel: {}", submodelIdShort);
            return result;
        }
        
        log.info("Enriching result with file properties for submodel: {} ({})", 
                submodelIdShort, submodelId);
        
        try {
            // Get the complete submodel data from the database
            SubmodelData submodel = mongoAASManager.getMongoSubmodelByIdShort(submodelIdShort);
            if (submodel == null) {
                log.warn("Submodel not found with idShort: {}", submodelIdShort);
                return result;
            }
            
            // Get the submodel elements
            List<?> elements = submodel.getSubmodelElements();
            if (elements == null || elements.isEmpty()) {
                log.warn("No submodel elements found for submodel: {}", submodelIdShort);
                return result;
            }
            
            log.info("Found {} submodel elements for submodel: {}", elements.size(), submodelIdShort);
            
            // Find all file paths in the submodel elements
            Map<String, List<String>> filePathsByType = new HashMap<>();
            findFilePathsRecursive(elements, filePathsByType);
            if (filePathsByType.isEmpty()) {
                log.warn("No files found for submodel: {}", submodelIdShort);
                return result;
            }
            
            log.info("Found {} file types for submodel: {}", filePathsByType.size(), submodelIdShort);
            
            // Log the file paths for debugging
            for (Map.Entry<String, List<String>> entry : filePathsByType.entrySet()) {
                log.info("Content type: {} has {} files", entry.getKey(), entry.getValue().size());
                for (String path : entry.getValue()) {
                    log.info("  File path: {}", path);
                }
            }
            
            // Check if any files match the queries
            Map<FilePropertyQuery, List<Object>> queryResults = 
                    checkFilesAgainstQueries(filePathsByType, fileQueries);
            
            // Add the query results to the result map
            Map<String, List<Object>> multiValueProperties = new HashMap<>();
            
            for (Map.Entry<FilePropertyQuery, List<Object>> entry : queryResults.entrySet()) {
                FilePropertyQuery query = entry.getKey();
                List<Object> values = entry.getValue();
                
                if (values.isEmpty()) {
                    log.debug("No values found for property: {}", query.getPropertyPath());
                    continue;
                }
                
                log.info("Found {} values for property: {}", values.size(), query.getPropertyPath());
                
                // Store all values for this property
                String propertyPath = query.getPropertyPath();
                multiValueProperties.computeIfAbsent(propertyPath, k -> new ArrayList<>())
                        .addAll(values);
            }
            // Add all properties to the result
            for (Map.Entry<String, List<Object>> entry : multiValueProperties.entrySet()) {
                String propertyPath = entry.getKey();
                List<Object> values = entry.getValue();
                
                if (values.isEmpty()) {
                    continue; // Skip empty value lists
                }
                
                // Always add the first value with the original property name for backward compatibility
                log.info("Adding first value for property: {}", propertyPath);
                result.put(propertyPath, values.get(0));
                
                // If there are multiple values, also add the full list with _all suffix
                if (values.size() > 1) {
                    log.info("Adding all {} values for property: {}_all", values.size(), propertyPath);
                    result.put(propertyPath + "_all", values);
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("Error enriching result with file properties: {}", e.getMessage(), e);
            return result;
        }
    }


/**
 * Enrich a query result with file properties
 * @param result The query result to enrich
 * @param filePathsByType Map of file content types to file paths
 * @param propertyPaths Set of property paths to extract and include
 * @param fileQueries Map of file property paths to query values
 * @return The enriched result map
 */
private Map<String, Object> enrichResultWithFileProperties(
        Map<String, Object> result, 
        Map<String, List<String>> filePathsByType,
        Set<String> propertyPaths,
        Map<String, Object> fileQueries) {

    log.info("Enriching result with file properties: {} file types, {} property paths", 
            filePathsByType.size(), propertyPaths.size());

    // Create a new map for the enriched result
    Map<String, Object> enrichedResult = new HashMap<>(result);
    
    // Initialize map to store property values with their source file paths
    Map<String, List<Map<String, Object>>> multiValueProperties = new HashMap<>();
    
    // Get file matching information if available from the previous filtering step
    Map<String, Boolean> fileMatchesQueries = new HashMap<>();
    String primaryFile = null;
    
    if (result.containsKey("_fileMatches") && result.get("_fileMatches") instanceof Map) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Boolean> matches = (Map<String, Boolean>) result.get("_fileMatches");
            fileMatchesQueries.putAll(matches);
            log.info("Using pre-computed file matches: {} files", fileMatchesQueries.size());
            
            // Remove this temporary field from the final result
            enrichedResult.remove("_fileMatches");
        } catch (ClassCastException e) {
            log.warn("Could not use pre-computed file matches: {}", e.getMessage());
        }
    }
    
    // Get primary file if specified
    if (result.containsKey("_primaryFile")) {
        primaryFile = (String) result.get("_primaryFile");
        log.info("Using primary file: {}", primaryFile);
        
        // Remove this temporary field from the final result
        enrichedResult.remove("_primaryFile");
    }

    // First pass: if we have file queries, determine which files match them
    if (!fileQueries.isEmpty()) {
        for (Map.Entry<String, List<String>> entry : filePathsByType.entrySet()) {
            String contentType = entry.getKey();
            List<String> filePathsInType = entry.getValue();
            
            log.debug("Checking query matches for {} files of type {}", filePathsInType.size(), contentType);
            
            Optional<FileContextInitializer> initializerOpt = fileContextInitializerRegistry.getInitializer(contentType);
            if (!initializerOpt.isPresent()) {
                log.warn("No file context initializer found for content type: {}", contentType);
                continue;
            }
            
            FileContextInitializer initializer = initializerOpt.get();
            
            // Check each file against the queries
            for (String filePath : filePathsInType) {
                boolean fileMatchesAllQueries = true;
                
                try {
                    File file = new File(filePath);
                    if (!file.exists()) {
                        continue;
                    }
                    
                    FileContext context = initializer.initialize(file);
                    if (context == null) {
                        continue;
                    }
                    
                    try {
                        // Check each query against this file
                        for (Map.Entry<String, Object> queryEntry : fileQueries.entrySet()) {
                            String propPath = normalizePropertyPath(queryEntry.getKey());
                            Object queryValue = queryEntry.getValue();
                            
                            QueryableProperty property = propertyRegistry.getPropertyByPath(propPath);
                            if (property == null) {
                                continue;
                            }
                            
                            try {
                                Object propertyValue = property.extract(context);
                                if (propertyValue != null) {
                                    boolean matches = matchesQueryValue(propertyValue, queryValue);
                                    log.info("File {} property {} value {} matches query {}: {}", 
                                            filePath, propPath, propertyValue, queryValue, matches);
                                    
                                    if (!matches) {
                                        fileMatchesAllQueries = false;
                                        break;
                                    }
                                } else {
                                    fileMatchesAllQueries = false;
                                    break;
                                }
                            } catch (Exception e) {
                                log.warn("Error extracting property {} from file {}: {}", 
                                        propPath, filePath, e.getMessage());
                                fileMatchesAllQueries = false;
                                break;
                            }
                        }
                    } finally {
                        context.close();
                    }
                } catch (Exception e) {
                    log.warn("Error processing file {}: {}", filePath, e.getMessage());
                    fileMatchesAllQueries = false;
                }
                
                // Record if this file matched all queries
                fileMatchesQueries.put(filePath, fileMatchesAllQueries);
                log.info("File {} matches all queries: {}", filePath, fileMatchesAllQueries);
            }
        }
    }
    
    // Second pass: collect property values from all files
    for (Map.Entry<String, List<String>> entry : filePathsByType.entrySet()) {
        String contentType = entry.getKey();
        List<String> filePathsInType = entry.getValue();

        log.debug("Processing {} files of type {}", filePathsInType.size(), contentType);

        // Get the file context initializer for this content type
        Optional<FileContextInitializer> initializerOpt = fileContextInitializerRegistry.getInitializer(contentType);
        if (!initializerOpt.isPresent()) {
            log.warn("No file context initializer found for content type: {}", contentType);
            continue;
        }

        FileContextInitializer initializer = initializerOpt.get();

        // Process each file
        for (String filePath : filePathsInType) {
            log.debug("Processing file: {}", filePath);

            try {
                // Initialize the file context
                File file = new File(filePath);
                if (!file.exists()) {
                    log.warn("File does not exist: {}", filePath);
                    continue;
                }
                
                FileContext context = initializer.initialize(file);
                if (context == null) {
                    log.warn("Failed to initialize file context for file: {}", filePath);
                    continue;
                }

                try {
                    // Extract all requested properties
                    for (String propPath : propertyPaths) {
                        // Get the property extractor
                        QueryableProperty property = propertyRegistry.getPropertyByPath(propPath);
                        if (property == null) {
                            log.warn("No property extractor found for path: {}", propPath);
                            continue;
                        }

                        try {
                            // Extract the property value
                            Object propertyValue = property.extract(context);

                            if (propertyValue != null) {
                                log.info("Extracted property {} value '{}' from file {}", 
                                        propPath, propertyValue, filePath);

                                // Store the property value along with file path for later
                                Map<String, Object> valueWithSource = new HashMap<>();
                                valueWithSource.put("value", propertyValue);
                                valueWithSource.put("filePath", filePath);
                                
                                multiValueProperties.computeIfAbsent(propPath, k -> new ArrayList<>())
                                        .add(valueWithSource);

                                // Also store with short path if it has a prefix
                                if (propPath.startsWith("file.")) {
                                    String shortPath = propPath.substring(5);
                                    multiValueProperties.computeIfAbsent(shortPath, k -> new ArrayList<>())
                                            .add(valueWithSource);
                                }
                            } else {
                                log.debug("Property {} from file {} (via context) was null", propPath, filePath);
                            }
                        } catch (IOException e) {
                            log.error("IOException extracting property {} from file {}: {}", 
                                    propPath, filePath, e.getMessage(), e);
                        } catch (Exception e) {
                            log.error("Unexpected error extracting property {} from file {}: {}", 
                                    propPath, filePath, e.getMessage(), e);
                        }
                    }
                } finally {
                    // Always close the context
                    try {
                        context.close();
                    } catch (Exception e) {
                        log.error("Error closing file context for {}: {}", filePath, e.getMessage());
                    }
                }
            } catch (IOException e) {
                log.error("IOException during FileContext initialization for file {}: {}", 
                        filePath, e.getMessage(), e);
            } catch (Exception e) {
                log.error("Unexpected error during FileContext initialization for file {}: {}", 
                        filePath, e.getMessage(), e);
            }
        }
    }

    // Add all collected property values to the enriched result
    for (Map.Entry<String, List<Map<String, Object>>> entry : multiValueProperties.entrySet()) {
        String propPath = entry.getKey();
        List<Map<String, Object>> valueWrappers = entry.getValue();

        if (valueWrappers.isEmpty()) {
            continue;
        }
        
        // Extract actual values and file paths
        List<Object> actualValues = new ArrayList<>();
        List<Object> matchingValues = new ArrayList<>(); // Values from files that match the query
        Map<Object, String> valueToFilePath = new HashMap<>();
        
        for (Map<String, Object> wrapper : valueWrappers) {
            Object value = wrapper.get("value");
            String filePath = (String) wrapper.get("filePath");
            actualValues.add(value);
            valueToFilePath.put(value, filePath);
            
            // If this file matches the queries, add its value to matchingValues
            if (!fileQueries.isEmpty() && Boolean.TRUE.equals(fileMatchesQueries.get(filePath))) {
                matchingValues.add(value);
            }
        }
        
        if (actualValues.isEmpty()) {
            continue;
        }
        
        // Determine which value to use as the primary value
        Object primaryValue = null;
        
        // If we have a primary file specified, try to use its value
        if (primaryFile != null) {
            for (Map<String, Object> wrapper : valueWrappers) {
                String filePath = (String) wrapper.get("filePath");
                if (primaryFile.equals(filePath)) {
                    primaryValue = wrapper.get("value");
                    log.info("Using value from primary file {} for property {}: {}", 
                            filePath, propPath, primaryValue);
                    break;
                }
            }
        }
        
        // If we couldn't find a value from the primary file, use matching values
        if (primaryValue == null && !matchingValues.isEmpty()) {
            primaryValue = matchingValues.get(0);
            log.info("Using value from matching file for property {}: {}", propPath, primaryValue);
        }
        
        // If we still don't have a value, use the first value
        if (primaryValue == null) {
            primaryValue = actualValues.get(0);
            log.info("Using first available value for property {}: {}", propPath, primaryValue);
        }
        
        // Add the appropriate list with _all suffix
        if (!matchingValues.isEmpty() && matchingValues.size() > 1) {
            // If we have multiple matching values, use them
            log.info("Adding {} matching values for property {}_all", matchingValues.size(), propPath);
            enrichedResult.put(propPath + "_all", matchingValues);
        } else if (actualValues.size() > 1) {
            // Otherwise add all values for completeness
            log.info("Adding all {} values for property {}_all", actualValues.size(), propPath);
            enrichedResult.put(propPath + "_all", actualValues);
        }
        
        // Add the primary value with the original property path
        log.info("Adding property {} = {} to result", propPath, primaryValue);
        enrichedResult.put(propPath, primaryValue);
        
        // Also add with short path if it has a prefix for easier access
        if (propPath.startsWith("file.")) {
            String shortPath = propPath.substring(5);
            enrichedResult.put(shortPath, primaryValue);
            
            // Add the appropriate list with _all suffix
            if (!matchingValues.isEmpty() && matchingValues.size() > 1) {
                enrichedResult.put(shortPath + "_all", matchingValues);
            } else if (actualValues.size() > 1) {
                enrichedResult.put(shortPath + "_all", actualValues);
            }
        }
    }
    
    log.info("Enriched result with {} properties", multiValueProperties.size());
    return enrichedResult;
}

/**
 * Find all file paths in a list of elements
 * @param elements The elements to find file paths for
 * @return Map of content type to list of file paths
 */
private Map<String, List<String>> findFilePaths(List<?> elements) {
    Map<String, List<String>> pathsByType = new HashMap<>();
    
    if (elements == null || elements.isEmpty()) {
        return pathsByType;
    }
    
    // Process each element recursively
    findFilePathsRecursive(elements, pathsByType);
    
    return pathsByType;
}

/**
 * Helper method to recursively collect all file paths
 * @param elements The elements to search through
 * @param pathsByType Map to collect paths by content type
 */
private void findFilePathsRecursive(List<?> elements, Map<String, List<String>> pathsByType) {
    if (elements == null || elements.isEmpty()) {
        return;
    }
    
    for (Object element : elements) {
        if (element == null) {
            continue;
        }
        
        log.debug("Processing element of type: {}", element.getClass().getName());
        
        // Handle different types of elements
        if (element instanceof Map) {
            // Process map elements
            Map<?, ?> mapElement = (Map<?, ?>) element;
            log.debug("Map element keys: {}", mapElement.keySet());
            
            // Check if this is a file element directly
            checkAndAddFileElement(mapElement, pathsByType);
            
            // Check for DefaultFile model type
            Object modelTypeObj = mapElement.get("modelType");
            if (modelTypeObj instanceof String && "DefaultFile".equals(modelTypeObj)) {
                log.debug("Found DefaultFile element");
                
                // Get the file field
                Object fileObj = mapElement.get("file");
                if (fileObj instanceof Map) {
                    Map<?, ?> fileMap = (Map<?, ?>) fileObj;
                    log.debug("File map keys: {}", fileMap.keySet());
                    
                    // Extract contentType and path
                    Object contentTypeObj = fileMap.get("contentType");
                    Object valueObj = fileMap.get("value");
                    
                    if (contentTypeObj instanceof String && valueObj instanceof String) {
                        String contentType = (String) contentTypeObj;
                        String filePath = (String) valueObj;
                        
                        if (contentType != null && !contentType.isEmpty() && filePath != null && !filePath.isEmpty()) {
                            log.info("Found file in DefaultFile: {} with content type: {}", filePath, contentType);
                            pathsByType.computeIfAbsent(contentType, k -> new ArrayList<>()).add(filePath);
                        }
                    }
                }
            }
            
            // Check various possible nested structures
            checkNestedElements(mapElement, "submodelElements", pathsByType);
            checkNestedElements(mapElement, "value", pathsByType);
            checkNestedElements(mapElement, "values", pathsByType);
            checkNestedElements(mapElement, "elements", pathsByType);
            
        } else if (element instanceof Document) {
            // Process BSON documents
            Document doc = (Document) element;
            log.debug("Document keys: {}", doc.keySet());
            
            // Check if this is a file element directly
            if (doc.containsKey("contentType") && doc.containsKey("filePath")) {
                String contentType = doc.getString("contentType");
                String filePath = doc.getString("filePath");
                
                if (contentType != null && !contentType.isEmpty() && filePath != null && !filePath.isEmpty()) {
                    log.info("Found file path in Document: {} with content type: {}", filePath, contentType);
                    pathsByType.computeIfAbsent(contentType, k -> new ArrayList<>()).add(filePath);
                }
            }
            
            // Check for DefaultFile model type
            if (doc.containsKey("modelType") && "DefaultFile".equals(doc.getString("modelType"))) {
                log.debug("Found DefaultFile document");
                
                // Get the file field
                if (doc.containsKey("file") && doc.get("file") instanceof Document) {
                    Document fileDoc = doc.get("file", Document.class);
                    
                    if (fileDoc.containsKey("contentType") && fileDoc.containsKey("value")) {
                        String contentType = fileDoc.getString("contentType");
                        String filePath = fileDoc.getString("value");
                        
                        if (contentType != null && !contentType.isEmpty() && filePath != null && !filePath.isEmpty()) {
                            log.info("Found file in DefaultFile document: {} with content type: {}", filePath, contentType);
                            pathsByType.computeIfAbsent(contentType, k -> new ArrayList<>()).add(filePath);
                        }
                    }
                }
            }
            
            // Check various possible nested structures
            checkNestedDocumentElements(doc, "submodelElements", pathsByType);
            checkNestedDocumentElements(doc, "value", pathsByType);
            checkNestedDocumentElements(doc, "values", pathsByType);
            checkNestedDocumentElements(doc, "elements", pathsByType);
        }
    }
}

/**
 * Helper method to check and add a file element from a map
 * @param mapElement The map element to check
 * @param pathsByType Map to collect paths by content type
 */
private void checkAndAddFileElement(Map<?, ?> mapElement, Map<String, List<String>> pathsByType) {
    // Check if this is a file element with content type and path
    Object contentTypeObj = mapElement.get("contentType");
    Object filePathObj = mapElement.get("filePath");
    
    if (contentTypeObj instanceof String && filePathObj instanceof String) {
        String contentType = (String) contentTypeObj;
        String filePath = (String) filePathObj;
        
        if (contentType != null && !contentType.isEmpty() && filePath != null && !filePath.isEmpty()) {
            log.info("Found direct file path: {} with content type: {}", filePath, contentType);
            pathsByType.computeIfAbsent(contentType, k -> new ArrayList<>()).add(filePath);
        }
    }
}

/**
 * Helper method to check nested elements in a map
 * @param mapElement The map element to check
 * @param key The key for the nested elements
 * @param pathsByType Map to collect paths by content type
 */
private void checkNestedElements(Map<?, ?> mapElement, String key, Map<String, List<String>> pathsByType) {
    Object nestedObj = mapElement.get(key);
    if (nestedObj instanceof List) {
        log.debug("Found nested list in '{}' with {} elements", key, ((List<?>) nestedObj).size());
        findFilePathsRecursive((List<?>) nestedObj, pathsByType);
    }
}

/**
 * Helper method to check nested elements in a document
 * @param doc The document to check
 * @param key The key for the nested elements
 * @param pathsByType Map to collect paths by content type
 */
private void checkNestedDocumentElements(Document doc, String key, Map<String, List<String>> pathsByType) {
    if (doc.containsKey(key) && doc.get(key) instanceof List) {
        log.debug("Found nested list in document '{}' with {} elements", key, doc.getList(key, Object.class).size());
        findFilePathsRecursive(doc.getList(key, Object.class), pathsByType);
    }
}



/**
 * Check if any file matches all query conditions and extract property values
 * @param filePathsByType Map of content type to list of file paths
 * @param fileQueries List of file property queries
 * @return Map of query to list of property values
 */
private Map<FilePropertyQuery, List<Object>> checkFilesAgainstQueries(
        Map<String, List<String>> filePathsByType, 
        List<FilePropertyQuery> fileQueries) {
    
    Map<FilePropertyQuery, List<Object>> results = new HashMap<>();
    
    // Initialize result lists for each query
    for (FilePropertyQuery query : fileQueries) {
        results.put(query, new ArrayList<>());
    }
    
    // Process each file type
    for (Map.Entry<String, List<String>> entry : filePathsByType.entrySet()) {
        String contentType = entry.getKey();
        List<String> filePaths = entry.getValue();
        
        log.debug("Checking {} files of type {}", filePaths.size(), contentType);
        
        // Process each file
        for (String filePath : filePaths) {
            log.debug("Processing file: {}", filePath);
            
            // Get the file context initializer for this content type
            Optional<FileContextInitializer> initializerOpt = fileContextInitializerRegistry.getInitializer(contentType);
            if (!initializerOpt.isPresent()) {
                log.warn("No file context initializer found for content type: {}", contentType);
                continue;
            }
            FileContextInitializer initializer = initializerOpt.get();
            
            try {
                // Initialize the file context
                FileContext context = initializer.initialize(new File(filePath));
                
                try {
                    // For each query, check if the file has the property and if it matches
                    for (FilePropertyQuery query : fileQueries) {
                        String propertyPath = query.getPropertyPath();
                        // Normalize property path if needed
                        if (propertyPath.startsWith("file.")) {
                            propertyPath = normalizePropertyPath(propertyPath);
                        }
                        
                        // Get the property extractor
                        QueryableProperty property = propertyRegistry.getPropertyByPath(propertyPath);
                        if (property == null) {
                            log.warn("No property extractor found for path: {}", propertyPath);
                            continue;
                        }
                        
                        try {
                            // Extract the property value
                            Object propertyValue = property.extract(context);
                            
                            if (propertyValue != null) {
                                log.debug("Extracted property {} value '{}' from file {}", 
                                        propertyPath, propertyValue, filePath);
                                
                                // Check if the property value matches the query value
                                if (matchesQueryValue(propertyValue, query.getQueryValue(), query.getOperator())) {
                                    results.get(query).add(propertyValue);
                                    log.debug("File {} property {} value '{}' MATCHED query '{}'",
                                            filePath, propertyPath, propertyValue, query);
                                } else {
                                    log.debug("File {} property {} value '{}' did NOT match query '{}'",
                                            filePath, propertyPath, propertyValue, query);
                                }
                            } else {
                                log.debug("Property {} from file {} (via context) was null", propertyPath, filePath);
                            }
                        } catch (IOException e) { // More specific catch for extraction
                            log.error("IOException extracting property {} from file {}: {}", 
                                    propertyPath, filePath, e.getMessage(), e);
                        } catch (Exception e) {
                            log.error("Unexpected error extracting property {} from file {}: {}", 
                                    propertyPath, filePath, e.getMessage(), e);
                        }
                    }
                } finally {
                    // Always close the context
                    context.close();
                }
            } catch (IOException e) { // For initialize() or context.close()
                log.error("IOException during FileContext initialization or closing for file {}: {}", filePath, e.getMessage(), e);
            } catch (Exception e) { // For other errors during initialize()
                log.error("Unexpected error during FileContext initialization for file {}: {}", filePath, e.getMessage(), e);
            }
        }
    }
    
    return results;
}


/**
 * Check if a property value matches a query value
 * @param propertyValue The property value to check
 * @param queryValue The query value to match against
 * @param operator The comparison operator to use
 * @return true if the property value matches the query value
 */
private boolean matchesQueryValue(Object propertyValue, Object queryValue, FilePropertyQuery.Operator operator) {
    // Handle null values
    if (propertyValue == null) {
        return queryValue == null && operator == FilePropertyQuery.Operator.EQUALS;
    }
    
    // Handle different operator types
    switch (operator) {
        case EQUALS:
            // Special handling for floating point comparisons
            if (propertyValue instanceof Number && queryValue instanceof Number) {
                Number propNumber = (Number)propertyValue;
                Number queryNumber = (Number)queryValue;
                
                // Use our compareNumbers method with rounding for consistent floating point comparison
                log.debug("Comparing numbers for equality: {} == {}", propNumber, queryNumber);
                return compareNumbers(propNumber, queryNumber) == 0;
            }
            return propertyValue.equals(queryValue);
            
        case NOT_EQUALS:
            // Special handling for floating point comparisons
            if (propertyValue instanceof Number && queryValue instanceof Number) {
                Number propNumber = (Number)propertyValue;
                Number queryNumber = (Number)queryValue;
                
                // Use our compareNumbers method with rounding for consistent floating point comparison
                log.debug("Comparing numbers for inequality: {} != {}", propNumber, queryNumber);
                return compareNumbers(propNumber, queryNumber) != 0;
            }
            return !propertyValue.equals(queryValue);
            
        case GREATER_THAN:
            if (!(propertyValue instanceof Comparable<?>)) {
                log.warn("Cannot compare non-comparable property value: {}", propertyValue.getClass());
                return false;
            }
            
            // Handle numeric comparisons with different types
            if (propertyValue instanceof Number && queryValue instanceof Number) {
                return compareNumbers((Number)propertyValue, (Number)queryValue) > 0;
            }
            
            try {
                return ((Comparable<Object>) propertyValue).compareTo(queryValue) > 0;
            } catch (ClassCastException e) {
                log.warn("Cannot compare values of different types: {} and {}", 
                        propertyValue.getClass(), queryValue.getClass());
                return false;
            }
            
        case GREATER_THAN_EQUALS:
            if (!(propertyValue instanceof Comparable<?>)) {
                log.warn("Cannot compare non-comparable property value: {}", propertyValue.getClass());
                return false;
            }
            
            // Handle numeric comparisons with different types
            if (propertyValue instanceof Number && queryValue instanceof Number) {
                return compareNumbers((Number)propertyValue, (Number)queryValue) >= 0;
            }
            
            try {
                return ((Comparable<Object>) propertyValue).compareTo(queryValue) >= 0;
            } catch (ClassCastException e) {
                log.warn("Cannot compare values of different types: {} and {}", 
                        propertyValue.getClass(), queryValue.getClass());
                return false;
            }
            
        case LESS_THAN:
            if (!(propertyValue instanceof Comparable<?>)) {
                log.warn("Cannot compare non-comparable property value: {}", propertyValue.getClass());
                return false;
            }
            
            // Handle numeric comparisons with different types
            if (propertyValue instanceof Number && queryValue instanceof Number) {
                return compareNumbers((Number)propertyValue, (Number)queryValue) < 0;
            }
            
            try {
                return ((Comparable<Object>) propertyValue).compareTo(queryValue) < 0;
            } catch (ClassCastException e) {
                log.warn("Cannot compare values of different types: {} and {}", 
                        propertyValue.getClass(), queryValue.getClass());
                return false;
            }
            
        case LESS_THAN_EQUALS:
            if (!(propertyValue instanceof Comparable<?>)) {
                log.warn("Cannot compare non-comparable property value: {}", propertyValue.getClass());
                return false;
            }
            
            // Handle numeric comparisons with different types
            if (propertyValue instanceof Number && queryValue instanceof Number) {
                return compareNumbers((Number)propertyValue, (Number)queryValue) <= 0;
            }
            
            try {
                return ((Comparable<Object>) propertyValue).compareTo(queryValue) <= 0;
            } catch (ClassCastException e) {
                log.warn("Cannot compare values of different types: {} and {}", 
                        propertyValue.getClass(), queryValue.getClass());
                return false;
            }
            
        case LIKE:
            // Handle LIKE operator for string values
            if (!(propertyValue instanceof String) || !(queryValue instanceof String)) {
                log.warn("LIKE operator can only be used with string values");
                return false;
            }
            
            String pattern = ((String) queryValue).replace("%", ".*").replace("_", ".");
            return ((String) propertyValue).matches(pattern);
            
        default:
            log.warn("Unsupported operator: {}", operator);
            return false;
    }
}

/**
 * Helper method to compare numbers of different types with precision control for floating point
 * @param n1 First number
 * @param n2 Second number
 * @return -1 if n1 < n2, 0 if n1 == n2, 1 if n1 > n2
 */
private int compareNumbers(Number n1, Number n2) {
    // For floating point types, round to a fixed precision to avoid floating point precision issues
    if (n1 instanceof Double || n2 instanceof Double) {
        // Get original values for logging
        double orig1 = n1.doubleValue();
        double orig2 = n2.doubleValue();
        
        // Round to 2 decimal places for comparison (reduced from 5 to be more lenient)
        double d1 = roundToDecimalPlaces(orig1, 2);
        double d2 = roundToDecimalPlaces(orig2, 2);
        
        int result = Double.compare(d1, d2);
        log.info("Comparing doubles: original values {} vs {} (types: {} vs {}), rounded to {} vs {}, result: {}", 
                orig1, orig2, n1.getClass().getSimpleName(), n2.getClass().getSimpleName(), d1, d2, 
                (result < 0 ? "<" : (result > 0 ? ">" : "=")));
        return result;
    } else if (n1 instanceof Float || n2 instanceof Float) {
        // Get original values for logging
        float orig1 = n1.floatValue();
        float orig2 = n2.floatValue();
        
        // Round to 2 decimal places for comparison (reduced from 5 to be more lenient)
        float f1 = (float)roundToDecimalPlaces(orig1, 2);
        float f2 = (float)roundToDecimalPlaces(orig2, 2);
        
        int result = Float.compare(f1, f2);
        log.info("Comparing floats: original values {} vs {} (types: {} vs {}), rounded to {} vs {}, result: {}", 
                orig1, orig2, n1.getClass().getSimpleName(), n2.getClass().getSimpleName(), f1, f2, 
                (result < 0 ? "<" : (result > 0 ? ">" : "=")));
        return result;
    } else if (n1 instanceof Long || n2 instanceof Long) {
        long l1 = n1.longValue();
        long l2 = n2.longValue();
        int result = Long.compare(l1, l2);
        log.info("Comparing longs: {} vs {}, result: {}", l1, l2, (result < 0 ? "<" : (result > 0 ? ">" : "=")));
        return result;
    } else {
        int i1 = n1.intValue();
        int i2 = n2.intValue();
        int result = Integer.compare(i1, i2);
        log.info("Comparing integers: {} vs {}, result: {}", i1, i2, (result < 0 ? "<" : (result > 0 ? ">" : "=")));
        return result;
    }
}

/**
 * Round a double value to a specified number of decimal places
 * @param value The value to round
 * @param places Number of decimal places
 * @return The rounded value
 */
private double roundToDecimalPlaces(double value, int places) {
    if (places < 0) throw new IllegalArgumentException();
    
    long factor = (long) Math.pow(10, places);
    value = value * factor;
    long tmp = Math.round(value);
    return (double) tmp / factor;
}

/**
 * Helper method to convert a value to a Number if possible
 * @param value The value to convert
 * @return The value as a Number, or null if conversion is not possible
 */
private Number convertToNumber(Object value) {
    if (value == null) {
        log.debug("Cannot convert null to number");
        return null;
    }
    
    if (value instanceof Number) {
        log.debug("Value is already a number: {} (type: {})", value, value.getClass().getSimpleName());
        return (Number) value;
    }
    
    if (value instanceof String) {
        String strValue = (String) value;
        log.info("Converting string value '{}' to number", strValue);
        
        // Trim the string to remove any whitespace
        strValue = strValue.trim();
        
        try {
            // Try to parse as integer first if no decimal point
            if (strValue.indexOf('.') == -1) {
                try {
                    int intValue = Integer.parseInt(strValue);
                    log.info("Converted string '{}' to integer: {}", strValue, intValue);
                    return intValue;
                } catch (NumberFormatException e) {
                    try {
                        long longValue = Long.parseLong(strValue);
                        log.info("Converted string '{}' to long: {}", strValue, longValue);
                        return longValue;
                    } catch (NumberFormatException e2) {
                        // Fall through to double parsing
                        log.debug("Could not parse '{}' as integer or long, trying double", strValue);
                    }
                }
            }
            
            // Try to parse as double
            double doubleValue = Double.parseDouble(strValue);
            log.info("Converted string '{}' to double: {}", strValue, doubleValue);
            return doubleValue;
        } catch (NumberFormatException e) {
            log.warn("Failed to convert string value '{}' to a number: {}", strValue, e.getMessage());
            return null;
        }
    }
    
    log.warn("Cannot convert value of type {} to a number: {}", value.getClass().getName(), value);
    return null;
}

/**
 * Check if a property value matches a query value with MongoDB-style operators
 * @param propertyValue The property value extracted from the file
 * @param queryValue The query value from the WHERE clause (may contain MongoDB operators)
 * @return true if the property value matches the query value
 */
private boolean matchesQueryValue(Object propertyValue, Object queryValue) {
    log.debug("Checking if property value {} (type: {}) matches query value {} (type: {})", 
            propertyValue, 
            propertyValue != null ? propertyValue.getClass().getName() : "null",
            queryValue,
            queryValue != null ? queryValue.getClass().getName() : "null");
    
    // Handle null property values
    if (propertyValue == null) {
        if (queryValue == null) {
            log.debug("Both property and query values are null, returning true");
            return true;
        }
        
        // Handle $exists operator for null values
        if (queryValue instanceof org.bson.Document) {
            org.bson.Document doc = (org.bson.Document) queryValue;
            if (doc.containsKey("$exists")) {
                boolean exists = doc.getBoolean("$exists");
                log.debug("Checking $exists: {} against null property value", exists);
                return !exists; // property is null, so it doesn't exist
            }
        }
        
        log.debug("Property value is null but query value is not, returning false");
        return false;
    }
    
    // Handle null query values
    if (queryValue == null) {
        log.debug("Query value is null but property value is not, returning false");
        return false;
    }
    
    // Direct equality comparison for non-document query values
    if (!(queryValue instanceof org.bson.Document)) {
        boolean result = matchesEqual(propertyValue, queryValue);
        log.debug("Direct equality comparison result: {}", result);
        return result;
    }
    
    // Handle MongoDB query operators (in a Document)
    org.bson.Document doc = (org.bson.Document) queryValue;
    log.info("Processing MongoDB query document with operators: {}", doc.keySet());
    
    // If the document is empty, it's a match
    if (doc.isEmpty()) {
        log.debug("Empty query document, returning true");
        return true;
    }
    
    // Process each operator in the document
    for (String operator : doc.keySet()) {
        Object operatorValue = doc.get(operator);
        log.info("Processing operator: {} with value: {}", operator, operatorValue);
        
        switch (operator) {
            case "$eq":
                if (!matchesEqual(propertyValue, operatorValue)) {
                    log.debug("$eq comparison failed: {} != {}", propertyValue, operatorValue);
                    return false;
                }
                log.debug("$eq comparison passed: {} == {}", propertyValue, operatorValue);
                break;
                
            case "$ne":
                if (matchesEqual(propertyValue, operatorValue)) {
                    log.debug("$ne comparison failed");
                    return false;
                }
                break;
                
            case "$gt":
                if (propertyValue instanceof Number) {
                    Number propNumber = (Number)propertyValue;
                    Number opNumber = convertToNumber(operatorValue);
                    
                    if (opNumber != null) {
                        if (!(compareNumbers(propNumber, opNumber) > 0)) {
                            log.debug("$gt numeric comparison failed: {} not > {}", propNumber, opNumber);
                            return false;
                        }
                        log.debug("$gt numeric comparison passed: {} > {}", propNumber, opNumber);
                    } else {
                        log.warn("Cannot convert operator value to number for $gt comparison: {}", operatorValue);
                        return false;
                    }
                } else if (propertyValue instanceof Comparable && operatorValue != null) {
                    try {
                        @SuppressWarnings("unchecked")
                        boolean result = ((Comparable<Object>)propertyValue).compareTo(operatorValue) > 0;
                        if (!result) {
                            log.debug("$gt comparable comparison failed");
                            return false;
                        }
                    } catch (ClassCastException e) {
                        log.warn("Cannot compare values of different types: {} and {}", 
                                propertyValue.getClass(), operatorValue.getClass());
                        return false;
                    }
                } else {
                    log.debug("$gt comparison not possible for types: {} and {}",
                            propertyValue.getClass(), operatorValue != null ? operatorValue.getClass() : "null");
                    return false;
                }
                break;
                
            case "$gte":
                if (propertyValue instanceof Number) {
                    Number propNumber = (Number)propertyValue;
                    Number opNumber = convertToNumber(operatorValue);
                    
                    if (opNumber != null) {
                        if (!(compareNumbers(propNumber, opNumber) >= 0)) {
                            log.debug("$gte numeric comparison failed: {} not >= {}", propNumber, opNumber);
                            return false;
                        }
                        log.debug("$gte numeric comparison passed: {} >= {}", propNumber, opNumber);
                    } else {
                        log.warn("Cannot convert operator value to number for $gte comparison: {}", operatorValue);
                        return false;
                    }
                } else if (propertyValue instanceof Comparable && operatorValue != null) {
                    try {
                        @SuppressWarnings("unchecked")
                        boolean result = ((Comparable<Object>)propertyValue).compareTo(operatorValue) >= 0;
                        if (!result) {
                            log.debug("$gte comparable comparison failed");
                            return false;
                        }
                    } catch (ClassCastException e) {
                        log.warn("Cannot compare values of different types: {} and {}", 
                                propertyValue.getClass(), operatorValue.getClass());
                        return false;
                    }
                } else {
                    log.debug("$gte comparison not possible for types: {} and {}",
                            propertyValue.getClass(), operatorValue != null ? operatorValue.getClass() : "null");
                    return false;
                }
                break;
                
            case "$lt":
                if (propertyValue instanceof Number) {
                    Number propNumber = (Number)propertyValue;
                    Number opNumber = convertToNumber(operatorValue);
                    
                    if (opNumber != null) {
                        if (!(compareNumbers(propNumber, opNumber) < 0)) {
                            log.debug("$lt numeric comparison failed: {} not < {}", propNumber, opNumber);
                            return false;
                        }
                        log.debug("$lt numeric comparison passed: {} < {}", propNumber, opNumber);
                    } else {
                        log.warn("Cannot convert operator value to number for $lt comparison: {}", operatorValue);
                        return false;
                    }
                } else if (propertyValue instanceof Comparable && operatorValue != null) {
                    try {
                        @SuppressWarnings("unchecked")
                        boolean result = ((Comparable<Object>)propertyValue).compareTo(operatorValue) < 0;
                        if (!result) {
                            log.debug("$lt comparable comparison failed");
                            return false;
                        }
                    } catch (ClassCastException e) {
                        log.warn("Cannot compare values of different types: {} and {}", 
                                propertyValue.getClass(), operatorValue.getClass());
                        return false;
                    }
                } else {
                    log.debug("$lt comparison not possible for types: {} and {}",
                            propertyValue.getClass(), operatorValue != null ? operatorValue.getClass() : "null");
                    return false;
                }
                break;
                
            case "$lte":
                log.info("Processing $lte operator with property value: {} (type: {}) and operator value: {} (type: {})",
                        propertyValue,
                        propertyValue != null ? propertyValue.getClass().getName() : "null",
                        operatorValue,
                        operatorValue != null ? operatorValue.getClass().getName() : "null");
                
                if (propertyValue instanceof Number) {
                    Number propNumber = (Number)propertyValue;
                    Number opNumber = convertToNumber(operatorValue);
                    
                    if (opNumber != null) {
                        // Get raw values for logging
                        double rawPropValue = propNumber.doubleValue();
                        double rawOpValue = opNumber.doubleValue();
                        log.info("$lte raw values before rounding: {} <= {}", rawPropValue, rawOpValue);
                        
                        int compResult = compareNumbers(propNumber, opNumber);
                        boolean isLessThanOrEqual = compResult <= 0;
                        
                        log.info("$lte comparison result: {} {} {} (result: {})", 
                                propNumber, isLessThanOrEqual ? "<=" : ">", opNumber, isLessThanOrEqual);
                        
                        if (!isLessThanOrEqual) {
                            log.info("$lte numeric comparison FAILED: {} > {}", propNumber, opNumber);
                            return false;
                        }
                        log.info("$lte numeric comparison PASSED: {} <= {}", propNumber, opNumber);
                    } else {
                        log.warn("Cannot convert operator value to number for $lte comparison: {}", operatorValue);
                        return false;
                    }
                } else if (propertyValue instanceof Comparable && operatorValue != null) {
                    try {
                        @SuppressWarnings("unchecked")
                        boolean result = ((Comparable<Object>)propertyValue).compareTo(operatorValue) <= 0;
                        log.info("$lte comparable comparison result: {} {} {}", 
                                propertyValue, result ? "<=" : ">", operatorValue);
                        if (!result) {
                            log.info("$lte comparable comparison FAILED: {} > {}", propertyValue, operatorValue);
                            return false;
                        }
                        log.info("$lte comparable comparison PASSED: {} <= {}", propertyValue, operatorValue);
                    } catch (ClassCastException e) {
                        log.warn("Cannot compare values of different types: {} and {}", 
                                propertyValue.getClass(), operatorValue.getClass());
                        return false;
                    }
                } else {
                    log.info("$lte comparison not possible for types: {} and {}",
                            propertyValue.getClass(), operatorValue != null ? operatorValue.getClass() : "null");
                    return false;
                }
                break;
                
            case "$in":
                boolean inResult = handleInOperator(propertyValue, operatorValue);
                log.debug("$in comparison result: {}", inResult);
                if (!inResult) return false;
                break;
                
            case "$nin":
                boolean ninResult = !handleInOperator(propertyValue, operatorValue);
                log.debug("$nin comparison result: {}", ninResult);
                if (!ninResult) return false;
                break;
                
            case "$regex":
                Object options = doc.get("$options");
                boolean regexResult = handleRegexOperator(propertyValue, operatorValue, options);
                log.debug("$regex comparison result: {}", regexResult);
                if (!regexResult) return false;
                break;
                
            case "$exists":
                boolean exists = doc.getBoolean(operator);
                log.debug("$exists comparison result: {}", exists);
                // Property exists since we got here (propertyValue is not null)
                if (!exists) return false;
                break;
                
            case "$options":
                // Skip, handled with $regex
                break;
                
            default:
                log.warn("Unsupported operator: {}", operator);
                return false;
        }
    }
    
    // All operators matched
    return true;
}

/**
 * Helper method to check if two values are equal, handling different types
 * @param propertyValue The property value
 * @param queryValue The query value to compare against
 * @return true if the values are equal
 */
private boolean matchesEqual(Object propertyValue, Object queryValue) {
    log.debug("Checking equality between {} and {}", propertyValue, queryValue);
    
    // Handle null values
    if (propertyValue == null || queryValue == null) {
        boolean result = (propertyValue == null && queryValue == null);
        log.debug("Null equality result: {}", result);
        return result;
    }
    
    // Handle string comparison with case insensitivity
    if (propertyValue instanceof String || queryValue instanceof String) {
        // Convert both to strings for comparison if either one is a string
        String propStr = propertyValue.toString();
        String queryStr = queryValue.toString();
        boolean result = propStr.equalsIgnoreCase(queryStr);
        log.debug("String equality result: {} = {} : {}", propStr, queryStr, result);
        return result;
    }
    
    // Handle numeric comparison
    if (propertyValue instanceof Number && queryValue instanceof Number) {
        int result = compareNumbers((Number) propertyValue, (Number) queryValue);
        log.debug("Numeric equality result: {} = {} : {}", propertyValue, queryValue, result == 0);
        return result == 0;
    }
    
    // Handle boolean comparison
    if (propertyValue instanceof Boolean && queryValue instanceof Boolean) {
        boolean result = propertyValue.equals(queryValue);
        log.debug("Boolean equality result: {}", result);
        return result;
    }
    
    // Handle date comparison
    if (propertyValue instanceof Date && queryValue instanceof Date) {
        boolean result = ((Date) propertyValue).compareTo((Date) queryValue) == 0;
        log.debug("Date equality result: {}", result);
        return result;
    }
    
    // Try string conversion as last resort
    try {
        boolean result = propertyValue.toString().equalsIgnoreCase(queryValue.toString());
        log.debug("String conversion equality result: {} = {} : {}", 
                propertyValue.toString(), queryValue.toString(), result);
        return result;
    } catch (Exception e) {
        log.warn("Failed to compare values: {} and {}", propertyValue, queryValue, e);
        return false;
    }
}

/**
 * Handle MongoDB $in operator
 * @param propertyValue The property value to check
 * @param operand The operand (should be a list)
 * @return true if the property value is in the list
 */
private boolean handleInOperator(Object propertyValue, Object operand) {
    if (!(operand instanceof List)) {
        log.warn("$in operator requires a list operand, got: {}", operand.getClass().getName());
        return false;
    }
    
    List<?> list = (List<?>) operand;
    for (Object item : list) {
        if (matchesEqual(propertyValue, item)) {
            return true;
        }
    }
    
    return false;
}

/**
 * Handle MongoDB $regex operator
 * @param propertyValue The property value to check
 * @param pattern The regex pattern
 * @param options The regex options
 * @return true if the property value matches the regex
 */
private boolean handleRegexOperator(Object propertyValue, Object pattern, Object options) {
    if (!(propertyValue instanceof String) || !(pattern instanceof String)) {
        return false;
    }
    
    String stringValue = (String) propertyValue;
    String regexPattern = (String) pattern;
    
    try {
        // Handle options
        int flags = 0;
        if (options instanceof String) {
            String optionsStr = (String) options;
            if (optionsStr.contains("i")) {
                flags |= Pattern.CASE_INSENSITIVE;
            }
            if (optionsStr.contains("m")) {
                flags |= Pattern.MULTILINE;
            }
        }
        
        Pattern regex = Pattern.compile(regexPattern, flags);
        return regex.matcher(stringValue).find();
    } catch (PatternSyntaxException e) {
        log.warn("Invalid regex pattern: {}", regexPattern, e);
        return false;
    }
}

/**
 * Find all file paths in a submodel by its ID
 * @param submodelId The ID of the submodel to find file paths for
 * @return Map of content type to list of file paths
 */
public Map<String, List<String>> findFilePathsForSubmodel(String submodelId) {
    log.info("Finding file paths for submodel: {}", submodelId);
    Map<String, List<String>> pathsByType = new HashMap<>();
    
    try {
        // Get the submodel data - try by ID first, then by idShort if needed
        SubmodelData submodelData = mongoAASManager.getMongoSubmodelByIdShort(submodelId);
        if (submodelData == null) {
            log.warn("No submodel found with ID/idShort: {}", submodelId);
            return pathsByType;
        }
        
        return findFilePathsForSubmodelData(submodelData);
    } catch (Exception e) {
        log.error("Error finding file paths for submodel {}: {}", submodelId, e.getMessage(), e);
        return pathsByType;
    }
}

/**
 * Find all file paths in a submodel data object
 * @param submodelData The submodel data to find file paths for
 * @return Map of content type to list of file paths
 */
public Map<String, List<String>> findFilePathsForSubmodelData(SubmodelData submodelData) {
    if (submodelData == null) {
        log.warn("Cannot find file paths for null submodel data");
        return new HashMap<>();
    }
    
    String submodelIdShort = submodelData.getIdShort();
    log.info("Finding file paths for submodel data: {}", submodelIdShort);
    
    try {
        // Get the submodel elements
        List<?> elements = submodelData.getSubmodelElements();
        if (elements == null || elements.isEmpty()) {
            log.warn("No submodel elements found for submodel: {}", submodelIdShort);
            return new HashMap<>();
        }
        
        // Find all file paths in the submodel elements
        Map<String, List<String>> pathsByType = findFilePaths(elements);
        
        // Log the results
        if (!pathsByType.isEmpty()) {
            log.info("Found {} file types for submodel: {}", pathsByType.size(), submodelIdShort);
            for (Map.Entry<String, List<String>> entry : pathsByType.entrySet()) {
                log.debug("Content type: {} has {} files", entry.getKey(), entry.getValue().size());
            }
        } else {
            log.info("No files found for submodel: {}", submodelIdShort);
        }
        
        return pathsByType;
    } catch (Exception e) {
        log.error("Error finding file paths for submodel data {}: {}", submodelIdShort, e.getMessage(), e);
        return new HashMap<>();
    }
}

// /**
//  * Get property values for a specific path from files
//  * @param filePathsByType Map of content type to list of file paths
//  * @param propertyPath The property path to get values for
//  * @return Map of file paths to property values
//  */
// private Map<String, Object> getPropertyValuesForPath(Map<String, List<String>> filePathsByType, String propertyPath) {
//     Map<String, Object> propertyValues = new HashMap<>();
    
//     if (filePathsByType.isEmpty() || propertyPath == null) {
//         log.debug("No file paths or null property path for: {}", propertyPath);
//         return new HashMap<>();
//     }
    
//     log.info("Getting property values for path: {}", propertyPath);
    
//     // Normalize the property path
//     String normalizedPath = normalizePropertyPath(propertyPath);
//     log.debug("Normalized property path: {}", normalizedPath);
    
//     // Get the property definition
//     QueryableProperty property = propertyRegistry.getPropertyByPath(normalizedPath);
//     if (property == null) {
//         // Try without the file. prefix if it exists
//         if (normalizedPath.startsWith("file.")) {
//             String pathWithoutPrefix = normalizedPath.substring(5);
//             property = propertyRegistry.getPropertyByPath(pathWithoutPrefix);
//             if (property != null) {
//                 log.info("Found property extractor using path without prefix: {}", pathWithoutPrefix);
//                 normalizedPath = pathWithoutPrefix;
//             }
//         } else {
//             // Try with the file. prefix
//             String pathWithPrefix = "file." + normalizedPath;
//             property = propertyRegistry.getPropertyByPath(pathWithPrefix);
//             if (property != null) {
//                 log.info("Found property extractor using path with prefix: {}", pathWithPrefix);
//                 normalizedPath = pathWithPrefix;
//             }
//         }
        
//         if (property == null) {
//             log.warn("No property found for normalized path: {} (original path: {})", normalizedPath, propertyPath);
//             return propertyValues;
//         }
//     }
    
//     log.info("Found property: {} with path: {}", property.getClass().getSimpleName(), property.getPropertyPath());
    
//     // Get the supported content types for this property
//     String[] supportedTypes = property.getSupportedContentTypes();
//     if (supportedTypes == null || supportedTypes.length == 0) {
//         log.warn("No supported content types for property: {}", normalizedPath);
//         return propertyValues;
//     }
    
//     log.debug("Supported content types for {}: {}", normalizedPath, Arrays.toString(supportedTypes));
    
//     // For each supported content type, get the file paths and extract the property values
//     for (String contentType : supportedTypes) {
//         List<String> filePaths = filePathsByType.get(contentType);
//         if (filePaths == null || filePaths.isEmpty()) {
//             log.debug("No files found for content type: {}", contentType);
//             continue;
//         }
        
//         log.info("Found {} files of type {} for property {}", filePaths.size(), contentType, normalizedPath);
        
//         // For each file path, get the property value
//         for (String filePath : filePaths) {
//             log.debug("Processing file: {}", filePath);
//             File file = new File(filePath);
            
//             if (!file.exists()) {
//                 log.warn("File does not exist: {}", filePath);
//                 continue;
//             }
            
//             try {
//                 // Get the file context initializer for this content type
//                 Optional<FileContextInitializer> initializerOpt = fileContextInitializerRegistry.getInitializer(contentType);
//                 if (!initializerOpt.isPresent()) {
//                     log.warn("No initializer found for content type: {}", contentType);
//                     continue;
//                 }
                
//                 FileContextInitializer initializer = initializerOpt.get();
//                 log.debug("Using initializer: {} for content type: {}", initializer.getClass().getSimpleName(), contentType);
                
//                 // Initialize the file context
//                 FileContext context = initializer.initialize(file);
//                 if (context == null) {
//                     log.warn("Failed to initialize file context for file: {}", filePath);
//                     continue;
//                 }
                
//                 try {
//                     // Extract the property value using the property's extract method
//                     log.debug("Extracting property {} from file {}", normalizedPath, filePath);
//                     Object value = property.extract(context);
                    
//                     if (value != null) {
//                         log.info("Extracted property {} value '{}' (type: {}) from file {}", 
//                                 normalizedPath, value, value.getClass().getName(), filePath);
//                         propertyValues.put(filePath, value);
//                     } else {
//                         log.debug("Property {} from file {} was null", normalizedPath, filePath);
//                     }
//                 } catch (Exception e) {
//                     log.error("Error extracting property {} from file {}: {}", 
//                             normalizedPath, filePath, e.getMessage(), e);
//                 } finally {
//                     // Always close the context
//                     try {
//                         context.close();
//                         log.debug("Closed file context for file: {}", filePath);
//                     } catch (Exception e) {
//                         log.warn("Error closing file context for file {}: {}", filePath, e.getMessage());
//                     }
//                 }
//             } catch (IOException e) {
//                 log.error("IOException initializing file context for file {}: {}", 
//                         filePath, e.getMessage(), e);
//             } catch (Exception e) {
//                 log.error("Error initializing file context for file {}: {}", 
//                         filePath, e.getMessage(), e);
//             }
//         }
//     }
    
//     log.info("Found {} property values for path {}: {}", propertyValues.size(), normalizedPath, propertyValues);
//     return propertyValues;
// }
}