package org.example.service.query.queryFile;

import org.bson.Document;
import org.example.database.fileParser.QueryablePropertyRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Integration between the AASQueryService and the file property query system
 */
@Component
public class FileQueryIntegration {
    private static final Logger log = LoggerFactory.getLogger(FileQueryIntegration.class);
    
    private final QueryablePropertyRegistry propertyRegistry;
    private final FileQueryHelper fileQueryHelper;
    
    @Autowired
    public FileQueryIntegration(
            QueryablePropertyRegistry propertyRegistry,
            FileQueryHelper fileQueryHelper) {
        this.propertyRegistry = propertyRegistry;
        this.fileQueryHelper = fileQueryHelper;
    }
    
    /**
     * Process a MongoDB query to handle file property queries
     * @param query The original MongoDB query
     * @return Modified query that will find documents that might contain matching files
     */
    public Document processQuery(Document query) {
        if (query == null) {
            return null;
        }
        
        // Validate that property registry has been initialized
        if (propertyRegistry.getAllPropertyPaths().isEmpty()) {
            log.warn("Property registry is empty, no file properties will be processed");
            return query;
        }
        
        // Extract file property queries
        Map<String, Object> fileQueries = fileQueryHelper.extractFileQueries(query);
        if (fileQueries.isEmpty()) {
            return query;
        }
        
        log.info("Found file property queries: {}", fileQueries);
        
        // Extract idShort condition from the original query to ensure it's preserved
        Document idShortCondition = extractIdShortCondition(query);
        log.info("Extracted idShort condition: {}", idShortCondition);
        
        // Create a query to find submodels with files of the right types
        Document fileSubmodelQuery = fileQueryHelper.createFileSubmodelQuery(fileQueries);
        if (fileSubmodelQuery == null) {
            return query;
        }
        
        // Remove file property queries from the original query
        Document cleanedQuery = removeFileQueries(query, fileQueries.keySet());
        
        // Combine the cleaned query with the file submodel query
        if (cleanedQuery.isEmpty() && idShortCondition.isEmpty()) {
            return fileSubmodelQuery;
        } else if (cleanedQuery.isEmpty()) {
            // If we only have idShort condition but no other regular conditions
            return new Document("$and", List.of(idShortCondition, fileSubmodelQuery));
        } else if (idShortCondition.isEmpty()) {
            // If we have regular conditions but no idShort condition
            return new Document("$and", List.of(cleanedQuery, fileSubmodelQuery));
        } else {
            // If we have both regular conditions and idShort condition
            // Make sure idShort condition is applied to both parts
            return new Document("$and", List.of(cleanedQuery, fileSubmodelQuery, idShortCondition));
        }
    }
    
    /**
     * Extract idShort condition from the query to ensure it's preserved in file property filtering
     * @param query The original query
     * @return Document containing only the idShort condition, or empty document if none found
     */
    private Document extractIdShortCondition(Document query) {
        Document idShortCondition = new Document();
        
        // Check if there's a direct idShort condition
        if (query.containsKey("idShort")) {
            idShortCondition.put("idShort", query.get("idShort"));
            return idShortCondition;
        }
        
        // Check in _where field (common in SQL-to-MongoDB parsed queries)
        if (query.containsKey("_where")) {
            Document whereClause = (Document) query.get("_where");
            if (whereClause.containsKey("idShort")) {
                idShortCondition.put("idShort", whereClause.get("idShort"));
                return idShortCondition;
            }
        }
        
        // Check in logical operators
        if (query.containsKey("$and")) {
            List<Document> andConditions = (List<Document>) query.get("$and");
            for (Document condition : andConditions) {
                if (condition.containsKey("idShort")) {
                    idShortCondition.put("idShort", condition.get("idShort"));
                    return idShortCondition;
                }
                
                // Also check in _where field of nested conditions
                if (condition.containsKey("_where")) {
                    Document whereClause = (Document) condition.get("_where");
                    if (whereClause.containsKey("idShort")) {
                        idShortCondition.put("idShort", whereClause.get("idShort"));
                        return idShortCondition;
                    }
                }
            }
        }
        
        return idShortCondition;
    }
    
