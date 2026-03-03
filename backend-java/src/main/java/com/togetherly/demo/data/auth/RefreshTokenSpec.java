package com.togetherly.demo.data.auth;

/**
 * The decoded contents (claims) of a refresh token.
 * Simpler than AccessTokenSpec — refresh tokens don't carry user info.
 */
public record RefreshTokenSpec(long exp, String iss, String jti, String type) {}
