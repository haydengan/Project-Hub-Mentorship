package com.togetherly.demo.utils;

import jakarta.servlet.http.Cookie;

/**
 * Utility for creating HTTP cookies (used to send JWT tokens to the browser).
 */
public class CookieUtils {

    private CookieUtils() {}

    /** Create a cookie with the given properties. */
    public static Cookie create(String key, String value, String domain, int maxAge, boolean isHttpOnly) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(maxAge);
        cookie.setDomain(domain);
        cookie.setPath("/");
        cookie.setHttpOnly(isHttpOnly);
        return cookie;
    }

    /** Create a cookie that removes itself (maxAge=0 tells the browser to delete it). */
    public static Cookie removed(String key, String domain, boolean isHttpOnly) {
        Cookie cookie = new Cookie(key, null);
        cookie.setMaxAge(0);
        cookie.setDomain(domain);
        cookie.setPath("/");
        cookie.setHttpOnly(isHttpOnly);
        return cookie;
    }
}
