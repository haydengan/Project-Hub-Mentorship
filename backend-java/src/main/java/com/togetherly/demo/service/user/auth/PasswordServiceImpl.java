package com.togetherly.demo.service.user.auth;

import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.UserDoesNotExist;
import com.togetherly.demo.exception.ValidationError;
import com.togetherly.demo.model.auth.User;
import com.togetherly.demo.model.auth.VerifyToken;
import com.togetherly.demo.repository.jwt.AccessTokenRepository;
import com.togetherly.demo.repository.user.UserRepository;
import com.togetherly.demo.repository.verification.VerifyTokenRepository;
import com.togetherly.demo.service.jwt.JwtService;
import com.togetherly.demo.utils.Utils;
import com.togetherly.demo.validation.validator.EmailValidator;
import com.togetherly.demo.validation.validator.PasswordValidator;
import com.togetherly.demo.validation.validator.UUIDValidator;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Password service implementation.
 *
 * TRANSACTION ISOLATION:
 * requestResetPasswordToken and resetPassword use REPEATABLE_READ isolation.
 * This prevents a race condition where two concurrent requests could both
 * delete the same VerifyToken. In READ_COMMITTED (default), the second
 * request would silently succeed even if the token was already deleted.
 * REPEATABLE_READ causes the second request to fail with a serialization error,
 * which is the correct behavior (token is single-use).
 *
 * HAND-WRITTEN.
 */
@Service
public class PasswordServiceImpl implements PasswordService {
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private AccessTokenRepository accessTokenRepository;
    @Autowired private VerifyTokenRepository verifyTokenRepository;

    private final EmailValidator emailValidator = EmailValidator.getInstance();
    private final PasswordValidator passwordValidator = PasswordValidator.getInstance();
    private final UUIDValidator uuidValidator = UUIDValidator.getInstance();

    @Retryable(retryFor = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void changePasswordOf(String userId, String oldPassword, String newPassword)
            throws InvalidOperation, UserDoesNotExist {
        UUID id = uuidValidator.validate(userId);

        try {
            oldPassword = passwordValidator.validate(oldPassword);
        } catch (ValidationError e) {
            throw new InvalidOperation("old password is not correct !");
        }
        newPassword = passwordValidator.validate(newPassword);
        newPassword = passwordEncoder.encode(newPassword);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserDoesNotExist("user is not exist !"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            throw new InvalidOperation("old password is not correct !");
        if (passwordEncoder.matches(oldPassword, newPassword))
            throw new InvalidOperation("new password is same with old password !");

        user.setPassword(newPassword);
        userRepository.save(user);

        // Delete any pending password reset token (password was just changed)
        verifyTokenRepository.deleteByUser(user);

        // Revoke all access tokens to force re-login
        jwtService.revokeAccessToken(accessTokenRepository.getByUser(user));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    @Override
    public VerifyToken requestResetPasswordToken(String email)
            throws InvalidOperation, UserDoesNotExist {
        email = emailValidator.validate(email);

        User user = userRepository.getByEmail(email)
                .orElseThrow(() -> new UserDoesNotExist("user is not exist !"));
        if (!user.isActive()) throw new InvalidOperation("User is disabled !");

        // Check for existing non-expired token (prevent spamming reset emails)
        Optional<VerifyToken> token = verifyTokenRepository.getByUser(user);
        if (token.isPresent() && token.get().getExpireAt().isAfter(Instant.now()))
            throw new InvalidOperation("already request to reset password, please try again later !");
        if (token.isPresent() && token.get().getExpireAt().isBefore(Instant.now()))
            verifyTokenRepository.delete(token.get());

        VerifyToken verifyToken = new VerifyToken();
        verifyToken.setUser(user);
        verifyToken.setToken(Utils.randomNumericCode(128));
        verifyToken.setExpireAt(Instant.now().plusSeconds(600)); // 10 minutes
        verifyTokenRepository.save(verifyToken);

        return verifyToken;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    @Override
    public void resetPassword(String verifyToken, String newPassword) throws InvalidOperation {
        newPassword = passwordValidator.validate(newPassword);
        newPassword = passwordEncoder.encode(newPassword);

        VerifyToken token = verifyTokenRepository
                .getByTokenAndExpireAtGreaterThan(verifyToken, Instant.now())
                .orElseThrow(() -> new InvalidOperation(
                        "reset password request is invalid or expired,"
                                + " please try to use forget password again later !"));

        User user = token.getUser();
        if (!user.isActive()) throw new InvalidOperation("User is disabled !");

        user.setPassword(newPassword);
        userRepository.save(user);
        verifyTokenRepository.delete(token);

        // Revoke all access tokens to force re-login
        jwtService.revokeAccessToken(accessTokenRepository.getByUser(user));
    }
}
