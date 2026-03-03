package com.togetherly.demo.service.user.auth;

import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.UserDoesNotExist;

/**
 * Admin-only service for activating/deactivating user accounts.
 *
 * Deactivated users cannot log in or refresh tokens.
 * Deactivation also revokes all existing access tokens.
 *
 * HAND-WRITTEN interface.
 */
public interface ActivationService {

    /** Activate a disabled user account. */
    void activateUser(String userId) throws InvalidOperation, UserDoesNotExist;

    /** Deactivate a user account. Revokes all their tokens. */
    void deactivateUser(String userId) throws InvalidOperation, UserDoesNotExist;
}
