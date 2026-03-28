package com.togetherly.demo.model.activity;

/**
 * Predefined activity categories.
 *
 * Groups can choose from these when creating activities.
 * Stored as STRING in the database for safety.
 */
public enum ActivityType {
    GYM("GYM"),
    STUDY("STUDY"),
    READING("READING"),
    MEDITATION("MEDITATION"),
    CODING("CODING"),
    RUNNING("RUNNING"),
    CUSTOM("CUSTOM");

    private final String value;

    ActivityType(String value) {
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
