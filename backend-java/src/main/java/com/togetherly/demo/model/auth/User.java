package com.togetherly.demo.model.auth;

import com.togetherly.demo.model.Base;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * The main User entity — represents the "account_user" table in the database.
 *
 * ANNOTATION GUIDE:
 *
 * @Entity — Tells JPA: "this class maps to a database table."
 *   Hibernate will create/manage the table based on this class.
 *
 * @Table(name = "account_user") — Overrides the default table name.
 *   Without this, the table would be named "user" (from the class name),
 *   but "user" is a reserved word in PostgreSQL, so we use "account_user".
 *
 * @Getter/@Setter (Lombok) — Auto-generates getters and setters for all fields.
 *   We DON'T use @Data (see Base.java comments for why).
 *
 * @NoArgsConstructor (Lombok) — Generates a no-argument constructor.
 *   JPA/Hibernate REQUIRES a no-arg constructor to create entity instances
 *   via reflection. Without this, Hibernate crashes at startup.
 *
 * extends Base — Inherits the UUID `id` field with @UUIDv7Id generation.
 *
 * FIELD-LEVEL ANNOTATIONS:
 *
 * @Version — Optimistic locking. Hibernate checks this timestamp before UPDATE.
 *   If another transaction modified the row since we read it, Hibernate throws
 *   OptimisticLockException instead of silently overwriting. Prevents lost updates.
 *
 * @Column(unique=true, length=32, nullable=false) — DB column constraints.
 *   These generate: VARCHAR(32) UNIQUE NOT NULL in the DDL.
 *
 * @Enumerated(EnumType.STRING) — Store the enum as a string in the DB.
 *   EnumType.ORDINAL would store 0/1/2 which breaks if you reorder the enum.
 *
 * @CreationTimestamp / @UpdateTimestamp — Hibernate auto-sets these.
 *   createAt is set once when the row is first inserted.
 *   updateAt is updated every time the row is modified.
 *
 * @Embedded — Embeds LoginAttempt's fields (attempts, lastAttempt) directly
 *   into this table. No separate table is created for LoginAttempt.
 *
 * HAND-WRITTEN. You define what fields a user has in your system.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "account_user")
public class User extends Base {

    // Optimistic locking version — prevents concurrent updates from overwriting each other
    @Version
    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT now()")
    private Instant version;

    @Column(unique = true, length = 32, nullable = false)
    private String userName;

    @Column(length = 64, nullable = false)
    private String password;

    @Column(unique = true, length = 128, nullable = false)
    private String email;

    @Column(length = 32, nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.NORMAL; // default role for new users

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isActive = true; // new users are active by default

    @CreationTimestamp
    private Instant createAt;

    @UpdateTimestamp
    private Instant updateAt;

    // When the user last authenticated (null if never)
    @Column(nullable = true)
    private Instant authAt;

    // Embedded login attempt tracking (fields go into THIS table)
    @Embedded
    private LoginAttempt loginAttempt = new LoginAttempt();

    /**
     * Business key equality — uses userName + email (natural identifiers).
     *
     * WHY NOT use the UUID id?
     * Before an entity is persisted (saved to DB), Hibernate hasn't generated
     * the ID yet — it's null. If you put a new User in a HashSet, then save it,
     * the hashCode changes and the Set can't find it anymore. Business keys
     * (userName, email) are known before persistence, so they're stable.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return isActive() == user.isActive()
                && getId().equals(user.getId())
                && getUserName().equals(user.getUserName())
                && getEmail().equals(user.getEmail())
                && getRole() == user.getRole();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUserName(), getEmail(), getRole(), isActive());
    }
}
