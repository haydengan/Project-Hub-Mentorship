package com.togetherly.demo.validation.validator;

import com.togetherly.demo.exception.ValidationError;
import com.togetherly.demo.model.auth.Role;
import com.togetherly.demo.validation.constraint.ValidRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Dual-purpose validator:
 * 1. As a ConstraintValidator — used by the @ValidRole annotation on DTOs
 * 2. As a Validator<Role, String> — used programmatically in services
 */
public class RoleValidator
        implements ConstraintValidator<ValidRole, String>, Validator<Role, String> {

    private static final RoleValidator instance = new RoleValidator();

    private RoleValidator() {}

    public static RoleValidator getInstance() {
        return instance;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            Role.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public Role validate(String data) throws ValidationError {
        try {
            return Role.valueOf(data);
        } catch (IllegalArgumentException e) {
            throw new ValidationError("role " + data + " is not exist !");
        }
    }
}
