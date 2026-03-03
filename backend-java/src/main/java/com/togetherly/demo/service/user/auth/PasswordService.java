package com.togetherly.demo.service.user.auth;

import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.UserDoesNotExist;
import com.togetherly.demo.model.auth.VerifyToken;

/**
 * Manages password changes and resets.
 *
 * Three operations:
 * - changePasswordOf: User changes their own password (knows old password)
 * - requestResetPasswordToken: User forgot password → system emails a reset link
 * - resetPassword: User clicks the reset link and sets a new password
 *
 * All operations revoke existing access tokens to force re-login.
 *
 * HAND-WRITTEN interface.
 */
public interface PasswordService {

    /** Change password (user knows old password). Revokes all tokens. */
    void changePasswordOf(String userId, String oldPassword, String newPassword)
            throws InvalidOperation, UserDoesNotExist;

    /** Generate a password reset token and return it (for emailing). */
    VerifyToken requestResetPasswordToken(String email)
            throws InvalidOperation, UserDoesNotExist;

    /** Reset password using a valid reset token. Revokes all tokens. */
    void resetPassword(String verifyToken, String newPassword) throws InvalidOperation;
}
