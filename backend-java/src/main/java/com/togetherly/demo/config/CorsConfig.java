package com.togetherly.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * CORS (Cross-Origin Resource Sharing) configuration.
 * Controls which frontend domains can call this API.
 */
@Getter
@Setter
@Configuration
public class CorsConfig {
    @Value("${allow.host}")
    private String allowOrigin;
}
