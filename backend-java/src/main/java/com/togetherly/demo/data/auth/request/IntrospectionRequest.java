package com.togetherly.demo.data.auth.request;

import jakarta.validation.constraints.NotEmpty;

/** Sent by the client to inspect/decode an access token. */
public record IntrospectionRequest(
        @NotEmpty(message = "access token cannot be empty !") String token) {}
