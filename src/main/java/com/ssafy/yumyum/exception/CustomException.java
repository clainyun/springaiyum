package com.ssafy.yumyum.exception;

public class CustomException extends RuntimeException {

    private final int statusCode;

    public CustomException(String message) {
        this(400, message);
    }

    public CustomException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}