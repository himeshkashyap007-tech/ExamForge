package com.examforge.model;

public enum ValidationCheckStatus {
    PASS("Pass"),
    FAIL("Fail");

    private final String displayName;

    ValidationCheckStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
