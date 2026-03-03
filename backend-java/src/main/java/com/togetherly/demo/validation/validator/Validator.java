package com.togetherly.demo.validation.validator;

import com.togetherly.demo.exception.ValidationError;

/**
 * Generic validation interface for programmatic (non-annotation) validation.
 * Used in Service layer when you need to validate + transform input.
 *
 * @param <O> output type after validation (e.g., UUID, Role enum)
 * @param <I> input type before validation (e.g., String)
 */
public interface Validator<O, I> {
    O validate(I data) throws ValidationError;
}
