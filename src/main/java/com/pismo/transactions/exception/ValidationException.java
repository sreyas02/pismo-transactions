package com.pismo.transactions.exception;

/**
 * Thrown when request data violates a business rule or is structurally invalid.
 * Unchecked — callers decide whether to handle; the global handler maps it to HTTP 422.
 *
 * SRP: one purpose — carry a validation error message up the call stack.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
