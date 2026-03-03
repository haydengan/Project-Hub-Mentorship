package com.togetherly.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for login attempt rate limiting.
 *
 * @Configuration — Tells Spring: "this is a bean, create one instance and manage it."
 *   Spring will create this object at startup and inject it wherever it's needed.
 *
 * @Value("${login.maxAttempts:5}") — Reads the value from application.properties.
 *   The ":5" is a default value — if the property isn't defined, use 5.
 *   Example in application.properties: login.maxAttempts=10
 *
 * This is HAND-WRITTEN. You decide what config your app needs.
 */
@Getter
@Setter
@Configuration
public class LoginConfig {
    // Max login failures before the account is temporarily locked
    @Value("${login.maxAttempts:5}")
    private int maxAttempts;

    // Seconds to wait after max failures before allowing login again (900 = 15 min)
    @Value("${login.attempts.coolTime:900}")
    private int coolTime;
}
