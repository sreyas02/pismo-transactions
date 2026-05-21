package com.pismo.transactions.service;

import com.pismo.transactions.domain.Account;
import com.pismo.transactions.domain.OperationType;
import com.pismo.transactions.domain.Transaction;
import com.pismo.transactions.dto.CreateTransactionRequest;
import com.pismo.transactions.dto.TransactionResponse;
import com.pismo.transactions.exception.ValidationException;
import com.pismo.transactions.repository.AccountRepository;
import com.pismo.transactions.repository.OperationTypeRepository;
import com.pismo.transactions.repository.TransactionRepository;
import com.pismo.transactions.validator.TransactionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Service unit tests with Mockito.
 *
 * DIP PROOF: TransactionService is tested with mock repositories —
 * it has zero knowledge of H2 or any database.
 *
 * TDD: these tests define the contract. Write them first, then implement.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService")
class TransactionServiceTest {

    @Mock private TransactionRepository   transactionRepository;
    @Mock private AccountRepository       accountRepository;
    @Mock private OperationTypeRepository operationTypeRepository;
    @Mock private TransactionValidator    validator;  // stubbed — unit isolation

    @InjectMocks
    private TransactionService transactionService;

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Account account(long id) {
        Account a = new Account();
        a.setAccountId(id);
        a.setDocumentNumber("12345678900");
        return a;
    }

    private OperationType opType(int id, boolean isDebit) {
        OperationType o = new OperationType();
        o.setOperationTypeId(id);
        o.setDescription("test");
        // Note: isDebit() checks DEBIT_TYPE_IDS set — we control by choosing ID
        return o;
    }

    private Transaction savedTx(long txId, long accountId, OperationType opType,
                                BigDecimal amount) {
        Transaction tx = new Transaction();
        tx.setTransactionId(txId);
        tx.setAccountId(accountId);
        tx.setOperationType(opType);
        tx.setAmount(amount);
        tx.setEventDate(LocalDateTime.now());
        return tx;
    }

    private CreateTransactionRequest request(long accountId, int opTypeId, double amount) {
        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setAccountId(accountId);
        req.setOperationTypeId(opTypeId);
        req.setAmount(BigDecimal.valueOf(amount));
        return req;
    }

    // ── Tests ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("sign normalisation")
    class SignNormalization {

        @Test
        @DisplayName("normal purchase (op 1) — positive input → stored negative")
        void normalPurchase_positiveInput_storedNegative() {
            OperationType op = opType(1, true); // debit
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account(1)));
            when(operationTypeRepository.findById(1)).thenReturn(Optional.of(op));
            when(transactionRepository.save(any())).thenAnswer(inv -> {
                Transaction tx = inv.getArgument(0);
                tx.setTransactionId(1L);
                tx.setEventDate(LocalDateTime.now());
                return tx;
            });

            TransactionResponse resp = transactionService.createTransaction(request(1, 1, 50.0));

