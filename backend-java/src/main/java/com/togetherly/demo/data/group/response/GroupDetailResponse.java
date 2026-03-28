package com.togetherly.demo.data.group.response;

import com.togetherly.demo.model.group.Group;
import java.util.List;

/**
 * Full group details including all members.
 * Returned when fetching a single group by ID.
 */
public record GroupDetailResponse(
        String id,
        String name,
        String description,
        String inviteCode,
        int maxMembers,
        String createdBy,
        String createdAt,
        List<GroupMemberResponse> members) {

    public static GroupDetailResponse from(Group group, List<GroupMemberResponse> members) {
        return new GroupDetailResponse(
                group.getId().toString(),
                group.getName(),
                group.getDescription(),
                group.getInviteCode(),
                group.getMaxMembers(),
                group.getCreatedBy().toString(),
                group.getCreateAt().toString(),
                members);
    }
}
