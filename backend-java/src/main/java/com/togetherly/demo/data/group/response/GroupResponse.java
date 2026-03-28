package com.togetherly.demo.data.group.response;

import com.togetherly.demo.model.group.Group;

/**
 * Summary view of a group, used in list responses.
 * Does not include the full member list (use GroupDetailResponse for that).
 */
public record GroupResponse(
        String id,
        String name,
        String description,
        String inviteCode,
        int maxMembers,
        long memberCount,
        String createdAt) {

    public static GroupResponse from(Group group, long memberCount) {
        return new GroupResponse(
                group.getId().toString(),
                group.getName(),
                group.getDescription(),
                group.getInviteCode(),
                group.getMaxMembers(),
                memberCount,
                group.getCreateAt().toString());
    }
}
