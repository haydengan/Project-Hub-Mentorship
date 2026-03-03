package com.togetherly.demo.model.auth;

/**
 * Enum representing user roles in the system.
 *
 * ADMIN  — Full access, can manage users and roles
 * USER — Default role for newly registered users
 *
 * Used in User.java with @Enumerated(EnumType.STRING), which means
 * the DB stores the role as a string ("ADMIN", "USER")
 * rather than an integer index (0, 1). String storage is safer
 * because reordering the enum won't break existing data.
 */
public enum Role {
    ADMIN("ADMIN"),
    NORMAL("USER");

    private final String value;

    Role(String role) {
        this.value = role;
    }

    @Override
    public String toString() {
        return value;
    }
}
