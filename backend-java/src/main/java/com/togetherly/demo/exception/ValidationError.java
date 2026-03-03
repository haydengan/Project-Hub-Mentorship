package com.togetherly.demo.exception;

/** Thrown for business-level validation failures (extends IllegalArgumentException → unchecked). */
public class ValidationError extends IllegalArgumentException {
    public ValidationError(String msg) {
        super(msg);
    }
}
