package com.togetherly.demo.data.activity.response;

import java.util.List;

/**
 * Summary of all activity logs for a group on a specific date.
 * Used for the group dashboard — shows who logged what today.
 */
public record DailyGroupSummary(
        String date,
        List<MemberDailyLog> members) {

    /**
     * One member's logs for the day across all activities.
     */
    public record MemberDailyLog(
            String userId,
            String username,
            List<ActivityLogResponse> logs) {}
}
