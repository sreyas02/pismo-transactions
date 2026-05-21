package com.pismo.transactions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountResponse {
    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("document_number")
    private String documentNumber;

    public static AccountResponse of(Long accountId, String documentNumber) {
        AccountResponse r = new AccountResponse();
        r.accountId = accountId;
        r.documentNumber = documentNumber;
        return r;
    }
}
