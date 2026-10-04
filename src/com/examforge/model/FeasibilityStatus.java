package com.examforge.model;

public enum FeasibilityStatus {
    FEASIBLE("Feasible"),
    PROVEN_INFEASIBLE("Proven Infeasible"),
    SEARCH_LIMIT_REACHED("Search Limit Reached");

    private final String displayName;

    FeasibilityStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
