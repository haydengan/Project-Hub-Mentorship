package com.togetherly.demo.data.auth.request;

import jakarta.validation.constraints.NotEmpty;

/** Sent by the client to request a password reset email. */
public record ForgetPasswordRequest(
        @NotEmpty(message = "email cannot be empty !") String email) {}
