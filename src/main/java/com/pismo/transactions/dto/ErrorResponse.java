package com.pismo.transactions.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ErrorResponse {
    private String error;

    public static ErrorResponse of(String message) {
        ErrorResponse e = new ErrorResponse();
        e.error = message;
        return e;
    }
}
