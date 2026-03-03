package com.togetherly.demo.validation.validator;

import com.togetherly.demo.exception.ValidationError;
import java.util.regex.Pattern;

public class PasswordValidator implements Validator<String, String> {
    public static final String REGEX = "[a-zA-Z0-9]+";
    public static final String NOT_MATCH_MSG = "password can only contain a-z, A-Z, and 0-9 !";
    private static final Pattern pattern = Pattern.compile(REGEX);
    private static final PasswordValidator instance = new PasswordValidator();

    private PasswordValidator() {}

    public static PasswordValidator getInstance() {
        return instance;
    }

    @Override
    public String validate(String data) throws ValidationError {
        if (data == null) throw new ValidationError("password can not be null !");
        String password = data.trim();
        if (password.isEmpty()) throw new ValidationError("password can not be empty !");
        if (password.length() < 8) throw new ValidationError("the length of password is at least 8 !");
        if (password.length() > 32) throw new ValidationError("the length of password is at most 32 !");
        if (!pattern.matcher(password).matches()) throw new ValidationError(NOT_MATCH_MSG);
        return password;
    }
}
