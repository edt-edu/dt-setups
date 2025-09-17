package org.example.database.fileParser;

import java.io.IOException;

/**
 * Interface for extracting queryable properties from files
 */
public interface QueryableProperty {
    /**
     * Extract a property value from a file
     * @param file The file to extract from
     * @return The extracted property value
     * @throws IOException If an error occurs during extraction
     */
    Object extract(FileContext context) throws IOException;
    
    /**
     * Get the path used to reference this property in queries
     * This is the full path including the 'file.' prefix
     * @return The property path
     */
    String getPropertyPath();
    
    /**
     * Get the short property path without the 'file.' prefix
     * This allows users to query without knowing the property comes from a file
     * @return The short property path
     */
    default String getShortPropertyPath() {
        String fullPath = getPropertyPath();
        if (fullPath.startsWith("file.")) {
            return fullPath.substring(5); // Remove 'file.' prefix
        }
        return fullPath;
    }
    
    /**
     * Get the content types this property extractor supports
     * @return Array of supported content types
     */
    String[] getSupportedContentTypes();
}
