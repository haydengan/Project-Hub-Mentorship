package com.togetherly.demo.data.activity.response;

import com.togetherly.demo.model.activity.ActivityLog;

/**
 * A single activity log entry returned to the client.
 */
public record ActivityLogResponse(
        String id,
        String userId,
        String activityId,
        int durationMins,
        String note,
        String mediaUrl,
        String loggedDate,
        String createdAt) {

    public static ActivityLogResponse from(ActivityLog log) {
        return new ActivityLogResponse(
                log.getId().toString(),
                log.getUserId().toString(),
                log.getActivityId().toString(),
                log.getDurationMins(),
                log.getNote(),
                log.getMediaUrl(),
                log.getLoggedDate().toString(),
                log.getCreateAt() != null ? log.getCreateAt().toString() : null);
    }
}
