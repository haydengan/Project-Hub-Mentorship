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
 * Role service implementation.
 *
 * SAFETY CHECKS (in order):
 * 1. Can't change your own role (prevents admin from locking themselves out)
 * 2. Can't change to the same role (no-op prevention)
 * 3. After changing, checks if any ADMINs remain (prevents removing last admin)
 *    — This check happens AFTER the save, inside the transaction. If it fails,
 *      the transaction rolls back and the change is undone.
 *
 * After role change, all user's access tokens are revoked so their next
 * request will use the new role (tokens contain the role claim).
 *
 * HAND-WRITTEN.
 */
@Service
public class RoleServiceImpl implements RoleService {
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;
    @Autowired private AccessTokenRepository accessTokenRepository;

    private final UUIDValidator uuidValidator = UUIDValidator.getInstance();

    @Retryable(retryFor = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void changeRoleOf(String userId, Role role) throws InvalidOperation, UserDoesNotExist {
        UUID id = uuidValidator.validate(userId);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserDoesNotExist("user is not exist !"));

        // Can't change your own role
        if (AuthUtil.isAuthenticated() && AuthUtil.currentUserDetail().getId().equals(id.toString()))
            throw new InvalidOperation("cannot change the role of yourself !");

        Role originalRole = user.getRole();
        if (role.equals(originalRole))
            throw new InvalidOperation("role doesn't change !");

        user.setRole(role);
        userRepository.save(user);

        // After save: check we didn't remove the last admin
        if (Role.ADMIN.equals(originalRole) && userRepository.getByRole(Role.ADMIN).isEmpty())
            throw new InvalidOperation("cannot change the role of the only ADMIN !");

        // Revoke tokens so the user's next request reflects the new role
        jwtService.revokeAccessToken(accessTokenRepository.getByUser(user));
    }
}
