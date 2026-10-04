package com.examforge.dao;

import com.examforge.exception.DatabaseException;
import com.examforge.model.QuestionExposure;
import java.util.List;

public interface ExposureDAO {
    QuestionExposure findByQuestionId(int questionId) throws DatabaseException;
    List<QuestionExposure> findAll() throws DatabaseException;
    List<QuestionExposure> findHighExposure() throws DatabaseException;
    int insert(QuestionExposure exposure) throws DatabaseException;
    boolean update(QuestionExposure exposure) throws DatabaseException;
    boolean incrementUsage(int questionId) throws DatabaseException;
    boolean updateExposureScore(int questionId, double score) throws DatabaseException;
}
