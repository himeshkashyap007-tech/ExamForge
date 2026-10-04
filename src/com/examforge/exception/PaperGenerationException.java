package com.examforge.exception;

public class PaperGenerationException extends ExamForgeException {
    public PaperGenerationException(String message) {
        super(message, "PAPER_GENERATION");
    }

    public PaperGenerationException(String message, Throwable cause) {
        super(message, "PAPER_GENERATION", cause);
    }
}
