package com.example.JourneyHub.exception;

public class InvalidPassengerDataException extends RuntimeException {
    private final String errorCode;

    public InvalidPassengerDataException(String message) {
        super(message);
        this.errorCode = "INVALID_PASSENGER_DATA";
    }

    public String getErrorCode() {
        return errorCode;
    }
}