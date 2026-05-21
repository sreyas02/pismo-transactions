package com.pismo.transactions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateAccountRequest {
    @JsonProperty("document_number")
    private String documentNumber;
}