            assertThat(resp.getAmount()).isLessThan(BigDecimal.ZERO);
            assertThat(resp.getAmount()).isEqualByComparingTo(new BigDecimal("-50.00"));
        }

        @Test
        @DisplayName("withdrawal (op 3) — positive input → stored negative")
        void withdrawal_positiveInput_storedNegative() {
            OperationType op = opType(3, true);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account(1)));
            when(operationTypeRepository.findById(3)).thenReturn(Optional.of(op));
            when(transactionRepository.save(any())).thenAnswer(inv -> {
                Transaction tx = inv.getArgument(0);
                tx.setTransactionId(1L);
                tx.setEventDate(LocalDateTime.now());
                return tx;
            });

            TransactionResponse resp = transactionService.createTransaction(request(1, 3, 200.0));

            assertThat(resp.getAmount()).isLessThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("credit voucher (op 4) — positive input → stored positive")
        void creditVoucher_positiveInput_storedPositive() {
            OperationType op = opType(4, false); // credit
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account(1)));
            when(operationTypeRepository.findById(4)).thenReturn(Optional.of(op));
            when(transactionRepository.save(any())).thenAnswer(inv -> {
                Transaction tx = inv.getArgument(0);
                tx.setTransactionId(1L);
                tx.setEventDate(LocalDateTime.now());
                return tx;
            });

            TransactionResponse resp = transactionService.createTransaction(request(1, 4, 60.0));

            assertThat(resp.getAmount()).isGreaterThan(BigDecimal.ZERO);
            assertThat(resp.getAmount()).isEqualByComparingTo(new BigDecimal("60.00"));
        }

        @Test
        @DisplayName("debit — caller sends negative → kept negative (no double-negation)")
        void debit_negativeInput_keptNegative() {
            OperationType op = opType(1, true);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account(1)));
            when(operationTypeRepository.findById(1)).thenReturn(Optional.of(op));
            when(transactionRepository.save(any())).thenAnswer(inv -> {
                Transaction tx = inv.getArgument(0);
                tx.setTransactionId(1L);
                tx.setEventDate(LocalDateTime.now());
                return tx;
            });

            CreateTransactionRequest req = request(1, 1, -50.0);
            TransactionResponse resp = transactionService.createTransaction(req);

            assertThat(resp.getAmount()).isEqualByComparingTo(new BigDecimal("-50.00"));
        }

        @Test
        @DisplayName("credit — caller sends negative → flipped to positive")
        void credit_negativeInput_flippedToPositive() {
            OperationType op = opType(4, false);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account(1)));
            when(operationTypeRepository.findById(4)).thenReturn(Optional.of(op));
            when(transactionRepository.save(any())).thenAnswer(inv -> {
                Transaction tx = inv.getArgument(0);
                tx.setTransactionId(1L);
                tx.setEventDate(LocalDateTime.now());
                return tx;
            });

            CreateTransactionRequest req = request(1, 4, -60.0);
            TransactionResponse resp = transactionService.createTransaction(req);

            assertThat(resp.getAmount()).isGreaterThan(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("validation failures")
    class ValidationFailures {

        @Test
        @DisplayName("account not found → ValidationException")
        void accountNotFound_throwsValidationException() {
            when(accountRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                transactionService.createTransaction(request(999, 1, 50.0))
            ).isInstanceOf(ValidationException.class)
             .hasMessageContaining("999");
        }

        @Test
        @DisplayName("operation type not found → ValidationException")
        void opTypeNotFound_throwsValidationException() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account(1)));
            when(operationTypeRepository.findById(99)).thenReturn(Optional.empty());

            CreateTransactionRequest req = request(1, 99, 50.0);
            assertThatThrownBy(() ->
                transactionService.createTransaction(req)
            ).isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("DIP: repository interactions")
    class RepositoryInteractions {

        @Test
        @DisplayName("transactionRepository.save() called exactly once per request")
        void repositorySaveCalled_oncePerRequest() {
            OperationType op = opType(4, false);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account(1)));
            when(operationTypeRepository.findById(4)).thenReturn(Optional.of(op));
            when(transactionRepository.save(any())).thenAnswer(inv -> {
                Transaction tx = inv.getArgument(0);
                tx.setTransactionId(1L);
                tx.setEventDate(LocalDateTime.now());
                return tx;
            });

            transactionService.createTransaction(request(1, 4, 100.0));

            verify(transactionRepository, times(1)).save(any(Transaction.class));
        }

        @Test
        @DisplayName("response contains all expected fields")
        void response_hasAllFields() {
            OperationType op = opType(4, false);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account(1)));
            when(operationTypeRepository.findById(4)).thenReturn(Optional.of(op));
            when(transactionRepository.save(any())).thenAnswer(inv -> {
                Transaction tx = inv.getArgument(0);
                tx.setTransactionId(42L);
                tx.setEventDate(LocalDateTime.now());
                return tx;
            });

            TransactionResponse resp = transactionService.createTransaction(request(1, 4, 100.0));

            assertThat(resp.getTransactionId()).isEqualTo(42L);
            assertThat(resp.getAccountId()).isEqualTo(1L);
            assertThat(resp.getOperationTypeId()).isEqualTo(4);
            assertThat(resp.getAmount()).isNotNull();
            assertThat(resp.getEventDate()).isNotNull();
        }
    }
}
