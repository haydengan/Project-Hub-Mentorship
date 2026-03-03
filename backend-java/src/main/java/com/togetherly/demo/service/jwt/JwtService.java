package com.togetherly.demo.service.jwt;

import com.togetherly.demo.data.auth.AccessTokenSpec;
import com.togetherly.demo.data.auth.TokenPair;
import com.togetherly.demo.data.auth.UserDetail;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.InvalidTokenException;
import com.togetherly.demo.exception.UserDoesNotExist;
import com.togetherly.demo.model.auth.AccessToken;
import java.util.List;

/**
 * Manages the full lifecycle of JWT access and refresh tokens.
 *
 * This is the core authentication service. It handles:
 * - Issuing new token pairs (login)
 * - Refreshing expired access tokens
 * - Revoking tokens (logout, password change, role change)
 * - Introspecting tokens (decoding claims)
 * - Blacklisting revoked tokens in Redis
 * - Cleaning up expired tokens
 *
 * HAND-WRITTEN interface — defines the contract for JWT operations.
 */
public interface JwtService {

    /** Issue access + refresh token pair for an authenticated user. */
    TokenPair issueTokens(UserDetail userDetail) throws UserDoesNotExist;

    /** Exchange a refresh token for a new token pair. Old pair is revoked. */
    TokenPair refreshTokens(String refreshPlainToken) throws InvalidTokenException, InvalidOperation;

    /** Extract UserDetail from an access token (used by JWT filter). */
    UserDetail getUserDetailFromAccessToken(String token) throws InvalidTokenException;

    /** Decode and validate an access token, returning its claims. */
    AccessTokenSpec introspect(String token) throws InvalidTokenException;

    /** Revoke an access token by its ID string. */
    void revokeAccessToken(String id);

    /** Revoke a single access token entity. */
    void revokeAccessToken(AccessToken accessToken);

    /** Revoke multiple access tokens (e.g., logout from all devices). */
    void revokeAccessToken(List<AccessToken> accessTokens);

    /** Add an access token to the Redis blacklist. */
    void addAccessTokenToBlackList(AccessToken accessToken);

    /** Check if an access token is in the Redis blacklist. */
    boolean isAccessTokenInBlackList(String accessPlainToken);

    /** Delete all expired refresh tokens and their cascaded access tokens. */
    void deleteExpiredTokens();
}
