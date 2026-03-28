package com.togetherly.demo.model.group;

/**
 * Role a user holds within a specific group.
 *
 * ADMIN — the group creator or promoted member. Can manage members and activities.
 * MEMBER — a regular participant who can log activities and view the group.
 *
 * Stored as STRING in the database (not ORDINAL) so reordering the enum won't break data.
 */
public enum GroupRole {
    ADMIN("ADMIN"),
    MEMBER("MEMBER");

    private final String value;

    GroupRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
