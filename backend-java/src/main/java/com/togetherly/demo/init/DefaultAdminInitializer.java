package com.togetherly.demo.init;

import com.togetherly.demo.exception.AlreadyExist;
import com.togetherly.demo.model.auth.Role;
import com.togetherly.demo.service.user.auth.RegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Creates a default ADMIN user on application startup.
 *
 * CommandLineRunner: Spring calls run() after the application context is fully initialized.
 * This is the standard way to run one-time setup logic at startup.
 *
 * Reads from application.properties (or environment variables):
 *   default.admin.username=admin
 *   default.admin.password=securePassword123
 *   default.admin.email=admin@togetherly.com
 *
 * If all three are set, creates the admin user. If the user already exists, skips silently.
 * If any property is missing, does nothing.
 *
 * HAND-WRITTEN.
 */
@Component
public class DefaultAdminInitializer implements CommandLineRunner {
    @Autowired private Environment env;
    @Autowired private RegistrationService registrationService;

    private static final Logger logger = LoggerFactory.getLogger(DefaultAdminInitializer.class);

    @Override
    public void run(String... args) {
        createDefaultAdmin();
    }

    private void createDefaultAdmin() {
        String adminName = env.getProperty("default.admin.username", "");
        String adminPassword = env.getProperty("default.admin.password", "");
        String adminEmail = env.getProperty("default.admin.email", "");

        if (!adminName.isEmpty() && !adminPassword.isEmpty() && !adminEmail.isEmpty()) {
            try {
                registrationService.createUser(adminName, adminPassword, adminEmail, Role.ADMIN);
                logger.info("create admin user from env !");
            } catch (AlreadyExist e) {
                logger.info("default admin already exist, skip creation !");
            } catch (Exception e) {
                logger.error("cannot create default admin: {}", e.getMessage());
            }
        }
    }
}
