package com.togetherly.demo.repository.verification;

import com.togetherly.demo.model.auth.User;
import com.togetherly.demo.model.auth.VerifyToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for VerifyToken entities (password reset tokens).
 *
 * CUSTOM QUERY METHODS:
 *
 *   deleteByExpireAtLessThan(now)
 *   → Cleanup: removes all expired password reset tokens.
 *
 *   getByTokenAndExpireAtGreaterThan(tokenString, now)
 *   → Finds a valid (non-expired) reset token by its string value.
 *     Used when the user clicks the reset link in their email.
 *
 *   getByUser(user)
 *   → Checks if a user already has a pending reset token.
 *     Prevents sending multiple reset emails.
 *
 *   deleteByUser(user)
 *   → Removes a user's reset token (e.g., after password is successfully reset).
 */
public interface VerifyTokenRepository extends JpaRepository<VerifyToken, UUID> {

    void deleteByExpireAtLessThan(Instant dateTime);

    Optional<VerifyToken> getByTokenAndExpireAtGreaterThan(String token, Instant dateTime);

    Optional<VerifyToken> getByUser(User user);

    void deleteByUser(User user);
}
