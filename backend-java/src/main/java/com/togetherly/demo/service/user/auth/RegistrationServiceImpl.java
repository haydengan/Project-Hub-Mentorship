package com.togetherly.demo.service.user.auth;

import com.togetherly.demo.data.auth.VerificationPair;
import com.togetherly.demo.exception.AlreadyExist;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.model.auth.Role;
import com.togetherly.demo.model.auth.User;
import com.togetherly.demo.repository.user.UserRepository;
import com.togetherly.demo.service.verification.VerificationService;
import com.togetherly.demo.validation.validator.EmailValidator;
import com.togetherly.demo.validation.validator.PasswordValidator;
import com.togetherly.demo.validation.validator.UserNameValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registration service implementation.
 *
 * KEY CHANGE FROM ORIGINAL:
 * Uses record accessors .key() and .code() instead of .getKey() and .getCode(),
 * because VerificationPair is now a Java record (not a Lombok class).
 *
 * VALIDATORS:
 * Input is validated using singleton validators before any DB operations.
 * This catches bad input early (fail fast) and returns clear error messages.
 *
 * PASSWORD ENCODING:
 * Raw passwords are NEVER stored. PasswordEncoder (BCrypt by default) produces
 * a one-way hash. Even if the DB is compromised, passwords can't be recovered.
 *
 * HAND-WRITTEN.
 */
@Service
public class RegistrationServiceImpl implements RegistrationService {
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private VerificationService verificationService;

    private final EmailValidator emailValidator = EmailValidator.getInstance();
    private final PasswordValidator passwordValidator = PasswordValidator.getInstance();
    private final UserNameValidator userNameValidator = UserNameValidator.getInstance();

    @Override
    public User createUser(String username, String password, String email, Role role)
            throws AlreadyExist {
        username = userNameValidator.validate(username);
        password = passwordValidator.validate(password);
        email = emailValidator.validate(email);

        if (userRepository.getByUserName(username).isPresent()
                || userRepository.getByEmail(email).isPresent()) {
            throw new AlreadyExist("username or email is already taken !");
        }

        User user = new User();
        user.setUserName(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(role);
        userRepository.save(user);

        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public User registerUser(
            String username, String password, String email, VerificationPair verificationPair)
            throws AlreadyExist, InvalidOperation {
        // Verify email first — .key() and .code() are record accessors (not getKey()/getCode())
        verificationService.verify(verificationPair.key(), email, verificationPair.code());
        return createUser(username, password, email, Role.NORMAL);
    }
}
