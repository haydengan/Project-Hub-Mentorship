package com.togetherly.demo.validation.validator;

import com.togetherly.demo.exception.ValidationError;
import java.util.regex.Pattern;

public class UserNameValidator implements Validator<String, String> {
    public static final String REGEX = "[a-zA-Z0-9]+";
    public static final String NOT_MATCH_MSG = "username can only contain a-z, A-Z, and 0-9 !";
    private static final Pattern pattern = Pattern.compile(REGEX);
    private static final UserNameValidator instance = new UserNameValidator();

    private UserNameValidator() {}

    public static UserNameValidator getInstance() {
        return instance;
    }

    @Override
    public String validate(String data) throws ValidationError {
        if (data == null) throw new ValidationError("username can not be null !");
        String username = data.trim();
        if (username.isEmpty()) throw new ValidationError("username can not be empty !");
        if (username.length() > 32) throw new ValidationError("the length of username is at most 32 !");
        if (!pattern.matcher(username).matches()) throw new ValidationError(NOT_MATCH_MSG);
        return username;
    }
}
