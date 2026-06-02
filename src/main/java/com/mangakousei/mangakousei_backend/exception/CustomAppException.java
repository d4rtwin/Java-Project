package com.mangakousei.mangakousei_backend.exception;

public class CustomAppException extends RuntimeException {
    public CustomAppException(String message) {
        super(message);
    }
}