package com.examforge.model;

public class BlueprintSection {
    private int sectionId;
    private int blueprintId;
    private String sectionName;
    private int questionCount;
    private int totalMarks;
    private String questionTypeFilter;
    private String difficultyFilter;
    private int sortOrder;

    public BlueprintSection() {}

    public BlueprintSection(String sectionName, int questionCount, int totalMarks) {
        this.sectionName = sectionName;
        this.questionCount = questionCount;
        this.totalMarks = totalMarks;
    }

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
    public int getBlueprintId() { return blueprintId; }
    public void setBlueprintId(int blueprintId) { this.blueprintId = blueprintId; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }
    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }
    public String getQuestionTypeFilter() { return questionTypeFilter; }
    public void setQuestionTypeFilter(String questionTypeFilter) { this.questionTypeFilter = questionTypeFilter; }
    public String getDifficultyFilter() { return difficultyFilter; }
    public void setDifficultyFilter(String difficultyFilter) { this.difficultyFilter = difficultyFilter; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
