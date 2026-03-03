package com.togetherly.demo.service.verification;

import com.togetherly.demo.data.auth.VerificationPair;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.ValidationError;
import com.togetherly.demo.model.auth.VerificationCode;
import com.togetherly.demo.repository.verification.VerificationCodeRepository;
import com.togetherly.demo.repository.verification.VerifyTokenRepository;
import com.togetherly.demo.service.email.EmailService;
import com.togetherly.demo.validation.validator.EmailValidator;
import com.togetherly.demo.validation.validator.UUIDValidator;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Verification service implementation.
 *
 * WHAT IT DOES:
 * - issueVerificationCode: Creates a 5-digit code, saves to DB, emails to user
 * - verify: Checks code + key + email match a DB record, deletes it (single-use)
 * - deleteExpired*: Cleanup jobs for expired codes and tokens
 *
 * VALIDATORS:
 * The service validates inputs using the singleton validator instances
 * (EmailValidator, UUIDValidator). These throw ValidationError on invalid input,
 * which gets caught by the global exception handler and returned as 400.
 *
 * HAND-WRITTEN.
 */
@Service
public class VerificationServiceImpl implements VerificationService {
    @Autowired private VerificationCodeRepository verificationRepository;
    @Autowired private VerifyTokenRepository verifyTokenRepository;
    @Autowired private EmailService emailService;

    private final EmailValidator emailValidator = EmailValidator.getInstance();
    private final UUIDValidator uuidValidator = UUIDValidator.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    @Override
    public VerificationPair issueVerificationCode(String email) {
        email = emailValidator.validate(email);

        // Create a verification code entity (code is auto-generated in the entity field initializer)
        VerificationCode emailVerification = new VerificationCode();
        emailVerification.setEmail(email);
        emailVerification.setExpireAt(Instant.now().plusSeconds(300)); // 5 minutes
        verificationRepository.save(emailVerification);

        // Send the code via email (async — @Async on EmailServiceImpl)
        emailService.sendSimpleEmail(
                emailVerification.getEmail(),
                "Verification",
                "your verification code is " + emailVerification.getCode());

        // Return key (UUID) + code to the caller
        // Key is returned to the client; code is sent via email
        // Client must submit both to complete verification
        return new VerificationPair(
                emailVerification.getId().toString(),
                emailVerification.getCode());
    }

    @Override
    public void verify(String key, String email, String code) throws InvalidOperation {
        UUID keyId = uuidValidator.validate(key);
        email = emailValidator.validate(email);
        if (code == null) throw new ValidationError("code cannot be null !");

        // Try to delete the matching record. Returns 0 if no match (wrong code or expired).
        if (verificationRepository.deleteByIdAndEmailAndCodeAndExpireAtGreaterThan(
                keyId, email, code, Instant.now()) == 0) {
            throw new InvalidOperation("verification fail !");
        }
    }

    @Transactional
    @Override
    public void deleteExpiredVerificationCodes() {
        logger.info("delete expired verification codes");
        verificationRepository.deleteByExpireAtLessThan(Instant.now());
    }

    @Transactional
    @Override
    public void deleteExpiredVerifyTokens() {
        logger.info("delete expired verification tokens");
        verifyTokenRepository.deleteByExpireAtLessThan(Instant.now());
    }
}
