package com.examforge.exception;

public class ExamForgeException extends Exception {
    private final String errorCode;

    public ExamForgeException(String message) {
        super(message);
        this.errorCode = "GENERAL_ERROR";
    }

    public ExamForgeException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ExamForgeException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "GENERAL_ERROR";
    }
    public ExamForgeException(String message, String errorCode, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
}

    public String getErrorCode() {
        return errorCode;
    }
}               
