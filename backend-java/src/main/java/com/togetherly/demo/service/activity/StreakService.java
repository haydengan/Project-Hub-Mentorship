package com.togetherly.demo.service.activity;

import com.togetherly.demo.data.activity.response.GroupLeaderboardEntry;
import com.togetherly.demo.data.activity.response.StreakResponse;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.NotFound;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Business logic for streak tracking and leaderboards.
 */
public interface StreakService {

    /**
     * Update a user's streak and total minutes after they log an activity.
     * Called transactionally from ActivityServiceImpl.
     *
     * Streak logic:
     * - If lastLoggedDate == yesterday → increment current streak
     * - If lastLoggedDate == today → no streak change (already counted today)
     * - Otherwise → streak resets to 1
     * - Update longest streak if current exceeds it
     *
     * Total minutes are always accumulated regardless of streak.
     */
    void updateStreak(UUID userId, UUID activityId, LocalDate loggedDate, int durationMins);

    /** Get all streaks for the current user across all their activities. */
    List<StreakResponse> getMyStreaks(UUID userId);

    /** Get the streak leaderboard for a group. Requires membership. */
    List<GroupLeaderboardEntry> getGroupLeaderboard(UUID userId, UUID groupId)
            throws NotFound, InvalidOperation;
}
