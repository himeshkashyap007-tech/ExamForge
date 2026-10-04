package com.examforge.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Blueprint {
    private int blueprintId;
    private String examName;
    private String subject;
    private int totalMarks;
    private int durationMinutes;
    private int numberOfQuestions;
    private BlueprintStatus status;
    private int createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BlueprintSection> sections;
    private List<BlueprintConstraint> constraints;
    private ExposurePolicyType exposurePolicyType;
    private int exposureWindow;

    public Blueprint() {
        this.status = BlueprintStatus.DRAFT;
        this.sections = new ArrayList<>();
        this.constraints = new ArrayList<>();
        this.exposurePolicyType = ExposurePolicyType.SOFT_AVOID;
        this.exposureWindow = 3;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int getBlueprintId() { return blueprintId; }
    public void setBlueprintId(int blueprintId) { this.blueprintId = blueprintId; }
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public int getNumberOfQuestions() { return numberOfQuestions; }
    public void setNumberOfQuestions(int numberOfQuestions) { this.numberOfQuestions = numberOfQuestions; }
    public BlueprintStatus getStatus() { return status; }
    public void setStatus(BlueprintStatus status) { this.status = status; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<BlueprintSection> getSections() { return sections; }
    public void setSections(List<BlueprintSection> sections) { this.sections = sections; }
    public List<BlueprintConstraint> getConstraints() { return constraints; }
    public void setConstraints(List<BlueprintConstraint> constraints) { this.constraints = constraints; }
    public ExposurePolicyType getExposurePolicyType() { return exposurePolicyType; }
    public void setExposurePolicyType(ExposurePolicyType exposurePolicyType) { this.exposurePolicyType = exposurePolicyType; }
    public int getExposureWindow() { return exposureWindow; }
    public void setExposureWindow(int exposureWindow) { this.exposureWindow = exposureWindow; }

    public void addSection(BlueprintSection section) {
        section.setBlueprintId(this.blueprintId);
        this.sections.add(section);
    }

    public void addConstraint(BlueprintConstraint constraint) {
        constraint.setBlueprintId(this.blueprintId);
        this.constraints.add(constraint);
    }

    public int getSectionMarksTotal() {
        return sections.stream().mapToInt(BlueprintSection::getTotalMarks).sum();
    }

    public int getSectionCountTotal() {
        return sections.stream().mapToInt(BlueprintSection::getQuestionCount).sum();
    }
}
