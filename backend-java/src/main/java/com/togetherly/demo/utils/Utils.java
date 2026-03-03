package com.togetherly.demo.utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * General utility methods.
 */
public class Utils {

    private Utils() {} // prevent instantiation — all methods are static

    /**
     * Generates a random numeric code of the specified length.
     * Used for email verification codes (e.g., "48302").
     *
     * @param length number of digits
     * @return a string of random digits, e.g. "48302" for length=5
     */
    public static String randomNumericCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be > 0");
        }

        // ThreadLocalRandom is the modern alternative to new Random().
        // It's thread-safe without synchronization overhead.
        StringBuilder buffer = new StringBuilder(length);
        for (int d : ThreadLocalRandom.current().ints(length, 0, 10).toArray()) {
            buffer.append(d);
        }
        return buffer.toString();
    }
}
