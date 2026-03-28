package com.togetherly.demo.model.activity;

import com.togetherly.demo.model.Base;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * An activity that a group tracks (e.g., "Morning Gym", "DSA Study").
 *
 * Belongs to a group — all group members can log entries against it.
 * The name is unique within a group (enforced by unique constraint).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "activity",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "name"}))
public class Activity extends Base {

    @Column(length = 64, nullable = false)
    private String name;

    @Column(length = 32, nullable = false)
    @Enumerated(EnumType.STRING)
    private ActivityType type = ActivityType.CUSTOM;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    private Instant createAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Activity activity)) return false;
        return getId().equals(activity.getId())
                && getName().equals(activity.getName())
                && getGroupId().equals(activity.getGroupId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getGroupId());
    }
}
