package com.togetherly.demo.data.activity.response;

import com.togetherly.demo.model.activity.Streak;

/**
 * A user's streak info for a single activity.
 */
public record StreakResponse(
        String userId,
        String activityId,
        String activityName,
        int currentStreak,
        int longestStreak,
        long totalMinutes,
        String lastLoggedDate) {

    public static StreakResponse from(Streak streak, String activityName) {
        return new StreakResponse(
                streak.getUserId().toString(),
                streak.getActivityId().toString(),
                activityName,
                streak.getCurrentStreak(),
                streak.getLongestStreak(),
                streak.getTotalMinutes(),
                streak.getLastLoggedDate() != null
                        ? streak.getLastLoggedDate().toString() : null);
    }
}
