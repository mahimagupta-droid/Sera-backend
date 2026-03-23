package com.era.emergency.model;

/**
 * Severity levels for emergency cases.
 * Higher numeric value = more critical.
 */
public enum Severity {
    LOW(1, "Low Priority – Non-critical condition"),
    MEDIUM(2, "Medium Priority – Requires attention within 1 hour"),
    HIGH(3, "High Priority – Requires immediate attention"),
    CRITICAL(4, "Critical – Life-threatening emergency");

    private final int level;
    private final String description;

    Severity(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() { return level; }
    public String getDescription() { return description; }
}
