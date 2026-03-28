package com.togetherly.demo.model.activity;

import com.togetherly.demo.model.Base;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * A single log entry — "User X did Activity Y on Date Z for N minutes."
 *
 * Unique constraint on (user_id, activity_id, logged_date) ensures
 * a user can only log once per activity per day.
 *
 * Uses LocalDate for logged_date (not Instant) because we care about
 * the calendar day, not the exact timestamp.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "activity_log",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "activity_id", "logged_date"}))
public class ActivityLog extends Base {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "activity_id", nullable = false)
    private UUID activityId;

    @Column(nullable = false)
    private int durationMins;

    @Column(length = 256)
    private String note;

    @Column(name = "logged_date", nullable = false)
    private LocalDate loggedDate;

    @CreationTimestamp
    private Instant createAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActivityLog log)) return false;
        return getUserId().equals(log.getUserId())
                && getActivityId().equals(log.getActivityId())
                && getLoggedDate().equals(log.getLoggedDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getActivityId(), getLoggedDate());
    }
}
