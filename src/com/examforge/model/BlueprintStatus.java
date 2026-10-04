package com.examforge.model;

public enum BlueprintStatus {
    DRAFT("Draft"),
    VALID("Valid"),
    INVALID("Invalid"),
    INACTIVE("Inactive");

    private final String displayName;

    BlueprintStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
