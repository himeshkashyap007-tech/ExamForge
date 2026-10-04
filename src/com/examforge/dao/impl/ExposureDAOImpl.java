package com.examforge.dao.impl;

import com.examforge.config.DatabaseConfig;
import com.examforge.dao.ExposureDAO;
import com.examforge.exception.DatabaseException;
import com.examforge.model.QuestionExposure;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExposureDAOImpl implements ExposureDAO {

    @Override
    public QuestionExposure findByQuestionId(int questionId) throws DatabaseException {
        String sql = "SELECT * FROM question_exposure WHERE question_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapExposure(rs);
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding exposure: " + e.getMessage(), e);
        }
    }

    @Override
    public List<QuestionExposure> findAll() throws DatabaseException {
        String sql = "SELECT * FROM question_exposure ORDER BY exposure_score DESC";
        List<QuestionExposure> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapExposure(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding all exposures: " + e.getMessage(), e);
        }
    }

    @Override
    public List<QuestionExposure> findHighExposure() throws DatabaseException {
        String sql = "SELECT * FROM question_exposure WHERE total_official_usage >= 4 ORDER BY exposure_score DESC";
        List<QuestionExposure> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapExposure(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding high exposure: " + e.getMessage(), e);
        }
    }

    @Override
    public int insert(QuestionExposure exposure) throws DatabaseException {
        String sql = "INSERT INTO question_exposure (question_id, total_official_usage, last_official_use, " +
                     "recent_usage_count, exposure_score) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, exposure.getQuestionId());
            ps.setInt(2, exposure.getTotalOfficialUsage());
            if (exposure.getLastOfficialUse() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(exposure.getLastOfficialUse()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            ps.setInt(4, exposure.getRecentUsageCount());
            ps.setDouble(5, exposure.getExposureScore());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new DatabaseException("Failed to retrieve generated exposure ID");
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting exposure: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(QuestionExposure exposure) throws DatabaseException {
        String sql = "UPDATE question_exposure SET total_official_usage=?, last_official_use=?, " +
                     "recent_usage_count=?, exposure_score=? WHERE question_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, exposure.getTotalOfficialUsage());
            if (exposure.getLastOfficialUse() != null) {
                ps.setTimestamp(2, Timestamp.valueOf(exposure.getLastOfficialUse()));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }
            ps.setInt(3, exposure.getRecentUsageCount());
            ps.setDouble(4, exposure.getExposureScore());
            ps.setInt(5, exposure.getQuestionId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating exposure: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean incrementUsage(int questionId) throws DatabaseException {
        String sql = "UPDATE question_exposure SET total_official_usage = total_official_usage + 1, " +
                     "last_official_use = CURRENT_TIMESTAMP, recent_usage_count = recent_usage_count + 1 " +
                     "WHERE question_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                String insertSql = "INSERT INTO question_exposure (question_id, total_official_usage, " +
                                   "last_official_use, recent_usage_count, exposure_score) VALUES (?, 1, CURRENT_TIMESTAMP, 1, 0)";
                try (PreparedStatement ips = conn.prepareStatement(insertSql)) {
                    ips.setInt(1, questionId);
                    ips.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            throw new DatabaseException("Error incrementing usage: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateExposureScore(int questionId, double score) throws DatabaseException {
        String sql = "UPDATE question_exposure SET exposure_score = ? WHERE question_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, score);
            ps.setInt(2, questionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating exposure score: " + e.getMessage(), e);
        }
    }

    private QuestionExposure mapExposure(ResultSet rs) throws SQLException {
        QuestionExposure e = new QuestionExposure();
        e.setExposureId(rs.getInt("exposure_id"));
        e.setQuestionId(rs.getInt("question_id"));
        e.setTotalOfficialUsage(rs.getInt("total_official_usage"));
        Timestamp lu = rs.getTimestamp("last_official_use");
        if (lu != null) e.setLastOfficialUse(lu.toLocalDateTime());
        e.setRecentUsageCount(rs.getInt("recent_usage_count"));
        e.setExposureScore(rs.getDouble("exposure_score"));
        return e;
    }
}
