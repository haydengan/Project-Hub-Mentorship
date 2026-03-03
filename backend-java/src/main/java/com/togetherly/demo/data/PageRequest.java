package com.togetherly.demo.data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Pagination parameters sent by the client as query params.
 * Example: GET /api/users?page=0&size=10
 *
 * Validation ensures: page >= 0, size >= 1.
 */
public record PageRequest(
        @Min(value = 0, message = "page must >= 0 !") @NotNull(message = "page is missing !") Integer page,
        @Min(value = 1, message = "size must >= 1 !") @NotNull(message = "size is missing !") Integer size) {}
