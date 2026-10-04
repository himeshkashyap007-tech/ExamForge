package com.examforge.model;

public enum RoundingPolicy {
    EXACT_INTEGER("Exact Integer"),
    ALLOW_TOLERANCE("Allow Tolerance");

    private final String displayName;

    RoundingPolicy(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
