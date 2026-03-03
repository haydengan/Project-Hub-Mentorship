package com.togetherly.demo.data.auth;

import jakarta.validation.constraints.NotEmpty;

/**
 * Sent by the client during registration: the verification key + code from their email.
 * Both must be non-empty.
 *
 * NOTE: The original used a custom @UUID validator on the key field.
 * We'll add that in the validation package phase.
 */
public record VerificationPair(
        @NotEmpty(message = "verification key cannot be empty !") String key,
        @NotEmpty(message = "verification code cannot be empty !") String code) {}
