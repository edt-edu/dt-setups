package org.example.database.fileParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple implementation of {@link FileContext} that primarily holds the original file reference.
 * It can also store basic shared resources in a map.
 */
public class SimpleFileContext implements FileContext {
    private final File originalFile;
    private final Map<String, Object> sharedResources = new HashMap<>();

    public SimpleFileContext(File originalFile) {
        if (originalFile == null) {
            throw new IllegalArgumentException("Original file cannot be null.");
        }
        this.originalFile = originalFile;
    }

    @Override
    public File getOriginalFile() {
        return originalFile;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getSharedResource(String key) {
        return (T) sharedResources.get(key);
    }

    @Override
    public void setSharedResource(String key, Object resource) {
        sharedResources.put(key, resource);
    }

    /**
     * Closes any closeable resources stored in the sharedResources map.
     * For this simple implementation, it iterates through shared resources and closes them if they are Closeable.
     * Specific FileContext implementations might have more direct resource management.
     */
    @Override
    public void close() throws IOException {
        for (Object resource : sharedResources.values()) {
            if (resource instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) resource).close();
                } catch (Exception e) {
                    // Log or handle exception, rethrow as IOException if appropriate
                    // For simplicity, we'll just print a stack trace here, but proper logging is advised
                    System.err.println("Error closing resource in SimpleFileContext: " + e.getMessage());
                    // if (e instanceof IOException) throw (IOException) e;
                    // throw new IOException("Failed to close resource", e);
                }
            }
        }
        // No other specific resources to close in this simple implementation.
    }
}
