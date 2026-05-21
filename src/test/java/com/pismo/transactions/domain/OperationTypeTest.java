package com.pismo.transactions.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD: domain tests are written first — they define the contract.
 * Pure unit tests, zero Spring context — fast to run.
 */
@DisplayName("OperationType domain rules")
class OperationTypeTest {

    private OperationType opType(int id) {
        OperationType o = new OperationType();
        o.setOperationTypeId(id);
        o.setDescription("test");
        return o;
    }

    @ParameterizedTest(name = "op type {0} is debit")
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("purchase and withdrawal types are debit (negative amount)")
    void debitTypes_returnTrue(int id) {
        assertThat(opType(id).isDebit()).isTrue();
    }

    @Test
    @DisplayName("credit voucher (type 4) is NOT debit")
    void creditVoucher_returnsFalse() {
        assertThat(opType(4).isDebit()).isFalse();
    }
}
