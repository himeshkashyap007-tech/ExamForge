package com.examforge.model;

import java.time.LocalDateTime;

public class StudentAttempt {
    private int attemptId;
    private int studentId;
    private String practiceType;
    private String subject;
    private int totalQuestions;
    private int correctAnswers;
    private int totalMarks;
    private int marksObtained;
    private int timeSpentSeconds;
    private LocalDateTime attemptedAt;

    public StudentAttempt() {
        this.attemptedAt = LocalDateTime.now();
    }

    public StudentAttempt(int studentId, String practiceType, String subject,
                          int totalQuestions, int totalMarks, int marksObtained) {
        this.studentId = studentId;
        this.practiceType = practiceType;
        this.subject = subject;
        this.totalQuestions = totalQuestions;
        this.totalMarks = totalMarks;
        this.marksObtained = marksObtained;
        this.attemptedAt = LocalDateTime.now();
    }

    public int getAttemptId() { return attemptId; }
    public void setAttemptId(int attemptId) { this.attemptId = attemptId; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getPracticeType() { return practiceType; }
    public void setPracticeType(String practiceType) { this.practiceType = practiceType; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }
    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }
    public int getMarksObtained() { return marksObtained; }
    public void setMarksObtained(int marksObtained) { this.marksObtained = marksObtained; }
    public int getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(int timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }
    public LocalDateTime getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(LocalDateTime attemptedAt) { this.attemptedAt = attemptedAt; }

    public double getPercentage() {
        if (totalMarks == 0) return 0;
        return (marksObtained * 100.0) / totalMarks;
    }
}
