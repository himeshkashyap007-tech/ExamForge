package com.examforge.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Paper {
    private int paperId;
    private int blueprintId;
    private String examName;
    private PaperStatus status;
    private int totalMarks;
    private int questionCount;
    private int createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PaperQuestion> questions;
    private Blueprint blueprint;

    public Paper() {
        this.status = PaperStatus.DRAFT;
        this.questions = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int getPaperId() { return paperId; }
    public void setPaperId(int paperId) { this.paperId = paperId; }
    public int getBlueprintId() { return blueprintId; }
    public void setBlueprintId(int blueprintId) { this.blueprintId = blueprintId; }
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public PaperStatus getStatus() { return status; }
    public void setStatus(PaperStatus status) { this.status = status; }
    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }
    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<PaperQuestion> getQuestions() { return questions; }
    public void setQuestions(List<PaperQuestion> questions) { this.questions = questions; }
    public Blueprint getBlueprint() { return blueprint; }
    public void setBlueprint(Blueprint blueprint) { this.blueprint = blueprint; }

    public void addQuestion(PaperQuestion pq) {
        pq.setPaperId(this.paperId);
        this.questions.add(pq);
    }
}
