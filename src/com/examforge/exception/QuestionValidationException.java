package com.examforge.exception;

import java.util.List;

public class QuestionValidationException extends ExamForgeException {
    private final List<String> errors;

    public QuestionValidationException(String message, List<String> errors) {
        super(message, "QUESTION_VALIDATION");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
