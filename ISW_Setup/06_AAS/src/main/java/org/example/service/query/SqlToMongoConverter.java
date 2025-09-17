package org.example.service.query;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.schema.Column;
import org.springframework.data.mongodb.core.query.Criteria;

/**
 * Converts SQL expressions to MongoDB criteria
 * Handles complex nested field queries and various operators
 */
@Slf4j
public class SqlToMongoConverter {

    /**
     * Build MongoDB criteria from a JSQLParser expression
     * @param expression The JSQLParser expression
     * @return MongoDB criteria
     */
    public static Criteria buildMongoCriteria(Expression expression) {
        log.debug("Building criteria for expression: {}", expression);
        
        try {
            if (expression instanceof AndExpression) {
                AndExpression and = (AndExpression) expression;
                Criteria left = buildMongoCriteria(and.getLeftExpression());
                Criteria right = buildMongoCriteria(and.getRightExpression());
                log.debug("Building AND criteria with left: {}, right: {}", left, right);
                return new Criteria().andOperator(left, right);
            } else if (expression instanceof OrExpression) {
                OrExpression or = (OrExpression) expression;
                Criteria left = buildMongoCriteria(or.getLeftExpression());
                Criteria right = buildMongoCriteria(or.getRightExpression());
                log.debug("Building OR criteria with left: {}, right: {}", left, right);
                return new Criteria().orOperator(left, right);
            } else if (expression instanceof EqualsTo) {
                return handleEqualsTo((EqualsTo) expression);
            } else if (expression instanceof GreaterThan) {
                return handleGreaterThan((GreaterThan) expression);
            } else if (expression instanceof GreaterThanEquals) {
                return handleGreaterThanEquals((GreaterThanEquals) expression);
            } else if (expression instanceof MinorThan) {
                return handleMinorThan((MinorThan) expression);
            } else if (expression instanceof MinorThanEquals) {
                return handleMinorThanEquals((MinorThanEquals) expression);
            } else if (expression instanceof LikeExpression) {
                return handleLikeExpression((LikeExpression) expression);
            } else {
                log.warn("Unsupported expression type: {}", expression.getClass().getName());
                throw new RuntimeException("Unsupported expression type: " + expression.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Error building MongoDB criteria for expression: {}", expression, e);
            throw new RuntimeException("Error building MongoDB criteria: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle EqualsTo expression
     * @param equals The EqualsTo expression
     * @return MongoDB criteria
     */
    private static Criteria handleEqualsTo(EqualsTo equals) {
        try {
            Column column = (Column) equals.getLeftExpression();
            String columnName = column.getColumnName();
            
            // Handle quoted column names
            if (columnName.startsWith("\"") && columnName.endsWith("\"")) {
                columnName = columnName.substring(1, columnName.length() - 1);
            }
            
            Object value = extractValue(equals.getRightExpression());
            log.debug("Building EQUALS criteria for field: {} with value: {}", columnName, value);
            
            // Normalize the field path (handles bracket/dot notation, quotes)
            String normalizedPath = MongoQueryUtils.normalizeIndexedPath(columnName);
            // Map each path segment using the Submodel field mapping
            String[] parts = normalizedPath.split("\\.");
            for (int i = 0; i < parts.length; i++) {
                parts[i] = MongoQueryUtils.mapSubmodelFieldName(parts[i]);
            }
            String mappedPath = String.join(".", parts);
            if (parts.length == 1) {
                return Criteria.where(mappedPath).is(value);
            } else {
                // Use robust deep criteria builder for nested/array fields
                return MongoQueryUtils.buildDeepCriteria(parts, 0, value, "eq");
            }
        } catch (Exception e) {
            log.error("Error handling EqualsTo expression: {}", equals, e);
            throw new RuntimeException("Error handling EqualsTo expression: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle GreaterThanEquals expression
     * @param gte The GreaterThanEquals expression
     * @return MongoDB criteria
     */
    private static Criteria handleGreaterThanEquals(GreaterThanEquals gte) {
        try {
            Column column = (Column) gte.getLeftExpression();
            String columnName = column.getColumnName();
            
            // Handle quoted column names
            if (columnName.startsWith("\"") && columnName.endsWith("\"")) {
                columnName = columnName.substring(1, columnName.length() - 1);
            }
            
            Object value = extractValue(gte.getRightExpression());
            log.debug("Building GREATER THAN OR EQUALS criteria for field: {} with value: {}", columnName, value);
            
            // Normalize the field path (handles bracket/dot notation, quotes)
            String normalizedPath = MongoQueryUtils.normalizeIndexedPath(columnName);
            // Map each path segment using the Submodel field mapping
            String[] parts = normalizedPath.split("\\.");
            for (int i = 0; i < parts.length; i++) {
                parts[i] = MongoQueryUtils.mapSubmodelFieldName(parts[i]);
            }
            String mappedPath = String.join(".", parts);
            if (parts.length == 1) {
                return Criteria.where(mappedPath).gte(value);
            } else {
                // Use robust deep criteria builder for nested/array fields
                return MongoQueryUtils.buildDeepCriteria(parts, 0, value, "gte");
            }
        } catch (Exception e) {
            log.error("Error handling GreaterThanEquals expression: {}", gte, e);
            throw new RuntimeException("Error handling GreaterThanEquals expression: " + e.getMessage(), e);
        }
    }

    /**
     * Handle MinorThanEquals expression
     * @param mte The MinorThanEquals expression
     * @return MongoDB criteria
     */
    private static Criteria handleMinorThanEquals(MinorThanEquals mte) {
        try {
            Column column = (Column) mte.getLeftExpression();
            String columnName = column.getColumnName();
            
            // Handle quoted column names
            if (columnName.startsWith("\"") && columnName.endsWith("\"")) {
                columnName = columnName.substring(1, columnName.length() - 1);
            }
            
            Object value = extractValue(mte.getRightExpression());
            log.debug("Building MINOR THAN OR EQUALS criteria for field: {} with value: {}", columnName, value);
            
            // Normalize the field path (handles bracket/dot notation, quotes)
            String normalizedPath = MongoQueryUtils.normalizeIndexedPath(columnName);
            // Map each path segment using the Submodel field mapping
            String[] parts = normalizedPath.split("\\.");
            for (int i = 0; i < parts.length; i++) {
                parts[i] = MongoQueryUtils.mapSubmodelFieldName(parts[i]);
            }
            String mappedPath = String.join(".", parts);
            if (parts.length == 1) {
                return Criteria.where(mappedPath).lte(value);
            } else {
                // Use robust deep criteria builder for nested/array fields
                return MongoQueryUtils.buildDeepCriteria(parts, 0, value, "lte");
            }
        } catch (Exception e) {
            log.error("Error handling MinorThanEquals expression: {}", mte, e);
            throw new RuntimeException("Error handling MinorThanEquals expression: " + e.getMessage(), e);
        }
    }

    /**
     * Handle MinorThan expression
     * @param mt The MinorThan expression
     * @return MongoDB criteria
     */
    private static Criteria handleMinorThan(MinorThan mt) {
        try {
            Column column = (Column) mt.getLeftExpression();
            String columnName = column.getColumnName();
            
            // Handle quoted column names
            if (columnName.startsWith("\"") && columnName.endsWith("\"")) {
                columnName = columnName.substring(1, columnName.length() - 1);
            }
            
            Object value = extractValue(mt.getRightExpression());
            log.debug("Building MINOR THAN criteria for field: {} with value: {}", columnName, value);
            
            // Normalize the field path (handles bracket/dot notation, quotes)
            String normalizedPath = MongoQueryUtils.normalizeIndexedPath(columnName);
            // Map each path segment using the Submodel field mapping
            String[] parts = normalizedPath.split("\\.");
            for (int i = 0; i < parts.length; i++) {
                parts[i] = MongoQueryUtils.mapSubmodelFieldName(parts[i]);
            }
            String mappedPath = String.join(".", parts);
            if (parts.length == 1) {
                return Criteria.where(mappedPath).lt(value);
            } else {
                // Use robust deep criteria builder for nested/array fields
                return MongoQueryUtils.buildDeepCriteria(parts, 0, value, "lt");
            }
        } catch (Exception e) {
            log.error("Error handling MinorThan expression: {}", mt, e);
            throw new RuntimeException("Error handling MinorThan expression: " + e.getMessage(), e);
        }
    }

    /**
     * Handle GreaterThan expression
     * @param gt The GreaterThan expression
     * @return MongoDB criteria
     */
    private static Criteria handleGreaterThan(GreaterThan gt) {
        try {
            Column column = (Column) gt.getLeftExpression();
            String columnName = column.getColumnName();
            
            // Handle quoted column names
            if (columnName.startsWith("\"") && columnName.endsWith("\"")) {
                columnName = columnName.substring(1, columnName.length() - 1);
            }
            
            Object value = extractValue(gt.getRightExpression());
            log.debug("Building GREATER THAN criteria for field: {} with value: {}", columnName, value);
            
            // Normalize the field path (handles bracket/dot notation, quotes)
            String normalizedPath = MongoQueryUtils.normalizeIndexedPath(columnName);
            // Map each path segment using the Submodel field mapping
            String[] parts = normalizedPath.split("\\.");
            for (int i = 0; i < parts.length; i++) {
                parts[i] = MongoQueryUtils.mapSubmodelFieldName(parts[i]);
            }
            String mappedPath = String.join(".", parts);
            if (parts.length == 1) {
                return Criteria.where(mappedPath).gt(value);
            } else {
                // Use robust deep criteria builder for nested/array fields
                return MongoQueryUtils.buildDeepCriteria(parts, 0, value, "gt");
            }
        } catch (Exception e) {
            log.error("Error handling GreaterThan expression: {}", gt, e);
            throw new RuntimeException("Error handling GreaterThan expression: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle LikeExpression
     * @param like The LikeExpression
     * @return MongoDB criteria
     */
    private static Criteria handleLikeExpression(LikeExpression like) {
        try {
            Column column = (Column) like.getLeftExpression();
            String columnName = column.getColumnName();
            
            // Handle quoted column names
            if (columnName.startsWith("\"") && columnName.endsWith("\"")) {
                columnName = columnName.substring(1, columnName.length() - 1);
            }
            
            String likePattern = ((StringValue) like.getRightExpression()).getValue();
            String regexPattern = convertLikeToRegex(likePattern);
            
            log.debug("Building LIKE criteria for field: {} with pattern: {} -> regex: {}", 
                    columnName, likePattern, regexPattern);
            
            // Normalize the field path (handles bracket/dot notation, quotes)
            String normalizedPath = MongoQueryUtils.normalizeIndexedPath(columnName);
            // Map each path segment using the Submodel field mapping
            String[] parts = normalizedPath.split("\\.");
            for (int i = 0; i < parts.length; i++) {
                parts[i] = MongoQueryUtils.mapSubmodelFieldName(parts[i]);
            }
            String mappedPath = String.join(".", parts);
            if (parts.length == 1) {
                return Criteria.where(mappedPath).regex(regexPattern, "i");
            } else {
                // Use robust deep criteria builder for nested/array fields
                return MongoQueryUtils.buildDeepCriteria(parts, 0, regexPattern, "regex");
            }
        } catch (Exception e) {
            log.error("Error handling LikeExpression: {}", like, e);
            throw new RuntimeException("Error handling LikeExpression: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract value from a JSQLParser expression
     * @param valueExpr The value expression
     * @return The extracted value
     */
    private static Object extractValue(Expression valueExpr) {
        try {
            if (valueExpr instanceof StringValue) {
                return ((StringValue) valueExpr).getValue();
            } else if (valueExpr instanceof LongValue) {
                return ((LongValue) valueExpr).getValue();
            } else if (valueExpr instanceof net.sf.jsqlparser.expression.DoubleValue) {
                return ((net.sf.jsqlparser.expression.DoubleValue) valueExpr).getValue();
            } else if (valueExpr instanceof net.sf.jsqlparser.expression.NullValue) {
                return null;
            } else {
                log.warn("Unsupported value expression type: {}, using toString() value", valueExpr.getClass().getName());
                return valueExpr.toString();
            }
        } catch (Exception e) {
            log.error("Error extracting value from expression: {}", valueExpr, e);
            throw new RuntimeException("Error extracting value: " + e.getMessage(), e);
        }
    }
    
    /**
     * Convert SQL LIKE pattern to MongoDB regex pattern
     * @param likePattern The SQL LIKE pattern
     * @return The MongoDB regex pattern
     */
    private static String convertLikeToRegex(String likePattern) {
        try {
            // Escape special regex characters except % and _
            StringBuilder regex = new StringBuilder("^");
            for (int i = 0; i < likePattern.length(); i++) {
                char c = likePattern.charAt(i);
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
            
            return regex.toString();
        } catch (Exception e) {
            log.error("Error converting LIKE pattern to regex: {}", likePattern, e);
            throw new RuntimeException("Error converting LIKE pattern: " + e.getMessage(), e);
        }
    }
}
