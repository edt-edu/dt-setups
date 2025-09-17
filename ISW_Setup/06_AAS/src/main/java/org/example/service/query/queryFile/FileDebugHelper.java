package org.example.service.query.queryFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Helper class for debugging file-related issues
 */
@Component
public class FileDebugHelper {
    private static final Logger log = LoggerFactory.getLogger(FileDebugHelper.class);
    
    /**
     * Debug the structure of a submodel to find file elements
     * @param submodel The submodel document to debug
     */
    public void debugSubmodelStructure(Map<String, Object> submodel) {
        log.info("Debugging submodel structure for: {}", submodel.get("idShort"));
        log.info("Submodel keys: {}", submodel.keySet());
        
        // Dump the entire submodel structure
        for (Map.Entry<String, Object> entry : submodel.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            log.info("Submodel field: {} = {}", key, value != null ? value.getClass().getName() : "null");
            
            // For collections, print their size
            if (value instanceof List) {
                log.info("  List size: {}", ((List<?>) value).size());
                
                // Print the first few elements of the list
                List<?> list = (List<?>) value;
                for (int i = 0; i < Math.min(list.size(), 3); i++) {
                    Object item = list.get(i);
                    log.info("  Item[{}] type: {}", i, item != null ? item.getClass().getName() : "null");
                    
                    if (item instanceof Map) {
                        Map<?, ?> itemMap = (Map<?, ?>) item;
                        log.info("  Item[{}] keys: {}", i, itemMap.keySet());
                        
                        // Check if this is a file element
                        Object modelType = ((Map<?, ?>) item).get("modelType");
                        if ("DefaultFile".equals(modelType)) {
                            log.info("  Found DefaultFile at index {}", i);
                            
                            // Dump the file element structure
                            Object fileObj = ((Map<?, ?>) item).get("file");
                            if (fileObj instanceof Map) {
                                log.info("  File keys: {}", ((Map<?, ?>) fileObj).keySet());
                                log.info("  File contentType: {}", ((Map<?, ?>) fileObj).get("contentType"));
                                log.info("  File value: {}", ((Map<?, ?>) fileObj).get("value"));
                            }
                        }
                    }
                }
            } else if (value instanceof Map) {
                log.info("  Map keys: {}", ((Map<?, ?>) value).keySet());
            }
        }
        
        // Check if the submodel has submodelElements
        Object elements = submodel.get("submodelElements");
        if (elements == null) {
            log.info("Submodel has no submodelElements field");
            return;
        }
        
        if (!(elements instanceof List)) {
            log.info("submodelElements is not a List, it's a: {}", elements.getClass().getName());
            return;
        }
        
        List<?> elementsList = (List<?>) elements;
        log.info("Submodel has {} submodelElements", elementsList.size());
        
        // Iterate through elements to find file elements
        for (int i = 0; i < elementsList.size(); i++) {
            Object element = elementsList.get(i);
            debugElement(element, "submodelElements[" + i + "]");
        }
    }
    
    /**
     * Recursively debug an element to find file elements
     * @param element The element to debug
     * @param path The path to this element
     */
    private void debugElement(Object element, String path) {
        if (element == null) {
            log.info("{} is null", path);
            return;
        }
        
        if (!(element instanceof Map)) {
            log.info("{} is not a Map, it's a: {}", path, element.getClass().getName());
            return;
        }
        
        Map<?, ?> elementMap = (Map<?, ?>) element;
        
        // Check if this is a file element
        Object modelType = elementMap.get("modelType");
        log.info("{} has modelType: {}", path, modelType);
        
        if ("DefaultFile".equals(modelType)) {
            log.info("Found file element at: {}", path);
            
            // Check the file structure
            Object fileObj = elementMap.get("file");
            if (fileObj == null) {
                log.info("{}.file is null", path);
                return;
            }
            
            if (!(fileObj instanceof Map)) {
                log.info("{}.file is not a Map, it's a: {}", path, fileObj.getClass().getName());
                return;
            }
            
            Map<?, ?> fileMap = (Map<?, ?>) fileObj;
            Object contentType = fileMap.get("contentType");
            Object value = fileMap.get("value");
            
            log.info("{}.file has contentType: {} and value: {}", path, contentType, value);
            
            // Check if the file exists
            if (value instanceof String) {
                String filePath = (String) value;
                File file = new File(filePath);
                if (file.exists()) {
                    log.info("File exists at path: {}, size: {} bytes", filePath, file.length());
                } else {
                    log.warn("File does not exist at path: {}", filePath);
                }
            }
        }
        
        // Check for nested elements in the value field
        Object valueObj = elementMap.get("value");
        if (valueObj instanceof List) {
            List<?> valueList = (List<?>) valueObj;
            log.info("{}.value is a List with {} elements", path, valueList.size());
            
            for (int i = 0; i < valueList.size(); i++) {
                debugElement(valueList.get(i), path + ".value[" + i + "]");
            }
        }
    }
}
