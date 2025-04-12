package com.example.JourneyHub.exception;

public class DuplicateEmailException extends RuntimeException {
    private final String errorCode;

    public DuplicateEmailException(String message) {
        super(message);
        this.errorCode = "DUPLICATE_EMAIL";
    }

    public String getErrorCode() {
        return errorCode;
    }
}