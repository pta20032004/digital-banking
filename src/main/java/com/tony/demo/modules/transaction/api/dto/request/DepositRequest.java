package com.tony.demo.modules.transaction.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record DepositRequest(
        @NotNull(message = "ID tài khoản không được để trống")
        UUID accountPublicId,

        @NotNull(message = "Số tiền nạp không được để trống")
        @DecimalMin(value = "0.01", message = "Số tiền nạp phải lớn hơn 0")
        BigDecimal amount,

        @NotBlank(message = "Nội dung giao dịch không được để trống")
        String description
) {
}
