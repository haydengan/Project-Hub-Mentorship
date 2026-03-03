package com.togetherly.demo.data.auth.request;

import jakarta.validation.constraints.NotEmpty;

/** Sent by the client to request a verification code be emailed. */
public record IssueVerificationCodeRequest(
        @NotEmpty(message = "email cannot be empty !") String email) {}
