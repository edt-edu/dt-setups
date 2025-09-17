package org.example.database.fileParser;

import java.io.File;
import java.io.IOException;

/**
 * Defines a contract for initializing a {@link FileContext} from a {@link java.io.File}.
 * This allows for common, potentially expensive, setup operations (like opening a file,
 * parsing a header, etc.) to be performed once and the results shared via the FileContext
 * among multiple {@link QueryableProperty} extractors.
 */
public interface FileContextInitializer {

    /**
     * Initializes a {@link FileContext} for the given file.
     * This method might open the file, parse common headers, or prepare other shared resources.
     *
     * @param file The file to initialize the context for.
     * @return A {@link FileContext} instance ready for use by {@link QueryableProperty} extractors.
     * @throws IOException If an error occurs during file access or initialization.
     */
    FileContext initialize(File file) throws IOException;

    /**
     * Returns an array of content types (e.g., "application/step", "text/csv") that this
     * initializer supports.
     *
     * @return An array of supported content type strings.
     */
    String[] getSupportedContentTypes();
}
