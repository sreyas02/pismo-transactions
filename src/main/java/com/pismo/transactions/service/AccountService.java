package com.pismo.transactions.service;

import com.pismo.transactions.domain.Account;
import com.pismo.transactions.dto.AccountResponse;
import com.pismo.transactions.dto.CreateAccountRequest;
import com.pismo.transactions.exception.ResourceNotFoundException;
import com.pismo.transactions.exception.ValidationException;
import com.pismo.transactions.repository.AccountRepository;
import com.pismo.transactions.validator.TransactionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates account creation and retrieval.
 *
 * SRP: orchestration only — delegates validation to TransactionValidator,
 *      persistence to AccountRepository.
 * DIP: both dependencies are interfaces injected by Spring.
 */
@Service
@RequiredArgsConstructor  // Lombok: generates constructor injection — no @Autowired needed
public class AccountService {

    private final AccountRepository accountRepository;   // DIP: interface
    private final TransactionValidator validator;        // DIP: interface

    /**
     * Creates a new account after validating input and checking for duplicates.
     *
     * @throws ValidationException       if input is invalid or document_number already exists
     */
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        // Step 1: Validate input (throws ValidationException on failure)
        validator.validateCreateAccount(request);

        String documentNumber = request.getDocumentNumber().trim();

        // Step 2: Enforce uniqueness — business rule, not a DB constraint race
        if (accountRepository.existsByDocumentNumber(documentNumber)) {
            throw new ValidationException(
                "account with this document_number already exists"
            );
        }

        // Step 3: Persist
        Account saved = accountRepository.save(Account.of(documentNumber));

        return AccountResponse.of(saved.getAccountId(), saved.getDocumentNumber());
    }

    /**
     * Retrieves an account by ID.
     *
     * @throws ResourceNotFoundException if no account exists with the given ID
     */
    /**
     * Retrieves an account by ID.
     *
     * @throws ResourceNotFoundException if no account exists with the given ID (→ HTTP 404)
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "account " + accountId + " not found"
            ));
        return AccountResponse.of(account.getAccountId(), account.getDocumentNumber());
    }
}
