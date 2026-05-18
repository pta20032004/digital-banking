package com.tony.demo.modules.transaction.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotBlank(message = "RequestId không được để trống")
    private String requestId;

    @NotBlank(message = "Tài khoản gửi không được để trống")
    private String senderAccountNumber;

    @NotBlank(message = "Tài khoản nhận không được để trống")
    private String receiverAccountNumber;

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "1.0", message = "Số tiền chuyển phải lớn hơn 0")
    private BigDecimal amount;

    private String description;
}
