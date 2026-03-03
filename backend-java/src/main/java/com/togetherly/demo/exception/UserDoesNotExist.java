package com.togetherly.demo.exception;

/** Thrown when a requested user cannot be found in the database. */
public class UserDoesNotExist extends Exception {
    public UserDoesNotExist(String message) {
        super(message);
    }
}
