package com.examforge.model;

public enum ConstraintPriority {
    HARD("Hard"),
    SOFT("Soft");

    private final String displayName;

    ConstraintPriority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
