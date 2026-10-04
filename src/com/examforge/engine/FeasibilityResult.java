package com.examforge.engine;

import com.examforge.model.FeasibilityStatus;

public class FeasibilityResult {
    private final FeasibilityStatus status;
    private final String message;
    private final int candidateCount;

    public FeasibilityResult(FeasibilityStatus status, String message) {
        this(status, message, 0);
    }

    public FeasibilityResult(FeasibilityStatus status, String message, int candidateCount) {
        this.status = status;
        this.message = message;
        this.candidateCount = candidateCount;
    }

    public FeasibilityStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public int getCandidateCount() { return candidateCount; }

    @Override
    public String toString() {
        return status.getDisplayName() + ": " + message;
    }
}
