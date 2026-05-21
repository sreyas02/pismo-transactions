package com.pismo.transactions.validator;

import com.pismo.transactions.dto.CreateAccountRequest;
import com.pismo.transactions.dto.CreateTransactionRequest;
import com.pismo.transactions.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Validates raw request DTOs before any business logic runs.
 *
 * SRP: this component ONLY validates — it never persists, never calculates.
 * All validation errors surface as ValidationException (unchecked).
 */
@Component
public class TransactionValidator {

    // ── Account validation ───────────────────────────────────────────────────

    public void validateCreateAccount(CreateAccountRequest request) {
        if (request == null || isBlank(request.getDocumentNumber())) {
            throw new ValidationException("document_number is required");
        }
        String doc = request.getDocumentNumber().trim();
        if (doc.length() < 5 || doc.length() > 20) {
            throw new ValidationException(
                "document_number must be between 5 and 20 characters"
            );
        }
    }

    // ── Transaction validation ───────────────────────────────────────────────

    public void validateCreateTransaction(CreateTransactionRequest request) {
        if (request == null) {
            throw new ValidationException("request body is required");
        }
        if (request.getAccountId() == null || request.getAccountId() <= 0) {
            throw new ValidationException("account_id must be a positive integer");
        }
        if (request.getOperationTypeId() == null
                || request.getOperationTypeId() < 1
                || request.getOperationTypeId() > 4) {
            throw new ValidationException("operation_type_id must be between 1 and 4");
        }
        if (request.getAmount() == null) {
            throw new ValidationException("amount is required");
        }
        // BigDecimal.compareTo — never use == for BigDecimal equality
        if (request.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new ValidationException("amount must not be zero");
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
