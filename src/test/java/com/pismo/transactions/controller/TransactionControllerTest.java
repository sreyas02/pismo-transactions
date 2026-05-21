package com.pismo.transactions.controller;

import com.pismo.transactions.dto.TransactionResponse;
import com.pismo.transactions.exception.ValidationException;
import com.pismo.transactions.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice test for TransactionController.
 * Verifies HTTP contract — status codes, response shape, error mapping.
 */
@WebMvcTest(TransactionController.class)
@DisplayName("TransactionController")
class TransactionControllerTest {

    @Autowired private MockMvc           mockMvc;
    @MockBean  private TransactionService transactionService;

    private TransactionResponse stubResponse(BigDecimal amount) {
        return TransactionResponse.of(1L, 1L, 4, amount, LocalDateTime.now());
    }

    @Nested
    @DisplayName("POST /transactions")
    class CreateTransaction {

        @Test
        @DisplayName("credit voucher — valid request → 201 with positive amount")
        void creditVoucher_returns201_positiveAmount() throws Exception {
            when(transactionService.createTransaction(any()))
                .thenReturn(stubResponse(new BigDecimal("123.45")));

            mockMvc.perform(post("/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "account_id": 1,
                          "operation_type_id": 4,
                          "amount": 123.45
                        }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transaction_id").exists())
                .andExpect(jsonPath("$.account_id").value(1))
                .andExpect(jsonPath("$.operation_type_id").value(4))
                .andExpect(jsonPath("$.amount").value(123.45))
                .andExpect(jsonPath("$.event_date").exists());
        }

        @Test
        @DisplayName("normal purchase — stored with negative amount → 201")
        void normalPurchase_returns201_negativeAmount() throws Exception {
            when(transactionService.createTransaction(any()))
                .thenReturn(stubResponse(new BigDecimal("-50.00")));

            mockMvc.perform(post("/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "account_id": 1,
                          "operation_type_id": 1,
                          "amount": 50.00
                        }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(-50.0));
        }

        @Test
        @DisplayName("ValidationException from service → 422")
        void validationException_returns422() throws Exception {
            when(transactionService.createTransaction(any()))
                .thenThrow(new ValidationException("account 999 not found"));

            mockMvc.perform(post("/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "account_id": 999,
                          "operation_type_id": 1,
                          "amount": 50.00
                        }
                    """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("account 999 not found"));
        }

        @Test
        @DisplayName("ValidationException from service → 422")
        void missingBody_returns422() throws Exception {
            when(transactionService.createTransaction(any()))
                .thenThrow(new ValidationException("account_id must be a positive integer"));

            mockMvc.perform(post("/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("account_id must be a positive integer"));
        }

        @Test
        @DisplayName("malformed JSON → 422")
        void malformedJson_returns422() throws Exception {
            mockMvc.perform(post("/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("invalid json"))
                .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("service called exactly once per request")
        void serviceCalledOnce() throws Exception {
            when(transactionService.createTransaction(any()))
                .thenReturn(stubResponse(new BigDecimal("100.00")));

            mockMvc.perform(post("/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"account_id": 1, "operation_type_id": 4, "amount": 100.00}
                    """))
                .andExpect(status().isCreated());

            verify(transactionService, times(1)).createTransaction(any());
        }
    }
}
