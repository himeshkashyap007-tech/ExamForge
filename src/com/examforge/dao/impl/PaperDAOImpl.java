package com.examforge.dao.impl;

import com.examforge.config.DatabaseConfig;
import com.examforge.dao.PaperDAO;
import com.examforge.exception.DatabaseException;
import com.examforge.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaperDAOImpl implements PaperDAO {

    @Override
    public Paper findById(int id) throws DatabaseException {
        String sql = "SELECT * FROM papers WHERE paper_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Paper paper = mapPaper(rs);
                paper.setQuestions(findQuestionsByPaperId(id));
                return paper;
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding paper: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Paper> findAll() throws DatabaseException {
        String sql = "SELECT * FROM papers ORDER BY created_at DESC";
        List<Paper> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapPaper(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding all papers: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Paper> findByStatus(PaperStatus status) throws DatabaseException {
        String sql = "SELECT * FROM papers WHERE status = ? ORDER BY created_at DESC";
        List<Paper> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapPaper(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding papers by status: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Paper> findByBlueprintId(int blueprintId) throws DatabaseException {
        String sql = "SELECT * FROM papers WHERE blueprint_id = ? ORDER BY created_at DESC";
        List<Paper> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, blueprintId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapPaper(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding papers by blueprint: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Paper> findByCreatedBy(int userId) throws DatabaseException {
        String sql = "SELECT * FROM papers WHERE created_by = ? ORDER BY created_at DESC";
        List<Paper> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapPaper(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding papers by creator: " + e.getMessage(), e);
        }
    }

    @Override
    public int insert(Paper paper) throws DatabaseException {
        String sql = "INSERT INTO papers (blueprint_id, exam_name, status, total_marks, question_count, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, paper.getBlueprintId());
            ps.setString(2, paper.getExamName());
            ps.setString(3, paper.getStatus().name());
            ps.setInt(4, paper.getTotalMarks());
            ps.setInt(5, paper.getQuestionCount());
            ps.setInt(6, paper.getCreatedBy());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new DatabaseException("Failed to retrieve generated paper ID");
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting paper: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Paper paper) throws DatabaseException {
        String sql = "UPDATE papers SET blueprint_id=?, exam_name=?, status=?, total_marks=?, question_count=?, " +
                     "updated_at=CURRENT_TIMESTAMP WHERE paper_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paper.getBlueprintId());
            ps.setString(2, paper.getExamName());
            ps.setString(3, paper.getStatus().name());
            ps.setInt(4, paper.getTotalMarks());
            ps.setInt(5, paper.getQuestionCount());
            ps.setInt(6, paper.getPaperId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating paper: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int paperId) throws DatabaseException {
        String sql = "DELETE FROM papers WHERE paper_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paperId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting paper: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(int paperId, PaperStatus status) throws DatabaseException {
        String sql = "UPDATE papers SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE paper_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, paperId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating paper status: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PaperQuestion> findQuestionsByPaperId(int paperId) throws DatabaseException {
        String sql = "SELECT pq.*, q.question_text, q.subject, q.unit, q.topic, q.marks, q.difficulty, " +
                     "q.question_type, q.active_status, q.option_a, q.option_b, q.option_c, q.option_d, " +
                     "q.correct_option, q.explanation " +
                     "FROM paper_questions pq JOIN questions q ON pq.question_id = q.question_id " +
                     "WHERE pq.paper_id = ? ORDER BY pq.section_index, pq.sort_order";
        List<PaperQuestion> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paperId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                PaperQuestion pq = new PaperQuestion();
                pq.setPaperQuestionId(rs.getInt("paper_question_id"));
                pq.setPaperId(rs.getInt("paper_id"));
                pq.setQuestionId(rs.getInt("question_id"));
                pq.setSectionIndex(rs.getInt("section_index"));
                pq.setSortOrder(rs.getInt("sort_order"));
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
                pq.setQuestion(q);
                list.add(pq);
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding paper questions: " + e.getMessage(), e);
        }
    }

    @Override
    public int insertPaperQuestion(PaperQuestion pq) throws DatabaseException {
        String sql = "INSERT INTO paper_questions (paper_id, question_id, section_index, sort_order) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pq.getPaperId());
            ps.setInt(2, pq.getQuestionId());
            ps.setInt(3, pq.getSectionIndex());
            ps.setInt(4, pq.getSortOrder());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new DatabaseException("Failed to retrieve generated paper_question ID");
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting paper question: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deletePaperQuestionsByPaperId(int paperId) throws DatabaseException {
        String sql = "DELETE FROM paper_questions WHERE paper_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paperId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting paper questions: " + e.getMessage(), e);
        }
    }

    @Override
    public int insertExaminationRecord(ExaminationRecord record) throws DatabaseException {
        String sql = "INSERT INTO examination_records (paper_id, exam_name, subject, total_marks, finalized_by) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getPaperId());
            ps.setString(2, record.getExamName());
            ps.setString(3, record.getSubject());
            ps.setInt(4, record.getTotalMarks());
            ps.setInt(5, record.getFinalizedBy());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new DatabaseException("Failed to retrieve generated exam record ID");
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting examination record: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ExaminationRecord> findAllExaminationRecords() throws DatabaseException {
        String sql = "SELECT * FROM examination_records ORDER BY finalized_at DESC";
        List<ExaminationRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapExamRecord(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding examination records: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ExaminationRecord> findBySubject(String subject) throws DatabaseException {
        String sql = "SELECT * FROM examination_records WHERE subject = ? ORDER BY finalized_at DESC";
        List<ExaminationRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapExamRecord(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding examination records by subject: " + e.getMessage(), e);
        }
    }

    @Override
    public ExaminationRecord findExaminationByPaperId(int paperId) throws DatabaseException {
        String sql = "SELECT * FROM examination_records WHERE paper_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paperId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapExamRecord(rs);
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding examination record by paper: " + e.getMessage(), e);
        }
    }

    private Paper mapPaper(ResultSet rs) throws SQLException {
        Paper p = new Paper();
        p.setPaperId(rs.getInt("paper_id"));
        p.setBlueprintId(rs.getInt("blueprint_id"));
        p.setExamName(rs.getString("exam_name"));
        p.setStatus(PaperStatus.valueOf(rs.getString("status")));
        p.setTotalMarks(rs.getInt("total_marks"));
        p.setQuestionCount(rs.getInt("question_count"));
        p.setCreatedBy(rs.getInt("created_by"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) p.setCreatedAt(cat.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) p.setUpdatedAt(uat.toLocalDateTime());
        return p;
    }

    private ExaminationRecord mapExamRecord(ResultSet rs) throws SQLException {
        ExaminationRecord r = new ExaminationRecord();
        r.setExamId(rs.getInt("exam_id"));
        r.setPaperId(rs.getInt("paper_id"));
        r.setExamName(rs.getString("exam_name"));
        r.setSubject(rs.getString("subject"));
        r.setTotalMarks(rs.getInt("total_marks"));
        Timestamp fat = rs.getTimestamp("finalized_at");
        if (fat != null) r.setFinalizedAt(fat.toLocalDateTime());
        r.setFinalizedBy(rs.getInt("finalized_by"));
        return r;
    }
}
