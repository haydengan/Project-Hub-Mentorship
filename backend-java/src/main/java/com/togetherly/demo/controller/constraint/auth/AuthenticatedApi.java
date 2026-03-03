package com.togetherly.demo.controller.constraint.auth;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a controller method as requiring authentication.
 * If the user is not authenticated, the request is rejected with the specified status.
 *
 * Can be used directly on a method, OR as a meta-annotation on other annotations
 * (like @ApiAllowsTo, @ApiRejectTo) — that's why Target includes ANNOTATION_TYPE.
 *
 * HAND-WRITTEN annotation — you decide which endpoints require auth.
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface AuthenticatedApi {
    String rejectMessage() default "";

    int rejectStatus() default 401;
}
