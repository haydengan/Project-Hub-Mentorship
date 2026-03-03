package com.togetherly.demo.model.auth;

import java.time.Instant;

import com.togetherly.demo.model.Base;
import com.togetherly.demo.utils.Utils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores email verification codes (e.g., "48302") sent during registration.
 *
 * FLOW:
 * 1. User registers → system generates a 5-digit code → saves it here
 * 2. System emails the code to the user
 * 3. User submits the code → system looks it up here → verifies it
 * 4. Code expires after a set time (expireAt)
 *
 * extends Base → inherits the UUID primary key with @UUIDv7Id
 *
 * The `code` field is initialized inline with Utils.randomNumericCode(5).
 * This means the code is generated when the Java object is created (new VerificationCode()),
 * NOT when it's saved to the DB. The DB just stores whatever value is already in the field.
 *
 */
@Getter
@Setter
@Entity
public class VerificationCode extends Base {
    @Column(length = 128, nullable = false)
    private String email;

    // Auto-generates a 5-digit code when the object is instantiated
    @Column(length = 5, nullable = false)
    private String code = Utils.randomNumericCode(5);

    @Column(updatable = false, nullable = false)
    private Instant expireAt;
}
