package com.bank.banking_api.exception;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String message,
        Instant timestamp) {
}