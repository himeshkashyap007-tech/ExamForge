package com.examforge.exception;

import java.util.List;

public class BlueprintValidationException extends ExamForgeException {
    private final List<String> errors;

    public BlueprintValidationException(String message, List<String> errors) {
        super(message, "BLUEPRINT_VALIDATION");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
