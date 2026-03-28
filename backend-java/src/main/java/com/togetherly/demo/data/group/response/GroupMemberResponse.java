package com.togetherly.demo.data.group.response;

import com.togetherly.demo.model.auth.User;
import com.togetherly.demo.model.group.GroupMember;
import com.togetherly.demo.model.group.GroupRole;

/**
 * A member's info within a group, returned as part of GroupDetailResponse.
 * Combines data from GroupMember (role, joinedAt) and User (username, email).
 */
public record GroupMemberResponse(
        String userId,
        String username,
        String email,
        GroupRole role,
        String joinedAt) {

    public static GroupMemberResponse from(GroupMember member, User user) {
        return new GroupMemberResponse(
                user.getId().toString(),
                user.getUserName(),
                user.getEmail(),
                member.getRole(),
                member.getJoinedAt().toString());
    }
}
