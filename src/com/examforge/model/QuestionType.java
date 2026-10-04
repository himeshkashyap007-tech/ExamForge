package com.examforge.model;

public enum QuestionType {
    MCQ("Multiple Choice"),
    SHORT_ANSWER("Short Answer"),
    LONG_ANSWER("Long Answer");

    private final String displayName;

    QuestionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static QuestionType fromString(String value) {
        if (value == null) throw new IllegalArgumentException("Question type cannot be null");
        String normalized = value.trim().toUpperCase().replace(" ", "_");
        for (QuestionType qt : values()) {
            if (qt.name().equals(normalized)) return qt;
        }
        throw new IllegalArgumentException("Invalid question type: " + value);
    }
}
