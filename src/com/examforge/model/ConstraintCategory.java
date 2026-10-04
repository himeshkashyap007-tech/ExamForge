package com.examforge.model;

public enum ConstraintCategory {
    UNIT("Unit"),
    DIFFICULTY("Difficulty"),
    QUESTION_TYPE("Question Type"),
    SECTION("Section"),
    EXPOSURE("Exposure");

    private final String displayName;

    ConstraintCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
