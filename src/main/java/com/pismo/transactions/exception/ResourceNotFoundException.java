package com.pismo.transactions.exception;

/**
 * Thrown when a requested resource (Account, OperationType) does not exist.
 * Maps to HTTP 404 in the global exception handler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
