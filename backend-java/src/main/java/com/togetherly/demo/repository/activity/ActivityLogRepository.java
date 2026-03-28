package com.togetherly.demo.repository.activity;

import com.togetherly.demo.model.activity.ActivityLog;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Data access for ActivityLog entries.
 *
 * Includes a custom JPQL query for fetching all logs for a group on a given date,
 * which joins through the activity table to filter by group_id.
 */
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    /** Check if a user already logged this activity today. */
    Optional<ActivityLog> findByUserIdAndActivityIdAndLoggedDate(
            UUID userId, UUID activityId, LocalDate loggedDate);

    /** All logs for a specific user and activity, ordered by date (for streak calculation). */
    List<ActivityLog> findByUserIdAndActivityIdOrderByLoggedDateDesc(
            UUID userId, UUID activityId);

    /** All logs for a specific activity on a given date (group dashboard). */
    List<ActivityLog> findByActivityIdAndLoggedDate(UUID activityId, LocalDate loggedDate);

    /** All logs for all activities in a group on a given date. */
    @Query("SELECT log FROM ActivityLog log WHERE log.activityId IN " +
            "(SELECT a.id FROM Activity a WHERE a.groupId = :groupId) " +
            "AND log.loggedDate = :date")
    List<ActivityLog> findByGroupIdAndDate(
            @Param("groupId") UUID groupId,
            @Param("date") LocalDate date);

    /** All logs for all activities in a group within a date range (weekly summary). */
    @Query("SELECT log FROM ActivityLog log WHERE log.activityId IN " +
            "(SELECT a.id FROM Activity a WHERE a.groupId = :groupId) " +
            "AND log.loggedDate BETWEEN :startDate AND :endDate")
    List<ActivityLog> findByGroupIdAndDateBetween(
            @Param("groupId") UUID groupId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
