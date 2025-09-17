package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
// We use the fully qualified name with generic type parameter in the code
import org.example.database.model.ConceptDescription;
import org.example.database.model.MongoAssetAdministrationShell;
import org.example.database.model.SubmodelData;
import org.example.service.query.EntityMapConverter;
import org.example.service.query.MongoQueryUtils;
import org.example.service.query.SqlToMongoConverter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jsqlparser.statement.delete.Delete;

/**
 * Service for parsing SQL queries and converting them to MongoDB queries for AAS entities
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AASQueryService {

    private final MongoTemplate mongoTemplate;
    
    // Collection mapping
    private static final Map<String, String> COLLECTION_MAPPING = Map.of(
        "asset_administration_shells", "asset_administration_shells",
        "aas", "asset_administration_shells",
        "submodels", "submodels",
        "submodel", "submodels",
        "concept_descriptions", "concept_descriptions",
        "conceptdescriptions", "concept_descriptions",
        "cd", "concept_descriptions"
    );
    
    // Class mapping
    private static final Map<String, Class<?>> CLASS_MAPPING = Map.of(
        "asset_administration_shells", MongoAssetAdministrationShell.class,
        "submodels", SubmodelData.class,
        "concept_descriptions", ConceptDescription.class
    );
    
    /**
     * Execute a SQL query against MongoDB collections
     * The collection to query is determined from the SQL statement (FROM clause)
     * This method handles SELECT queries and delegates UPDATE and DELETE queries to specialized methods
     * @param sqlQuery The SQL query to execute
     * @return List of objects as maps with only the requested fields
     */
    /**
     * Helper method to handle array indices in field paths for MongoDB queries
     * This is needed because MongoDB requires special handling for querying elements at specific array positions
     * 
     * @param fieldPath The field path that may contain array indices
     * @param value The value to query for
     * @return A MongoDB query document that properly handles array indices
     */
    private org.bson.Document createArrayIndexQuery(String fieldPath, Object value) {
        log.info("Creating array index query for field: {} with value: {}", fieldPath, value);
        
        // Use the generic field path mapping method for consistent handling of nested fields
        String mappedFieldPath = MongoQueryUtils.mapFieldPath(fieldPath);
        
        // Parse the mapped field path to extract array field, index, and element path
        String[] parts = mappedFieldPath.split("\\.");
        
        // Check if this is actually an array path with an index
        boolean hasArrayIndex = false;
        for (String part : parts) {
            if (part.matches("\\d+")) {
                hasArrayIndex = true;
                break;
            }
        }
        
        if (!hasArrayIndex || parts.length < 3) {
            // Not an array index path or too short to be meaningful
            return new org.bson.Document(fieldPath, value);
        }
        
        // Extract the array field, index, and element path
        StringBuilder arrayPath = new StringBuilder();
        int arrayIndex = -1;
        StringBuilder elementPath = new StringBuilder();
        boolean foundIndex = false;
        
        for (int i = 0; i < parts.length; i++) {
            if (!foundIndex && parts[i].matches("\\d+")) {
                // Found the array index
                arrayIndex = Integer.parseInt(parts[i]);
                foundIndex = true;
            } else if (!foundIndex) {
                // Still building the path to the array
                if (arrayPath.length() > 0) {
                    arrayPath.append(".");
                }
                arrayPath.append(parts[i]);
            } else {
                // Building the path within the array element
                if (elementPath.length() > 0) {
                    elementPath.append(".");
                }
                elementPath.append(parts[i]);
            }
        }
        
        String arrayFieldPath = arrayPath.toString();
        String elementFieldPath = elementPath.toString();
        
        log.info("Parsed array query - Array: {}, Index: {}, Element: {}", 
                arrayFieldPath, arrayIndex, elementFieldPath);
        
        // Create a query that uses the $elemMatch operator if we have an element path
        org.bson.Document query = new org.bson.Document();
        
        if (elementFieldPath.isEmpty()) {
            // Direct array element access
            query.put(arrayFieldPath + "." + arrayIndex, value);
        } else {
            // Need to match a specific field within an array element
            // Two approaches:
            
            // 1. Using positional operator directly (works for simple cases)
            query.put(arrayFieldPath + "." + arrayIndex + "." + elementFieldPath, value);
            
            // 2. Using $elemMatch (more powerful but more complex)
            // This is an alternative approach that might work better in some cases
            /*
            org.bson.Document elemMatchQuery = new org.bson.Document();
            elemMatchQuery.put(elementFieldPath, value);
            query.put(arrayFieldPath, new org.bson.Document("$elemMatch", elemMatchQuery));
            */
        }
        
        log.info("Created array index query: {}", query.toJson());
        return query;
    }
    
    public List<Map<String, Object>> executeQuery(String sqlQuery) {
        try {
            log.info("Executing SQL query: {}", sqlQuery);
            
            // Clean up the SQL query - remove any markdown formatting that might be present
            sqlQuery = cleanSqlQuery(sqlQuery);
            log.debug("Cleaned SQL query: {}", sqlQuery);
            
            // This can throw net.sf.jsqlparser.parser.ParseException
            Statement statement = CCJSqlParserUtil.parse(sqlQuery);
            
            if (statement instanceof Select) {
                Select select = (Select) statement;
                
                // Get the select body as PlainSelect
                if (!(select.getSelectBody() instanceof PlainSelect)) {
                    throw new IllegalArgumentException("Only simple SELECT statements are supported.");
                }
                
                PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
                
                // Extract table name from FROM clause
                if (plainSelect.getFromItem() == null) {
                    throw new IllegalArgumentException("FROM clause is required in the SQL query.");
                }
                
                if (!(plainSelect.getFromItem() instanceof Table)) {
                    throw new IllegalArgumentException("Only simple table references are supported in FROM clause.");
                }
                
                Table table = (Table) plainSelect.getFromItem();
                String tableName = table.getName().toLowerCase();
                
                // Map table name to MongoDB collection
                if (!COLLECTION_MAPPING.containsKey(tableName)) {
                    throw new IllegalArgumentException("Unknown table name: " + tableName);
                }
                
                String collectionName = COLLECTION_MAPPING.get(tableName);
                Class<?> entityClass = CLASS_MAPPING.get(collectionName);
                
                log.debug("Mapped table '{}' to collection '{}' with entity class '{}'", 
                          tableName, collectionName, entityClass.getSimpleName());
                
                // Extract selected fields
                List<String> selectedFields = new ArrayList<>();
                boolean selectAll = false;
                
                for (net.sf.jsqlparser.statement.select.SelectItem<?> item : plainSelect.getSelectItems()) {
                    String itemString = item.toString();
                    if (itemString.equals("*")) {
                        selectAll = true;
                        break;
                    } else {
                        // Handle quoted column names - JSQLParser keeps the quotes in the toString() output
                        if (itemString.startsWith("\"") && itemString.endsWith("\"")) {
                            itemString = itemString.substring(1, itemString.length() - 1);
                        }
                        
                        // Handle nested fields by normalizing the path
                        if (itemString.contains(".")) {
                            itemString = MongoQueryUtils.normalizeIndexedPath(itemString);
                        }
                        
                        log.debug("Processing SELECT field: {}", itemString);
                        selectedFields.add(itemString);
                    }
                }
                
                log.debug("Selected fields: {}, selectAll: {}", selectedFields, selectAll);
                
                // Build MongoDB query
                Query query = new Query();
                
                // Add WHERE clause if present
                if (plainSelect.getWhere() != null) {
                    // Use raw MongoDB query for better handling of nested fields
                    org.bson.Document whereQuery = buildRawMongoQuery(plainSelect.getWhere(), entityClass);
                    if (whereQuery != null && !whereQuery.isEmpty()) {
                        // Execute the query directly using the raw MongoDB query document
                        log.debug("Executing raw MongoDB query: {}", whereQuery);
                        log.info("Raw MongoDB Criteria: {}", whereQuery != null ? whereQuery.toJson() : "null");
                        log.info("Selected fields: {} (selectAll={})", selectedFields, selectAll);
                        log.info("MongoDB Entity Class: {}", entityClass.getSimpleName());
                        log.info("MongoDB Collection: {}", collectionName);
                        
                        // Create the MongoDB query
                        org.springframework.data.mongodb.core.query.Query mongoQuery = new org.springframework.data.mongodb.core.query.Query(
                            org.springframework.data.mongodb.core.query.Criteria.where("_id").exists(true)
                        ).addCriteria(
                            new org.springframework.data.mongodb.core.query.CriteriaDefinition() {
                                @Override
                                public org.bson.Document getCriteriaObject() {
                                    return whereQuery;
                                }
                                
                                @Override
                                public String getKey() {
                                    return "";
                                }
                            }
                        );
                        
                        // Log the exact MongoDB query being executed
                        log.info("Executing MongoDB query: {} in collection: {}", mongoQuery.getQueryObject().toJson(), collectionName);
                        
                        // Get a sample document to understand the structure
                        if (plainSelect.getWhere() != null) {
                            try {
                                // Try to get a sample document first to understand the structure
                                org.bson.Document sampleDoc = mongoTemplate.getCollection(collectionName).find().first();
                                if (sampleDoc != null) {
                                    // Check if the field path exists in the sample document
                                    String fieldPath = "";
                                    if (plainSelect.getWhere() instanceof net.sf.jsqlparser.expression.operators.relational.EqualsTo) {
                                        net.sf.jsqlparser.expression.operators.relational.EqualsTo equals = 
                                            (net.sf.jsqlparser.expression.operators.relational.EqualsTo) plainSelect.getWhere();
                                        net.sf.jsqlparser.schema.Column column = (net.sf.jsqlparser.schema.Column) equals.getLeftExpression();
                                        fieldPath = column.getColumnName();
                                        if (fieldPath.startsWith("\"") && fieldPath.endsWith("\"")) {
                                            fieldPath = fieldPath.substring(1, fieldPath.length() - 1);
                                        }
                                        
                                        // Log the document structure for debugging
                                        log.info("Sample document structure: {}", sampleDoc.toJson());
                                        log.info("Looking for field path: {} in document", fieldPath);
                                        
                                        // Try to access the field to see if it exists
                                        try {
                                            Object fieldValue = getNestedFieldValue(sampleDoc, fieldPath);
                                            log.info("Field '{}' exists in sample document with value: {}", fieldPath, fieldValue);
                                        } catch (Exception e) {
                                            log.warn("Field '{}' does not exist in sample document: {}", fieldPath, e.getMessage());
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.warn("Could not get sample document: {}", e.getMessage());
                            }
                        }
                        
                        // Execute the query
                        List<?> results = mongoTemplate.find(mongoQuery, entityClass, collectionName);
                        
                        // Provide helpful feedback if no results are found
                        if (results.isEmpty()) {
                            log.info("Query returned no results. Possible reasons:");
                            log.info("1. No documents match the criteria");
                            log.info("2. Field names might be incorrect (check case sensitivity)");
                            log.info("3. For array fields like 'submodelElements', try using quotes and array indices: \"submodelElements.0.modelType\"");
                            log.info("4. Values might not match exactly (MongoDB is case-sensitive)");
                            log.info("5. Try a simpler query like 'SELECT * FROM {} LIMIT 1' to see the data structure", collectionName);
                        }
                        
                        log.debug("Raw query returned {} results", results.size());
                        
                        // Convert results to maps with only the selected fields
                        List<Map<String, Object>> resultMaps = new ArrayList<>();
                        for (Object result : results) {
                            Map<String, Object> resultMap = EntityMapConverter.convertEntityToMap(result, selectedFields, selectAll);
                            resultMaps.add(resultMap);
                        }
                        
                        return resultMaps;
                    } else {
                        // Fallback to the standard approach if raw query building fails
                        preprocessWhereClause(plainSelect.getWhere());
                        Criteria criteria = SqlToMongoConverter.buildMongoCriteria(plainSelect.getWhere());
                        query.addCriteria(criteria);
                        log.debug("Added WHERE criteria using standard approach: {}", criteria);
                        log.info("Standard MongoDB Query: {}", query.getQueryObject());
                        log.info("Selected fields: {} (selectAll={})", selectedFields, selectAll);
                        log.info("MongoDB Entity Class: {}", entityClass.getSimpleName());
                        log.info("MongoDB Collection: {}", collectionName);
                    }
                }
                
                // Execute query
                List<?> results = mongoTemplate.find(query, entityClass, collectionName);
                log.debug("Query returned {} results", results.size());
                
                // Convert results to maps with only the selected fields
                List<Map<String, Object>> resultMaps = new ArrayList<>();
                for (Object result : results) {
                    Map<String, Object> resultMap = EntityMapConverter.convertEntityToMap(result, selectedFields, selectAll);
                    resultMaps.add(resultMap);
                }
                
                return resultMaps;
            } else {
                throw new IllegalArgumentException("Only SELECT statements are supported.");
            }
        } catch (Exception e) {
            log.error("Failed to execute SQL query: {}", sqlQuery, e);
            throw new RuntimeException("Failed to execute SQL query: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build a raw MongoDB query document from a SQL WHERE clause
     * This provides better handling for deeply nested fields
     * @param expression The WHERE clause expression
     * @param entityClass The entity class for the query
     * @return MongoDB query document or null if building fails
     */
    private org.bson.Document buildRawMongoQuery(net.sf.jsqlparser.expression.Expression expression, Class<?> entityClass) {
        try {
            log.debug("Building raw MongoDB query for expression: {}", expression);
            log.info("Expression class: {}", expression.getClass().getSimpleName());
            org.bson.Document query = new org.bson.Document();
            
            if (expression instanceof net.sf.jsqlparser.expression.operators.conditional.AndExpression) {
                net.sf.jsqlparser.expression.operators.conditional.AndExpression and = 
                    (net.sf.jsqlparser.expression.operators.conditional.AndExpression) expression;
                
                org.bson.Document leftQuery = buildRawMongoQuery(and.getLeftExpression(), entityClass);
                org.bson.Document rightQuery = buildRawMongoQuery(and.getRightExpression(), entityClass);
                
                if (leftQuery != null && rightQuery != null) {
                    List<org.bson.Document> andClauses = new ArrayList<>();
                    andClauses.add(leftQuery);
                    andClauses.add(rightQuery);
                    query.put("$and", andClauses);
                    return query;
                }
                
            } else if (expression instanceof net.sf.jsqlparser.expression.operators.conditional.OrExpression) {
                net.sf.jsqlparser.expression.operators.conditional.OrExpression or = 
                    (net.sf.jsqlparser.expression.operators.conditional.OrExpression) expression;
                
                org.bson.Document leftQuery = buildRawMongoQuery(or.getLeftExpression(), entityClass);
                org.bson.Document rightQuery = buildRawMongoQuery(or.getRightExpression(), entityClass);
                
                if (leftQuery != null && rightQuery != null) {
                    List<org.bson.Document> orClauses = new ArrayList<>();
                    orClauses.add(leftQuery);
                    orClauses.add(rightQuery);
                    query.put("$or", orClauses);
                    return query;
                }
                
            } else if (expression instanceof net.sf.jsqlparser.expression.operators.relational.EqualsTo) {
                return buildEqualsCondition((net.sf.jsqlparser.expression.operators.relational.EqualsTo) expression, entityClass);
                
            } else if (expression instanceof net.sf.jsqlparser.expression.operators.relational.GreaterThan) {
                return buildGreaterThanCondition((net.sf.jsqlparser.expression.operators.relational.GreaterThan) expression, entityClass);
                
            } else if (expression instanceof net.sf.jsqlparser.expression.operators.relational.LikeExpression) {
                return buildLikeCondition((net.sf.jsqlparser.expression.operators.relational.LikeExpression) expression, entityClass);
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error building raw MongoDB query: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Build equals condition for MongoDB query
     * @param equals The EqualsTo expression
     * @param entityClass The entity class for the query
     * @return MongoDB query document
     */
    
    private org.bson.Document buildEqualsCondition(net.sf.jsqlparser.expression.operators.relational.EqualsTo equals, Class<?> entityClass) {
        try {
            net.sf.jsqlparser.schema.Column column = (net.sf.jsqlparser.schema.Column) equals.getLeftExpression();
            log.info("Raw column name from SQL parser: {}", column.getColumnName());
            String fieldPath = column.getColumnName();
            
            // Handle quoted column names FIRST
            if (fieldPath.startsWith("\"") && fieldPath.endsWith("\"")) {
                fieldPath = fieldPath.substring(1, fieldPath.length() - 1);
                log.info("Removed quotes: {}", fieldPath);
            }
            
            // Check for deeply nested submodel fields that need special handling
            if (fieldPath.contains("submodelElements") && 
                (fieldPath.contains("supplementalSemanticIds") || 
                 fieldPath.contains("qualifiers") || 
                 fieldPath.contains("semanticId") || 
                 fieldPath.contains("keys") || 
                 fieldPath.contains("idShort"))) {
                
                log.info("Using enhanced field mapping for deeply nested submodel field: {}", fieldPath);
                // Use the enhanced processFieldPath method for consistent field mapping
                String enhancedPath = processFieldPath(fieldPath, entityClass);
                log.info("Enhanced field mapping: {} -> {}", fieldPath, enhancedPath);
                
                // Extract the value
                Object value;
                if (equals.getRightExpression() instanceof net.sf.jsqlparser.expression.StringValue) {
                    value = ((net.sf.jsqlparser.expression.StringValue) equals.getRightExpression()).getValue();
                } else if (equals.getRightExpression() instanceof net.sf.jsqlparser.expression.LongValue) {
                    value = ((net.sf.jsqlparser.expression.LongValue) equals.getRightExpression()).getValue();
                } else if (equals.getRightExpression() instanceof net.sf.jsqlparser.expression.DoubleValue) {
                    value = ((net.sf.jsqlparser.expression.DoubleValue) equals.getRightExpression()).getValue();
                } else {
                    value = equals.getRightExpression().toString();
                }
                
                // Create a query with the enhanced field mapping
                return new org.bson.Document(enhancedPath, value);
            }
            
            // Only apply context if the field doesn't already have a path structure
            if (!fieldPath.contains(".")) {
                fieldPath = applyFieldContext(fieldPath, entityClass);
            } else {
                log.info("Using explicit path: {}", fieldPath);
            }
    
            // Normalize and map the field path
            String normalizedPath = MongoQueryUtils.normalizeIndexedPath(fieldPath);
            String[] parts = normalizedPath.split("\\.");
            for (int i = 0; i < parts.length; i++) {
                parts[i] = MongoQueryUtils.mapSubmodelFieldName(parts[i]);
            }
            String mappedPath = String.join(".", parts);
    
            // Extract the value
            Object value;
            if (equals.getRightExpression() instanceof net.sf.jsqlparser.expression.StringValue) {
                value = ((net.sf.jsqlparser.expression.StringValue) equals.getRightExpression()).getValue();
            } else if (equals.getRightExpression() instanceof net.sf.jsqlparser.expression.LongValue) {
                value = ((net.sf.jsqlparser.expression.LongValue) equals.getRightExpression()).getValue();
            } else if (equals.getRightExpression() instanceof net.sf.jsqlparser.expression.DoubleValue) {
                value = ((net.sf.jsqlparser.expression.DoubleValue) equals.getRightExpression()).getValue();
            } else {
                value = equals.getRightExpression().toString();
            }
    
            log.debug("Building equals condition for field: {} (mapped: {}) with value: {}", fieldPath, mappedPath, value);
    
            // Check if this is an array path with numeric indices
            boolean hasArrayIndex = false;
            for (String part : parts) {
                if (part.matches("\\d+")) {
                    hasArrayIndex = true;
                    break;
                }
            }
    
            // For deeply nested fields in concept descriptions, ensure exact matching
            if (mappedPath.startsWith("embedded_data_specifications.") && 
                (mappedPath.contains(".preferred_name.") || mappedPath.contains(".definition."))) {
    
                log.debug("Using exact match for deeply nested field: {}", mappedPath);
    
                // Create a query that ensures exact matching
                org.bson.Document query = new org.bson.Document();
                query.put(mappedPath, new org.bson.Document("$eq", value));
                return query;
            }
            
            // For array paths with numeric indices, use our special array index query handler
            if (hasArrayIndex) {
                log.info("Using special array index query handling for field: {}", mappedPath);
                return createArrayIndexQuery(mappedPath, value);
            }
    
            // For other fields, use standard equality
            return new org.bson.Document(mappedPath, value);
        } catch (Exception e) {
            log.error("Error building equals condition: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Build greater than condition for MongoDB query
     * @param gt The GreaterThan expression
     * @param entityClass The entity class for the query
     * @return MongoDB query document
     */
    private org.bson.Document buildGreaterThanCondition(net.sf.jsqlparser.expression.operators.relational.GreaterThan gt, Class<?> entityClass) {
        try {
            net.sf.jsqlparser.schema.Column column = (net.sf.jsqlparser.schema.Column) gt.getLeftExpression();
            String fieldPath = column.getColumnName();
            
            // Handle quoted column names FIRST
            if (fieldPath.startsWith("\"") && fieldPath.endsWith("\"")) {
                fieldPath = fieldPath.substring(1, fieldPath.length() - 1);
                log.info("Removed quotes: {}", fieldPath);
            }
            
            // Check for deeply nested submodel fields that need special handling
            String mappedPath;
            if (fieldPath.contains("submodelElements") && 
                (fieldPath.contains("supplementalSemanticIds") || 
                 fieldPath.contains("qualifiers") || 
                 fieldPath.contains("semanticId") || 
                 fieldPath.contains("keys") || 
                 fieldPath.contains("idShort"))) {
                
                log.info("Using enhanced field mapping for deeply nested submodel field in GT condition: {}", fieldPath);
                // Use the enhanced processFieldPath method for consistent field mapping
                mappedPath = processFieldPath(fieldPath, entityClass);
                log.info("Enhanced field mapping for GT: {} -> {}", fieldPath, mappedPath);
            } else {
                // Only apply context if the field doesn't already have a path structure
                if (!fieldPath.contains(".")) {
                    fieldPath = applyFieldContext(fieldPath, entityClass);
                } else {
                    log.info("Using explicit path: {}", fieldPath);
                }
        
                // Normalize and map the field path
                String normalizedPath = MongoQueryUtils.normalizeIndexedPath(fieldPath);
                String[] parts = normalizedPath.split("\\.");
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = MongoQueryUtils.mapSubmodelFieldName(parts[i]);
                }
                mappedPath = String.join(".", parts);
            }

            // Extract the value
            Object value;
            if (gt.getRightExpression() instanceof net.sf.jsqlparser.expression.LongValue) {
                value = ((net.sf.jsqlparser.expression.LongValue) gt.getRightExpression()).getValue();
            } else if (gt.getRightExpression() instanceof net.sf.jsqlparser.expression.DoubleValue) {
                value = ((net.sf.jsqlparser.expression.DoubleValue) gt.getRightExpression()).getValue();
            } else {
                value = gt.getRightExpression().toString();
            }

            log.debug("Building greater than condition for field: {} (mapped: {}) with value: {}", fieldPath, mappedPath, value);
            return new org.bson.Document(mappedPath, new org.bson.Document("$gt", value));
        } catch (Exception e) {
            log.error("Error building greater than condition: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Build LIKE condition for MongoDB query
     * @param like The LikeExpression
     * @param entityClass The entity class for the query
     * @return MongoDB query document
     */
    private org.bson.Document buildLikeCondition(net.sf.jsqlparser.expression.operators.relational.LikeExpression like, Class<?> entityClass) {
        try {
            net.sf.jsqlparser.schema.Column column = (net.sf.jsqlparser.schema.Column) like.getLeftExpression();
            String fieldPath = column.getColumnName();
            
            // Handle quoted column names FIRST
            if (fieldPath.startsWith("\"") && fieldPath.endsWith("\"")) {
                fieldPath = fieldPath.substring(1, fieldPath.length() - 1);
                log.info("Removed quotes: {}", fieldPath);
            }
            
            // Check for deeply nested submodel fields that need special handling
            String mappedPath;
            if (fieldPath.contains("submodelElements") && 
                (fieldPath.contains("supplementalSemanticIds") || 
                 fieldPath.contains("qualifiers") || 
                 fieldPath.contains("semanticId") || 
                 fieldPath.contains("keys") || 
                 fieldPath.contains("idShort"))) {
                
                log.info("Using enhanced field mapping for deeply nested submodel field in LIKE condition: {}", fieldPath);
                // Use the enhanced processFieldPath method for consistent field mapping
                mappedPath = processFieldPath(fieldPath, entityClass);
                log.info("Enhanced field mapping for LIKE: {} -> {}", fieldPath, mappedPath);
            } else {
                // Only apply context if the field doesn't already have a path structure
                if (!fieldPath.contains(".")) {
                    fieldPath = applyFieldContext(fieldPath, entityClass);
                } else {
                    log.info("Using explicit path: {}", fieldPath);
                }
        
                // Normalize and map the field path
                String normalizedPath = MongoQueryUtils.normalizeIndexedPath(fieldPath);
                String[] parts = normalizedPath.split("\\.");
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = MongoQueryUtils.mapSubmodelFieldName(parts[i]);
                }
                mappedPath = String.join(".", parts);
            }

            // Extract the pattern
            String pattern = ((net.sf.jsqlparser.expression.StringValue) like.getRightExpression()).getValue();

            // Convert SQL LIKE pattern to MongoDB regex pattern
            StringBuilder regex = new StringBuilder("^");
            for (int i = 0; i < pattern.length(); i++) {
                char c = pattern.charAt(i);
                if (c == '%') {
                    regex.append(".*");
                } else if (c == '_') {
                    regex.append(".");
                } else if ("\\[]{}().*+?^$|".indexOf(c) != -1) {
                    // Escape special regex characters
                    regex.append('\\').append(c);
                } else {
                    regex.append(c);
                }
            }
            regex.append("$");

            String regexPattern = regex.toString();
            log.debug("Building LIKE condition for field: {} (mapped: {}) with pattern: {} -> regex: {}", 
                    fieldPath, mappedPath, pattern, regexPattern);

            // For deeply nested fields in concept descriptions, ensure proper regex matching
            if (mappedPath.startsWith("embedded_data_specifications.") && 
                (mappedPath.contains(".preferred_name.") || mappedPath.contains(".definition."))) {

                log.debug("Using exact regex match for deeply nested field: {}", mappedPath);

                // Create a query that ensures exact matching with regex
                org.bson.Document query = new org.bson.Document();
                query.put(mappedPath, new org.bson.Document("$regex", regexPattern).append("$options", "i"));
                return query;
            }

            // For other fields, use standard regex matching
            return new org.bson.Document(mappedPath, 
                    new org.bson.Document("$regex", regexPattern).append("$options", "i"));
        } catch (Exception e) {
            log.error("Error building LIKE condition: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Preprocess the WHERE clause to handle nested fields
     * Recursively processes the expression tree to handle column names
     * @param expression The WHERE clause expression
     */
    private void preprocessWhereClause(net.sf.jsqlparser.expression.Expression expression) {
        log.debug("Preprocessing WHERE clause: {}", expression);
        
        if (expression instanceof net.sf.jsqlparser.expression.operators.conditional.AndExpression) {
            net.sf.jsqlparser.expression.operators.conditional.AndExpression and = 
                (net.sf.jsqlparser.expression.operators.conditional.AndExpression) expression;
            preprocessWhereClause(and.getLeftExpression());
            preprocessWhereClause(and.getRightExpression());
        } else if (expression instanceof net.sf.jsqlparser.expression.operators.conditional.OrExpression) {
            net.sf.jsqlparser.expression.operators.conditional.OrExpression or = 
                (net.sf.jsqlparser.expression.operators.conditional.OrExpression) expression;
            preprocessWhereClause(or.getLeftExpression());
            preprocessWhereClause(or.getRightExpression());
        } else if (expression instanceof net.sf.jsqlparser.expression.operators.relational.EqualsTo) {
            net.sf.jsqlparser.expression.operators.relational.EqualsTo equals = 
                (net.sf.jsqlparser.expression.operators.relational.EqualsTo) expression;
            preprocessColumnExpression(equals.getLeftExpression());
        } else if (expression instanceof net.sf.jsqlparser.expression.operators.relational.GreaterThan) {
            net.sf.jsqlparser.expression.operators.relational.GreaterThan gt = 
                (net.sf.jsqlparser.expression.operators.relational.GreaterThan) expression;
            preprocessColumnExpression(gt.getLeftExpression());
        } else if (expression instanceof net.sf.jsqlparser.expression.operators.relational.LikeExpression) {
            net.sf.jsqlparser.expression.operators.relational.LikeExpression like = 
                (net.sf.jsqlparser.expression.operators.relational.LikeExpression) expression;
            preprocessColumnExpression(like.getLeftExpression());
        }
    }
    
    /**
     * Preprocess a column expression to handle nested fields
     * @param expression The column expression
     */
    private void preprocessColumnExpression(net.sf.jsqlparser.expression.Expression expression) {
        if (expression instanceof net.sf.jsqlparser.schema.Column) {
            net.sf.jsqlparser.schema.Column column = (net.sf.jsqlparser.schema.Column) expression;
            String columnName = column.getColumnName();
            
            // Handle quoted column names
            if (columnName.startsWith("\"") && columnName.endsWith("\"")) {
                // Remove quotes but keep the original field path
                String unquotedName = columnName.substring(1, columnName.length() - 1);
                
                // Update the column name in the expression without quotes
                // This is critical for MongoDB to properly handle the field path
                column.setColumnName(unquotedName);
                log.debug("Preprocessed column name: {} -> {}", columnName, unquotedName);
            }
        }
    }
    
    /**
     * Execute an UPDATE query against MongoDB collections
     * @param sqlQuery The SQL UPDATE query to execute
     * @return Map containing the update result information
     */
    public Map<String, Object> executeUpdateQuery(String sqlQuery) {
        try {
            // Clean and preprocess the SQL query
            sqlQuery = cleanSqlQuery(sqlQuery);
            log.info("Processing SQL UPDATE query: {}", sqlQuery);
            
            // Parse the SQL query
            Statement statement = CCJSqlParserUtil.parse(sqlQuery);
            
            if (!(statement instanceof net.sf.jsqlparser.statement.update.Update)) {
                throw new IllegalArgumentException("Not an UPDATE statement");
            }
            
            net.sf.jsqlparser.statement.update.Update updateStatement = 
                (net.sf.jsqlparser.statement.update.Update) statement;
            
            // Extract table name
            Table table = updateStatement.getTable();
            String tableName = table.getName().toLowerCase();
            
            // Map to MongoDB collection name
            String mongoCollection = COLLECTION_MAPPING.getOrDefault(tableName, tableName);
            Class<?> entityClass = CLASS_MAPPING.get(mongoCollection);
            
            if (entityClass == null) {
                throw new IllegalArgumentException("Unknown collection: " + tableName);
            }
            
            log.info("Updating collection: {} (mapped to: {})", tableName, mongoCollection);
            
            // Build MongoDB query from WHERE clause
            Query query = new Query();
            if (updateStatement.getWhere() != null) {
                // Preprocess the WHERE clause to handle nested fields
                preprocessWhereClause(updateStatement.getWhere());
                
                // Try to build a raw MongoDB query document first
                org.bson.Document rawQuery = buildRawMongoQuery(updateStatement.getWhere(), entityClass);
                
                if (rawQuery != null) {
                    // Use the raw query document
                    query = Query.query(new Criteria() {
                        @Override
                        public org.bson.Document getCriteriaObject() {
                            return rawQuery;
                        }
                        
                        @Override
                        public String getKey() {
                            return null;
                        }
                    });
                } else {
                    // Fall back to standard criteria builder
                    Criteria criteria = SqlToMongoConverter.buildMongoCriteria(updateStatement.getWhere());
                    query.addCriteria(criteria);
                }
            }
            
            // Build MongoDB update from SET clause
            Update update = new Update();
            for (int i = 0; i < updateStatement.getColumns().size(); i++) {
                net.sf.jsqlparser.schema.Column column = updateStatement.getColumns().get(i);
                net.sf.jsqlparser.expression.Expression valueExpr = updateStatement.getExpressions().get(i);
                
                String fieldName = column.getColumnName();
                // Handle quoted field names
                if (fieldName.startsWith("\"") && fieldName.endsWith("\"")) {
                    fieldName = fieldName.substring(1, fieldName.length() - 1);
                }
                
                // Process the field path for MongoDB
                String processedField = processFieldPath(fieldName, entityClass);
                
                // Extract the value to set
                Object value = null;
                if (valueExpr instanceof net.sf.jsqlparser.expression.StringValue) {
                    value = ((net.sf.jsqlparser.expression.StringValue) valueExpr).getValue();
                } else if (valueExpr instanceof net.sf.jsqlparser.expression.LongValue) {
                    value = ((net.sf.jsqlparser.expression.LongValue) valueExpr).getValue();
                } else if (valueExpr instanceof net.sf.jsqlparser.expression.DoubleValue) {
                    value = ((net.sf.jsqlparser.expression.DoubleValue) valueExpr).getValue();
                } else if (valueExpr instanceof net.sf.jsqlparser.expression.NullValue) {
                    value = null;
                } else {
                    value = valueExpr.toString();
                }
                
                // Handle array indices in field path
                if (processedField.contains(".") && processedField.matches(".*\\.\\d+\\..*")) {
                    // This is a field with array indices
                    log.info("Handling array index in update for field: {}", processedField);
                    // For now, we'll use the standard update approach, but this could be enhanced
                }
                
                update.set(processedField, value);
                log.info("Adding update: {} = {}", processedField, value);
            }
            
            // Execute the update
            com.mongodb.client.result.UpdateResult result = 
                    mongoTemplate.updateMulti(query, update, mongoCollection);
            
            long count = result.getModifiedCount();
            log.info("Updated {} documents in collection {}", count, mongoCollection);
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("operation", "UPDATE");
            resultMap.put("collection", mongoCollection);
            resultMap.put("modifiedCount", count);
            resultMap.put("matchedCount", result.getMatchedCount());
            return resultMap;
            
        } catch (Exception e) {
            log.error("Error executing UPDATE query: {}", e.getMessage(), e);
            throw new RuntimeException("Error executing UPDATE query: " + e.getMessage(), e);
        }
    }
    
    /**
     * Execute a DELETE query against MongoDB collections
     * @param sqlQuery The SQL DELETE query to execute
     * @return Map containing the delete result information
     */
    public Map<String, Object> executeDeleteQuery(String sqlQuery) {
        try {
            // Clean and preprocess the SQL query
            sqlQuery = cleanSqlQuery(sqlQuery);
            log.info("Processing SQL DELETE query: {}", sqlQuery);
            
            // Parse the SQL query
            Statement statement = CCJSqlParserUtil.parse(sqlQuery);
            
            if (!(statement instanceof Delete)) {
                throw new IllegalArgumentException("Not a DELETE statement");
            }
            
            Delete deleteStatement = (Delete) statement;
            
            // Extract table name
            Table table = deleteStatement.getTable();
            String tableName = table.getName().toLowerCase();
            
            // Map to MongoDB collection name
            String mongoCollection = COLLECTION_MAPPING.getOrDefault(tableName, tableName);
            Class<?> entityClass = CLASS_MAPPING.get(mongoCollection);
            
            if (entityClass == null) {
                throw new IllegalArgumentException("Unknown collection: " + tableName);
            }
            
            log.info("Deleting from collection: {} (mapped to: {})", tableName, mongoCollection);
            
            // Build MongoDB query from WHERE clause
            Query query = new Query();
            if (deleteStatement.getWhere() != null) {
                // Preprocess the WHERE clause to handle nested fields
                preprocessWhereClause(deleteStatement.getWhere());
                
                // Try to build a raw MongoDB query document first
                org.bson.Document rawQuery = buildRawMongoQuery(deleteStatement.getWhere(), entityClass);
                
                if (rawQuery != null) {
                    // Use the raw query document
                    query = Query.query(new Criteria() {
                        @Override
                        public org.bson.Document getCriteriaObject() {
                            return rawQuery;
                        }
                        
                        @Override
                        public String getKey() {
                            return null;
                        }
                    });
                } else {
                    // Fall back to standard criteria builder
                    Criteria criteria = SqlToMongoConverter.buildMongoCriteria(deleteStatement.getWhere());
                    query.addCriteria(criteria);
                }
            }
            
            // Execute the delete
            com.mongodb.client.result.DeleteResult result = 
                    mongoTemplate.remove(query, mongoCollection);
            
            long count = result.getDeletedCount();
            log.info("Deleted {} documents from collection {}", count, mongoCollection);
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("operation", "DELETE");
            resultMap.put("collection", mongoCollection);
            resultMap.put("deletedCount", count);
            return resultMap;
            
        } catch (Exception e) {
            log.error("Error executing DELETE query: {}", e.getMessage(), e);
            throw new RuntimeException("Error executing DELETE query: " + e.getMessage(), e);
        }
    }
    
    /**
     * Clean SQL query by removing any markdown formatting or other unwanted characters
     * @param sqlQuery The SQL query to clean
     * @return Cleaned SQL query
     */
    private String cleanSqlQuery(String sqlQuery) {
        // Remove any markdown code block formatting
        sqlQuery = sqlQuery.replaceAll("^```sql\\s*", "").replaceAll("\\s*```$", "");
        
        // Remove any leading/trailing whitespace
        sqlQuery = sqlQuery.trim();
        
        return sqlQuery;
    }
    
    /**
     * Helper method to access nested field values in a MongoDB document
     * @param document The MongoDB document
     * @param fieldPath The dot-notation path to the field
     * @return The field value or null if not found
     */
    private Object getNestedFieldValue(org.bson.Document document, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        Object current = document;
        
        for (String part : parts) {
            // Handle array indices
            if (part.matches("\\d+")) {
                int index = Integer.parseInt(part);
                if (current instanceof List) {
                    List<?> list = (List<?>) current;
                    if (index < list.size()) {
                        current = list.get(index);
                    } else {
                        return null; // Index out of bounds
                    }
                } else {
                    throw new IllegalArgumentException("Expected array at path component: " + part);
                }
            } else {
                // Handle object fields
                if (current instanceof org.bson.Document) {
                    current = ((org.bson.Document) current).get(part);
                    if (current == null) {
                        return null; // Field not found
                    }
                } else {
                    throw new IllegalArgumentException("Expected document at path component: " + part);
                }
            }
        }
        
        return current;
    }
    
    /**
     * Apply context-aware field path handling based on entity type
     * This ensures fields like 'modelType' are properly prefixed with 'submodelElements.'
     * when querying the submodels collection
     * 
     * @param fieldPath The original field path from SQL parser
     * @param entityClass The entity class being queried
     * @return The contextualized field path
     */
    private String applyFieldContext(String fieldPath, Class<?> entityClass) {
        // If already has a parent path (contains a dot), return as is
        if (fieldPath.contains(".")) {
            return fieldPath;
        }
        
        // For SubmodelData entity, certain fields should be prefixed with submodelElements
        if (entityClass == SubmodelData.class) {
            // Fields that are commonly found in submodelElements but NOT at the top level
            Set<String> submodelElementFields = Set.of(
                "modelType", "value", "valueType", "category", "qualifiers"
            );
            
            // Fields that can exist both at top level AND in submodelElements
            Set<String> ambiguousFields = Set.of(
                "idShort", "description", "kind", "semanticId"
            );
            
            // Only add prefix for fields that are exclusively in submodelElements
            if (submodelElementFields.contains(fieldPath)) {
                log.info("Adding context to field: {} -> submodelElements.{}", fieldPath, fieldPath);
                return "submodelElements." + fieldPath;
            }
            
            // For ambiguous fields, we'll prioritize the top-level field
            if (ambiguousFields.contains(fieldPath)) {
                log.info("Using top-level field: {}", fieldPath);
                return fieldPath;
            }
        }
        
        // For other cases, return the original field path
        return fieldPath;
    }
    
    /**
     * Process and map a field path for MongoDB queries, handling all collection types
     * This is a wrapper around MongoQueryUtils.mapFieldPath that adds additional logging
     * and special handling for deeply nested fields in different collections
     * 
     * @param fieldPath The original field path from SQL parser
     * @param entityClass The entity class being queried
     * @return The properly mapped field path for MongoDB queries
     */
    private String processFieldPath(String fieldPath, Class<?> entityClass) {
        log.info("Processing field path: {} for entity class: {}", fieldPath, entityClass.getSimpleName());
        
        // Apply context first (adds submodelElements prefix if needed)
        String contextualizedPath = applyFieldContext(fieldPath, entityClass);
        
        // Then use the generic field mapping method
        String mappedPath = MongoQueryUtils.mapFieldPath(contextualizedPath);
        
        log.info("Processed field path: {} -> {}", fieldPath, mappedPath);
        return mappedPath;
    }
    
}
