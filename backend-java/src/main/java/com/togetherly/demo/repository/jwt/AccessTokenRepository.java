package com.togetherly.demo.repository.jwt;

import com.togetherly.demo.model.auth.AccessToken;
import com.togetherly.demo.model.auth.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for AccessToken entities.
 *
 * CUSTOM QUERY METHODS:
 *
 *   getByIdAndExpireAtGreaterThan(uuid, now)
 *   → SELECT * FROM access_token WHERE id = ? AND expire_at > ?
 *   → Finds a token by ID, but ONLY if it hasn't expired yet.
 *     "GreaterThan" means expire_at > the given Instant.
 *     This is how token validation works: find token AND check it's not expired,
 *     all in one query.
 *
 *   getByUser(user)
 *   → SELECT * FROM access_token WHERE user_id = ?
 *   → Finds all active tokens for a user (e.g., to list sessions or revoke all).
 */
public interface AccessTokenRepository extends JpaRepository<AccessToken, UUID> {

    Optional<AccessToken> getByIdAndExpireAtGreaterThan(UUID id, Instant dateTime);

    List<AccessToken> getByUser(User user);
}
