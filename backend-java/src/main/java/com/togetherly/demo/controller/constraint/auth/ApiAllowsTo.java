package com.togetherly.demo.controller.constraint.auth;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.togetherly.demo.model.auth.Role;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Restricts a controller method to specific roles (whitelist).
 * Only users with one of the listed roles can access the endpoint.
 *
 * Example: @ApiAllowsTo(roles = {Role.ADMIN}) — only admins can call this.
 *
 * @AuthenticatedApi is a meta-annotation: it means this annotation
 * automatically implies authentication is required (no need to add both).
 *
 * HAND-WRITTEN annotation.
 */
@Target({ElementType.METHOD})
@Retention(RUNTIME)
@AuthenticatedApi
public @interface ApiAllowsTo {
    Role[] roles() default {};

    String rejectMessage() default "you don't have enough permission !";

    int rejectStatus() default 403;
}
