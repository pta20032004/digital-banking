package com.tony.demo.modules.auth.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank String fullName,
    @NotNull LocalDate dateOfBirth,
    @NotBlank @Size(max = 50) String username,
    @Email String email,
    @NotBlank String password,
    String phoneNumber,
    @NotBlank String identityNumber,
    @NotBlank String transactionPin
) {}
