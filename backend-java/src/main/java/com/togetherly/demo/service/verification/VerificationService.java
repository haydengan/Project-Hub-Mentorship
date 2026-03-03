package com.togetherly.demo.service.verification;

import com.togetherly.demo.data.auth.VerificationPair;
import com.togetherly.demo.exception.InvalidOperation;

/**
 * Manages email verification codes for user registration.
 *
 * FLOW:
 * 1. User enters email → issueVerificationCode() creates a code, saves to DB, emails it
 * 2. User enters the code → verify() checks if it matches and deletes it (single-use)
 * 3. Scheduled job calls deleteExpired*() to clean up old codes/tokens
 *
 * HAND-WRITTEN interface.
 */
public interface VerificationService {

    /** Issue a verification code for the given email. Sends the code via email. */
    VerificationPair issueVerificationCode(String email);

    /** Verify a code matches the key + email. Deletes the code on success (single-use). */
    void verify(String key, String email, String code) throws InvalidOperation;

    /** Cleanup: delete all expired verification codes. */
    void deleteExpiredVerificationCodes();

    /** Cleanup: delete all expired password reset tokens. */
    void deleteExpiredVerifyTokens();
}
