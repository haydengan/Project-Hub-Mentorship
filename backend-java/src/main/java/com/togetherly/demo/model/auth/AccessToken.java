package com.togetherly.demo.model.auth;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Stores issued JWT access tokens in the database.
 *
 * WHY STORE JWTs IN THE DB?
 * JWTs are self-contained (they carry their own claims), so you CAN validate
 * them without a DB lookup. But storing them allows:
 * - Token revocation (delete from DB → token is invalid even if not expired)
 * - Tracking active sessions
 * - Cleaning up expired tokens via scheduled jobs
 *
 * NOTE: This entity does NOT extend Base. It manages its own UUID ID because
 * the ID is generated inline (not via @UUIDv7Id). The original code did this
 * because the token is generated in the constructor using the ID, so the ID
 * must exist before the entity is persisted. We keep this pattern.
 *
 * RELATIONSHIP: AccessToken ↔ RefreshToken (bidirectional one-to-one)
 *
 * @OneToOne(cascade = CascadeType.ALL) to RefreshToken:
 *   - CascadeType.ALL means: when you save/delete an AccessToken,
 *     the RefreshToken is also saved/deleted automatically.
 *   - FetchType.LAZY: don't load the RefreshToken from DB until you access it.
 *   - This is the OWNING side (has the foreign key column).
 *
 * @ManyToOne to User:
 *   - Many tokens can belong to one user (user logs in from multiple devices).
 *   - @OnDelete(CASCADE): if User is deleted, all their tokens are deleted at DB level.
 *
 * SIMPLIFIED: Removed the constructor that generated JWT strings using JwtConfig/JwtUtil.
 * Token generation will live in JwtService (business logic ≠ entity).
 *
 * HAND-WRITTEN.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
public class AccessToken {
    @Id
    @Column(unique = true, updatable = false, nullable = false)
    private UUID id = Generators.timeBasedEpochGenerator().generate();

    @Column(unique = true, updatable = false, nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(updatable = false, nullable = false)
    private Instant expireAt;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // delete RefreshToken → also delete this
    private RefreshToken refreshToken;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessToken that = (AccessToken) o;
        return id.equals(that.id) && token.equals(that.token) && expireAt.equals(that.expireAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, token, expireAt);
    }
}
