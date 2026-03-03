package com.togetherly.demo.model.auth;

import java.time.Instant;

import com.togetherly.demo.config.LoginConfig;
import com.togetherly.demo.exception.InvalidOperation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Tracks failed login attempts for rate limiting. Embedded in the User entity.
 *
 * @Embeddable — This is NOT a separate table. Its fields (attempts, lastAttempt)
 *   are stored as columns directly in the User's table. 
 *
 *   Why not just put these fields directly in User? Separation of concerns —
 *   login attempt tracking is its own concept with its own logic.
 *
 * @Setter(AccessLevel.PACKAGE) — Lombok generates setters that are package-private
 *   (not public). This means only classes in the same package (model.auth) can
 *   call setAttempts(). External code must use the attempt() method, which
 *   enforces the rate-limiting rules.
 */
@Getter
@Setter
@Embeddable
public class LoginAttempt {
    @Column(nullable = false, columnDefinition = "integer default 0")
    @Setter(AccessLevel.PACKAGE)
    private int attempts;

    @Column(nullable = true)
    @Setter(AccessLevel.PACKAGE)
    private Instant lastAttempt;

    /**
     * Has the user exceeded the max login attempts?
     */
    private boolean isExceedLimit(LoginConfig loginConfig) {
        return getAttempts() >= loginConfig.getMaxAttempts();
    }

    /**
     * Can the user attempt to log in?
     * Returns true if:
     * - They haven't hit the limit yet, OR
     * - Enough time has passed since their last attempt (cool-down expired)
     *
     * Public so LoginService can pre-check before authenticating.
     */
    public boolean canAttempt(LoginConfig loginConfig) {
        if (getLastAttempt() != null
                && getLastAttempt().plusSeconds(loginConfig.getCoolTime()).isBefore(Instant.now())) {
            return true;
        }
        return !isExceedLimit(loginConfig);
    }

    /**
     * Record a login attempt (success or failure).
     * Called by LoginService after authentication.
     *
     * @param loginConfig rate limiting configuration
     * @param success     whether the login attempt succeeded
     * @throws InvalidOperation if the user is currently locked out
     */
    public void attempt(LoginConfig loginConfig, boolean success) throws InvalidOperation {
        if (!canAttempt(loginConfig)) {
            throw new InvalidOperation("cannot login anymore !");
        }
        if (success) {
            setAttempts(0); // reset counter on success
        } else {
            if (isExceedLimit(loginConfig)) {
                setAttempts(0); // reset older expired failures
            }
            setAttempts(getAttempts() + 1);
        }
        setLastAttempt(Instant.now());
    }
}
