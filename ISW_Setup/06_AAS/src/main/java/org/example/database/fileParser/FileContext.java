package org.example.database.fileParser;

import java.io.Closeable;

/**
 * Represents the context for a file being processed, potentially holding shared resources
 * like open streams or parsed common structures. This allows multiple QueryableProperty
 * extractors to use the same pre-processed file information without redundant operations.
 *
 * Implementations of this interface should ideally implement {@link java.io.Closeable}
 * if they hold resources that need explicit cleanup, to be managed by the
 * {@link FileContextInitializer}.
 */
public interface FileContext extends Closeable {
    /**
     * Provides access to the underlying raw file if needed by an extractor.
     * However, extractors should prefer using pre-processed resources if available
     * through specific methods in concrete FileContext implementations.
     * @return The original {@link java.io.File} object.
     */
    java.io.File getOriginalFile();

    /**
     * Gets a shared resource by key. Concrete implementations will define what keys
     * and resource types are available.
     * @param key The key for the shared resource.
     * @param <T> The type of the resource.
     * @return The shared resource, or null if not found.
     */
    <T> T getSharedResource(String key);

    /**
     * Puts a shared resource into the context. Concrete implementations will manage
     * how these resources are stored and typed.
     * @param key The key for the shared resource.
     * @param resource The resource to store.
     */
    void setSharedResource(String key, Object resource);

    /**
     * Called when the context is no longer needed, allowing for resource cleanup.
     * This is automatically called if the FileContext is used in a try-with-resources statement.
     * @throws java.io.IOException if an I/O error occurs during cleanup.
     */
    @Override
    void close() throws java.io.IOException;
}
