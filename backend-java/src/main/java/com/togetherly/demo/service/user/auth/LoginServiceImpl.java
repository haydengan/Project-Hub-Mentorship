package com.togetherly.demo.service.user.auth;

import com.togetherly.demo.config.LoginConfig;
import com.togetherly.demo.data.auth.UserDetail;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.model.auth.LoginAttempt;
import com.togetherly.demo.model.auth.User;
import com.togetherly.demo.repository.user.UserRepository;
import com.togetherly.demo.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Login service with rate limiting and optimistic locking.
 *
 * KEY CHANGE FROM ORIGINAL:
 * The original called loginAttempt.login() which mixed authentication logic
 * into the entity. We moved that logic here (where it belongs):
 * 1. Pre-check rate limit via loginAttempt.canAttempt()
 * 2. Authenticate via AuthUtil.authenticate() (delegates to Spring Security)
 * 3. Record result via loginAttempt.attempt(success)
 *
 * ANNOTATIONS EXPLAINED:
 *
 * @Retryable(OptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
 *   If two concurrent logins try to update the same User's loginAttempt,
 *   the @Version field causes one to fail with OptimisticLockingFailureException.
 *   @Retryable automatically retries up to 3 times with 100ms delay.
 *
 * @Transactional(noRollbackFor = AuthenticationException.class)
 *   Normally, any exception inside @Transactional rolls back the transaction.
 *   But we WANT to save the failed login attempt count even when auth fails.
 *   noRollbackFor tells Spring: "if AuthenticationException is thrown, still commit."
 *
 * HAND-WRITTEN.
 */
@Service
public class LoginServiceImpl implements LoginService {
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository userRepository;
    @Autowired private LoginConfig loginConfig;

    @Retryable(retryFor = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
    @Transactional(noRollbackFor = AuthenticationException.class)
    @Override
    public UserDetail login(String username, String password) throws AuthenticationException {
        User user = userRepository
                .getByUserName(username)
                .or(() -> userRepository.getByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("Username or email does not exist !"));

        LoginAttempt loginAttempt = user.getLoginAttempt();

        // Step 1: Pre-check — is the user rate-limited?
        if (!loginAttempt.canAttempt(loginConfig)) {
            throw new AuthenticationServiceException(
                    "You have try too many times, please try again later");
        }

        // Step 2: Authenticate with Spring Security (use actual userName, not email)
        try {
            UserDetail userDetail = AuthUtil.authenticate(authenticationManager, user.getUserName(), password);
            // Step 3a: Success — record it (resets counter)
            try {
                loginAttempt.attempt(loginConfig, true);
            } catch (InvalidOperation e) {
                // Should never happen — we already checked canAttempt() above
                throw new AuthenticationServiceException(e.getMessage());
            }
            return userDetail;
        } catch (AuthenticationException ex) {
            // Step 3b: Failure — record it (increments counter)
            try {
                loginAttempt.attempt(loginConfig, false);
            } catch (InvalidOperation e) {
                throw new AuthenticationServiceException(
                        "You have try too many times, please try again later");
            }
            throw ex;
        }
    }
}
