package org.example.database.fileParser;

import org.example.database.fileParser.stepFileHandling.StepFileProperties;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registry for queryable file properties
 */
@Component
public class QueryablePropertyRegistry {
    private final Map<String, List<QueryableProperty>> propertiesByContentType = new HashMap<>();
    private final Map<String, QueryableProperty> propertyByPath = new HashMap<>();
    private final Map<String, QueryableProperty> propertyByShortPath = new HashMap<>();
    
    @PostConstruct
    public void init() {
        // Register STEP file properties
        registerProperty(new StepFileProperties.WidthProperty());
        registerProperty(new StepFileProperties.LengthProperty());
        registerProperty(new StepFileProperties.HeightProperty());
        registerProperty(new StepFileProperties.ColorProperty());
        registerProperty(new StepFileProperties.PartNameProperty());
        // Add more properties as needed for other file types in the future
    }
    
    /**
     * Register a queryable property
     */
    public void registerProperty(QueryableProperty property) {
        // Register by content type
        for (String contentType : property.getSupportedContentTypes()) {
            propertiesByContentType.computeIfAbsent(contentType, k -> new ArrayList<>())
                                  .add(property);
        }
        
        // Register with full path (with 'file.' prefix)
        propertyByPath.put(property.getPropertyPath(), property);
        
        // Also register with short path (without 'file.' prefix)
        propertyByShortPath.put(property.getShortPropertyPath(), property);
    }
    
    /**
     * Get all properties for a content type
     */
    public List<QueryableProperty> getPropertiesForContentType(String contentType) {
        return propertiesByContentType.getOrDefault(contentType, new ArrayList<>());
    }
    
    /**
     * Get a property by its path
     * @param propertyPath The property path (can be with or without 'file.' prefix)
     * @return The property extractor, or null if not found
     */
    public QueryableProperty getPropertyByPath(String propertyPath) {
        // First try with the exact path provided
        QueryableProperty property = propertyByPath.get(propertyPath);
        
        // If not found, try with the short path
        if (property == null) {
            property = propertyByShortPath.get(propertyPath);
        }
        
        // If still not found and the path has 'file.' prefix, try without it
        if (property == null && propertyPath.startsWith("file.")) {
            property = propertyByShortPath.get(propertyPath.substring(5));
        }
        
        // If still not found and the path doesn't have 'file.' prefix, try with it
        if (property == null && !propertyPath.startsWith("file.")) {
            property = propertyByPath.get("file." + propertyPath);
        }
        
        return property;
    }
    
    /**
     * Check if a property path is registered
     * @param propertyPath The property path (can be with or without 'file.' prefix)
     * @return true if the property path is registered, false otherwise
     */
    public boolean hasPropertyPath(String propertyPath) {
        return getPropertyByPath(propertyPath) != null;
    }
    
    /**
     * Get all supported content types
     */
    public List<String> getSupportedContentTypes() {
        return new ArrayList<>(propertiesByContentType.keySet());
    }
    
    /**
     * Get all registered property paths (both with and without 'file.' prefix)
     * @return Set of all registered property paths
     */
    public Set<String> getAllPropertyPaths() {
        Set<String> allPaths = new HashSet<>();
        allPaths.addAll(propertyByPath.keySet());
        allPaths.addAll(propertyByShortPath.keySet());
        return allPaths;
    }
    
    /**
     * Get all registered property paths with 'file.' prefix
     * @return Set of all registered property paths with 'file.' prefix
     */
    public Set<String> getAllFullPropertyPaths() {
        return new HashSet<>(propertyByPath.keySet());
    }
    
    /**
     * Get all registered property paths without 'file.' prefix
     * @return Set of all registered property paths without 'file.' prefix
     */
    public Set<String> getAllShortPropertyPaths() {
        return new HashSet<>(propertyByShortPath.keySet());
    }
}
