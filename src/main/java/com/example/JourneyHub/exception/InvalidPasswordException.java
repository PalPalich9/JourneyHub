package com.example.JourneyHub.exception;

public class InvalidPasswordException extends RuntimeException {
    private final String errorCode;

    public InvalidPasswordException(String message) {
        super(message);
        this.errorCode = "INVALID_PASSWORD";
    }

    public String getErrorCode() {
        return errorCode;
    }
}