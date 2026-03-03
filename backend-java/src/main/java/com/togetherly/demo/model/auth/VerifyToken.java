package com.togetherly.demo.model.auth;

import java.time.Instant;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.togetherly.demo.model.Base;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores password reset tokens. When a user clicks "Forgot Password",
 * the system generates a token, saves it here, and emails a reset link.
 *
 * RELATIONSHIP ANNOTATIONS:
 *
 * @OneToOne — Each VerifyToken belongs to exactly one User, and each User
 *   can have at most one active VerifyToken.
 *
 * @JoinColumn(unique = true) — Creates a foreign key column in this table
 *   pointing to the User table. "unique = true" enforces the one-to-one
 *   relationship at the DB level.
 *
 * @OnDelete(action = OnDeleteAction.CASCADE) — If the User is deleted from
 *   the DB, this VerifyToken is automatically deleted too. This is a
 *   DATABASE-LEVEL cascade (not JPA-level), meaning it works even for
 *   bulk deletes via SQL/JPQL.
 *
 */
@Getter
@Setter
@Entity
public class VerifyToken extends Base {
    @Column(unique = true, updatable = false, nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(updatable = false, nullable = false)
    private Instant expireAt;

    @OneToOne
    @JoinColumn(unique = true) // unidirectional one-to-one
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
}
