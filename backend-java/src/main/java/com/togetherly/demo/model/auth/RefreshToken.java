package com.togetherly.demo.model.auth;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.uuid.Generators;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores issued JWT refresh tokens in the database.
 *
 * REFRESH TOKEN FLOW:
 * 1. User logs in → system creates AccessToken + RefreshToken pair
 * 2. AccessToken expires (e.g., after 15 min)
 * 3. Client sends RefreshToken to /api/auth/refresh
 * 4. System validates the RefreshToken, issues a NEW AccessToken + RefreshToken pair
 * 5. Old pair is deleted
 *
 * This is the NON-OWNING side of the bidirectional relationship.
 *
 * @OneToOne(mappedBy = "refreshToken") — Says: "AccessToken owns the relationship.
 *   Look at AccessToken.refreshToken to find the foreign key."
 *   This means the RefreshToken table does NOT have a foreign key column to AccessToken.
 *   Instead, the AccessToken table has a refresh_token_id column.
 *
 *   cascade = CascadeType.ALL: when you delete a RefreshToken, its AccessToken
 *   is also deleted. HOWEVER, cascade doesn't work for bulk JPQL/SQL deletes,
 *   which is why AccessToken has @OnDelete(CASCADE) as a DB-level backup.
 *
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
public class RefreshToken {
    @Id
    @Column(unique = true, updatable = false, nullable = false)
    private UUID id = Generators.timeBasedEpochGenerator().generate();

    @Column(unique = true, updatable = false, nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(updatable = false, nullable = false)
    private Instant expireAt;

    // Non-owning side — AccessToken has the foreign key
    @OneToOne(mappedBy = "refreshToken", cascade = CascadeType.ALL)
    private AccessToken accessToken;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshToken that = (RefreshToken) o;
        return id.equals(that.id) && token.equals(that.token) && expireAt.equals(that.expireAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, token, expireAt);
    }
}
