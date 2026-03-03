package com.togetherly.demo.data.auth;

import com.togetherly.demo.model.auth.AccessToken;
import com.togetherly.demo.model.auth.RefreshToken;

/**
 * Holds an AccessToken + RefreshToken pair.
 * Used internally between Service and Controller layers.
 * Not sent directly to the client — TokenResponse converts this to JSON strings.
 */
public record TokenPair(AccessToken accessToken, RefreshToken refreshToken) {}
