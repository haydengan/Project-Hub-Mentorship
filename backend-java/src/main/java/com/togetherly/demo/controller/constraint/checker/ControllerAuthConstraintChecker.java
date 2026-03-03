package com.togetherly.demo.controller.constraint.checker;

import com.togetherly.demo.controller.constraint.auth.ApiAllowsTo;
import com.togetherly.demo.controller.constraint.auth.ApiRejectTo;
import com.togetherly.demo.controller.constraint.auth.AuthenticatedApi;
import com.togetherly.demo.exception.ControllerConstraintViolation;
import com.togetherly.demo.utils.AuthUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.springframework.stereotype.Component;

/**
 * Enforces authentication and role-based constraints on controller methods.
 *
 * Called by ControllerConstraintInterceptor BEFORE the controller method runs.
 *
 * HOW IT WORKS:
 * 1. checkAuthenticatedApiConstraint:
 *    - Checks if the method has @AuthenticatedApi (directly or via meta-annotation)
 *    - If yes and user is NOT authenticated → 401
 *    - If no auth annotation at all → clears any authentication (treats as public endpoint)
 *
 * 2. checkRoleConstraints:
 *    - @ApiAllowsTo(roles = {ADMIN}) → user must have ADMIN role, else 403
 *    - @ApiRejectTo(roles = {USER}) → user must NOT have USER role, else 403
 *
 * WHY CLEAR AUTH ON PUBLIC ENDPOINTS?
 * If a user sends a JWT to a public endpoint, we don't want that endpoint
 * to accidentally use the authenticated user's data. Clearing auth ensures
 * public endpoints always run in anonymous mode.
 *
 * HAND-WRITTEN.
 */
@Component
public class ControllerAuthConstraintChecker {

    public void checkWithMethod(Method method) throws ControllerConstraintViolation {
        checkAuthenticatedApiConstraint(method);
        checkRoleConstraints(method);
    }

    private static void checkAuthenticatedApiConstraint(Method method)
            throws ControllerConstraintViolation {
        boolean requireAuthentication = false;

        // Direct check: is @AuthenticatedApi on the method?
        AuthenticatedApi constraint = method.getAnnotation(AuthenticatedApi.class);
        if (constraint != null) {
            requireAuthentication = true;
            if (!AuthUtil.isAuthenticated())
                throw new ControllerConstraintViolation(
                        constraint.rejectStatus(), constraint.rejectMessage());
        }

        // Composed check: is @AuthenticatedApi on any of the method's annotations?
        // (e.g., @ApiAllowsTo is annotated with @AuthenticatedApi)
        for (Annotation annotation : method.getAnnotations()) {
            constraint = annotation.annotationType().getAnnotation(AuthenticatedApi.class);
            if (constraint != null) {
                requireAuthentication = true;
                if (!AuthUtil.isAuthenticated())
                    throw new ControllerConstraintViolation(
                            constraint.rejectStatus(), constraint.rejectMessage());
                else break;
            }
        }

        // Public endpoint — clear any authentication to prevent accidental use
        if (!requireAuthentication) AuthUtil.removeAuthentication();
    }

    private static void checkRoleConstraints(Method method) throws ControllerConstraintViolation {
        for (Annotation constraint : method.getAnnotations()) {
            if (constraint instanceof ApiAllowsTo apiAllowsTo) {
                // Whitelist: user must have one of the allowed roles
                if (Arrays.stream(apiAllowsTo.roles())
                        .noneMatch(role -> role == AuthUtil.currentUserDetail().getRole()))
                    throw new ControllerConstraintViolation(
                            apiAllowsTo.rejectStatus(), apiAllowsTo.rejectMessage());
                else break;
            } else if (constraint instanceof ApiRejectTo apiRejectTo) {
                // Blacklist: user must NOT have any of the rejected roles
                if (Arrays.stream(apiRejectTo.roles())
                        .anyMatch(role -> role == AuthUtil.currentUserDetail().getRole()))
                    throw new ControllerConstraintViolation(
                            apiRejectTo.rejectStatus(), apiRejectTo.rejectMessage());
                else break;
            }
        }
    }
}
