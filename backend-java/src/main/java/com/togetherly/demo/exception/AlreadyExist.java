package com.togetherly.demo.exception;

/** Thrown when trying to create something that already exists (e.g., duplicate username/email). */
public class AlreadyExist extends Exception {
    public AlreadyExist(String message) {
        super(message);
    }
}
