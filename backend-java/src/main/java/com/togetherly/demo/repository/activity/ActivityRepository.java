package com.togetherly.demo.repository.activity;

import com.togetherly.demo.model.activity.Activity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for Activity entities.
 *
 * findByGroupId → SELECT * FROM activity WHERE group_id = ?
 * findByGroupIdAndName → check for duplicate activity names within a group
 */
public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    List<Activity> findByGroupId(UUID groupId);

    Optional<Activity> findByGroupIdAndName(UUID groupId, String name);
}
