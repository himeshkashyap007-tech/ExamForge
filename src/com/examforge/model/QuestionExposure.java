package com.examforge.model;

import java.time.LocalDateTime;

public class QuestionExposure {
    private int exposureId;
    private int questionId;
    private int totalOfficialUsage;
    private LocalDateTime lastOfficialUse;
    private int recentUsageCount;
    private double exposureScore;

    public QuestionExposure() {}

    public QuestionExposure(int questionId) {
        this.questionId = questionId;
        this.totalOfficialUsage = 0;
        this.recentUsageCount = 0;
        this.exposureScore = 0;
    }

    public int getExposureId() { return exposureId; }
    public void setExposureId(int exposureId) { this.exposureId = exposureId; }
    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }
    public int getTotalOfficialUsage() { return totalOfficialUsage; }
    public void setTotalOfficialUsage(int totalOfficialUsage) { this.totalOfficialUsage = totalOfficialUsage; }
    public LocalDateTime getLastOfficialUse() { return lastOfficialUse; }
    public void setLastOfficialUse(LocalDateTime lastOfficialUse) { this.lastOfficialUse = lastOfficialUse; }
    public int getRecentUsageCount() { return recentUsageCount; }
    public void setRecentUsageCount(int recentUsageCount) { this.recentUsageCount = recentUsageCount; }
    public double getExposureScore() { return exposureScore; }
    public void setExposureScore(double exposureScore) { this.exposureScore = exposureScore; }

    public ExposureLevel getExposureLevel() {
        return ExposureLevel.fromUsageCount(totalOfficialUsage);
    }
}
