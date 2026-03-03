package com.togetherly.demo.exception;

/**
 * Thrown when a user attempts an operation that isn't allowed.
 *
 * Examples:
 * - Too many failed login attempts (account temporarily locked)
 * - Trying to activate an already-active account
 *
 * This is a CHECKED exception (extends Exception, not RuntimeException),
 * meaning callers MUST handle it with try/catch or declare "throws".
 *
 * HAND-WRITTEN. You create custom exceptions for your domain-specific errors.
 */
public class InvalidOperation extends Exception {
    public InvalidOperation(String message) {
        super(message);
    }
}
