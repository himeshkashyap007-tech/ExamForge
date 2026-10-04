package com.examforge.model;

public enum ConstraintBasis {
    MARKS("Marks"),
    COUNT("Count"),
    PERCENTAGE("Percentage");

    private final String displayName;

    ConstraintBasis(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
