package com.pismo.transactions.controller;

import com.pismo.transactions.dto.CreateTransactionRequest;
import com.pismo.transactions.dto.TransactionResponse;
import com.pismo.transactions.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * HTTP handler for the /transactions resource.
 *
 * SRP: HTTP concerns only.
 * DIP: depends on TransactionService interface injected by Spring.
 */
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management endpoints")
public class TransactionController {

    private final TransactionService transactionService;  // DIP

    /**
     * POST /transactions
     * Creates a financial transaction, enforcing the sign rule for debit/credit.
     *
     * @return 201 Created with the full transaction payload
     */
    @Operation(
            summary = "Create a transaction",
            description = "Debit operations (types 1-3) stored as negative. Credit voucher (type 4) stored as positive."
    )
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
