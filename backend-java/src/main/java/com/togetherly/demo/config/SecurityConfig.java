package com.togetherly.demo.config;

import com.togetherly.demo.filter.JwtAuthenticationFilter;
import com.togetherly.demo.service.user.UserDetailService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Central Spring Security configuration.
 *
 * WHAT THIS CONFIGURES:
 *
 * 1. PasswordEncoder — BCrypt for hashing passwords. Injected wherever
 *    PasswordEncoder is needed (RegistrationService, PasswordService, etc.)
 *
 * 2. SecurityFilterChain — The HTTP security pipeline:
 *    - CORS enabled (cross-origin requests from frontend)
 *    - CSRF disabled (we use JWT, not cookies for auth, so CSRF isn't needed)
 *    - Sessions NEVER created (stateless JWT auth)
 *    - JwtAuthenticationFilter runs BEFORE Spring's default auth filter
 *    - Form login disabled (we have our own login endpoint)
 *    - No URL-based authorization rules (we use custom annotations instead)
 *
 * 3. AuthenticationManager — Used by LoginService to authenticate username/password.
 *    Configured to use our UserDetailService + BCrypt encoder.
 *
 * 4. CorsConfigurationSource — Allows the configured frontend origin to call /api/**
 *
 * HAND-WRITTEN — this is your app's security policy.
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    @Autowired private UserDetailService userDetailService;
    @Autowired private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired private CorsConfig corsConfig;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.NEVER))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder
                .userDetailsService(userDetailService)
                .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration apiConfiguration = new CorsConfiguration();
        apiConfiguration.setAllowedOriginPatterns(List.of(corsConfig.getAllowOrigin()));
        apiConfiguration.setAllowCredentials(true);
        apiConfiguration.addAllowedHeader(CorsConfiguration.ALL);
        apiConfiguration.addAllowedMethod(CorsConfiguration.ALL);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", apiConfiguration);
        source.registerCorsConfiguration("/web/api/**", apiConfiguration);
        return source;
    }
}
