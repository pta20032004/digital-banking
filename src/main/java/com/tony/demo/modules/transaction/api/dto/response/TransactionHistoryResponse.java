// src/main/java/com/tony/demo/modules/transaction/api/dto/response/TransactionHistoryResponse.java
package com.tony.demo.modules.transaction.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionHistoryResponse(
        UUID transactionId,
        String transactionType,
        BigDecimal amount,
        BigDecimal fee,
        String status,
        LocalDateTime transactionTime,
        String description
) {
}
