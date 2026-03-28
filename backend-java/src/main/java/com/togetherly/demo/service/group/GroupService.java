package com.togetherly.demo.service.group;

import com.togetherly.demo.data.group.response.GroupDetailResponse;
import com.togetherly.demo.data.group.response.GroupResponse;
import com.togetherly.demo.exception.AlreadyExist;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.NotFound;
import java.util.List;
import java.util.UUID;

/**
 * Business logic for group operations.
 *
 * All methods that modify state require a userId parameter
 * to enforce resource-level authorization.
 */
public interface GroupService {

    /** Create a new group. The creator becomes ADMIN automatically. */
    GroupDetailResponse createGroup(UUID userId, String name, String description);

    /** List all groups the user belongs to. */
    List<GroupResponse> getUserGroups(UUID userId);

    /** Get full group details including members. Only accessible by group members. */
    GroupDetailResponse getGroupDetail(UUID userId, UUID groupId)
            throws NotFound, InvalidOperation;

    /** Join a group using an invite code. */
    GroupResponse joinGroup(UUID userId, String inviteCode)
            throws NotFound, AlreadyExist, InvalidOperation;

    /** Leave a group. Admins cannot leave unless they are the last member. */
    void leaveGroup(UUID userId, UUID groupId)
            throws NotFound, InvalidOperation;
}
