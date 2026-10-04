package com.examforge.model;

public enum ExposureLevel {
    LOW("Low", 0, 1),
    MEDIUM("Medium", 2, 3),
    HIGH("High", 4, Integer.MAX_VALUE);

    private final String displayName;
    private final int minCount;
    private final int maxCount;

    ExposureLevel(String displayName, int minCount, int maxCount) {
        this.displayName = displayName;
        this.minCount = minCount;
        this.maxCount = maxCount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinCount() {
        return minCount;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public static ExposureLevel fromUsageCount(int count) {
        if (count <= 1) return LOW;
        if (count <= 3) return MEDIUM;
        return HIGH;
    }
}
