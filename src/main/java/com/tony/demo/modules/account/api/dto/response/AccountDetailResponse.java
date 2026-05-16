// src/main/java/com/tony/demo/modules/account/api/dto/response/AccountDetailResponse.java
package com.tony.demo.modules.account.api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountDetailResponse(
        UUID publicId,
        String accountNumber,
        BigDecimal balance,
        String status,
        String currency
) {
}
