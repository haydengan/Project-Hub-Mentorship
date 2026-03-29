package com.togetherly.demo.data.activity.response;

import com.togetherly.demo.model.activity.Activity;
import com.togetherly.demo.model.activity.ActivityType;

/**
 * Activity info returned to the client.
 */
public record ActivityResponse(
        String id,
        String name,
        ActivityType type,
        String groupId,
        String createdBy,
        String createdAt) {

    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(
                activity.getId().toString(),
                activity.getName(),
                activity.getType(),
                activity.getGroupId().toString(),
                activity.getCreatedBy().toString(),
                activity.getCreateAt() != null ? activity.getCreateAt().toString() : null);
    }
}
