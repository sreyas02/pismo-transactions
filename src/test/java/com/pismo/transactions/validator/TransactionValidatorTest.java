package com.pismo.transactions.validator;

import com.pismo.transactions.dto.CreateAccountRequest;
import com.pismo.transactions.dto.CreateTransactionRequest;
import com.pismo.transactions.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Pure unit tests for TransactionValidator.
 * No Spring context — instantiated directly. Fast, isolated.
 */
@DisplayName("TransactionValidator")
class TransactionValidatorTest {

    private TransactionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TransactionValidator();
    }

    // ── Account validation ───────────────────────────────────────────────────

    @Nested
    @DisplayName("validateCreateAccount")
    class ValidateCreateAccount {

        @Test
        @DisplayName("valid document number passes without exception")
        void valid_passes() {
            CreateAccountRequest req = new CreateAccountRequest();
            req.setDocumentNumber("12345678900");
            assertThatCode(() -> validator.validateCreateAccount(req))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null document_number throws ValidationException")
        void nullDocument_throws() {
            CreateAccountRequest req = new CreateAccountRequest();
            req.setDocumentNumber(null);
            assertThatThrownBy(() -> validator.validateCreateAccount(req))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("document_number is required");
        }

        @Test
        @DisplayName("empty document_number throws ValidationException")
        void emptyDocument_throws() {
            CreateAccountRequest req = new CreateAccountRequest();
            req.setDocumentNumber("");
            assertThatThrownBy(() -> validator.validateCreateAccount(req))
                .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("whitespace-only document_number throws ValidationException")
        void whitespaceDocument_throws() {
            CreateAccountRequest req = new CreateAccountRequest();
            req.setDocumentNumber("   ");
            assertThatThrownBy(() -> validator.validateCreateAccount(req))
                .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("document_number shorter than 5 chars throws ValidationException")
        void tooShortDocument_throws() {
            CreateAccountRequest req = new CreateAccountRequest();
            req.setDocumentNumber("1234"); // 4 chars
            assertThatThrownBy(() -> validator.validateCreateAccount(req))
                .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("document_number longer than 20 chars throws ValidationException")
        void tooLongDocument_throws() {
            CreateAccountRequest req = new CreateAccountRequest();
            req.setDocumentNumber("123456789012345678901"); // 21 chars
            assertThatThrownBy(() -> validator.validateCreateAccount(req))
                .isInstanceOf(ValidationException.class);
        }
    }

    // ── Transaction validation ───────────────────────────────────────────────

    @Nested
    @DisplayName("validateCreateTransaction")
    class ValidateCreateTransaction {

        private CreateTransactionRequest validRequest() {
            CreateTransactionRequest req = new CreateTransactionRequest();
            req.setAccountId(1L);
            req.setOperationTypeId(4);
            req.setAmount(new BigDecimal("123.45"));
            return req;
        }

        @Test
        @DisplayName("valid request passes without exception")
        void valid_passes() {
            assertThatCode(() -> validator.validateCreateTransaction(validRequest()))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null account_id throws ValidationException")
        void nullAccountId_throws() {
            CreateTransactionRequest req = validRequest();
            req.setAccountId(null);
            assertThatThrownBy(() -> validator.validateCreateTransaction(req))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("account_id");
        }

        @Test
        @DisplayName("negative account_id throws ValidationException")
        void negativeAccountId_throws() {
            CreateTransactionRequest req = validRequest();
            req.setAccountId(-1L);
            assertThatThrownBy(() -> validator.validateCreateTransaction(req))
                .isInstanceOf(ValidationException.class);
        }

        @ParameterizedTest(name = "operation_type_id {0} is invalid")
        @ValueSource(ints = {0, 5, -1, 99})
        @DisplayName("operation_type_id outside 1-4 throws ValidationException")
        void invalidOpType_throws(int opTypeId) {
            CreateTransactionRequest req = validRequest();
            req.setOperationTypeId(opTypeId);
            assertThatThrownBy(() -> validator.validateCreateTransaction(req))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("operation_type_id");
        }

        @ParameterizedTest(name = "operation_type_id {0} is valid")
        @ValueSource(ints = {1, 2, 3, 4})
        @DisplayName("operation_type_id in range 1-4 is accepted")
        void validOpTypes_pass(int opTypeId) {
            CreateTransactionRequest req = validRequest();
            req.setOperationTypeId(opTypeId);
            assertThatCode(() -> validator.validateCreateTransaction(req))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null amount throws ValidationException")
        void nullAmount_throws() {
            CreateTransactionRequest req = validRequest();
            req.setAmount(null);
            assertThatThrownBy(() -> validator.validateCreateTransaction(req))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("amount");
        }

        @Test
        @DisplayName("zero amount throws ValidationException")
        void zeroAmount_throws() {
            CreateTransactionRequest req = validRequest();
            req.setAmount(BigDecimal.ZERO);
            assertThatThrownBy(() -> validator.validateCreateTransaction(req))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("amount must not be zero");
        }
    }
}
