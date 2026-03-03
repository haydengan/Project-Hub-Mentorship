package com.togetherly.demo.utils;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Stores/retrieves the client IP address for the current request.
 * Used for rate limiting and audit logging.
 *
 * RequestContextHolder is a thread-local store for request-scoped data.
 */
public class IPUtils {
    private static final String REQUEST_IP_ATTRIBUTE = "REQUEST_IP";

    private IPUtils() {}

    public static void setRequestIP(String ip) {
        RequestContextHolder.currentRequestAttributes()
                .setAttribute(REQUEST_IP_ATTRIBUTE, ip, RequestAttributes.SCOPE_REQUEST);
    }

    public static String getRequestIP() {
        return (String) RequestContextHolder.currentRequestAttributes()
                .getAttribute(REQUEST_IP_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
    }
}
