package com.togetherly.demo.service.group;

import com.togetherly.demo.data.group.response.GroupDetailResponse;
import com.togetherly.demo.data.group.response.GroupMemberResponse;
import com.togetherly.demo.data.group.response.GroupResponse;
import com.togetherly.demo.exception.AlreadyExist;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.NotFound;
import com.togetherly.demo.model.auth.User;
import com.togetherly.demo.model.group.Group;
import com.togetherly.demo.model.group.GroupMember;
import com.togetherly.demo.model.group.GroupRole;
import com.togetherly.demo.repository.group.GroupMemberRepository;
import com.togetherly.demo.repository.group.GroupRepository;
import com.togetherly.demo.repository.user.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Group service implementation.
 *
 * Handles group creation, joining, leaving, and detail retrieval.
 * Enforces resource-level authorization: only group members can view/interact with a group.
 */
@Service
public class GroupServiceImpl implements GroupService {

    @Autowired private GroupRepository groupRepository;
    @Autowired private GroupMemberRepository groupMemberRepository;
    @Autowired private UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public GroupDetailResponse createGroup(UUID userId, String name, String description) {
        // Generate a short unique invite code
        String inviteCode = UUID.randomUUID().toString().substring(0, 8);

        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        group.setInviteCode(inviteCode);
        group.setCreatedBy(userId);
        groupRepository.save(group);

        // Creator automatically becomes ADMIN
        GroupMember member = new GroupMember();
        member.setUserId(userId);
        member.setGroupId(group.getId());
        member.setRole(GroupRole.ADMIN);
        groupMemberRepository.save(member);

        List<GroupMemberResponse> members = buildMemberResponses(group.getId());
        return GroupDetailResponse.from(group, members);
    }

    @Override
    public List<GroupResponse> getUserGroups(UUID userId) {
        List<GroupMember> memberships = groupMemberRepository.findByUserId(userId);

        return memberships.stream()
                .map(membership -> {
                    Group group = groupRepository.findById(membership.getGroupId()).orElse(null);
                    if (group == null) return null;
                    long count = groupMemberRepository.countByGroupId(group.getId());
                    return GroupResponse.from(group, count);
                })
                .filter(response -> response != null)
                .toList();
    }

    @Override
    public GroupDetailResponse getGroupDetail(UUID userId, UUID groupId)
            throws NotFound, InvalidOperation {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFound("group not found !"));

        // Only members can view group details
        requireMembership(userId, groupId);

        List<GroupMemberResponse> members = buildMemberResponses(groupId);
        return GroupDetailResponse.from(group, members);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public GroupResponse joinGroup(UUID userId, String inviteCode)
            throws NotFound, AlreadyExist, InvalidOperation {
        Group group = groupRepository.getByInviteCode(inviteCode)
                .orElseThrow(() -> new NotFound("invalid invite code !"));

        // Check if already a member
        if (groupMemberRepository.findByUserIdAndGroupId(userId, group.getId()).isPresent()) {
            throw new AlreadyExist("you are already a member of this group !");
        }

        // Check capacity
        long currentCount = groupMemberRepository.countByGroupId(group.getId());
        if (currentCount >= group.getMaxMembers()) {
            throw new InvalidOperation("group is full !");
        }

        GroupMember member = new GroupMember();
        member.setUserId(userId);
        member.setGroupId(group.getId());
        member.setRole(GroupRole.MEMBER);
        groupMemberRepository.save(member);

        long count = groupMemberRepository.countByGroupId(group.getId());
        return GroupResponse.from(group, count);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void leaveGroup(UUID userId, UUID groupId)
            throws NotFound, InvalidOperation {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFound("group not found !"));

        GroupMember member = groupMemberRepository.findByUserIdAndGroupId(userId, groupId)
                .orElseThrow(() -> new NotFound("you are not a member of this group !"));

        // If user is the only ADMIN, they can't leave unless they're the last member
        if (member.getRole() == GroupRole.ADMIN) {
            long totalMembers = groupMemberRepository.countByGroupId(groupId);
            if (totalMembers > 1) {
                // Check if there's another admin
                List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
                long adminCount = members.stream()
                        .filter(m -> m.getRole() == GroupRole.ADMIN)
                        .count();
                if (adminCount <= 1) {
                    throw new InvalidOperation(
                            "you are the only admin — promote another member before leaving !");
                }
            }
        }

        groupMemberRepository.delete(member);

        // If no members left, delete the group
        long remaining = groupMemberRepository.countByGroupId(groupId);
        if (remaining == 0) {
            groupRepository.deleteById(groupId);
        }
    }

    // --- Helper methods ---

    private void requireMembership(UUID userId, UUID groupId) throws InvalidOperation {
        if (groupMemberRepository.findByUserIdAndGroupId(userId, groupId).isEmpty()) {
            throw new InvalidOperation("you are not a member of this group !");
        }
    }

    private List<GroupMemberResponse> buildMemberResponses(UUID groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        return members.stream()
                .map(member -> {
                    User user = userRepository.findById(member.getUserId()).orElse(null);
                    if (user == null) return null;
                    return GroupMemberResponse.from(member, user);
                })
                .filter(response -> response != null)
                .toList();
    }
}
