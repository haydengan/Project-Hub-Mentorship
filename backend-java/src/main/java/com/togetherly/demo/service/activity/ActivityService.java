package com.togetherly.demo.service.activity;

import com.togetherly.demo.data.activity.response.ActivityLogResponse;
import com.togetherly.demo.data.activity.response.ActivityResponse;
import com.togetherly.demo.data.activity.response.DailyGroupSummary;
import com.togetherly.demo.exception.AlreadyExist;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.NotFound;
import com.togetherly.demo.model.activity.ActivityType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Business logic for activities and activity logging.
 */
public interface ActivityService {

    /** Create a new activity within a group. Only group members can create. */
    ActivityResponse createActivity(UUID userId, UUID groupId, String name, ActivityType type)
            throws NotFound, InvalidOperation, AlreadyExist;

    /** List all activities for a group. Requires membership. */
    List<ActivityResponse> getGroupActivities(UUID userId, UUID groupId)
            throws NotFound, InvalidOperation;

    /** Log an activity entry for today. One log per activity per day. */
    ActivityLogResponse logActivity(UUID userId, UUID activityId, int durationMins, String note,
                                     String mediaUrl)
            throws NotFound, InvalidOperation, AlreadyExist;

    /** Get today's logs for all members in a group. */
    DailyGroupSummary getGroupLogsForDate(UUID userId, UUID groupId, LocalDate date)
            throws NotFound, InvalidOperation;

    /** Get logs for a group over a date range (weekly summary). */
    List<DailyGroupSummary> getGroupLogsForWeek(UUID userId, UUID groupId,
                                                 LocalDate startDate, LocalDate endDate)
            throws NotFound, InvalidOperation;
}
