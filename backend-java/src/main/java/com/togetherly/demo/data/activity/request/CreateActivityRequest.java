package com.togetherly.demo.data.activity.request;

import com.togetherly.demo.model.activity.ActivityType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Sent by the client to create a new activity within a group.
 */
public record CreateActivityRequest(
        @NotEmpty(message = "activity name cannot be empty !")
        @Size(max = 64, message = "activity name must be at most 64 characters !")
        String name,

        @NotNull(message = "activity type cannot be null !")
        ActivityType type) {}
