package com.togetherly.demo.service.user.auth;

import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.UserDoesNotExist;
import com.togetherly.demo.model.auth.Role;

/**
 * Admin-only service for changing user roles.
 *
 * Safety checks:
 * - Can't change your own role
 * - Can't remove the last ADMIN
 * - Can't change to the same role (no-op prevention)
 *
 * HAND-WRITTEN interface.
 */
public interface RoleService {

    /** Change the role of a user. Revokes all their tokens to force re-login. */
    void changeRoleOf(String userId, Role role) throws InvalidOperation, UserDoesNotExist;
}
