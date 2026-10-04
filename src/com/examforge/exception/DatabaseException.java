package com.examforge.exception;

public class DatabaseException extends ExamForgeException {
    public DatabaseException(String message) {
        super(message, "DATABASE_ERROR");
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, "DATABASE_ERROR", cause);
    }
}
