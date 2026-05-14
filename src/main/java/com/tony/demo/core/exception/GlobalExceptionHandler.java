package com.tony.demo.core.exception;

import com.tony.demo.core.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
// Thằng này giống như bảo vệ đứng ở cửa ra vào của Controller. Lỗi nào văng ra từ Service mà Controller không bọc try-catch, bảo vệ sẽ tóm lại và gói thành ErrorResponse.
@Slf4j // Dùng để ghi log ra console/file
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. TÓM CÁC LỖI NGHIỆP VỤ (AppException)
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ErrorResponse> handlingAppException(AppException exception, HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    // 2. TÓM TẤT CẢ CÁC LỖI CÒN LẠI (NullPointer, SQL Exception...)
    // Để không bao giờ bị lộ lỗi DB hay code ra cho user
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handlingRuntimeException(Exception exception, HttpServletRequest request) {
        // Log lại chi tiết lỗi 
        log.error("Lỗi hệ thống nghiêm trọng: ", exception);

        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }
}