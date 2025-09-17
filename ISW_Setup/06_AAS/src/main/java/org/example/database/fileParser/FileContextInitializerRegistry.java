package org.example.database.fileParser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for {@link FileContextInitializer} instances.
 * This allows for dynamic lookup of the appropriate initializer based on file content type.
 */
@Component
public class FileContextInitializerRegistry {

    private final List<FileContextInitializer> initializers; // Autowire all available initializers
    private final Map<String, FileContextInitializer> initializersByContentType = new HashMap<>();

    /**
     * Constructs the registry and autowires all beans that implement FileContextInitializer.
     * @param initializers A list of FileContextInitializer beans found in the Spring context.
     */
    @Autowired
    public FileContextInitializerRegistry(List<FileContextInitializer> initializers) {
        this.initializers = initializers;
    }

    /**
     * Initializes the registry by mapping content types to their respective initializers.
     * This method is called after dependency injection is done to populate the registry.
     */
    @PostConstruct
    public void init() {
        if (initializers != null) {
            for (FileContextInitializer initializer : initializers) {
                for (String contentType : initializer.getSupportedContentTypes()) {
                    if (initializersByContentType.containsKey(contentType)) {
                        // Handle potential conflicts, e.g., log a warning or decide on a strategy
                        System.err.println("Warning: Duplicate FileContextInitializer registration for content type: " + contentType);
                    } else {
                        initializersByContentType.put(contentType.toLowerCase(), initializer);
                    }
                }
            }
        }
    }

    /**
     * Retrieves a {@link FileContextInitializer} for the given content type.
     *
     * @param contentType The content type of the file (e.g., "application/step").
     * @return An {@link Optional} containing the initializer if one is found for the content type,
     *         otherwise an empty Optional.
     */
    public Optional<FileContextInitializer> getInitializer(String contentType) {
        if (contentType == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(initializersByContentType.get(contentType.toLowerCase()));
    }

    /**
     * Registers a new FileContextInitializer programmatically. 
     * Useful for testing or dynamic registration outside of Spring's component scanning.
     * @param initializer The initializer to register.
     */
    public void registerInitializer(FileContextInitializer initializer) {
        if (initializer != null) {
            for (String contentType : initializer.getSupportedContentTypes()) {
                initializersByContentType.put(contentType.toLowerCase(), initializer);
            }
            // Add to the main list if not already present (e.g. if not autowired)
            if (this.initializers != null && !this.initializers.contains(initializer)) {
                this.initializers.add(initializer);
            }
        }
    }
}
