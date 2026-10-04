package com.examforge.service;

import com.examforge.dao.QuestionDAO;
import com.examforge.dao.impl.QuestionDAOImpl;
import com.examforge.exception.DatabaseException;
import com.examforge.exception.ExamForgeException;
import com.examforge.exception.QuestionValidationException;
import com.examforge.model.Question;
import com.examforge.util.ValidationUtil;

import java.util.List;

public class QuestionService {
    private final QuestionDAO questionDAO;

    public QuestionService() {
        this.questionDAO = new QuestionDAOImpl();
    }

    public Question addQuestion(Question question) throws ExamForgeException {
        List<String> errors = ValidationUtil.validateQuestion(question);
        if (!errors.isEmpty()) {
            throw new QuestionValidationException("Question validation failed", errors);
        }
        try {
            question.setActiveStatus(true);
            int id = questionDAO.insert(question);
            question.setQuestionId(id);
            return question;
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to add question: " + e.getMessage(), e);
        }
    }

    public boolean updateQuestion(Question question) throws ExamForgeException {
        List<String> errors = ValidationUtil.validateQuestion(question);
        if (!errors.isEmpty()) {
            throw new QuestionValidationException("Question validation failed", errors);
        }
        try {
            return questionDAO.update(question);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to update question: " + e.getMessage(), e);
        }
    }

    public boolean deactivateQuestion(int questionId) throws ExamForgeException {
        try {
            return questionDAO.deactivate(questionId);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to deactivate question: " + e.getMessage(), e);
        }
    }

    public boolean activateQuestion(int questionId) throws ExamForgeException {
        try {
            return questionDAO.activate(questionId);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to activate question: " + e.getMessage(), e);
        }
    }

    public Question findById(int id) throws ExamForgeException {
        try {
            return questionDAO.findById(id);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error finding question: " + e.getMessage(), e);
        }
    }

    public List<Question> findAll() throws ExamForgeException {
        try {
            return questionDAO.findAll();
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error listing questions: " + e.getMessage(), e);
        }
    }

    public List<Question> findActiveBySubject(String subject) throws ExamForgeException {
        try {
            return questionDAO.findActiveBySubject(subject);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error listing questions: " + e.getMessage(), e);
        }
    }

    public List<Question> findByFilters(String subject, String unit, String difficulty, String questionType) throws ExamForgeException {
        try {
            return questionDAO.findByFilters(subject, unit, difficulty, questionType);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error listing questions: " + e.getMessage(), e);
        }
    }

    public List<String> findDistinctSubjects() throws ExamForgeException {
        try {
            return questionDAO.findDistinctSubjects();
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error listing subjects: " + e.getMessage(), e);
        }
    }

    public List<String> findDistinctUnits(String subject) throws ExamForgeException {
        try {
            return questionDAO.findDistinctUnits(subject);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error listing units: " + e.getMessage(), e);
        }
    }

    public List<String> findDistinctTopics(String subject, String unit) throws ExamForgeException {
        try {
            return questionDAO.findDistinctTopics(subject, unit);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error listing topics: " + e.getMessage(), e);
        }
    }

    public int countActiveBySubject(String subject) throws ExamForgeException {
        try {
            return questionDAO.countActiveBySubject(subject);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error counting questions: " + e.getMessage(), e);
        }
    }
}
