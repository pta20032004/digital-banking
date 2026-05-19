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
    ROLE_NOT_FOUND("USR_003", "Không tìm thấy role", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // LỖI QUYỀN TRUY CẬP & XÁC THỰC (HTTP 401/403)
    ACCESS_DENIED("AUTH_000", "Bạn không có quyền truy cập", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS("AUTH_001", "Tên đăng nhập hoặc mật khẩu không chính xác", HttpStatus.UNAUTHORIZED),
    INVALID_PIN("AUTH_002", "Mã PIN không chính xác", HttpStatus.BAD_REQUEST),
    PIN_NOT_SET("AUTH_003", "Bạn chưa thiết lập mã PIN", HttpStatus.BAD_REQUEST),
    
    // LỖI ACCOUNT & GIAO DỊCH (HTTP 400) 

    ACCOUNT_NOT_FOUND("ACC_001", "Tài khoản không tồn tại", HttpStatus.NOT_FOUND),
    INSUFFICIENT_BALANCE("ACC_002", "Số dư không đủ để thực hiện giao dịch", HttpStatus.BAD_REQUEST),
    ACCOUNT_BLOCKED("ACC_003", "Tài khoản đang bị khóa", HttpStatus.FORBIDDEN),

    // LỖI GIAO DỊCH (HTTP 4xx / 429)
    TOO_MANY_REQUESTS("TXN_001", "Quá nhiều yêu cầu, vui lòng thử lại sau", HttpStatus.TOO_MANY_REQUESTS),
    DUPLICATE_TRANSACTION("TXN_002", "Giao dịch đang được xử lý hoặc đã bị trùng lặp", HttpStatus.CONFLICT),
    INVALID_REQUEST("TXN_003", "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT("TXN_004", "Số tiền giao dịch không hợp lệ", HttpStatus.BAD_REQUEST),
    
    // LỖI NOTIFICATION
    NOTIFICATION_NOT_FOUND("NOTIF_001", "Không tìm thấy thông báo", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}