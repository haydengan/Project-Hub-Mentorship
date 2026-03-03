package com.togetherly.demo.data.auth;

/**
 * The decoded contents (claims) of an access token.
 * Returned by the /introspect endpoint so clients can inspect a token.
 *
 * Fields map to JWT claims:
 *   id       → user ID
 *   username → user's name
 *   role     → user's role (ADMIN, STAFF, NORMAL)
 *   isActive → whether the account is active
 *   exp      → expiration (seconds since epoch)
 *   iss      → issuer
 *   jti      → JWT ID (unique token identifier)
 *   type     → "access_token"
 */
public record AccessTokenSpec(
        String id,
        String username,
        String role,
        Boolean isActive,
        long exp,
        String iss,
        String jti,
        String type) {}
