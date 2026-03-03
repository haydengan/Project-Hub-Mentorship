package com.togetherly.demo.data.auth.request;

import jakarta.validation.constraints.NotEmpty;

/** Sent by the client with the reset token (from email link) + new password. */
public record ResetPasswordRequest(
        @NotEmpty(message = "token can not be empty !") String token,
        @NotEmpty(message = "new password cannot be empty !") String newPassword) {}
