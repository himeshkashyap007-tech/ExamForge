package com.examforge.model;

public class PaperQuestion {
    private int paperQuestionId;
    private int paperId;
    private int questionId;
    private int sectionIndex;
    private int sortOrder;
    private Question question;

    public PaperQuestion() {}

    public PaperQuestion(int paperId, int questionId, int sectionIndex, int sortOrder) {
        this.paperId = paperId;
        this.questionId = questionId;
        this.sectionIndex = sectionIndex;
        this.sortOrder = sortOrder;
    }

    public int getPaperQuestionId() { return paperQuestionId; }
    public void setPaperQuestionId(int paperQuestionId) { this.paperQuestionId = paperQuestionId; }
    public int getPaperId() { return paperId; }
    public void setPaperId(int paperId) { this.paperId = paperId; }
    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }
    public int getSectionIndex() { return sectionIndex; }
    public void setSectionIndex(int sectionIndex) { this.sectionIndex = sectionIndex; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
}
