package com.togetherly.demo.data.activity.response;

import com.togetherly.demo.model.activity.Streak;

/**
 * One entry in the group streak leaderboard.
 * Shows username, activity name, and their current/longest streak.
 */
public record GroupLeaderboardEntry(
        String userId,
        String username,
        String activityId,
        String activityName,
        int currentStreak,
        int longestStreak) {

    public static GroupLeaderboardEntry from(
            Streak streak, String username, String activityName) {
        return new GroupLeaderboardEntry(
                streak.getUserId().toString(),
                username,
                streak.getActivityId().toString(),
                activityName,
                streak.getCurrentStreak(),
                streak.getLongestStreak());
    }
}
