package com.examforge.model;

import java.time.LocalDateTime;

public class ExaminationRecord {
    private int examId;
    private int paperId;
    private String examName;
    private String subject;
    private int totalMarks;
    private LocalDateTime finalizedAt;
    private int finalizedBy;

    public ExaminationRecord() {}

    public ExaminationRecord(int paperId, String examName, String subject, int totalMarks, int finalizedBy) {
        this.paperId = paperId;
        this.examName = examName;
        this.subject = subject;
        this.totalMarks = totalMarks;
        this.finalizedAt = LocalDateTime.now();
        this.finalizedBy = finalizedBy;
    }

    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }
    public int getPaperId() { return paperId; }
    public void setPaperId(int paperId) { this.paperId = paperId; }
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }
    public LocalDateTime getFinalizedAt() { return finalizedAt; }
    public void setFinalizedAt(LocalDateTime finalizedAt) { this.finalizedAt = finalizedAt; }
    public int getFinalizedBy() { return finalizedBy; }
    public void setFinalizedBy(int finalizedBy) { this.finalizedBy = finalizedBy; }
}
