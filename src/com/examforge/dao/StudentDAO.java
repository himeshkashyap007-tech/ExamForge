package com.examforge.dao;

import com.examforge.exception.DatabaseException;
import com.examforge.model.StudentAttempt;
import com.examforge.model.StudentResponse;
import java.util.List;

public interface StudentDAO {
    int insertAttempt(StudentAttempt attempt) throws DatabaseException;
    boolean updateAttempt(StudentAttempt attempt) throws DatabaseException;
    boolean completeAttempt(int attemptId, int correctAnswers, int marksObtained, int timeSpentSeconds) throws DatabaseException;
    StudentAttempt findOpenAttempt(int studentId, String practiceType, String subject) throws DatabaseException;
    boolean deleteAttempt(int attemptId) throws DatabaseException;
    List<StudentAttempt> findAttemptsByStudentId(int studentId) throws DatabaseException;
    List<StudentAttempt> findAttemptsByStudentAndSubject(int studentId, String subject) throws DatabaseException;
    StudentAttempt findAttemptById(int attemptId) throws DatabaseException;

    int insertResponse(StudentResponse response) throws DatabaseException;
    List<StudentResponse> findResponsesByAttemptId(int attemptId) throws DatabaseException;

    int getTotalAttempts(int studentId) throws DatabaseException;
    double getAverageScore(int studentId) throws DatabaseException;
    double getSubjectAverageScore(int studentId, String subject) throws DatabaseException;
    List<String> findWeakTopics(int studentId, String subject) throws DatabaseException;
}
