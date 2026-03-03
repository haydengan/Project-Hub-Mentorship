package com.togetherly.demo.validation.constraint;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.togetherly.demo.validation.validator.RoleValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotEmpty;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Validates that a string is a valid Role enum value (ADMIN, STAFF, NORMAL).
 * Uses RoleValidator for the actual check.
 *
 * Named @ValidRole (not @Role) to avoid clashing with the model.auth.Role enum.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = {RoleValidator.class})
@Retention(RUNTIME)
@NotEmpty(message = "role cannot be empty !")
@ReportAsSingleViolation
public @interface ValidRole {
    String message() default "invalid role !";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
