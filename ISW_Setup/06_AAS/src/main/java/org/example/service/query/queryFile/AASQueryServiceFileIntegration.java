package org.example.service.query.queryFile;

import org.bson.Document;
import org.example.service.AASQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extension of AASQueryService that integrates file property querying
 */
@Service
@Primary
public class AASQueryServiceFileIntegration extends AASQueryService {
    private static final Logger log = LoggerFactory.getLogger(AASQueryServiceFileIntegration.class);
    
    private final FileQueryIntegration fileQueryIntegration;
    private final FileDebugHelper fileDebugHelper;
    
    @Autowired
    public AASQueryServiceFileIntegration(MongoTemplate mongoTemplate, 
                                         FileQueryIntegration fileQueryIntegration,
                                         FileDebugHelper fileDebugHelper) {
        super(mongoTemplate);
        this.fileQueryIntegration = fileQueryIntegration;
        this.fileDebugHelper = fileDebugHelper;
    }
    
    /**
     * Override the executeQuery method to add file property query support
     */
    @Override
    public List<Map<String, Object>> executeQuery(String sqlQuery) {
        try {
            log.info("Executing query with file property support: {}", sqlQuery);
            
            // Parse the SQL query to get the MongoDB query
            Document mongoQuery = parseSqlToMongoQuery(sqlQuery);
            if (mongoQuery == null) {
                throw new RuntimeException("Failed to parse SQL query");
            }
            
            // Process the query to handle file property queries
            Document processedQuery = fileQueryIntegration.processQuery(mongoQuery);
            
            // Execute the query using the parent method
            // We need to convert back to SQL for the parent method
            String processedSql = convertMongoQueryToSql(processedQuery, sqlQuery);
            List<Map<String, Object>> results = super.executeQuery(processedSql);
            
            log.info("Raw query returned {} results, now enriching with file properties", results.size());
            
            // Debug the structure of each submodel to find file elements
            if (!results.isEmpty()) {
                log.info("Debugging the first submodel to find file elements");
                fileDebugHelper.debugSubmodelStructure(results.get(0));
            }
            
            // Extract the selected fields from the SQL query
            List<String> selectedFields = new ArrayList<>();
            if (mongoQuery.containsKey("_select")) {
                selectedFields = (List<String>) mongoQuery.get("_select");
                log.info("Selected fields from query: {}", selectedFields);
            }
            
            // Explicitly check for file properties in the SQL query
            String sqlQueryUpper = sqlQuery.toUpperCase();
            
            // Check for dimensions.length in the query (both regular and _all versions)
            if (sqlQueryUpper.contains("DIMENSIONS.LENGTH_ALL") && !selectedFields.contains("dimensions.length_all")) {
                selectedFields.add("dimensions.length_all");
                log.info("Added dimensions.length_all to selected fields");
            } else if (sqlQueryUpper.contains("DIMENSIONS.LENGTH") && !selectedFields.contains("dimensions.length")) {
                selectedFields.add("dimensions.length");
                log.info("Added dimensions.length to selected fields");
            }
            
            // Check for partName in the query (both regular and _all versions)
            if (sqlQueryUpper.contains("PARTNAME_ALL") && !selectedFields.contains("partName_all")) {
                selectedFields.add("partName_all");
                log.info("Added partName_all to selected fields");
            } else if (sqlQueryUpper.contains("PARTNAME") && !selectedFields.contains("partName")) {
                selectedFields.add("partName");
                log.info("Added partName to selected fields");
            }
            
            // Check for dimensions.width in the query (both regular and _all versions)
            if (sqlQueryUpper.contains("DIMENSIONS.WIDTH_ALL") && !selectedFields.contains("dimensions.width_all")) {
                selectedFields.add("dimensions.width_all");
                log.info("Added dimensions.width_all to selected fields");
            } else if (sqlQueryUpper.contains("DIMENSIONS.WIDTH") && !selectedFields.contains("dimensions.width")) {
                selectedFields.add("dimensions.width");
                log.info("Added dimensions.width to selected fields");
            }
            
            // Check for dimensions.height in the query (both regular and _all versions)
            if (sqlQueryUpper.contains("DIMENSIONS.HEIGHT_ALL") && !selectedFields.contains("dimensions.height_all")) {
                selectedFields.add("dimensions.height_all");
                log.info("Added dimensions.height_all to selected fields");
            } else if (sqlQueryUpper.contains("DIMENSIONS.HEIGHT") && !selectedFields.contains("dimensions.height")) {
                selectedFields.add("dimensions.height");
                log.info("Added dimensions.height to selected fields");
            }
            
            // Check for color in the query (both regular and _all versions)
            if (sqlQueryUpper.contains("COLOR_ALL") && !selectedFields.contains("color_all")) {
                selectedFields.add("color_all");
                log.info("Added color_all to selected fields");
            } else if (sqlQueryUpper.contains("COLOR") && !selectedFields.contains("color")) {
                selectedFields.add("color");
                log.info("Added color to selected fields");
            }
            
            log.info("Final selected fields: {}", selectedFields);
            
            // Always post-process the results to include file properties
            // This ensures file properties are included even in simple SELECT queries
            // But only include the properties that were explicitly requested
            List<Map<String, Object>> processedResults = fileQueryIntegration.postProcessResults(results, mongoQuery, selectedFields);
            
            // If we have selected fields, filter the results to only include those fields
            if (!selectedFields.isEmpty()) {
                log.info("STRICTLY filtering results to ONLY include selected fields: {}", selectedFields);
                
                // Debug the current result structure
                if (!processedResults.isEmpty()) {
                    Map<String, Object> firstResult = processedResults.get(0);
                    log.info("Before filtering, result contains {} fields: {}", 
                            firstResult.size(), firstResult.keySet());
                }
                
                // Create a map of new results with only the selected fields
                List<Map<String, Object>> strictlyFilteredResults = new ArrayList<>();
                
                for (Map<String, Object> result : processedResults) {
                    // Create a new map with only the selected fields
                    Map<String, Object> filteredResult = new HashMap<>();
                    
                    // Always include 'id' and 'idShort' fields for identification with their actual values
                    if (result.containsKey("id")) {
                        filteredResult.put("id", result.get("id"));
                    }
                    if (result.containsKey("idShort")) {
                        filteredResult.put("idShort", result.get("idShort"));
                    }
                    
                    // Add only the explicitly selected fields
                    for (String field : selectedFields) {
                        // Skip idShort as we've already handled it
                        if (field.equals("idShort")) {
                            continue;
                        }
                        
                        // Try the exact field name first
                        if (result.containsKey(field)) {
                            log.debug("Adding field '{}' with value: {}", field, result.get(field));
                            filteredResult.put(field, result.get(field));
                            continue;
                        }
                        
                        // If still not found, check if it's a nested property
                        if (field.contains(".")) {
                            // Special handling for deeply nested fields like submodelElements.0.value...
                            if (field.startsWith("submodelElements.") || field.contains(".value.")) {
                                // First try to map the field using the standard MongoDB field mapping
                                String mappedField = org.example.service.query.MongoQueryUtils.mapFieldPath(field);
                                log.info("Mapped deeply nested field: {} -> {}", field, mappedField);
                                
                                // Try to get the value using the mapped field
                                Object nestedValue = getNestedValue(result, mappedField);
                                if (nestedValue != null) {
                                    log.debug("Adding deeply nested field '{}' with value: {}", field, nestedValue);
                                    filteredResult.put(field, nestedValue);
                                    continue;
                                }
                                
                                // If that didn't work, try with the original field
                                nestedValue = getNestedValue(result, field);
                                if (nestedValue != null) {
                                    log.debug("Adding deeply nested field '{}' with value: {}", field, nestedValue);
                                    filteredResult.put(field, nestedValue);
                                    continue;
                                }
                                
                                // If still not found, try with MongoDB dot notation for arrays
                                String normalizedField = field.replaceAll("\\.(\\d+)", "[$1]");
                                nestedValue = result.get(normalizedField);
                                if (nestedValue != null) {
                                    log.debug("Adding deeply nested field '{}' with value: {}", field, nestedValue);
                                    filteredResult.put(field, nestedValue);
                                    continue;
                                }
                            }
                            
                            // For file properties like 'dimensions.length', try to find them
                            // in both regular and file-prefixed versions
                            String[] parts;
                            boolean isAllSuffix = false;
                            
                            // Check if this is a field with _all suffix
                            if (field.endsWith("_all")) {
                                isAllSuffix = true;
                                String fieldWithoutSuffix = field.substring(0, field.length() - 4); // Remove _all suffix
                                parts = fieldWithoutSuffix.split("\\.");
                            } else {
                                parts = field.split("\\.");
                            }
                            
                            if (parts.length == 2) {
                                String category = parts[0];
                                String property = parts[1];
                                
                                // Create base field name without _all suffix
                                String baseField = category + "." + property;
                                
                                // Create variations with and without _all suffix
                                List<String> variations = new ArrayList<>();
                                
                                if (isAllSuffix) {
                                    // If the requested field has _all suffix, prioritize the _all versions
                                    variations.add(baseField + "_all");
                                    variations.add("file." + baseField + "_all");
                                    variations.add(category + "." + property + "_all");
                                    variations.add("file." + category + "." + property + "_all");
                                } else {
                                    // Otherwise, try the regular versions
                                    variations.add(baseField);
                                    variations.add("file." + baseField);
                                    variations.add(category + "." + property);
                                    variations.add("file." + category + "." + property);
                                }
                                
                                String[] fieldVariations = variations.toArray(new String[0]);
                                
                                for (String variation : fieldVariations) {
                                    if (result.containsKey(variation)) {
                                        log.debug("Adding nested field '{}' from '{}' with value: {}", 
                                                field, variation, result.get(variation));
                                        filteredResult.put(field, result.get(variation));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    strictlyFilteredResults.add(filteredResult);
                }
                
                // Replace the original results with the strictly filtered ones
                processedResults = strictlyFilteredResults;
                
                // Debug the filtered result structure
                if (!processedResults.isEmpty()) {
                    Map<String, Object> firstResult = processedResults.get(0);
                    log.info("After strict filtering, result contains {} fields: {}", 
                            firstResult.size(), firstResult.keySet());
                } else {
                    log.warn("After filtering, no results remain");
                }
            }
            
            // Check if file properties were added
            if (!processedResults.isEmpty()) {
                Map<String, Object> firstResult = processedResults.get(0);
                log.info("First result keys after processing: {}", firstResult.keySet());
                
                // Check if specific file properties were added
                for (String field : selectedFields) {
                    if (field.equals("id") || field.equals("idShort")) {
                        continue; // Skip standard fields
                    }
                    
                    // Check for the exact field name
                    if (firstResult.containsKey(field)) {
                        log.info("File property '{}' was successfully added with value: {}", field, firstResult.get(field));
                        continue;
                    }
                    
                    // Check for the field with 'file.' prefix
                    String fieldWithPrefix = "file." + field;
                    if (firstResult.containsKey(fieldWithPrefix)) {
                        log.info("File property '{}' was added as '{}' with value: {}", 
                                field, fieldWithPrefix, firstResult.get(fieldWithPrefix));
                        
                        // Copy the value to the expected field name to ensure it's available as requested
                        firstResult.put(field, firstResult.get(fieldWithPrefix));
                        log.info("Copied value from '{}' to '{}'", fieldWithPrefix, field);
                        continue;
                    }
                    
                    // For nested properties like 'dimensions.length', check if parts exist
                    if (field.contains(".")) {
                        String[] parts = field.split("\\.");
                        if (parts.length == 2) {
                            String category = parts[0];
                            String property = parts[1];
                            
                            // Check various combinations
                            String[] fieldVariations = {
                                field,
                                "file." + field,
                                category + "." + property,
                                "file." + category + "." + property
                            };
                            
                            boolean found = false;
                            for (String variation : fieldVariations) {
                                if (firstResult.containsKey(variation)) {
                                    log.info("Nested file property '{}' was found as '{}' with value: {}", 
                                            field, variation, firstResult.get(variation));
                                    
                                    // Copy the value to the expected field name
                                    firstResult.put(field, firstResult.get(variation));
                                    log.info("Copied value from '{}' to '{}'", variation, field);
                                    found = true;
                                    break;
                                }
                            }
                            
                            if (found) continue;
                        }
                    }
                    
                    log.warn("File property '{}' was NOT found in the result", field);
                }
            }
            
            log.info("Returning {} processed results with file properties", processedResults.size());
            return processedResults;
        } catch (Exception e) {
            log.error("Error executing file query: {}", e.getMessage(), e);
            throw new RuntimeException("Error executing file query: " + e.getMessage(), e);
        }
    }
    
    /**
     * Helper method to get a value from a nested path in a map
     * This handles deeply nested fields like submodelElements.0.value.0.value
     * @param map The map to search in
     * @param path The dot-separated path to the value
     * @return The value at the path, or null if not found
     */
    private Object getNestedValue(Map<String, Object> map, String path) {
        try {
            String[] parts = path.split("\\.");
            Object current = map;
            
            for (String part : parts) {
                if (current == null) {
                    return null;
                }
                
                if (current instanceof Map) {
                    current = ((Map<?, ?>) current).get(part);
                } else if (current instanceof List && part.matches("\\d+")) {
                    // If current is a list and part is a number, get the element at that index
                    int index = Integer.parseInt(part);
                    List<?> list = (List<?>) current;
                    if (index >= 0 && index < list.size()) {
                        current = list.get(index);
                    } else {
                        return null; // Index out of bounds
                    }
                } else {
                    return null; // Can't navigate further
                }
            }
            
            return current;
        } catch (Exception e) {
            log.warn("Error getting nested value for path {}: {}", path, e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse SQL query to MongoDB query document
     * This extracts both the WHERE conditions and the SELECT fields
     */
    private Document parseSqlToMongoQuery(String sqlQuery) {
        try {
            log.info("Parsing SQL query: {}", sqlQuery);
            
            // Create a document to hold both query conditions and selected fields
            Document queryDoc = new Document();
            
            // Extract the SELECT fields
            int selectIndex = sqlQuery.toUpperCase().indexOf("SELECT");
            int fromIndex = sqlQuery.toUpperCase().indexOf("FROM");
            
            if (selectIndex != -1 && fromIndex != -1) {
                String selectClause = sqlQuery.substring(selectIndex + 6, fromIndex).trim();
                String[] fields = selectClause.split(",");
                
                List<String> selectedFields = new ArrayList<>();
                log.info("Raw SELECT clause: {}", selectClause);
                for (String field : fields) {
                    field = field.trim();
                    
                    // Remove quotes if present (handles both double and single quotes)
                    if (field.startsWith("\"") && field.endsWith("\"")) {
                        field = field.substring(1, field.length() - 1);
                        log.info("Removed double quotes from field: {}", field);
                    } else if (field.startsWith("'") && field.endsWith("'")) {
                        field = field.substring(1, field.length() - 1);
                        log.info("Removed single quotes from field: {}", field);
                    }
                    
                    if (!field.equals("*")) {
                        selectedFields.add(field);
                    }
                }
                
                if (!selectedFields.isEmpty()) {
                    // Always make sure idShort is included in the selected fields
                    if (!selectedFields.contains("idShort")) {
                        selectedFields.add("idShort");
                        log.info("Automatically added idShort to selected fields");
                    }
                    queryDoc.put("_select", selectedFields);
                    log.info("Selected fields: {}", selectedFields);
                }
            }
            
            // Extract the WHERE clause
            int whereIndex = sqlQuery.toUpperCase().indexOf("WHERE");
            if (whereIndex == -1) {
                // No WHERE clause, just return the document with selected fields
                return queryDoc;
            }
            
            String whereClause = sqlQuery.substring(whereIndex + 5).trim();
            log.debug("WHERE clause: {}", whereClause);
            
            // Convert the WHERE clause to a MongoDB query
            Document whereDoc = new Document();
            
            // Parse conditions with better handling of AND/OR operators
            // First, check if we have OR conditions
            if (whereClause.toUpperCase().contains(" OR ")) {
                log.info("Found OR conditions in WHERE clause");
                String[] orConditions = whereClause.split("\\s+OR\\s+|\\s+or\\s+");
                List<Document> orDocs = new ArrayList<>();
                
                for (String orCondition : orConditions) {
                    orCondition = orCondition.trim();
                    log.debug("Processing OR condition: {}", orCondition);
                    
                    // Handle AND conditions within each OR block
                    if (orCondition.toUpperCase().contains(" AND ")) {
                        String[] andConditions = orCondition.split("\\s+AND\\s+|\\s+and\\s+");
                        Document andDoc = new Document();
                        
                        for (String andCondition : andConditions) {
                            parseCondition(andCondition.trim(), andDoc);
                        }
                        
                        orDocs.add(andDoc);
                    } else {
                        // Single condition within OR
                        Document condDoc = new Document();
                        parseCondition(orCondition, condDoc);
                        orDocs.add(condDoc);
                    }
                }
                
                // Add the OR conditions to the where document
                whereDoc.put("$or", orDocs);
                log.info("Added $or conditions: {}", orDocs);
            } else {
                // Only AND conditions
                String[] conditions = whereClause.split("\\s+AND\\s+|\\s+and\\s+");
                for (String condition : conditions) {
                    parseCondition(condition.trim(), whereDoc);
                }
            }
            
            // Add the WHERE conditions to the query document
            queryDoc.put("_where", whereDoc);
            log.info("Parsed WHERE conditions: {}", whereDoc.toJson());
            
            return queryDoc;
        } catch (Exception e) {
            log.error("Error parsing SQL query: {}", e.getMessage(), e);
            return null;
        }
    }

/**
 * Parse a single condition and add it to the document
 * @param condition The condition string to parse
 * @param doc The document to add the condition to
 */
private void parseCondition(String condition, Document doc) {
    log.debug("Processing condition: {}", condition);
    
    // Handle parenthesis
    if (condition.startsWith("(") && condition.endsWith(")")) {
        condition = condition.substring(1, condition.length() - 1).trim();
        log.debug("Removed parenthesis: {}", condition);
    }
    
    // Handle equals condition
    if (condition.contains("=")) {
        String[] parts = condition.split("=");
        if (parts.length == 2) {
            String field = parts[0].trim();
            String valueStr = parts[1].trim().replaceAll("'", "").replaceAll("\"", "");
            
            // Check if we already have a condition for this field
            if (doc.containsKey(field)) {
                // If we already have a condition for this field, we need to convert to $and
                Object existingValue = doc.get(field);
                List<Document> andConditions = new ArrayList<>();
                
                // Add the existing condition
                Document existingCondition = new Document();
                existingCondition.put(field, existingValue);
                andConditions.add(existingCondition);
                
                // Add the new condition
                Document newCondition = new Document();
                // Try to parse as number if it looks like one
                if (valueStr.matches("-?\\d+(\\.\\d+)?")) {
                    try {
                        double numValue = Double.parseDouble(valueStr);
                        newCondition.put(field, numValue);
                        log.debug("Added equals condition as number to $and: {} = {}", field, numValue);
                    } catch (NumberFormatException e) {
                        newCondition.put(field, valueStr);
                        log.debug("Added equals condition as string to $and: {} = {}", field, valueStr);
                    }
                } else {
                    newCondition.put(field, valueStr);
                    log.debug("Added equals condition to $and: {} = {}", field, valueStr);
                }
                andConditions.add(newCondition);
                
                // Remove the existing field and add the $and condition
                doc.remove(field);
                
                // If we already have an $and, merge with it
                if (doc.containsKey("$and")) {
                    List<Document> existingAnd = (List<Document>) doc.get("$and");
                    existingAnd.addAll(andConditions);
                } else {
                    doc.put("$and", andConditions);
                }
                log.debug("Converted to $and condition for field: {}", field);
            } else {
                // Try to parse as number if it looks like one
                if (valueStr.matches("-?\\d+(\\.\\d+)?")) {
                    try {
                        double numValue = Double.parseDouble(valueStr);
                        doc.put(field, numValue);
                        log.debug("Added equals condition as number: {} = {}", field, numValue);
                    } catch (NumberFormatException e) {
                        doc.put(field, valueStr);
                        log.debug("Added equals condition as string: {} = {}", field, valueStr);
                    }
                } else {
                    doc.put(field, valueStr);
                    log.debug("Added equals condition: {} = {}", field, valueStr);
                }
            }
        }
    }
    // Handle greater than or equal condition
    else if (condition.contains(">=")) {
        String[] parts = condition.split(">=");
        if (parts.length == 2) {
            String field = parts[0].trim();
            String valueStr = parts[1].trim().replaceAll("'", "").replaceAll("\"", "");
            
            // Try to parse as number
            try {
                double value = Double.parseDouble(valueStr);
                doc.put(field, new Document("$gte", value));
                log.debug("Added greater than or equal condition: {} >= {}", field, value);
            } catch (NumberFormatException e) {
                // Use as string if not a number
                doc.put(field, new Document("$gte", valueStr));
                log.debug("Added greater than or equal condition: {} >= {}", field, valueStr);
            }
        }
    }
    // Handle greater than condition
    else if (condition.contains(">")) {
        String[] parts = condition.split(">");
        if (parts.length == 2) {
            String field = parts[0].trim();
            String valueStr = parts[1].trim().replaceAll("'", "").replaceAll("\"", "");
            
            // Try to parse as number
            try {
                double value = Double.parseDouble(valueStr);
                doc.put(field, new Document("$gt", value));
                log.debug("Added greater than condition: {} > {}", field, value);
            } catch (NumberFormatException e) {
                // Use as string if not a number
                doc.put(field, new Document("$gt", valueStr));
                log.debug("Added greater than condition: {} > {}", field, valueStr);
            }
        }
    }
    // Handle less than or equal condition
    else if (condition.contains("<=")) {
        String[] parts = condition.split("<=");
        if (parts.length == 2) {
            String field = parts[0].trim();
            String valueStr = parts[1].trim().replaceAll("'", "").replaceAll("\"", "");
            
            // Try to parse as number
            try {
                double value = Double.parseDouble(valueStr);
                doc.put(field, new Document("$lte", value));
                log.debug("Added less than or equal condition: {} <= {}", field, value);
            } catch (NumberFormatException e) {
                // Use as string if not a number
                doc.put(field, new Document("$lte", valueStr));
                log.debug("Added less than or equal condition: {} <= {}", field, valueStr);
            }
        }
    }
    // Handle less than condition
    else if (condition.contains("<")) {
        String[] parts = condition.split("<");
        if (parts.length == 2) {
            String field = parts[0].trim();
            String valueStr = parts[1].trim().replaceAll("'", "").replaceAll("\"", "");
            
            // Try to parse as number
            try {
                double value = Double.parseDouble(valueStr);
                doc.put(field, new Document("$lt", value));
                log.debug("Added less than condition: {} < {}", field, value);
            } catch (NumberFormatException e) {
                // Use as string if not a number
                doc.put(field, new Document("$lt", valueStr));
                log.debug("Added less than condition: {} < {}", field, valueStr);
            }
        }
    }
    // Default case for unrecognized conditions
    else {
        log.warn("Unrecognized condition format: {}", condition);
    }
} // Added this closing brace

/**
 * Convert a MongoDB query document back to SQL
 * This is needed for the parent executeQuery method
 * @param mongoQuery The MongoDB query document
 * @param originalSql The original SQL query (used as fallback)
 * @return The SQL query string
 */
private String convertMongoQueryToSql(Document mongoQuery, String originalSql) {
    try {
        log.info("Converting MongoDB query to SQL: {}", mongoQuery.toJson());
        
        // Extract the SELECT and FROM parts from the original SQL
        String selectPart = "SELECT *";
        String fromPart = "FROM submodels";
        
        int selectIndex = originalSql.toUpperCase().indexOf("SELECT");
        int fromIndex = originalSql.toUpperCase().indexOf("FROM");
        int whereIndex = originalSql.toUpperCase().indexOf("WHERE");
        
        if (selectIndex != -1 && fromIndex != -1) {
            selectPart = originalSql.substring(selectIndex, fromIndex).trim();
            
            if (whereIndex != -1) {
                fromPart = originalSql.substring(fromIndex, whereIndex).trim();
            } else {
                fromPart = originalSql.substring(fromIndex).trim();
            }
        }
        
        log.debug("Extracted SELECT part: {}", selectPart);
        log.debug("Extracted FROM part: {}", fromPart);
        
        // Build the WHERE clause from the MongoDB query
        Document whereDoc = (Document) mongoQuery.get("_where");
        if (whereDoc == null || whereDoc.isEmpty()) {
            // No WHERE conditions, just return SELECT + FROM
            return selectPart + " " + fromPart;
        }
        
        StringBuilder whereSql = new StringBuilder("WHERE ");
        boolean first = true;
        
        // Handle $or operator
        if (whereDoc.containsKey("$or")) {
            List<Document> orDocs = (List<Document>) whereDoc.get("$or");
            first = true;
            
            whereSql.append("(");
            for (Document orDoc : orDocs) {
                if (!first) {
                    whereSql.append(" OR ");
                }
                
                // Handle AND conditions within OR
                boolean hasMultipleConditions = orDoc.size() > 1;
                if (hasMultipleConditions) {
                    whereSql.append("(");
                }
                
                boolean firstAnd = true;
                for (String field : orDoc.keySet()) {
                    if (!firstAnd) {
                        whereSql.append(" AND ");
                    }
                    
                    appendCondition(whereSql, field, orDoc.get(field));
                    firstAnd = false;
                }
                
                if (hasMultipleConditions) {
                    whereSql.append(")");
                }
                
                first = false;
            }
            whereSql.append(")");
        } else {
            // Handle regular AND conditions
            for (String field : whereDoc.keySet()) {
                if (!first) {
                    whereSql.append(" AND ");
                }
                
                appendCondition(whereSql, field, whereDoc.get(field));
                first = false;
            }
        }
        
        // Combine all parts
        String result = selectPart + " " + fromPart + " " + whereSql.toString();
        log.debug("Converted SQL query: {}", result);
        return result;
        
    } catch (Exception e) {
        log.error("Error converting MongoDB query to SQL: {}", e.getMessage(), e);
        return originalSql; // Fall back to original SQL on error
    }
}

/**
 * Helper method to append a condition to the SQL WHERE clause
 * @param sql The StringBuilder to append to
 * @param field The field name
 * @param value The field value (can be a Document for operators)
 */
private void appendCondition(StringBuilder sql, String field, Object value) {
    // For file property fields, we need to handle them specially
    // Use a simple field name without dots for SQL compatibility
    String formattedField = field;
    
    // Handle field names with dots or special MongoDB operators
    if (field.contains(".") || field.contains("$")) {
        // For file properties or fields with special characters, use a simple placeholder in SQL
        // The actual filtering will be done in the FileQueryHelper
        formattedField = "idShort";
        log.debug("Using idShort as placeholder for field with special chars: {}", field);
    }
    
    if (value instanceof Document) {
        // Handle operators like $gt, $lt, etc.
        Document opDoc = (Document) value;
        for (String op : opDoc.keySet()) {
            Object opValue = opDoc.get(op);
            
            // Map MongoDB operators to SQL operators
            switch (op) {
                case "$gt":
                    sql.append(formattedField).append(" > ");
                    break;
                case "$gte":
                    sql.append(formattedField).append(" >= ");
                    break;
                case "$lt":
                    sql.append(formattedField).append(" < ");
                    break;
                case "$lte":
                    sql.append(formattedField).append(" <= ");
                    break;
                case "$eq":
                    sql.append(formattedField).append(" = ");
                    break;
                case "$ne":
                    sql.append(formattedField).append(" != ");
                    break;
                default:
                    // Default to equality for unknown operators
                    log.warn("Unknown MongoDB operator: {}, defaulting to equality", op);
                    sql.append(formattedField).append(" = ");
            }
            
            // Format the value based on its type
            appendValueToSql(sql, opValue);
        }
    } else {
        // Simple equality condition
        sql.append(formattedField).append(" = ");
        appendValueToSql(sql, value);
    }
}

/**
 * Helper method to append a value to an SQL statement with proper formatting
 * @param sql The StringBuilder to append to
 * @param value The value to append
 */
private void appendValueToSql(StringBuilder sql, Object value) {
    if (value == null) {
        sql.append("NULL");
    } else if (value instanceof String) {
        // Escape single quotes in string values (SQL standard)
        String escapedValue = ((String) value).replace("'", "''");
        sql.append("'").append(escapedValue).append("'");
    } else if (value instanceof Number || value instanceof Boolean) {
        // Numbers and booleans can be appended directly
        sql.append(value);
    } else {
        // For other types, convert to string and quote
        sql.append("'").append(value.toString().replace("'", "''")).append("'");
    }
}
}