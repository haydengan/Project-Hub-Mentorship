package com.togetherly.demo.data.auth.request;

import jakarta.validation.constraints.NotEmpty;

/** Sent by the client to exchange a refresh token for a new token pair. */
public record RefreshRequest(
        @NotEmpty(message = "refresh token cannot be empty !") String token) {}
