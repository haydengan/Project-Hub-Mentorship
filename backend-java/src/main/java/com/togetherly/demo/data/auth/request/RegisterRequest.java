package com.togetherly.demo.data.auth.request;

import com.togetherly.demo.data.auth.VerificationPair;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Sent by the client to register a new account.
 *
 * @Valid on verification triggers validation of the nested VerificationPair record.
 *
 * NOTE: The original used custom @Username, @Email, @Password validators.
 * We'll add those in the validation package phase. For now, @NotEmpty ensures
 * the fields aren't blank.
 */
public record RegisterRequest(
        @NotEmpty(message = "username cannot be empty !") String username,
        @NotEmpty(message = "email cannot be empty !") String email,
        @NotEmpty(message = "password cannot be empty !") String password,
        @Valid @NotNull(message = "verification cannot be empty !") VerificationPair verification) {}
