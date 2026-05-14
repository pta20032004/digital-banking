package com.tony.demo.core.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ErrorResponse {
    private final Instant timestamp;
    private final String code;     // Ví dụ: "ACC_001"
    private final String message;  // Ví dụ: "Số dư không đủ"
    private final String path;     // API nào bị lỗi (vd: /api/v1/transfer)
}