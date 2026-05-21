package com.pismo.transactions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pismo.transactions.dto.AccountResponse;
import com.pismo.transactions.dto.CreateAccountRequest;
import com.pismo.transactions.exception.ResourceNotFoundException;
import com.pismo.transactions.exception.ValidationException;
import com.pismo.transactions.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice test — loads ONLY the web layer (@WebMvcTest).
 * AccountService is mocked — proves controller handles HTTP correctly
 * regardless of service implementation.
 *
 * DIP PROOF: controller tested in isolation from service internals.
 */
@WebMvcTest(AccountController.class)
@DisplayName("AccountController")
class AccountControllerTest {

    @Autowired private MockMvc       mockMvc;
    @Autowired private ObjectMapper  objectMapper;
    @MockBean  private AccountService accountService;

    // ── POST /accounts ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /accounts")
    class CreateAccount {

        @Test
        @DisplayName("valid request → 201 with account payload")
        void validRequest_returns201() throws Exception {
            AccountResponse stub = AccountResponse.of(1L, "12345678900");
            when(accountService.createAccount(any())).thenReturn(stub);

            mockMvc.perform(post("/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"document_number": "12345678900"}
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.account_id").value(1))
                .andExpect(jsonPath("$.document_number").value("12345678900"));
        }

        @Test
        @DisplayName("ValidationException from service → 422")
        void validationException_returns422() throws Exception {
            when(accountService.createAccount(any()))
                .thenThrow(new ValidationException("document_number is required"));

            mockMvc.perform(post("/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("document_number is required"));
        }

        @Test
        @DisplayName("duplicate document_number → 422")
        void duplicateDocument_returns422() throws Exception {
            when(accountService.createAccount(any()))
                .thenThrow(new ValidationException(
                    "account with this document_number already exists"
                ));

            mockMvc.perform(post("/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"document_number": "12345678900"}
                    """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("malformed JSON body → 422")
        void malformedJson_returns422() throws Exception {
            mockMvc.perform(post("/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("not valid json"))
                .andExpect(status().isUnprocessableEntity());
        }
    }

    // ── GET /accounts/:accountId ─────────────────────────────────────────────

    @Nested
    @DisplayName("GET /accounts/:accountId")
    class GetAccount {

        @Test
        @DisplayName("existing account → 200 with account payload")
        void existingAccount_returns200() throws Exception {
            AccountResponse stub = AccountResponse.of(1L, "12345678900");
            when(accountService.getAccount(1L)).thenReturn(stub);

            mockMvc.perform(get("/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value(1))
                .andExpect(jsonPath("$.document_number").value("12345678900"));
        }

        @Test
        @DisplayName("non-existent account → 404")
        void notFound_returns404() throws Exception {
            when(accountService.getAccount(999L))
                .thenThrow(new ResourceNotFoundException("account 999 not found"));

            mockMvc.perform(get("/accounts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("account 999 not found"));
        }

        @Test
        @DisplayName("non-integer accountId → 400")
        void nonIntegerAccountId_returns400() throws Exception {
            mockMvc.perform(get("/accounts/abc"))
                .andExpect(status().isBadRequest());
        }
    }
}
