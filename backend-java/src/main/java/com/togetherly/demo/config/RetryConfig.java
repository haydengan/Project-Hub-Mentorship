package com.togetherly.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Enables @Retryable on service methods.
 * HIGHEST_PRECEDENCE ensures retry wraps around other AOP proxies (like @Transactional).
 */
@Configuration
@EnableRetry(order = Ordered.HIGHEST_PRECEDENCE)
public class RetryConfig {}
