package com.togetherly.demo.controller.constraint.auth;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.togetherly.demo.model.auth.Role;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Blocks specific roles from accessing a controller method (blacklist).
 * Users with any of the listed roles are rejected.
 *
 * Example: @ApiRejectTo(roles = {Role.NORMAL}) — NORMAL users can't call this.
 *
 * Opposite of @ApiAllowsTo: use @ApiAllowsTo for whitelisting, @ApiRejectTo for blacklisting.
 *
 * HAND-WRITTEN annotation.
 */
@Target({ElementType.METHOD})
@Retention(RUNTIME)
@AuthenticatedApi
public @interface ApiRejectTo {
    Role[] roles() default {};

    String rejectMessage() default "you don't have enough permission !";

    int rejectStatus() default 403;
}
