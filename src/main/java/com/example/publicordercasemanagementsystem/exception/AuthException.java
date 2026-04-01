package com.example.publicordercasemanagementsystem.exception;

public class AuthException extends RuntimeException {

    private final int status;
    private final Object data;

    public AuthException(int status, String message) {
        this(status, message, null);
    }

    public AuthException(int status, String message, Object data) {
        super(message);
        this.status = status;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }
}
