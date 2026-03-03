package com.togetherly.demo.service.user.auth;

import com.togetherly.demo.data.auth.UserDetail;
import org.springframework.security.core.AuthenticationException;

/**
 * Handles user login with rate limiting.
 *
 * HAND-WRITTEN interface.
 */
public interface LoginService {

    /** Authenticate a user with username + password. Returns UserDetail on success. */
    UserDetail login(String username, String password) throws AuthenticationException;
}
