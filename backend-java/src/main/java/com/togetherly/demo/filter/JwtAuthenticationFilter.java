package com.togetherly.demo.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import com.togetherly.demo.data.auth.UserDetail;
import com.togetherly.demo.exception.InvalidTokenException;
import com.togetherly.demo.service.jwt.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT authentication filter — runs once per request before any controller.
 *
 * HOW IT WORKS:
 * 1. Checks for a JWT in two places:
 *    a. Authorization header: "Bearer eyJ..."
 *    b. access_token cookie (for browser-based auth)
 * 2. If found, validates the token:
 *    a. Checks if it's in the Redis blacklist (revoked)
 *    b. Parses the JWT and extracts user claims
 * 3. If valid, sets the user in Spring Security's SecurityContext
 *    → All downstream code can call AuthUtil.currentUserDetail()
 * 4. If invalid, returns 401 immediately (no controller runs)
 * 5. If no token at all, continues without auth (anonymous request)
 *
 * EXTENDS OncePerRequestFilter:
 * Guarantees this filter runs exactly once per request, even if the
 * request is forwarded internally (e.g., error handling).
 *
 * @Component makes Spring auto-detect this and let us @Autowire it into SecurityConfig.
 *
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired private JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = null;

        // Priority 1: Authorization header ("Bearer <token>")
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null) {
            accessToken = authHeader.replace("Bearer ", "");
        }

        // Priority 2: access_token cookie (for browser-based auth)
        if (accessToken == null) {
            Cookie cookie = WebUtils.getCookie(request, "access_token");
            if (cookie != null && cookie.getValue() != null) {
                accessToken = cookie.getValue();
            }
        }

        // If we found a token, validate it and set the authenticated user
        if (accessToken != null) {
            try {
                // Check Redis blacklist first (fast rejection)
                boolean blacklisted = jwtService.isAccessTokenInBlackList(accessToken);
                System.err.println("[JWT FILTER] Blacklist check: " + blacklisted);
                if (blacklisted) {
                    throw new InvalidTokenException("token is blacklisted !");
                }

                // Parse JWT → extract UserDetail (id, username, role, etc.)
                UserDetail userDetail = jwtService.getUserDetailFromAccessToken(accessToken);

                // Set the authenticated user in Spring Security's context
                // null for credentials (password not needed for token-based auth)
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetail, null, userDetail.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                System.err.println("[JWT FILTER] Token validation failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("[JWT FILTER] Caused by: " + e.getCause().getClass().getSimpleName() + " - " + e.getCause().getMessage());
                }
                e.printStackTrace(System.err);
                SecurityContextHolder.clearContext();
            }
        }

        // Continue to the next filter/controller
        filterChain.doFilter(request, response);
    }
}
