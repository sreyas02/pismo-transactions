package com.pismo.transactions.controller;

import com.pismo.transactions.dto.AccountResponse;
import com.pismo.transactions.dto.CreateAccountRequest;
import com.pismo.transactions.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * HTTP handler for the /accounts resource.
 *
 * SRP: HTTP concerns only — parse request, call service, return response.
 * DIP: depends on AccountService, injected by Spring.
 */
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;  // DIP

    /**
     * POST /accounts
     * Creates a new account.
     *
     * @return 201 Created with the account payload
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /accounts/:accountId
     * Retrieves account information.
     *
     * @return 200 OK with the account payload
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable Long accountId) {
        AccountResponse response = accountService.getAccount(accountId);
        return ResponseEntity.ok(response);
    }
}
