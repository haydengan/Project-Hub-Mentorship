package com.togetherly.demo.service.user.auth;

import com.togetherly.demo.data.auth.VerificationPair;
import com.togetherly.demo.exception.AlreadyExist;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.model.auth.Role;
import com.togetherly.demo.model.auth.User;

/**
 * Handles user creation and registration.
 *
 * Two methods:
 * - createUser: Direct creation with a specified role (used by admin/init)
 * - registerUser: Self-registration with email verification (always NORMAL role)
 *
 * HAND-WRITTEN interface.
 */
public interface RegistrationService {

    /** Create a user with the given role. Used by admin operations. */
    User createUser(String username, String password, String email, Role role) throws AlreadyExist;

    /** Self-register with email verification. Always assigns NORMAL role. */
    User registerUser(String username, String password, String email, VerificationPair verification)
            throws AlreadyExist, InvalidOperation;
}
