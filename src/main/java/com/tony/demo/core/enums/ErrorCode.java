package com.tony.demo.core.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    
    // LỖI HỆ THỐNG (HTTP 500) 
    UNCATEGORIZED_EXCEPTION("SYS_500", "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // LỖI USER (HTTP 400 / 404) 
    USER_NOT_FOUND("USR_001", "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    USER_EXISTED("USR_002", "Người dùng đã tồn tại", HttpStatus.BAD_REQUEST),
    
    // LỖI ACCOUNT & GIAO DỊCH (HTTP 400) 
    ACCOUNT_NOT_FOUND("ACC_001", "Tài khoản không tồn tại", HttpStatus.NOT_FOUND),
    INSUFFICIENT_BALANCE("ACC_002", "Số dư không đủ để thực hiện giao dịch", HttpStatus.BAD_REQUEST),
    ACCOUNT_BLOCKED("ACC_003", "Tài khoản đang bị khóa", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}