package com.examforge.model;

public enum Difficulty {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard");

    private final String displayName;

    Difficulty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Difficulty fromString(String value) {
        if (value == null) throw new IllegalArgumentException("Difficulty cannot be null");
        String normalized = value.trim().toUpperCase().replace(" ", "_");
        for (Difficulty d : values()) {
            if (d.name().equals(normalized)) return d;
        }
        throw new IllegalArgumentException("Invalid difficulty: " + value);
    }
}
