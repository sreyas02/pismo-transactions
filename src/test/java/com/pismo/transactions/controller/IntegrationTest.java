package com.pismo.transactions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full end-to-end integration tests.
 *
 * @SpringBootTest loads the entire application context with real H2 DB.
 * @Transactional rolls back DB changes after each test method (isolated H2 state).
 *
 * These tests verify the full request → controller → service → repository → DB flow.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("End-to-End Integration Tests")
class IntegrationTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // ── Helpers ──────────────────────────────────────────────────────────────

    private long createAccount(String documentNumber) throws Exception {
        MvcResult result = mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    Map.of("document_number", documentNumber)
                )))
            .andExpect(status().isCreated())
            .andReturn();

        Map<?, ?> resp = objectMapper.readValue(
            result.getResponse().getContentAsString(), Map.class
        );
        return ((Number) resp.get("account_id")).longValue();
    }

    // ── Account E2E ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /accounts")
    class CreateAccountE2E {

        @Test
        @DisplayName("creates account and returns 201 with generated account_id")
        void createAccount_success() throws Exception {
            mockMvc.perform(post("/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"document_number": "12345678900"}
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.account_id").isNumber())
                .andExpect(jsonPath("$.document_number").value("12345678900"));
        }

        @Test
        @DisplayName("duplicate document_number → 422")
        void duplicateDocument_returns422() throws Exception {
            createAccount("12345678900");

            mockMvc.perform(post("/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"document_number": "12345678900"}
                    """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("missing document_number → 422")
        void missingDocument_returns422() throws Exception {
            mockMvc.perform(post("/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isUnprocessableEntity());
        }
    }

    @Nested
    @DisplayName("GET /accounts/:accountId")
    class GetAccountE2E {

        @Test
        @DisplayName("retrieve created account → 200 with correct data")
        void getAccount_success() throws Exception {
            long accountId = createAccount("99988877700");

            mockMvc.perform(get("/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value(accountId))
                .andExpect(jsonPath("$.document_number").value("99988877700"));
        }

        @Test
        @DisplayName("non-existent account → 404")
        void notFound_returns404() throws Exception {
            mockMvc.perform(get("/accounts/99999"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("non-integer accountId → 400")
        void invalidId_returns400() throws Exception {
            mockMvc.perform(get("/accounts/abc"))
                .andExpect(status().isBadRequest());
        }
    }

    // ── Transaction E2E ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /transactions")
    class CreateTransactionE2E {

        @Test
        @DisplayName("credit voucher → 201 with positive amount")
        void creditVoucher_returns201_positiveAmount() throws Exception {
            long accountId = createAccount("11122233300");

            MvcResult result = mockMvc.perform(post("/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                        "account_id", accountId,
                        "operation_type_id", 4,
                        "amount", 123.45
                    ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transaction_id").isNumber())
                .andExpect(jsonPath("$.account_id").value(accountId))
                .andExpect(jsonPath("$.operation_type_id").value(4))
                .andExpect(jsonPath("$.event_date").exists())
                .andReturn();

            Map<?, ?> resp = objectMapper.readValue(
                result.getResponse().getContentAsString(), Map.class
            );
            double amount = ((Number) resp.get("amount")).doubleValue();
            assertThat(amount).isGreaterThan(0);
        }

        @Test
        @DisplayName("normal purchase (op 1) → amount stored as negative")
        void normalPurchase_amountNegative() throws Exception {
            long accountId = createAccount("44455566600");

            MvcResult result = mockMvc.perform(post("/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                        "account_id", accountId,
                        "operation_type_id", 1,
                        "amount", 50.0
                    ))))
                .andExpect(status().isCreated())
                .andReturn();

            Map<?, ?> resp = objectMapper.readValue(
                result.getResponse().getContentAsString(), Map.class
            );
            double amount = ((Number) resp.get("amount")).doubleValue();
            assertThat(amount).isLessThan(0);
        }

        @Test
        @DisplayName("withdrawal (op 3) → amount stored as negative")
        void withdrawal_amountNegative() throws Exception {
            long accountId = createAccount("77788899900");

            MvcResult result = mockMvc.perform(post("/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                        "account_id", accountId,
                        "operation_type_id", 3,
                        "amount", 200.0
                    ))))
                .andExpect(status().isCreated())
                .andReturn();

            Map<?, ?> resp = objectMapper.readValue(
                result.getResponse().getContentAsString(), Map.class
            );
            double amount = ((Number) resp.get("amount")).doubleValue();
            assertThat(amount).isLessThan(0);
        }

        @Test
        @DisplayName("account not found → 422")
        void accountNotFound_returns422() throws Exception {
            mockMvc.perform(post("/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                        "account_id", 99999,
                        "operation_type_id", 1,
                        "amount", 50.0
                    ))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("invalid operation_type_id → 422")
        void invalidOpType_returns422() throws Exception {
            long accountId = createAccount("55544433300");

            mockMvc.perform(post("/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                        "account_id", accountId,
                        "operation_type_id", 99,
                        "amount", 50.0
                    ))))
                .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("zero amount → 422")
        void zeroAmount_returns422() throws Exception {
            long accountId = createAccount("66677788800");

            mockMvc.perform(post("/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                        "account_id", accountId,
                        "operation_type_id", 1,
                        "amount", 0
                    ))))
                .andExpect(status().isUnprocessableEntity());
        }
    }
}
