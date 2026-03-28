package com.togetherly.demo.repository.activity;

import com.togetherly.demo.model.activity.Streak;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Data access for Streak entities.
 *
 * Provides lookups for:
 * - A specific user's streak on a specific activity
 * - All of a user's streaks across activities
 * - All streaks for activities in a group (leaderboard)
 */
public interface StreakRepository extends JpaRepository<Streak, UUID> {

    Optional<Streak> findByUserIdAndActivityId(UUID userId, UUID activityId);

    List<Streak> findByUserId(UUID userId);

    /** All streaks for all activities in a group, ordered by current streak descending. */
    @Query("SELECT s FROM Streak s WHERE s.activityId IN " +
            "(SELECT a.id FROM Activity a WHERE a.groupId = :groupId) " +
            "ORDER BY s.currentStreak DESC")
    List<Streak> findByGroupIdOrderByCurrentStreakDesc(@Param("groupId") UUID groupId);
}
