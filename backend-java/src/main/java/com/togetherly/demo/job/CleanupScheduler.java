package com.togetherly.demo.job;

import com.togetherly.demo.service.jwt.JwtService;
import com.togetherly.demo.service.verification.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled cleanup jobs — replaces JobRunr from the original project.
 *
 * WHY REPLACE JOBRUNR?
 * JobRunr is a full job scheduling framework with its own dashboard, database tables,
 * and dependency. For simple periodic cleanup tasks, Spring's built-in @Scheduled
 * does the same thing with zero extra dependencies.
 *
 * @EnableScheduling tells Spring to scan for @Scheduled methods and run them.
 *
 * @Scheduled(fixedRate = 3600000) — Run every 3,600,000 ms (1 hour).
 * fixedRate means: "run every N ms regardless of how long the previous run took."
 * Alternative: fixedDelay = "wait N ms AFTER the previous run finishes."
 *
 * WHAT GETS CLEANED:
 * 1. Expired JWT tokens (access + refresh) — they're useless after expiry
 * 2. Expired verification codes — 5-minute TTL, cleaned up periodically
 * 3. Expired verify tokens (password reset) — 10-minute TTL
 *
 * HAND-WRITTEN.
 */
@Component
@EnableScheduling
public class CleanupScheduler {
    @Autowired private JwtService jwtService;
    @Autowired private VerificationService verificationService;

    /** Delete expired JWT tokens every hour. */
    @Scheduled(fixedRate = 3600000)
    public void cleanUpExpiredTokens() {
        jwtService.deleteExpiredTokens();
    }

    /** Delete expired verification codes and password reset tokens every hour. */
    @Scheduled(fixedRate = 3600000)
    public void cleanUpExpiredVerifications() {
        verificationService.deleteExpiredVerificationCodes();
        verificationService.deleteExpiredVerifyTokens();
    }
}
