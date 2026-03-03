package com.togetherly.demo.validation.validator;

import com.togetherly.demo.exception.ValidationError;
import java.util.UUID;

public class UUIDValidator implements Validator<UUID, String> {
    private static final UUIDValidator instance = new UUIDValidator();

    private UUIDValidator() {}

    public static UUIDValidator getInstance() {
        return instance;
    }

    @Override
    public UUID validate(String data) throws ValidationError {
        if (data == null) throw new ValidationError("uuid can not be null !");
        try {
            return UUID.fromString(data);
        } catch (Exception e) {
            throw new ValidationError(e.getMessage());
        }
    }
}
