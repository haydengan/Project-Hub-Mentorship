package com.togetherly.demo.data.auth;

/**
 * Sent to the client after login/refresh. Contains the JWT strings.
 * Example JSON: { "access_token": "eyJ...", "refresh_token": "eyJ..." }
 *
 * Factory method from(TokenPair) extracts the string tokens from the entities.
 */
public record TokenResponse(String access_token, String refresh_token) {

    public static TokenResponse from(TokenPair tokenPair) {
        return new TokenResponse(
                tokenPair.accessToken().getToken(),
                tokenPair.refreshToken().getToken());
    }
}
