package com.togetherly.demo.repository.group;

import com.togetherly.demo.model.group.Group;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for the Group entity.
 *
 * Spring Data JPA auto-generates the implementation from method names.
 * getByInviteCode → SELECT * FROM accountability_group WHERE invite_code = ?
 */
public interface GroupRepository extends JpaRepository<Group, UUID> {

    Optional<Group> findById(UUID id);

    Optional<Group> getByInviteCode(String inviteCode);
}
