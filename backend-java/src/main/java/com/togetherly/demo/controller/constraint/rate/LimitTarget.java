package com.togetherly.demo.controller.constraint.rate;

/**
 * Determines what the rate limiter counts against:
 * - USER: each authenticated user gets their own rate limit bucket
 * - IP: each IP address gets their own bucket (for unauthenticated endpoints)
 */
public enum LimitTarget {
    USER,
    IP
}
