package com.togetherly.demo.data.group.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * Sent by the client to create a new group.
 * The creator automatically becomes the group's ADMIN.
 */
public record CreateGroupRequest(
        @NotEmpty(message = "group name cannot be empty !")
        @Size(max = 64, message = "group name must be at most 64 characters !")
        String name,

        @Size(max = 256, message = "description must be at most 256 characters !")
        String description) {}
