package com.togetherly.demo.model.group;

import com.togetherly.demo.model.Base;
import com.togetherly.demo.model.auth.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * A group where users track activities together.
 *
 * Each group has a unique invite code that others can use to join.
 * The creator is tracked via createdBy (stores the User's UUID).
 *
 * Table name "accountability_group" avoids the reserved word "group" in SQL.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "accountability_group")
public class Group extends Base {

    @Column(length = 64, nullable = false)
    private String name;

    @Column(length = 256)
    private String description;

    @Column(unique = true, length = 16, nullable = false)
    private String inviteCode;

    @Column(nullable = false)
    private UUID createdBy;

    @Column(nullable = false)
    private int maxMembers = 10;

    @CreationTimestamp
    private Instant createAt;

    @UpdateTimestamp
    private Instant updateAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group group)) return false;
        return getId().equals(group.getId())
                && getName().equals(group.getName())
                && getInviteCode().equals(group.getInviteCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getInviteCode());
    }
}
