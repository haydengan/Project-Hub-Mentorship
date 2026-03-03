package com.togetherly.demo.repository.verification;

import com.togetherly.demo.model.auth.VerificationCode;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for VerificationCode entities (email verification codes).
 *
 * CUSTOM QUERY METHODS:
 *
 *   deleteByExpireAtLessThan(now)
 *   → DELETE FROM verification_code WHERE expire_at < ?
 *   → Cleanup job: removes all expired codes.
 *
 *   deleteByIdAndEmailAndCodeAndExpireAtGreaterThan(id, email, code, now)
 *   → DELETE FROM verification_code WHERE id = ? AND email = ? AND code = ? AND expire_at > ?
 *   → Used during verification: finds the code matching ALL criteria (right id,
 *     right email, right code, not expired) and deletes it (single-use code).
 *   → Returns long: the number of rows deleted (0 = code was wrong/expired, 1 = success).
 */
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {

    void deleteByExpireAtLessThan(Instant dateTime);

    long deleteByIdAndEmailAndCodeAndExpireAtGreaterThan(
            UUID id, String email, String code, Instant dateTime);
}
