package com.togetherly.demo.model.activity;

import com.togetherly.demo.model.Base;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tracks a user's streak for a specific activity.
 *
 * "Stored" approach — we maintain current/longest streak in a row
 * rather than recalculating from all logs every time. This is fast
 * for reads (dashboard), and updated transactionally when a log is created.
 *
 * The streak is considered broken if lastLoggedDate is not yesterday or today.
 * A nightly scheduled job can also reset stale streaks.
 *
 * Unique constraint on (user_id, activity_id) — one streak record per user per activity.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "streak",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "activity_id"}))
public class Streak extends Base {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "activity_id", nullable = false)
    private UUID activityId;

    @Column(nullable = false)
    private int currentStreak = 0;

    @Column(nullable = false)
    private int longestStreak = 0;

    @Column(name = "last_logged_date")
    private LocalDate lastLoggedDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Streak streak)) return false;
        return getUserId().equals(streak.getUserId())
                && getActivityId().equals(streak.getActivityId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getActivityId());
    }
}
