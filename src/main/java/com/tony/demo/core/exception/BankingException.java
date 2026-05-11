package com.tony.demo.core.exception;

import lombok.Getter;

@Getter
public class BankingException extends RuntimeException {
    private final ErrorCode errorCode;

    public BankingException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
