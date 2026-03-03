package com.togetherly.demo.interceptor;

import com.togetherly.demo.controller.constraint.checker.ControllerAuthConstraintChecker;
import com.togetherly.demo.controller.constraint.checker.ControllerRateConstraintChecker;
import com.togetherly.demo.exception.ControllerConstraintViolation;
import com.togetherly.demo.utils.IPUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring MVC interceptor that enforces controller constraints BEFORE the handler runs.
 *
 * EXECUTION ORDER (per request):
 * 1. JwtAuthenticationFilter (filter) — sets auth in SecurityContext
 * 2. This interceptor — checks auth + rate limit annotations
 * 3. Controller method — runs only if interceptor returns true
 *
 * preHandle() is called before every controller method:
 * 1. Stores the client IP (for rate limiting by IP)
 * 2. Checks auth constraints (@AuthenticatedApi, @ApiAllowsTo, @ApiRejectTo)
 * 3. Checks rate limit constraints (@RateLimit)
 * 4. If any check fails → writes JSON error response and returns false (stops the request)
 *
 * HAND-WRITTEN.
 */
@Component
public class ControllerConstraintInterceptor implements HandlerInterceptor {
    @Autowired private ControllerAuthConstraintChecker authConstraintChecker;
    @Autowired private ControllerRateConstraintChecker rateConstraintChecker;

    private static final Logger logger =
            LoggerFactory.getLogger(ControllerConstraintInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // Store client IP for rate limiting and audit
        IPUtils.setRequestIP(request.getRemoteAddr());

        try {
            if (handler instanceof HandlerMethod handlerMethod) {
                authConstraintChecker.checkWithMethod(handlerMethod.getMethod());
                rateConstraintChecker.checkWithMethod(handlerMethod.getMethod());
            }
        } catch (ControllerConstraintViolation ex) {
            setJsonResponse(response, ex.getRejectStatus(), ex.getRejectMessage());
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage());
            setJsonResponse(response, 500, "");
            return false;
        }

        return true;
    }

    private void setJsonResponse(HttpServletResponse response, int status, String message) {
        if (message != null && !message.isEmpty()) {
            try {
                response.getWriter().write("{ \"message\": \"" + message + "\"}");
            } catch (IOException e) {
                logger.error("Failed to write response: {}", e.getMessage());
            }
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
    }
}
