package com.examforge.model;

public class StudentResponse {
    private int responseId;
    private int attemptId;
    private int questionId;
    private String selectedOption;
    private boolean isCorrect;
    private int marksObtained;
    private Question question;

    public StudentResponse() {}

    public StudentResponse(int attemptId, int questionId, String selectedOption) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.selectedOption = selectedOption;
    }

    public int getResponseId() { return responseId; }
    public void setResponseId(int responseId) { this.responseId = responseId; }
    public int getAttemptId() { return attemptId; }
    public void setAttemptId(int attemptId) { this.attemptId = attemptId; }
    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }
    public String getSelectedOption() { return selectedOption; }
    public void setSelectedOption(String selectedOption) { this.selectedOption = selectedOption; }
    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }
    public int getMarksObtained() { return marksObtained; }
    public void setMarksObtained(int marksObtained) { this.marksObtained = marksObtained; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
}
