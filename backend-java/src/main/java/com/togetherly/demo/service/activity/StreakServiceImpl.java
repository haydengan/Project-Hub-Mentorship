package com.togetherly.demo.service.activity;

import com.togetherly.demo.data.activity.response.GroupLeaderboardEntry;
import com.togetherly.demo.data.activity.response.StreakResponse;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.NotFound;
import com.togetherly.demo.model.activity.Activity;
import com.togetherly.demo.model.activity.Streak;
import com.togetherly.demo.model.auth.User;
import com.togetherly.demo.repository.activity.ActivityRepository;
import com.togetherly.demo.repository.activity.StreakRepository;
import com.togetherly.demo.repository.group.GroupMemberRepository;
import com.togetherly.demo.repository.group.GroupRepository;
import com.togetherly.demo.repository.user.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Streak service implementation.
 *
 * STREAK ALGORITHM:
 * When a user logs an activity, we check their last logged date:
 *
 *   lastLoggedDate == today     → already counted, no change
 *   lastLoggedDate == yesterday → consecutive day, increment streak
 *   lastLoggedDate == null      → first time ever, start at 1
 *   anything else               → streak broken, reset to 1
 *
 * Longest streak is updated whenever current streak surpasses it.
 * This runs inside the same transaction as the log insert.
 */
@Service
public class StreakServiceImpl implements StreakService {

    @Autowired private StreakRepository streakRepository;
    @Autowired private ActivityRepository activityRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private GroupMemberRepository groupMemberRepository;
    @Autowired private UserRepository userRepository;

    @Override
    public void updateStreak(UUID userId, UUID activityId, LocalDate loggedDate) {
        Streak streak = streakRepository.findByUserIdAndActivityId(userId, activityId)
                .orElse(null);

        if (streak == null) {
            // First time logging this activity — create streak record
            streak = new Streak();
            streak.setUserId(userId);
            streak.setActivityId(activityId);
            streak.setCurrentStreak(1);
            streak.setLongestStreak(1);
            streak.setLastLoggedDate(loggedDate);
            streakRepository.save(streak);
            return;
        }

        LocalDate lastDate = streak.getLastLoggedDate();

        if (lastDate != null && lastDate.equals(loggedDate)) {
            // Already logged today — no streak change
            return;
        }

        if (lastDate != null && lastDate.plusDays(1).equals(loggedDate)) {
            // Consecutive day — increment streak
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        } else {
            // Streak broken (gap of 2+ days or first log) — reset to 1
            streak.setCurrentStreak(1);
        }

        // Update longest streak if current exceeds it
        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }

        streak.setLastLoggedDate(loggedDate);
        streakRepository.save(streak);
    }

    @Override
    public List<StreakResponse> getMyStreaks(UUID userId) {
        List<Streak> streaks = streakRepository.findByUserId(userId);

        return streaks.stream()
                .map(streak -> {
                    String activityName = activityRepository.findById(streak.getActivityId())
                            .map(Activity::getName)
                            .orElse("Unknown");

                    // Check if streak is still active (last log was today or yesterday)
                    LocalDate today = LocalDate.now();
                    if (streak.getLastLoggedDate() != null
                            && streak.getLastLoggedDate().isBefore(today.minusDays(1))) {
                        // Streak has expired — show 0 current but keep longest
                        return new StreakResponse(
                                streak.getUserId().toString(),
                                streak.getActivityId().toString(),
                                activityName,
                                0,
                                streak.getLongestStreak(),
                                streak.getLastLoggedDate().toString());
                    }

                    return StreakResponse.from(streak, activityName);
                })
                .toList();
    }

    @Override
    public List<GroupLeaderboardEntry> getGroupLeaderboard(UUID userId, UUID groupId)
            throws NotFound, InvalidOperation {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFound("group not found !"));

        if (groupMemberRepository.findByUserIdAndGroupId(userId, groupId).isEmpty()) {
            throw new InvalidOperation("you are not a member of this group !");
        }

        List<Streak> streaks = streakRepository.findByGroupIdOrderByCurrentStreakDesc(groupId);
        LocalDate today = LocalDate.now();

        return streaks.stream()
                .map(streak -> {
                    String username = userRepository.findById(streak.getUserId())
                            .map(User::getUserName)
                            .orElse("Unknown");
                    String activityName = activityRepository.findById(streak.getActivityId())
                            .map(Activity::getName)
                            .orElse("Unknown");

                    // If streak has expired, show 0
                    int currentStreak = streak.getCurrentStreak();
                    if (streak.getLastLoggedDate() != null
                            && streak.getLastLoggedDate().isBefore(today.minusDays(1))) {
                        currentStreak = 0;
                    }

                    return new GroupLeaderboardEntry(
                            streak.getUserId().toString(),
                            username,
                            streak.getActivityId().toString(),
                            activityName,
                            currentStreak,
                            streak.getLongestStreak());
                })
                .toList();
    }
}
