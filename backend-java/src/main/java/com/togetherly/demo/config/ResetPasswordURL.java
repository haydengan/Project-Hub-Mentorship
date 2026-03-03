package com.togetherly.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * URL prefix for password reset links sent via email.
 */
@Getter
@Setter
@Configuration
public class ResetPasswordURL {
    @Value("${reset.password.url}")
    private String urlPrefix;
}
