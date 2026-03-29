package com.togetherly.demo.data.activity.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Sent by the client to log an activity entry.
 * loggedDate is optional — defaults to today on the server side.
 */
public record LogActivityRequest(
        @NotNull(message = "activity id cannot be null !")
        UUID activityId,

        @Min(value = 1, message = "duration must be at least 1 minute !")
        int durationMins,

        @Size(max = 256, message = "note must be at most 256 characters !")
        String note,

        @Size(max = 512, message = "media url must be at most 512 characters !")
        String mediaUrl) {}
