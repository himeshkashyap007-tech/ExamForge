package com.examforge.dao.impl;

import com.examforge.config.DatabaseConfig;
import com.examforge.dao.StudentDAO;
import com.examforge.exception.DatabaseException;
import com.examforge.model.StudentAttempt;
import com.examforge.model.StudentResponse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAOImpl implements StudentDAO {

    @Override
    public int insertAttempt(StudentAttempt attempt) throws DatabaseException {
        String sql = "INSERT INTO student_attempts (student_id, practice_type, subject, total_questions, " +
                     "correct_answers, total_marks, marks_obtained, time_spent_seconds) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, attempt.getStudentId());
            ps.setString(2, attempt.getPracticeType());
            ps.setString(3, attempt.getSubject());
            ps.setInt(4, attempt.getTotalQuestions());
            ps.setInt(5, attempt.getCorrectAnswers());
            ps.setInt(6, attempt.getTotalMarks());
            ps.setInt(7, attempt.getMarksObtained());
            ps.setInt(8, attempt.getTimeSpentSeconds());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new DatabaseException("Failed to retrieve generated attempt ID");
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting attempt: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateAttempt(StudentAttempt attempt) throws DatabaseException {
        String sql = "UPDATE student_attempts SET correct_answers=?, marks_obtained=?, time_spent_seconds=? WHERE attempt_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attempt.getCorrectAnswers());
            ps.setInt(2, attempt.getMarksObtained());
            ps.setInt(3, attempt.getTimeSpentSeconds());
            ps.setInt(4, attempt.getAttemptId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating attempt: " + e.getMessage(), e);
        }
    }

    @Override
    public List<StudentAttempt> findAttemptsByStudentId(int studentId) throws DatabaseException {
        String sql = "SELECT * FROM student_attempts WHERE student_id = ? ORDER BY attempted_at DESC";
        List<StudentAttempt> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapAttempt(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding attempts: " + e.getMessage(), e);
        }
    }

    @Override
    public List<StudentAttempt> findAttemptsByStudentAndSubject(int studentId, String subject) throws DatabaseException {
        String sql = "SELECT * FROM student_attempts WHERE student_id = ? AND subject = ? ORDER BY attempted_at DESC";
        List<StudentAttempt> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, subject);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapAttempt(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding attempts by subject: " + e.getMessage(), e);
        }
    }

    @Override
    public StudentAttempt findAttemptById(int attemptId) throws DatabaseException {
        String sql = "SELECT * FROM student_attempts WHERE attempt_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attemptId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapAttempt(rs);
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding attempt: " + e.getMessage(), e);
        }
    }

    @Override
    public int insertResponse(StudentResponse response) throws DatabaseException {
        String sql = "INSERT INTO student_responses (attempt_id, question_id, selected_option, is_correct, marks_obtained) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, response.getAttemptId());
            ps.setInt(2, response.getQuestionId());
            ps.setString(3, response.getSelectedOption());
            ps.setBoolean(4, response.isCorrect());
            ps.setInt(5, response.getMarksObtained());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new DatabaseException("Failed to retrieve generated response ID");
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting response: " + e.getMessage(), e);
        }
    }

    @Override
    public List<StudentResponse> findResponsesByAttemptId(int attemptId) throws DatabaseException {
        String sql = "SELECT sr.*, q.question_text, q.subject, q.unit, q.topic, q.marks, q.difficulty, " +
                     "q.question_type, q.correct_option, q.option_a, q.option_b, q.option_c, q.option_d, q.explanation " +
                     "FROM student_responses sr JOIN questions q ON sr.question_id = q.question_id " +
                     "WHERE sr.attempt_id = ?";
        List<StudentResponse> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attemptId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                StudentResponse sr = new StudentResponse();
                sr.setResponseId(rs.getInt("response_id"));
                sr.setAttemptId(rs.getInt("attempt_id"));
                sr.setQuestionId(rs.getInt("question_id"));
                sr.setSelectedOption(rs.getString("selected_option"));
                sr.setCorrect(rs.getBoolean("is_correct"));
                sr.setMarksObtained(rs.getInt("marks_obtained"));
                com.examforge.model.Question q = new com.examforge.model.Question();
                q.setQuestionId(rs.getInt("question_id"));
                q.setQuestionText(rs.getString("question_text"));
                q.setSubject(rs.getString("subject"));
                q.setUnit(rs.getString("unit"));
                q.setTopic(rs.getString("topic"));
                q.setMarks(rs.getInt("marks"));
                q.setDifficulty(com.examforge.model.Difficulty.valueOf(rs.getString("difficulty")));
                q.setQuestionType(com.examforge.model.QuestionType.valueOf(rs.getString("question_type")));
                q.setOptionA(rs.getString("option_a"));
                q.setOptionB(rs.getString("option_b"));
                q.setOptionC(rs.getString("option_c"));
                q.setOptionD(rs.getString("option_d"));
                q.setCorrectOption(rs.getString("correct_option"));
                q.setExplanation(rs.getString("explanation"));
                sr.setQuestion(q);
                list.add(sr);
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding responses: " + e.getMessage(), e);
        }
    }

    @Override
    public int getTotalAttempts(int studentId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM student_attempts WHERE student_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error counting attempts: " + e.getMessage(), e);
        }
    }

    @Override
    public double getAverageScore(int studentId) throws DatabaseException {
        String sql = "SELECT COALESCE(AVG(CASE WHEN total_marks > 0 THEN (marks_obtained * 100.0 / total_marks) ELSE 0 END), 0) " +
                     "FROM student_attempts WHERE student_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating average: " + e.getMessage(), e);
        }
    }

    @Override
    public double getSubjectAverageScore(int studentId, String subject) throws DatabaseException {
        String sql = "SELECT COALESCE(AVG(CASE WHEN total_marks > 0 THEN (marks_obtained * 100.0 / total_marks) ELSE 0 END), 0) " +
                     "FROM student_attempts WHERE student_id = ? AND subject = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, subject);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating subject average: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> findWeakTopics(int studentId, String subject) throws DatabaseException {
        String sql = "SELECT q.topic, " +
                     "COALESCE(AVG(CASE WHEN sr.is_correct THEN 1.0 ELSE 0.0 END), 0) as accuracy " +
                     "FROM student_responses sr " +
                     "JOIN questions q ON sr.question_id = q.question_id " +
                     "JOIN student_attempts sa ON sr.attempt_id = sa.attempt_id " +
                     "WHERE sa.student_id = ? AND q.subject = ? " +
                     "GROUP BY q.topic " +
                     "HAVING accuracy < 0.5 " +
                     "ORDER BY accuracy ASC";
        List<String> topics = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, subject);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) topics.add(rs.getString("topic"));
            return topics;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding weak topics: " + e.getMessage(), e);
        }
    }

    private StudentAttempt mapAttempt(ResultSet rs) throws SQLException {
        StudentAttempt a = new StudentAttempt();
        a.setAttemptId(rs.getInt("attempt_id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setPracticeType(rs.getString("practice_type"));
        a.setSubject(rs.getString("subject"));
        a.setTotalQuestions(rs.getInt("total_questions"));
        a.setCorrectAnswers(rs.getInt("correct_answers"));
        a.setTotalMarks(rs.getInt("total_marks"));
        a.setMarksObtained(rs.getInt("marks_obtained"));
        a.setTimeSpentSeconds(rs.getInt("time_spent_seconds"));
        Timestamp at = rs.getTimestamp("attempted_at");
        if (at != null) a.setAttemptedAt(at.toLocalDateTime());
        return a;
    }
}
