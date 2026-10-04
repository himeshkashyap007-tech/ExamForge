package com.examforge.dao.impl;

import com.examforge.config.DatabaseConfig;
import com.examforge.dao.QuestionDAO;
import com.examforge.exception.DatabaseException;
import com.examforge.model.Difficulty;
import com.examforge.model.Question;
import com.examforge.model.QuestionType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAOImpl implements QuestionDAO {

    @Override
    public Question findById(int id) throws DatabaseException {
        String sql = "SELECT * FROM questions WHERE question_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapQuestion(rs);
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding question: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Question> findAll() throws DatabaseException {
        String sql = "SELECT * FROM questions ORDER BY subject, unit, topic";
        return executeQueryList(sql);
    }

    @Override
    public List<Question> findActiveBySubject(String subject) throws DatabaseException {
        String sql = "SELECT * FROM questions WHERE subject = ? AND active_status = TRUE ORDER BY unit, topic";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject);
            return mapList(ps.executeQuery());
        } catch (SQLException e) {
            throw new DatabaseException("Error finding active questions by subject: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Question> findBySubject(String subject) throws DatabaseException {
        String sql = "SELECT * FROM questions WHERE subject = ? ORDER BY unit, topic";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject);
            return mapList(ps.executeQuery());
        } catch (SQLException e) {
            throw new DatabaseException("Error finding questions by subject: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Question> findByFilters(String subject, String unit, String difficulty, String questionType) throws DatabaseException {
        StringBuilder sql = new StringBuilder("SELECT * FROM questions WHERE active_status = TRUE");
        List<Object> params = new ArrayList<>();

        if (subject != null && !subject.isEmpty()) {
            sql.append(" AND subject = ?");
            params.add(subject);
        }
        if (unit != null && !unit.isEmpty()) {
            sql.append(" AND unit = ?");
            params.add(unit);
        }
        if (difficulty != null && !difficulty.isEmpty()) {
            sql.append(" AND difficulty = ?");
            params.add(difficulty);
        }
        if (questionType != null && !questionType.isEmpty()) {
            sql.append(" AND question_type = ?");
            params.add(questionType);
        }
        sql.append(" ORDER BY unit, topic");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            return mapList(ps.executeQuery());
        } catch (SQLException e) {
            throw new DatabaseException("Error finding questions by filters: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Question> findInactiveQuestions() throws DatabaseException {
        String sql = "SELECT * FROM questions WHERE active_status = FALSE ORDER BY subject, unit";
        return executeQueryList(sql);
    }

    @Override
    public int insert(Question question) throws DatabaseException {
        String sql = "INSERT INTO questions (question_text, subject, unit, topic, marks, difficulty, question_type, " +
                     "active_status, option_a, option_b, option_c, option_d, correct_option, explanation, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, question.getQuestionText());
            ps.setString(2, question.getSubject());
            ps.setString(3, question.getUnit());
            ps.setString(4, question.getTopic());
            ps.setInt(5, question.getMarks());
            ps.setString(6, question.getDifficulty().name());
            ps.setString(7, question.getQuestionType().name());
            ps.setBoolean(8, question.isActiveStatus());
            ps.setString(9, question.getOptionA());
            ps.setString(10, question.getOptionB());
            ps.setString(11, question.getOptionC());
            ps.setString(12, question.getOptionD());
            ps.setString(13, question.getCorrectOption());
            ps.setString(14, question.getExplanation());
            ps.setInt(15, question.getCreatedBy());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new DatabaseException("Failed to retrieve generated question ID");
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting question: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Question question) throws DatabaseException {
        String sql = "UPDATE questions SET question_text=?, subject=?, unit=?, topic=?, marks=?, difficulty=?, " +
                     "question_type=?, active_status=?, option_a=?, option_b=?, option_c=?, option_d=?, " +
                     "correct_option=?, explanation=?, updated_at=CURRENT_TIMESTAMP WHERE question_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, question.getQuestionText());
            ps.setString(2, question.getSubject());
            ps.setString(3, question.getUnit());
            ps.setString(4, question.getTopic());
            ps.setInt(5, question.getMarks());
            ps.setString(6, question.getDifficulty().name());
            ps.setString(7, question.getQuestionType().name());
            ps.setBoolean(8, question.isActiveStatus());
            ps.setString(9, question.getOptionA());
            ps.setString(10, question.getOptionB());
            ps.setString(11, question.getOptionC());
            ps.setString(12, question.getOptionD());
            ps.setString(13, question.getCorrectOption());
            ps.setString(14, question.getExplanation());
            ps.setInt(15, question.getQuestionId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating question: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deactivate(int questionId) throws DatabaseException {
        String sql = "UPDATE questions SET active_status = FALSE, updated_at = CURRENT_TIMESTAMP WHERE question_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deactivating question: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean activate(int questionId) throws DatabaseException {
        String sql = "UPDATE questions SET active_status = TRUE, updated_at = CURRENT_TIMESTAMP WHERE question_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error activating question: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int questionId) throws DatabaseException {
        String sql = "DELETE FROM questions WHERE question_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting question: " + e.getMessage(), e);
        }
    }

    @Override
    public int countBySubject(String subject) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM questions WHERE subject = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error counting questions: " + e.getMessage(), e);
        }
    }

    @Override
    public int countActiveBySubject(String subject) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM questions WHERE subject = ? AND active_status = TRUE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error counting active questions: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> findDistinctSubjects() throws DatabaseException {
        String sql = "SELECT DISTINCT subject FROM questions WHERE active_status = TRUE ORDER BY subject";
        List<String> subjects = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) subjects.add(rs.getString(1));
            return subjects;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding subjects: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> findDistinctUnits(String subject) throws DatabaseException {
        String sql = "SELECT DISTINCT unit FROM questions WHERE subject = ? AND active_status = TRUE ORDER BY unit";
        List<String> units = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) units.add(rs.getString(1));
            return units;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding units: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> findDistinctTopics(String subject, String unit) throws DatabaseException {
        String sql = "SELECT DISTINCT topic FROM questions WHERE subject = ? AND unit = ? AND active_status = TRUE ORDER BY topic";
        List<String> topics = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject);
            ps.setString(2, unit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) topics.add(rs.getString(1));
            return topics;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding topics: " + e.getMessage(), e);
        }
    }

    private List<Question> executeQueryList(String sql) throws DatabaseException {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return mapList(rs);
        } catch (SQLException e) {
            throw new DatabaseException("Error executing query: " + e.getMessage(), e);
        }
    }

    private List<Question> mapList(ResultSet rs) throws SQLException {
        List<Question> list = new ArrayList<>();
        while (rs.next()) list.add(mapQuestion(rs));
        return list;
    }

    private Question mapQuestion(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setQuestionId(rs.getInt("question_id"));
        q.setQuestionText(rs.getString("question_text"));
        q.setSubject(rs.getString("subject"));
        q.setUnit(rs.getString("unit"));
        q.setTopic(rs.getString("topic"));
        q.setMarks(rs.getInt("marks"));
        q.setDifficulty(Difficulty.valueOf(rs.getString("difficulty")));
        q.setQuestionType(QuestionType.valueOf(rs.getString("question_type")));
        q.setActiveStatus(rs.getBoolean("active_status"));
        q.setOptionA(rs.getString("option_a"));
        q.setOptionB(rs.getString("option_b"));
        q.setOptionC(rs.getString("option_c"));
        q.setOptionD(rs.getString("option_d"));
        q.setCorrectOption(rs.getString("correct_option"));
        q.setExplanation(rs.getString("explanation"));
        q.setCreatedBy(rs.getInt("created_by"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) q.setCreatedAt(cat.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) q.setUpdatedAt(uat.toLocalDateTime());
        return q;
    }
}
