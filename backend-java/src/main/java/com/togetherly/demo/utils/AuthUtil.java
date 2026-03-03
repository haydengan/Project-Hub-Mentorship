package com.togetherly.demo.utils;

import com.togetherly.demo.data.auth.UserDetail;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility methods for Spring Security authentication.
 *
 * SecurityContextHolder is a thread-local storage where Spring Security
 * keeps the current user's authentication info for the duration of a request.
 */
public class AuthUtil {

    private AuthUtil() {}

    /** Authenticate with username/password, store in SecurityContext, return UserDetail. */
    public static UserDetail authenticate(
            AuthenticationManager authenticationManager, String username, String password)
            throws AuthenticationException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return (UserDetail) authentication.getPrincipal();
    }

    /** Check if the current request is from an authenticated user. */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken);
    }

    /** Get the current authenticated user's details, or throw if not authenticated. */
    public static UserDetail currentUserDetail() throws AuthenticationException {
        if (!isAuthenticated()) {
            throw new InternalAuthenticationServiceException("has not been authenticated !");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserDetail) authentication.getPrincipal();
    }

    /** Clear authentication (logout). */
    public static void removeAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
