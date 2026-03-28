package com.togetherly.demo.exception;

/** Thrown when a requested resource does not exist (e.g., group not found). */
public class NotFound extends Exception {
    public NotFound(String message) {
        super(message);
    }
}