    /**
     * Remove file property queries from a MongoDB query
     * @param query The original query
     * @param filePropertyPaths Set of file property paths to remove
     * @return Query with file property queries removed
     */
    private Document removeFileQueries(Document query, Set<String> filePropertyPaths) {
        Document result = new Document();
        
        for (String key : query.keySet()) {
            boolean isFileProperty = false;
            for (String propertyPath : filePropertyPaths) {
                if (key.equals(propertyPath) || key.startsWith(propertyPath + ".")) {
                    isFileProperty = true;
                    break;
                }
            }
            
            if (!isFileProperty) {
                if (key.equals("$and") || key.equals("$or")) {
                    // Handle logical operators
                    List<Document> conditions = (List<Document>) query.get(key);
                    List<Document> cleanedConditions = new ArrayList<>();
                    
                    for (Document condition : conditions) {
                        Document cleanedCondition = removeFileQueries(condition, filePropertyPaths);
                        if (!cleanedCondition.isEmpty()) {
                            cleanedConditions.add(cleanedCondition);
                        }
                    }
                    
                    if (!cleanedConditions.isEmpty()) {
                        result.put(key, cleanedConditions);
                    }
                } else {
                    // Keep non-file property conditions
                    result.put(key, query.get(key));
                }
            }
        }
        
        return result;
    }
    
    /**
     * Post-process query results to filter based on file properties and enrich with file property values
     * @param results The initial query results
     * @param originalQuery The original query document
     * @param selectedFields The fields selected in the query (optional)
     * @return Processed results with file properties included
     */
    public List<Map<String, Object>> postProcessResults(
            List<Map<String, Object>> results, 
            Document originalQuery,
            List<String> selectedFields) {
        
        // Log the selected fields we received
        if (selectedFields != null && !selectedFields.isEmpty()) {
            log.info("FileQueryIntegration received selected fields: {}", selectedFields);
        } else {
            log.warn("FileQueryIntegration received empty or null selected fields");
        }
        
        // Extract file property queries
        Map<String, Object> fileQueries = fileQueryHelper.extractFileQueries(originalQuery);
        
        // Check if we need to add dimensions.length or partName to the selected fields
        if (selectedFields != null) {
            // Make a copy of the selected fields to avoid modifying the original list
            List<String> enhancedSelectedFields = new ArrayList<>(selectedFields);
            
            // Check if any of the selected fields are file properties
            boolean hasFileProperties = false;
            for (String field : selectedFields) {
                if (field.contains("dimensions.") || field.equals("partName") || 
                    field.startsWith("file.") || propertyRegistry.getPropertyByPath(field) != null) {
                    hasFileProperties = true;
                    log.info("Found file property in selected fields: {}", field);
                }
            }
            
            // If we have file properties in the selected fields, make sure we pass them to the FileQueryHelper
            if (hasFileProperties) {
                log.info("Passing enhanced selected fields to FileQueryHelper: {}", enhancedSelectedFields);
                return fileQueryHelper.filterResultsByFileProperties(results, fileQueries, enhancedSelectedFields);
            }
        }
        
        // Always process results to include file properties, even if there are no file queries
        // This ensures file properties are included in SELECT results
        return fileQueryHelper.filterResultsByFileProperties(results, fileQueries, selectedFields);
    }
    
    /**
     * Post-process query results to filter based on file properties and enrich with file property values
     * @param results The initial query results
     * @param originalQuery The original query document
     * @return Processed results with file properties included
     */
    public List<Map<String, Object>> postProcessResults(
            List<Map<String, Object>> results, 
            Document originalQuery) {
        // Call the overloaded method with null selectedFields to include all properties
        return postProcessResults(results, originalQuery, null);
    }
}
