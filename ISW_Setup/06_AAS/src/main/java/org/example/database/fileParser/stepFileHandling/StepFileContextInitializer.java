package org.example.database.fileParser.stepFileHandling;

import org.example.database.fileParser.FileContext;
import org.example.database.fileParser.FileContextInitializer;
import org.example.database.fileParser.SimpleFileContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Initializes a {@link FileContext} for STEP files.
 * For STEP files, the basic initialization might just involve creating a {@link SimpleFileContext}
 * that holds the file reference, as individual property extractors often perform their own
 * specific parsing on demand.
 */
@Component // Mark as a Spring component if it's to be auto-detected
public class StepFileContextInitializer implements FileContextInitializer {

    private static final String[] SUPPORTED_CONTENT_TYPES = {
        "application/step", "application/STEP", "application/x-step", 
        "application/p21", "application/x-p21", "text/plain" // text/plain for .stp files sometimes
    };

    @Override
    public FileContext initialize(File file) throws IOException {
        // For STEP files, a simple context holding the file is often sufficient
        // as extractors might read specific parts of the file on demand.
        // If there were common pre-parsing (e.g., reading a header that all properties use),
        // that logic would go here, and the result stored in the FileContext.
        return new SimpleFileContext(file);
    }

    @Override
    public String[] getSupportedContentTypes() {
        return SUPPORTED_CONTENT_TYPES.clone(); // Return a clone for immutability
    }
}
