package com.pismo.transactions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class TransactionResponse {
    @JsonProperty("transaction_id")
    private Long transactionId;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("operation_type_id")
    private Integer operationTypeId;

    private BigDecimal amount;

    @JsonProperty("event_date")
    private LocalDateTime eventDate;

    public static TransactionResponse of(Long transactionId, Long accountId,
                                         Integer operationTypeId,
                                         BigDecimal amount,
                                         LocalDateTime eventDate) {
        TransactionResponse r = new TransactionResponse();
        r.transactionId = transactionId;
        r.accountId = accountId;
        r.operationTypeId = operationTypeId;
        r.amount = amount;
        r.eventDate = eventDate;
        return r;
    }
}
