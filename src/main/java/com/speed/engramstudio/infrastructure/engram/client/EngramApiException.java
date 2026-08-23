package com.speed.engramstudio.infrastructure.engram.client;

public class EngramApiException extends RuntimeException {
    
    private final int statusCode;
    
    public EngramApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public EngramApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public boolean isNotFound() {
        return statusCode == 404;
    }
    
    public boolean isUnauthorized() {
        return statusCode == 401;
    }
    
    public boolean isServerError() {
        return statusCode >= 500;
    }
}