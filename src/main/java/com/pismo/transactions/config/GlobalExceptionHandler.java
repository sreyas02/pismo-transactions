package com.pismo.transactions.config;

import com.pismo.transactions.dto.ErrorResponse;
import com.pismo.transactions.exception.ResourceNotFoundException;
import com.pismo.transactions.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Centralised HTTP error mapping.
 *
 * SRP: this class ONLY maps exceptions to HTTP responses — no business logic.
 * OCP: adding a new exception type = new @ExceptionHandler method, nothing else changes.
 *
 * @RestControllerAdvice applies this to all @RestController beans automatically.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Business rule violations and input validation failures → 422 Unprocessable Entity.
     * Used instead of 400 to distinguish "malformed JSON" from "semantically invalid data".
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse.of(ex.getMessage()));
    }

    /**
     * Resource not found (account, operation type) → 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of(ex.getMessage()));
    }

    /**
     * Malformed JSON or missing required body → 422 Unprocessable Entity.
     * Keeps the client response consistent — all input problems are 422.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse.of("request body is missing or malformed"));
    }

    /**
     * Path variable type mismatch (e.g. /accounts/abc) → 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(ex.getName() + " must be a valid number"));
    }

    /**
     * Catch-all for unexpected errors → 500 Internal Server Error.
     * Never exposes stack trace to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of("internal server error"));
    }
}
