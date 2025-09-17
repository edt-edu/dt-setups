package org.example.service.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for extracting nested fields from entity objects
 * Handles complex nested paths with array indices
 */
@Slf4j
public class EntityFieldExtractor {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extract nested field from an object using dot notation
     * @param field The field to extract (e.g., assetRef.type, submodelRefs.0.keys.0.value)
     * @param object The object to extract from
     * @param entityMap The map to store the extracted field
     */
    public static void extractNestedFieldWithDotNotation(String field, Object object, Map<String, Object> entityMap) {
        try {
            // First convert the object to a map to handle everything uniformly
            Map<String, Object> objectAsMap = convertObjectToMap(object);
            
            // Normalize the path to handle both bracket and dot notation
            String normalizedPath = MongoQueryUtils.normalizeIndexedPath(field);
            
            // Extract the value using the normalized path
            Object value = getValueByPath(objectAsMap, normalizedPath);
            
            if (value != null) {
                entityMap.put(field, value);
                log.debug("Successfully extracted field: {} with value: {}", field, value);
            } else {
                log.debug("Field not found or null: {}", field);
            }
        } catch (Exception e) {
            log.error("Exception while extracting nested field: {}", field, e);
        }
    }
    
    /**
     * Convert any object to a Map<String, Object> for uniform handling
     * Handles both Java objects and Map structures
     * @param obj The object to convert
     * @return A map representation of the object
     */
    private static Map<String, Object> convertObjectToMap(Object obj) {
        if (obj == null) {
            return new HashMap<>();
        }
        
        // If it's already a map, just cast it
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            return map;
        }
        
        // Create a new map to store the object properties
        Map<String, Object> result = new HashMap<>();
        
        try {
            // First try to use Jackson to convert the object to a map
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> jacksonMap = objectMapper.convertValue(obj, Map.class);
                result = jacksonMap;
                return result;
            } catch (Exception e) {
                log.debug("Failed to convert object to map using Jackson: {}", e.getMessage());
                // Continue with manual conversion if Jackson fails
            }
            
            // Get all fields including inherited ones
            List<Field> fields = getAllFields(obj.getClass());
            
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object value = field.get(obj);
                
                // Try to get the field name from MongoDB annotation if present
                try {
                    org.springframework.data.mongodb.core.mapping.Field annotation = 
                        field.getAnnotation(org.springframework.data.mongodb.core.mapping.Field.class);
                    if (annotation != null && !annotation.value().isEmpty()) {
                        fieldName = annotation.value();
                    }
                } catch (Exception e) {
                    // Ignore if annotation processing fails
                }
                
                // Add the field to the result map
                result.put(fieldName, value);
                
                // Also try to use getter methods
                try {
                    String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    Method getter = obj.getClass().getMethod(getterName);
                    Object getterValue = getter.invoke(obj);
                    if (getterValue != null) {
                        result.put(fieldName, getterValue);
                    }
                } catch (Exception e) {
                    // Ignore if getter fails
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("Failed to convert object to map: {}", e.getMessage());
            return result;
        }
    }
    
    /**
     * Get all fields from a class and its superclasses
     * @param type The class to get fields from
     * @return List of all fields
     */
    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            fields.add(field);
        }
        
        if (type.getSuperclass() != null && type.getSuperclass() != Object.class) {
            fields.addAll(getAllFields(type.getSuperclass()));
        }
        
        return fields;
    }
    
    /**
     * Get a value from a nested map using a dot-notation path
     * @param map The map to extract from
     * @param path The path in dot notation (e.g., "submodelElements.0.semanticId.keys.0.value")
     * @return The extracted value or null if not found
     */
    private static Object getValueByPath(Map<String, Object> map, String path) {
        String[] parts = path.split("\\.");
        return getValueByPathParts(map, parts, 0);
    }
    
    /**
     * Recursive helper to get a value from a nested structure using path parts
     * @param current The current object in the traversal
     * @param parts Array of path parts
     * @param index Current index in the parts array
     * @return The extracted value or null if not found
     */
    private static Object getValueByPathParts(Object current, String[] parts, int index) {
        if (current == null) {
            return null;
        }
        
        if (index >= parts.length) {
            return current;
        }
        
        String part = parts[index];
        
        // Handle numeric indices (array access)
        if (part.matches("\\d+")) {
            int arrayIndex = Integer.parseInt(part);
            
            if (current instanceof List) {
                List<?> list = (List<?>) current;
                if (arrayIndex < list.size()) {
                    return getValueByPathParts(list.get(arrayIndex), parts, index + 1);
                } else {
                    log.debug("Index out of bounds: {} for list of size {}", arrayIndex, list.size());
                    return null;
                }
            } else if (current instanceof Map) {
                // Some maps might use string numbers as keys
                Map<?, ?> currentMap = (Map<?, ?>) current;
                if (currentMap.containsKey(arrayIndex)) {
                    return getValueByPathParts(currentMap.get(arrayIndex), parts, index + 1);
                } else if (currentMap.containsKey(part)) {
                    return getValueByPathParts(currentMap.get(part), parts, index + 1);
                } else {
                    log.debug("Numeric key not found in map: {}", part);
                    return null;
                }
            } else {
                log.debug("Cannot apply numeric index to {}", current.getClass().getSimpleName());
                return null;
            }
        }
        
        // Handle map access
        if (current instanceof Map) {
            Map<?, ?> currentMap = (Map<?, ?>) current;
            
            // First try with the exact key
            if (currentMap.containsKey(part)) {
                return getValueByPathParts(currentMap.get(part), parts, index + 1);
            }
            
            // Try with camelCase conversion (for MongoDB field names)
            String camelCase = toCamelCase(part);
            if (!camelCase.equals(part) && currentMap.containsKey(camelCase)) {
                return getValueByPathParts(currentMap.get(camelCase), parts, index + 1);
            }
            
            // Try with snake_case conversion
            String snakeCase = toSnakeCase(part);
            if (!snakeCase.equals(part) && currentMap.containsKey(snakeCase)) {
                return getValueByPathParts(currentMap.get(snakeCase), parts, index + 1);
            }
            
            log.debug("Key not found in map: {}", part);
            return null;
        }
        
        // For regular objects, convert to map first
        Map<String, Object> objAsMap = convertObjectToMap(current);
        return getValueByPathParts(objAsMap, parts, index);
    }
    
    /**
     * Convert snake_case to camelCase
     * @param input The input string in snake_case
     * @return The string in camelCase
     */
    private static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(c);
                }
            }
        }
        
        return result.toString();
    }
    
    /**
     * Convert camelCase to snake_case
     * @param input The input string in camelCase
     * @return The string in snake_case
     */
    private static String toSnakeCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(input.charAt(0)));
        
        for (int i = 1; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append('_');
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
}
