package com.togetherly.demo.data.auth.request;

import jakarta.validation.constraints.NotEmpty;

/** Sent by a logged-in user to change their password. */
public record ChangePasswordRequest(
        @NotEmpty(message = "password cannot be empty !") String oldPassword,
        @NotEmpty(message = "new password cannot be empty !") String newPassword) {}
