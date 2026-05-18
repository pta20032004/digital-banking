package com.tony.demo.modules.user.api.dto;

import java.time.Instant;
import java.util.UUID;

public record StaffDTO(
    UUID publicId,
    String employeeCode,
    String fullName,
    String email,
    String department,
    String branchCode,
    Boolean isEnabled,
    Boolean isLocked,
    Integer failedAttempt,
    Instant lockTime,
    String roleCode,
    Instant createdAt,
    Instant updatedAt
) {}
