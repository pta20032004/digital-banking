package com.tony.demo.core.exception;

import com.tony.demo.core.enums.ErrorCode;
import lombok.Getter;
//cha của các ex khác
@Getter
public class AppException extends RuntimeException {
    
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}