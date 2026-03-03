package com.togetherly.demo.data.auth.request;

import jakarta.validation.constraints.NotEmpty;

/** Sent by the client to log in. */
public record LoginRequest(
        @NotEmpty(message = "username cannot be empty !") String username,
        @NotEmpty(message = "password cannot be empty !") String password) {}
