package com.togetherly.demo.data.admin.request;

import jakarta.validation.constraints.NotEmpty;

/** Admin request to change a user's role. */
public record ChangeUserRoleRequest(
        @NotEmpty(message = "user id cannot be empty !") String id,
        @NotEmpty(message = "role cannot be empty !") String role) {}
