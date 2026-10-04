package com.examforge.model;

public enum PaperStatus {
    DRAFT("Draft"),
    VALIDATED("Validated"),
    FINALIZED("Finalized");

    private final String displayName;

    PaperStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
