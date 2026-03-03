package com.togetherly.demo.exception;

/**
 * Thrown when a controller-level constraint is violated
 * (e.g., user doesn't have the required role, or rate limit exceeded).
 * Carries an HTTP status code and message to return to the client.
 */
public class ControllerConstraintViolation extends Exception {
    private final int rejectStatus;
    private final String rejectMessage;

    public ControllerConstraintViolation(int rejectStatus, String rejectMessage) {
        this.rejectStatus = rejectStatus;
        this.rejectMessage = rejectMessage;
    }

    public int getRejectStatus() {
        return rejectStatus;
    }

    public String getRejectMessage() {
        return rejectMessage;
    }
}
