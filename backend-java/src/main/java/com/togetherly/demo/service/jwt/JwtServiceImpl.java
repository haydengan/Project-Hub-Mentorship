package com.togetherly.demo.service.jwt;

import com.togetherly.demo.config.JwtConfig;
import com.togetherly.demo.data.auth.AccessTokenSpec;
import com.togetherly.demo.data.auth.TokenPair;
import com.togetherly.demo.data.auth.UserDetail;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.InvalidTokenException;
import com.togetherly.demo.exception.UserDoesNotExist;
import com.togetherly.demo.model.auth.AccessToken;
import com.togetherly.demo.model.auth.RefreshToken;
import com.togetherly.demo.model.auth.User;
import com.togetherly.demo.repository.jwt.AccessTokenRepository;
import com.togetherly.demo.repository.jwt.RefreshTokenRepository;
import com.togetherly.demo.repository.user.UserRepository;
import com.togetherly.demo.service.redis.RedisService;
import com.togetherly.demo.utils.JwtUtil;
import io.jsonwebtoken.JwtException;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JWT service implementation — the most complex service in the auth system.
 *
 * KEY CHANGE FROM ORIGINAL:
 * The original code used `new AccessToken(jwtConfig, user)` — the entity constructor
 * generated the JWT string. We removed that constructor because business logic
 * (JWT generation) doesn't belong in entities. Instead, this service:
 * 1. Creates a blank AccessToken/RefreshToken entity
 * 2. Calls JwtUtil to generate the JWT string
 * 3. Sets the token string + expiry on the entity
 * 4. Links them together
 * 5. Persists via repository
 *
 * TOKEN REVOCATION STRATEGY:
 * - Delete the token from the DB (so it can't be refreshed)
 * - Add the token string to a Redis blacklist with TTL = token lifetime
 * - The JWT filter checks the blacklist before accepting any token
 * - After TTL expires, the token would be invalid anyway (JWT expiry)
 *
 * HAND-WRITTEN — this is core business logic that ties together multiple components.
 */
@Service
public class JwtServiceImpl implements JwtService {
    @Autowired private JwtConfig jwtConfig;
    @Autowired private UserRepository userRepository;
    @Autowired private AccessTokenRepository accessTokenRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private RedisService redisService;

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    /**
     * Build an AccessToken entity with a generated JWT string.
     *
     * WHAT HAPPENS HERE (step by step):
     * 1. Create a new AccessToken — its ID is auto-generated (UUIDv7) in the field initializer
     * 2. Calculate expiration time (now + configured lifetime in seconds)
     * 3. Call JwtUtil.generateAccessToken() to create the signed JWT string
     *    - The JWT contains: jti (token ID), issuer, expiration, user claims
     * 4. Set the token string and expiry on the entity
     * 5. Link the token to the user
     */
    private AccessToken buildAccessToken(User user) {
        AccessToken accessToken = new AccessToken();

        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, jwtConfig.getAccessTokenLifetimeSec());

        String jwt = JwtUtil.generateAccessToken(
                jwtConfig.getPrivateKey(),
                accessToken.getId().toString(),
                jwtConfig.getIssuer(),
                user,
                exp);

        accessToken.setToken(jwt);
        accessToken.setExpireAt(exp.toInstant());
        accessToken.setUser(user);
        return accessToken;
    }

    /**
     * Build a RefreshToken entity linked to an AccessToken.
     * Same pattern as buildAccessToken, but simpler (no user claims in refresh tokens).
     */
    private RefreshToken buildRefreshToken(AccessToken accessToken) {
        RefreshToken refreshToken = new RefreshToken();

        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, jwtConfig.getRefreshTokenLifetimeSec());

        String jwt = JwtUtil.generateRefreshToken(
                jwtConfig.getPrivateKey(),
                refreshToken.getId().toString(),
                jwtConfig.getIssuer(),
                exp);

        refreshToken.setToken(jwt);
        refreshToken.setExpireAt(exp.toInstant());
        refreshToken.setUser(accessToken.getUser());

        // Link the two tokens bidirectionally
        accessToken.setRefreshToken(refreshToken);
        refreshToken.setAccessToken(accessToken);

        return refreshToken;
    }

    /**
     * Create a complete token pair (access + refresh) and persist it.
     * Saving the RefreshToken cascades to save the AccessToken too
     * (because AccessToken has CascadeType.ALL on its RefreshToken relationship).
     */
    private TokenPair createTokens(User user) {
        AccessToken accessToken = buildAccessToken(user);
        RefreshToken refreshToken = buildRefreshToken(accessToken);
        refreshTokenRepository.save(refreshToken);
        return new TokenPair(accessToken, refreshToken);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TokenPair issueTokens(UserDetail userDetail) throws UserDoesNotExist {
        User user = userRepository
                .getByUserName(userDetail.getUsername())
                .orElseThrow(() -> new UserDoesNotExist("user is not exist !"));

        TokenPair tokenPair = createTokens(user);

        // Update authAt to prevent concurrent role/password/active changes
        // from using stale tokens. The @Version field will catch conflicts.
        user.setAuthAt(Instant.now());
        userRepository.save(user);

        return tokenPair;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TokenPair refreshTokens(String refreshPlainToken)
            throws InvalidTokenException, InvalidOperation {
        // Step 1: Parse the JWT to extract the token ID (jti)
        String jti;
        try {
            jti = JwtUtil.parseRefreshToken(jwtConfig.getPublicKey(), refreshPlainToken).jti();
        } catch (JwtException e) {
            throw new InvalidTokenException("invalid refresh token !");
        }

        // Step 2: Look up the token in DB (must exist AND not be expired)
        RefreshToken refreshToken = refreshTokenRepository
                .getByIdAndExpireAtGreaterThan(UUID.fromString(jti), Instant.now())
                .orElseThrow(() -> new InvalidTokenException("invalid refresh token !"));

        User user = refreshToken.getUser();
        if (!user.isActive()) {
            throw new InvalidOperation("cannot refresh tokens for inactive user !");
        }

        // Step 3: Revoke the old access token (cascades to delete old refresh token)
        revokeAccessToken(refreshToken.getAccessToken());

        // Step 4: Issue new token pair
        AccessToken newAccessToken = buildAccessToken(user);
        RefreshToken newRefreshToken = buildRefreshToken(newAccessToken);
        refreshTokenRepository.save(newRefreshToken);

        // Step 5: Bump authAt for optimistic lock protection
        user.setAuthAt(Instant.now());
        userRepository.save(user);

        return new TokenPair(newAccessToken, newRefreshToken);
    }

    @Override
    public UserDetail getUserDetailFromAccessToken(String token) throws InvalidTokenException {
        return JwtUtil.extractUserDetailFromAccessToken(jwtConfig.getPublicKey(), token);
    }

    @Override
    public AccessTokenSpec introspect(String token) throws InvalidTokenException {
        if (isAccessTokenInBlackList(token)) {
            throw new InvalidTokenException("invalid token !");
        }
        return JwtUtil.parseAccessToken(jwtConfig.getPublicKey(), token);
    }

    @Override
    public void revokeAccessToken(String id) {
        AccessToken accessToken = accessTokenRepository
                .getByIdAndExpireAtGreaterThan(UUID.fromString(id), Instant.now())
                .orElse(null);
        if (accessToken == null) return;
        accessTokenRepository.delete(accessToken); // refresh token cascades
        addAccessTokenToBlackList(accessToken);
    }

    @Override
    public void revokeAccessToken(AccessToken accessToken) {
        accessTokenRepository.delete(accessToken); // refresh token cascades
        addAccessTokenToBlackList(accessToken);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revokeAccessToken(List<AccessToken> accessTokens) {
        accessTokenRepository.deleteAll(accessTokens); // refresh tokens cascade
        accessTokens.forEach(this::addAccessTokenToBlackList);
    }

    @Override
    public void addAccessTokenToBlackList(AccessToken accessToken) {
        // Store in Redis with TTL = access token lifetime
        // After TTL, the JWT would be expired anyway, so no need to keep the blacklist entry
        redisService.set(
                "revoked_access_token:{" + accessToken.getToken() + "}",
                "",
                Duration.ofSeconds(jwtConfig.getAccessTokenLifetimeSec()));
    }

    @Override
    public boolean isAccessTokenInBlackList(String accessPlainToken) {
        return redisService.has("revoked_access_token:{" + accessPlainToken + "}");
    }

    @Transactional
    @Override
    public void deleteExpiredTokens() {
        logger.info("delete expired tokens");
        // Deleting expired refresh tokens cascades to delete their access tokens
        refreshTokenRepository.deleteAllByExpireAtLessThan(Instant.now());
    }
}
