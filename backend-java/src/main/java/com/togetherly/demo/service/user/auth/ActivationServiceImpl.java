package com.togetherly.demo.service.user.auth;

import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.UserDoesNotExist;
import com.togetherly.demo.model.auth.Role;
import com.togetherly.demo.model.auth.User;
import com.togetherly.demo.repository.jwt.AccessTokenRepository;
import com.togetherly.demo.repository.user.UserRepository;
import com.togetherly.demo.service.jwt.JwtService;
import com.togetherly.demo.utils.AuthUtil;
import com.togetherly.demo.validation.validator.UUIDValidator;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Activation service implementation.
 *
 * SAFETY CHECKS:
 * - Can't activate/deactivate yourself
 * - Can't deactivate an admin
 * - Can't activate an already-active user (and vice versa)
 *
 * Deactivation revokes all tokens to immediately log the user out.
 * Activation does NOT issue new tokens — the user must log in again.
 *
 * HAND-WRITTEN.
 */
@Service
public class ActivationServiceImpl implements ActivationService {
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;
    @Autowired private AccessTokenRepository accessTokenRepository;

    private final UUIDValidator uuidValidator = UUIDValidator.getInstance();

    @Retryable(retryFor = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
    @Override
    public void activateUser(String userId) throws InvalidOperation, UserDoesNotExist {
        UUID id = uuidValidator.validate(userId);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserDoesNotExist("user is not exist !"));
        if (AuthUtil.isAuthenticated() && AuthUtil.currentUserDetail().getId().equals(id.toString()))
            throw new InvalidOperation("cannot activate yourself !");
        if (user.isActive())
            throw new InvalidOperation("target user is already active !");

        user.setActive(true);
        userRepository.save(user);
    }

    @Retryable(retryFor = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deactivateUser(String userId) throws InvalidOperation, UserDoesNotExist {
        UUID id = uuidValidator.validate(userId);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserDoesNotExist("user is not exist !"));
        if (AuthUtil.isAuthenticated() && AuthUtil.currentUserDetail().getId().equals(id.toString()))
            throw new InvalidOperation("cannot deactivate yourself !");
        if (user.getRole() == Role.ADMIN)
            throw new InvalidOperation("cannot deactivate an admin !");
        if (!user.isActive())
            throw new InvalidOperation("target user is already inactive !");

        user.setActive(false);
        userRepository.save(user);

        // Immediately logout the deactivated user
        jwtService.revokeAccessToken(accessTokenRepository.getByUser(user));
    }
}
