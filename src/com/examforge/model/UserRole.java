package com.examforge.model;

public enum UserRole {
    TEACHER("Teacher"),
    SUPER_ADMIN("Super Admin"),
    STUDENT("Student");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
