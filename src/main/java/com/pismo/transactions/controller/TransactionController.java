package com.pismo.transactions.controller;

import com.pismo.transactions.dto.CreateTransactionRequest;
import com.pismo.transactions.dto.TransactionResponse;
import com.pismo.transactions.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * HTTP handler for the /transactions resource.
 *
 * SRP: HTTP concerns only.
 * DIP: depends on TransactionService interface injected by Spring.
 */
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;  // DIP

    /**
     * POST /transactions
     * Creates a financial transaction, enforcing the sign rule for debit/credit.
     *
     * @return 201 Created with the full transaction payload
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
