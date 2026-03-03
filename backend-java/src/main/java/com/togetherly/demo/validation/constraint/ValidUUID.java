package com.togetherly.demo.validation.constraint;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Validates that a string is a valid UUID format.
 * Uses regex to match standard UUID patterns (v1-v5 and v7).
 *
 * @ReportAsSingleViolation — If the inner @Pattern fails, only report
 * THIS annotation's message, not the inner regex mismatch.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = {})
@Retention(RUNTIME)
@Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
@ReportAsSingleViolation
public @interface ValidUUID {
    String message() default "invalid uuid !";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
