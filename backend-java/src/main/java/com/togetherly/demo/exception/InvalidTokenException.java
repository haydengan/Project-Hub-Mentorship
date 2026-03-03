package com.togetherly.demo.exception;

/** Thrown when a JWT token is invalid, expired, or malformed. */
public class InvalidTokenException extends Exception {
    public InvalidTokenException(String message) {
        super(message);
    }
}
