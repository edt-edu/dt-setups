package org.example.aasManager;

public class AASOperationException extends RuntimeException {
    public AASOperationException(String message) {
        super(message);
    }

    public AASOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
