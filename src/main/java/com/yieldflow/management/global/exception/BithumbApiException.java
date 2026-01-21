package com.yieldflow.management.global.exception;

public class BithumbApiException extends RuntimeException {

    private final String errorCode;

    public BithumbApiException(String message) {
        super(message);
        this.errorCode = null;
    }

    public BithumbApiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BithumbApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
