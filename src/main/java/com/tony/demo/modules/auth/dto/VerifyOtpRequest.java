package com.tony.demo.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotBlank(message = "Temp Token không được để trống")
    private String tempToken;

    @NotBlank(message = "Mã OTP không được để trống")
    private String otp;
}
