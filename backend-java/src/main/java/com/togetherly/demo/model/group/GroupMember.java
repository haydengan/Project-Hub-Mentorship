package com.togetherly.demo.model.group;

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
 * Join table between User and Group, with extra fields (role, joinedAt).
 *
 * We use a separate entity instead of @ManyToMany because we need to store
 * the member's role within the group and when they joined.
 *
 * Unique constraint on (userId, groupId) prevents a user from joining the same group twice.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "group_member",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "group_id"}))
public class GroupMember extends Base {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(length = 32, nullable = false)
    @Enumerated(EnumType.STRING)
    private GroupRole role = GroupRole.MEMBER;

    @CreationTimestamp
    private Instant joinedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupMember that)) return false;
        return getUserId().equals(that.getUserId())
                && getGroupId().equals(that.getGroupId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getGroupId());
    }
}
