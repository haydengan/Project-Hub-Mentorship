package com.togetherly.demo.utils;

import com.togetherly.demo.data.auth.AccessTokenSpec;
import com.togetherly.demo.data.auth.RefreshTokenSpec;
import com.togetherly.demo.data.auth.UserDetail;
import com.togetherly.demo.exception.InvalidTokenException;
import com.togetherly.demo.model.auth.Role;
import com.togetherly.demo.model.auth.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;

/**
 * JWT creation and parsing utilities.
 *
 * - Jwts.builder().claim(), Jwts.parser().verifyWith()
 *
 * Also updated parsing: original used reflection to fill AccessTokenSpec fields
 * (which breaks with Java records since records are immutable).
 * Now we extract claims explicitly and pass them to the record constructor.
 */
public class JwtUtil {

    private JwtUtil() {}

    /** Create a signed JWT access token containing user claims. */
    public static String generateAccessToken(
            RSAPrivateKey key, String jti, String issuer, User user, Calendar exp) {
        return Jwts.builder()
                .id(jti)
                .issuer(issuer)
                .expiration(exp.getTime())
                .claim("type", "access_token")
                .claim("id", user.getId().toString())
                .claim("username", user.getUserName())
                .claim("role", user.getRole().toString())
                .claim("isActive", user.isActive())
                .signWith(key)
                .compact();
    }

    /** Create a signed JWT refresh token (no user claims, just metadata). */
    public static String generateRefreshToken(
            RSAPrivateKey key, String jti, String issuer, Calendar exp) {
        return Jwts.builder()
                .id(jti)
                .issuer(issuer)
                .expiration(exp.getTime())
                .claim("type", "refresh_token")
                .signWith(key)
                .compact();
    }

    /** Parse an access token and extract a UserDetail (used by JWT filter). */
    public static UserDetail extractUserDetailFromAccessToken(RSAPublicKey publicKey, String token)
            throws InvalidTokenException {
        try {
            AccessTokenSpec data = parseAccessToken(publicKey, token);
            if (!"access_token".equals(data.type())) {
                throw new InvalidTokenException("invalid token !");
            }
            return new UserDetail(
                    data.id(),
                    data.username(),
                    data.isActive(),
                    Role.valueOf(data.role()),
                    token,
                    data.jti());
        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidTokenException("invalid token !");
        }
    }

    /** Parse and validate an access token, returning its decoded claims as a record. */
    public static AccessTokenSpec parseAccessToken(RSAPublicKey key, String token)
            throws JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new AccessTokenSpec(
                getRequiredClaim(claims, "id", String.class),
                getRequiredClaim(claims, "username", String.class),
                getRequiredClaim(claims, "role", String.class),
                getRequiredClaim(claims, "isActive", Boolean.class),
                claims.getExpiration().getTime() / 1000,
                claims.getIssuer(),
                claims.getId(),
                getRequiredClaim(claims, "type", String.class));
    }

    /** Parse and validate a refresh token. */
    public static RefreshTokenSpec parseRefreshToken(RSAPublicKey key, String token)
            throws JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new RefreshTokenSpec(
                claims.getExpiration().getTime() / 1000,
                claims.getIssuer(),
                claims.getId(),
                getRequiredClaim(claims, "type", String.class));
    }

    private static <T> T getRequiredClaim(Claims claims, String name, Class<T> type) {
        T value = claims.get(name, type);
        if (value == null) {
            throw new JwtException("Missing field %s in token !".formatted(name));
        }
        return value;
    }
}
