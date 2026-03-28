package com.togetherly.demo.service.activity;

import com.togetherly.demo.data.activity.response.ActivityLogResponse;
import com.togetherly.demo.data.activity.response.ActivityResponse;
import com.togetherly.demo.data.activity.response.DailyGroupSummary;
import com.togetherly.demo.exception.AlreadyExist;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.NotFound;
import com.togetherly.demo.model.activity.Activity;
import com.togetherly.demo.model.activity.ActivityLog;
import com.togetherly.demo.model.activity.ActivityType;
import com.togetherly.demo.model.auth.User;
import com.togetherly.demo.model.group.GroupMember;
import com.togetherly.demo.repository.activity.ActivityLogRepository;
import com.togetherly.demo.repository.activity.ActivityRepository;
import com.togetherly.demo.repository.group.GroupMemberRepository;
import com.togetherly.demo.repository.group.GroupRepository;
import com.togetherly.demo.repository.user.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Activity service implementation.
 *
 * Handles creating activities, logging entries, and building group summaries.
 * All operations enforce group membership before proceeding.
 */
@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired private ActivityRepository activityRepository;
    @Autowired private ActivityLogRepository activityLogRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private GroupMemberRepository groupMemberRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private StreakService streakService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ActivityResponse createActivity(UUID userId, UUID groupId, String name, ActivityType type)
            throws NotFound, InvalidOperation, AlreadyExist {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFound("group not found !"));
        requireMembership(userId, groupId);

        // Check for duplicate activity name within the group
        if (activityRepository.findByGroupIdAndName(groupId, name).isPresent()) {
            throw new AlreadyExist("an activity with this name already exists in the group !");
        }

        Activity activity = new Activity();
        activity.setName(name);
        activity.setType(type);
        activity.setGroupId(groupId);
        activity.setCreatedBy(userId);
        activityRepository.save(activity);

        return ActivityResponse.from(activity);
    }

    @Override
    public List<ActivityResponse> getGroupActivities(UUID userId, UUID groupId)
            throws NotFound, InvalidOperation {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFound("group not found !"));
        requireMembership(userId, groupId);

        return activityRepository.findByGroupId(groupId).stream()
                .map(ActivityResponse::from)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ActivityLogResponse logActivity(UUID userId, UUID activityId,
                                            int durationMins, String note)
            throws NotFound, InvalidOperation, AlreadyExist {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFound("activity not found !"));

        // User must be a member of the group this activity belongs to
        requireMembership(userId, activity.getGroupId());

        LocalDate today = LocalDate.now();

        // One log per activity per day
        if (activityLogRepository.findByUserIdAndActivityIdAndLoggedDate(
                userId, activityId, today).isPresent()) {
            throw new AlreadyExist("you already logged this activity today !");
        }

        ActivityLog log = new ActivityLog();
        log.setUserId(userId);
        log.setActivityId(activityId);
        log.setDurationMins(durationMins);
        log.setNote(note);
        log.setLoggedDate(today);
        activityLogRepository.save(log);

        // Update streak in the same transaction
        streakService.updateStreak(userId, activityId, today);

        return ActivityLogResponse.from(log);
    }

    @Override
    public DailyGroupSummary getGroupLogsForDate(UUID userId, UUID groupId, LocalDate date)
            throws NotFound, InvalidOperation {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFound("group not found !"));
        requireMembership(userId, groupId);

        List<ActivityLog> logs = activityLogRepository.findByGroupIdAndDate(groupId, date);
        return buildDailySummary(groupId, date, logs);
    }

    @Override
    public List<DailyGroupSummary> getGroupLogsForWeek(UUID userId, UUID groupId,
                                                        LocalDate startDate, LocalDate endDate)
            throws NotFound, InvalidOperation {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFound("group not found !"));
        requireMembership(userId, groupId);

        List<ActivityLog> allLogs = activityLogRepository.findByGroupIdAndDateBetween(
                groupId, startDate, endDate);

        // Group logs by date, then build a summary for each day
        Map<LocalDate, List<ActivityLog>> logsByDate = new LinkedHashMap<>();
        for (ActivityLog log : allLogs) {
            logsByDate.computeIfAbsent(log.getLoggedDate(), k -> new ArrayList<>()).add(log);
        }

        // Fill in missing dates with empty summaries
        List<DailyGroupSummary> summaries = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<ActivityLog> dayLogs = logsByDate.getOrDefault(date, List.of());
            summaries.add(buildDailySummary(groupId, date, dayLogs));
        }

        return summaries;
    }

    // --- Helper methods ---

    private void requireMembership(UUID userId, UUID groupId) throws InvalidOperation {
        if (groupMemberRepository.findByUserIdAndGroupId(userId, groupId).isEmpty()) {
            throw new InvalidOperation("you are not a member of this group !");
        }
    }

    /**
     * Build a DailyGroupSummary from raw logs.
     * Groups logs by user, attaches username, returns structured response.
     */
    private DailyGroupSummary buildDailySummary(UUID groupId, LocalDate date,
                                                 List<ActivityLog> logs) {
        // Get all group members to show everyone (even those who didn't log)
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);

        // Group logs by userId
        Map<UUID, List<ActivityLogResponse>> logsByUser = new LinkedHashMap<>();
        for (ActivityLog log : logs) {
            logsByUser.computeIfAbsent(log.getUserId(), k -> new ArrayList<>())
                    .add(ActivityLogResponse.from(log));
        }

        // Build member summaries (include members with no logs as empty)
        List<DailyGroupSummary.MemberDailyLog> memberLogs = members.stream()
                .map(member -> {
                    User user = userRepository.findById(member.getUserId()).orElse(null);
                    if (user == null) return null;
                    List<ActivityLogResponse> userLogs =
                            logsByUser.getOrDefault(member.getUserId(), List.of());
                    return new DailyGroupSummary.MemberDailyLog(
                            user.getId().toString(),
                            user.getUserName(),
                            userLogs);
                })
                .filter(m -> m != null)
                .toList();

        return new DailyGroupSummary(date.toString(), memberLogs);
    }
}
