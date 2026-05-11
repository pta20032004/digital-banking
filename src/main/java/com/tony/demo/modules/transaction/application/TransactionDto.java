package com.tony.demo.modules.transaction.application;

import java.math.BigDecimal;

import lombok.Data;

public class TransactionDto {

    @Data
    public static class TransactionRequest {
        private String requestId; // For idempotency
        private String sourceAccountNumber;
        private String targetAccountNumber; // Nullable for deposit/withdraw
        private BigDecimal amount;
        private String description;
    }

    @Data
    public static class TransactionResponse {
        private Long transactionId;
        private String status;
        private BigDecimal amount;
        private String transactionType;
        private String message;
    }
}
