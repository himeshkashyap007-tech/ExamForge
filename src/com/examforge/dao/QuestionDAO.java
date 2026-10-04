package com.examforge.dao;

import com.examforge.exception.DatabaseException;
import com.examforge.model.Question;
import java.util.List;

public interface QuestionDAO {
    Question findById(int id) throws DatabaseException;
    List<Question> findAll() throws DatabaseException;
    List<Question> findActiveBySubject(String subject) throws DatabaseException;
    List<Question> findBySubject(String subject) throws DatabaseException;
    List<Question> findByFilters(String subject, String unit, String difficulty, String questionType) throws DatabaseException;
    List<Question> findInactiveQuestions() throws DatabaseException;
    int insert(Question question) throws DatabaseException;
    boolean update(Question question) throws DatabaseException;
    boolean deactivate(int questionId) throws DatabaseException;
    boolean activate(int questionId) throws DatabaseException;
    boolean delete(int questionId) throws DatabaseException;
    int countBySubject(String subject) throws DatabaseException;
    int countActiveBySubject(String subject) throws DatabaseException;
    List<String> findDistinctSubjects() throws DatabaseException;
    List<String> findDistinctUnits(String subject) throws DatabaseException;
    List<String> findDistinctTopics(String subject, String unit) throws DatabaseException;
}
