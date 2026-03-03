package com.togetherly.demo.data.admin.request;

import jakarta.validation.constraints.NotEmpty;

/** Admin request containing a target user's ID. */
public record UserIdRequest(
        @NotEmpty(message = "user id cannot be empty !") String id) {}
