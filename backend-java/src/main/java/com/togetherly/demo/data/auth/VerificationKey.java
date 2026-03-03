package com.togetherly.demo.data.auth;

/**
 * Returned after requesting a verification code.
 * The client must send this key + the code (from email) together to complete verification.
 */
public record VerificationKey(String key) {}
