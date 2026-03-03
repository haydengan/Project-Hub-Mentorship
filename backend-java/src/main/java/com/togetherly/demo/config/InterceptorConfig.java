package com.togetherly.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.togetherly.demo.interceptor.ControllerConstraintInterceptor;

/**
 * Registers the ControllerConstraintInterceptor to run on all URL patterns.
 *
 * WebMvcConfigurer is the standard way to customize Spring MVC behavior.
 * addInterceptors() registers our interceptor in the MVC pipeline.
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Autowired private ControllerConstraintInterceptor controllerConstraintInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(controllerConstraintInterceptor).addPathPatterns("/**");
    }
}
