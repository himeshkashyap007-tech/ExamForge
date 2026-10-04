package com.examforge.strategy;

import com.examforge.exception.ExamForgeException;
import com.examforge.model.Blueprint;
import com.examforge.model.Question;

import java.util.List;

public interface PaperGenerationStrategy {
    List<Question> selectQuestions(Blueprint blueprint, List<Question> candidates) throws ExamForgeException;
}