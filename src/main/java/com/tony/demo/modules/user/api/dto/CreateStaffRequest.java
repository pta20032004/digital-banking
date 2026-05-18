package com.tony.demo.modules.user.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateStaffRequest(
    @NotBlank @Size(max = 50) String employeeCode,
    @NotBlank @Size(max = 255) String fullName,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6, max = 255) String password,
    @NotNull Long roleId,
    @Size(max = 100) String department,
    @Size(max = 50) String branchCode
) {}
