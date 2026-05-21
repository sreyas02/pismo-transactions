package com.pismo.transactions.service;

import com.pismo.transactions.domain.Account;
import com.pismo.transactions.domain.OperationType;
import com.pismo.transactions.domain.Transaction;
import com.pismo.transactions.dto.CreateTransactionRequest;
import com.pismo.transactions.dto.TransactionResponse;
import com.pismo.transactions.exception.ResourceNotFoundException;
import com.pismo.transactions.exception.ValidationException;
import com.pismo.transactions.repository.AccountRepository;
import com.pismo.transactions.repository.OperationTypeRepository;
import com.pismo.transactions.repository.TransactionRepository;
import com.pismo.transactions.validator.TransactionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Orchestrates transaction creation, enforcing the domain sign rule.
 *
 * Business rules (OCP: adding a rule = new check block, existing ones unchanged):
 *   1. account_id must reference an existing account
 *   2. operation_type_id must be a known type
 *   3. Debit operations (purchase / withdrawal) → amount is stored as NEGATIVE
 *   4. Credit operations (voucher) → amount is stored as POSITIVE
 *
 * SRP: only orchestrates — delegates validation, sign logic, and persistence.
 * DIP: all three repository dependencies are interfaces injected by Spring.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository   transactionRepository;  // DIP
    private final AccountRepository       accountRepository;      // DIP
    private final OperationTypeRepository operationTypeRepository;// DIP
    private final TransactionValidator    validator;              // DIP

    /**
     * Validates the request, resolves referenced entities, normalises the
     * amount sign, and persists the transaction.
     *
     * @throws ValidationException       on bad input
     * @throws ResourceNotFoundException if account or operation type not found
     */
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {

        // Step 1: Validate raw input
        validator.validateCreateTransaction(request);

        // Step 2: Verify account exists
        Account account = accountRepository.findById(request.getAccountId())
            .orElseThrow(() -> new ValidationException(
                "account " + request.getAccountId() + " not found"
            ));

        // Step 3: Verify operation type exists
        OperationType operationType = operationTypeRepository
            .findById(request.getOperationTypeId())
            .orElseThrow(() -> new ValidationException("invalid operation_type_id"));

        // Step 4: Normalise amount sign — domain rule lives in OperationType.isDebit()
        BigDecimal normalizedAmount = normalizeAmount(operationType, request.getAmount());

        // Step 5: Persist
        Transaction saved = transactionRepository.save(
            Transaction.of(account.getAccountId(), operationType, normalizedAmount)
        );

        return TransactionResponse.of(
            saved.getTransactionId(),
            saved.getAccountId(),
            saved.getOperationType().getOperationTypeId(),
            saved.getAmount(),
            saved.getEventDate()
        );
    }

    /**
     * Enforces the domain sign rule:
     *   - Debit operations (purchase / withdrawal): always negative
     *   - Credit operations (voucher): always positive
     *
     * Accepts the caller's sign and corrects it — no rejections for wrong sign.
     * This mirrors real payment network behaviour (terminals send positive; network signs).
     *
     * OCP: sign rule is owned by OperationType.isDebit() — adding a new type
     *      only changes the DEBIT_TYPE_IDS set in OperationType, not this method.
     */
    private BigDecimal normalizeAmount(OperationType operationType, BigDecimal amount) {
        if (operationType.isDebit()) {
            // Debit: must be negative
            return amount.compareTo(BigDecimal.ZERO) > 0 ? amount.negate() : amount;
        } else {
            // Credit: must be positive
            return amount.compareTo(BigDecimal.ZERO) < 0 ? amount.negate() : amount;
        }
    }
}
