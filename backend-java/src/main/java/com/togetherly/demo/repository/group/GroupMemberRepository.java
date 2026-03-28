package com.togetherly.demo.repository.group;

import com.togetherly.demo.model.group.GroupMember;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for the GroupMember join entity.
 *
 * Provides queries for membership lookups:
 * - Find all groups a user belongs to
 * - Find all members of a group
 * - Check if a specific user is in a specific group
 */
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    List<GroupMember> findByUserId(UUID userId);

    List<GroupMember> findByGroupId(UUID groupId);

    Optional<GroupMember> findByUserIdAndGroupId(UUID userId, UUID groupId);

    long countByGroupId(UUID groupId);
}
