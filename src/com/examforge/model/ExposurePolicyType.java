package com.examforge.model;

public enum ExposurePolicyType {
    SOFT_AVOID("Soft Avoid"),
    STRICT_BLOCK("Strict Block");

    private final String displayName;

    ExposurePolicyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
