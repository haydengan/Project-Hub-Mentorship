package com.togetherly.demo.controller.constraint.rate;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Applies rate limiting to a controller method using a token bucket algorithm.
 *
 * HOW TOKEN BUCKET WORKS:
 * - Each user/IP gets a "bucket" with `limit` tokens
 * - Each request consumes 1 token
 * - Tokens refill at a rate of `limit / period` per second
 * - When the bucket is empty → 429 Too Many Requests
 *
 * `key` scopes the rate limit. Different methods with the same key share a bucket.
 * IMPORTANT: If using default key="", all @RateLimit with same target must have
 * the same limit and period — otherwise tokens refill at inconsistent speeds.
 *
 * HAND-WRITTEN annotation.
 */
@Target({ElementType.METHOD})
@Retention(RUNTIME)
public @interface RateLimit {
    String key() default "";

    LimitTarget target() default LimitTarget.IP;

    long limit() default 10;

    long period() default 60;
}
