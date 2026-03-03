package com.togetherly.demo.validation.validator;

import com.togetherly.demo.exception.ValidationError;
import java.util.regex.Pattern;

public class EmailValidator implements Validator<String, String> {
    public static final String REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
    public static final String NOT_MATCH_MSG = "invalid email format !";
    private static final Pattern pattern = Pattern.compile(REGEX);
    private static final EmailValidator instance = new EmailValidator();

    private EmailValidator() {}

    public static EmailValidator getInstance() {
        return instance;
    }

    @Override
    public String validate(String data) throws ValidationError {
        if (data == null) throw new ValidationError("email can not be null !");
        String email = data.trim();
        if (email.isEmpty()) throw new ValidationError("email can not be empty !");
        if (email.length() > 64) throw new ValidationError("the length of email is at most 64 !");
        if (!pattern.matcher(email).matches()) throw new ValidationError(NOT_MATCH_MSG);
        return email;
    }
}
