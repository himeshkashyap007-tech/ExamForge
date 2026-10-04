package com.examforge.dao.impl;

import com.examforge.config.DatabaseConfig;
import com.examforge.dao.BlueprintDAO;
import com.examforge.exception.DatabaseException;
import com.examforge.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlueprintDAOImpl implements BlueprintDAO {

    @Override
    public Blueprint findById(int id) throws DatabaseException {
        String sql = "SELECT * FROM blueprints WHERE blueprint_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Blueprint bp = mapBlueprint(rs);
                bp.setSections(findSectionsByBlueprintId(id));
                bp.setConstraints(findConstraintsByBlueprintId(id));
                return bp;
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding blueprint: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Blueprint> findAll() throws DatabaseException {
        String sql = "SELECT * FROM blueprints ORDER BY created_at DESC";
        List<Blueprint> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapBlueprint(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding all blueprints: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Blueprint> findByStatus(BlueprintStatus status) throws DatabaseException {
        String sql = "SELECT * FROM blueprints WHERE status = ? ORDER BY created_at DESC";
        List<Blueprint> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapBlueprint(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding blueprints by status: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Blueprint> findBySubject(String subject) throws DatabaseException {
        String sql = "SELECT * FROM blueprints WHERE subject = ? ORDER BY created_at DESC";
        List<Blueprint> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapBlueprint(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding blueprints by subject: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Blueprint> findByCreatedBy(int userId) throws DatabaseException {
        String sql = "SELECT * FROM blueprints WHERE created_by = ? ORDER BY created_at DESC";
        List<Blueprint> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapBlueprint(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding blueprints by creator: " + e.getMessage(), e);
        }
    }

    @Override
    public int insert(Blueprint blueprint) throws DatabaseException {
        String sql = "INSERT INTO blueprints (exam_name, subject, total_marks, duration_minutes, number_of_questions, " +
                     "status, exposure_policy_type, exposure_window, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, blueprint.getExamName());
            ps.setString(2, blueprint.getSubject());
            ps.setInt(3, blueprint.getTotalMarks());
            ps.setInt(4, blueprint.getDurationMinutes());
            ps.setInt(5, blueprint.getNumberOfQuestions());
            ps.setString(6, blueprint.getStatus().name());
            ps.setString(7, blueprint.getExposurePolicyType().name());
            ps.setInt(8, blueprint.getExposureWindow());
            ps.setInt(9, blueprint.getCreatedBy());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new DatabaseException("Failed to retrieve generated blueprint ID");
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting blueprint: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Blueprint blueprint) throws DatabaseException {
        String sql = "UPDATE blueprints SET exam_name=?, subject=?, total_marks=?, duration_minutes=?, " +
                     "number_of_questions=?, status=?, exposure_policy_type=?, exposure_window=?, " +
                     "updated_at=CURRENT_TIMESTAMP WHERE blueprint_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, blueprint.getExamName());
            ps.setString(2, blueprint.getSubject());
            ps.setInt(3, blueprint.getTotalMarks());
            ps.setInt(4, blueprint.getDurationMinutes());
            ps.setInt(5, blueprint.getNumberOfQuestions());
            ps.setString(6, blueprint.getStatus().name());
            ps.setString(7, blueprint.getExposurePolicyType().name());
            ps.setInt(8, blueprint.getExposureWindow());
            ps.setInt(9, blueprint.getBlueprintId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating blueprint: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int blueprintId) throws DatabaseException {
        String sql = "DELETE FROM blueprints WHERE blueprint_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, blueprintId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting blueprint: " + e.getMessage(), e);
        }
    }

@Override
    public boolean updateStatus(int blueprintId, BlueprintStatus status) throws DatabaseException {
        String sql = "UPDATE blueprints SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE blueprint_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, blueprintId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating blueprint status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deactivate(int blueprintId) throws DatabaseException {
        String sql = "UPDATE blueprints SET previous_status = status, status = 'INACTIVE', " +
                     "updated_at = CURRENT_TIMESTAMP WHERE blueprint_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, blueprintId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deactivating blueprint: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean reactivate(int blueprintId) throws DatabaseException {
        String sql = "UPDATE blueprints SET status = COALESCE(previous_status, 'DRAFT'), previous_status = NULL, " +
                     "updated_at = CURRENT_TIMESTAMP WHERE blueprint_id = ? AND status = 'INACTIVE'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, blueprintId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error reactivating blueprint: " + e.getMessage(), e);
        }
    }

    @Override
    public int countSectionsByBlueprintId(int blueprintId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM blueprint_sections WHERE blueprint_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, blueprintId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error counting blueprint sections: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BlueprintSection> findSectionsByBlueprintId(int blueprintId) throws DatabaseException {
        String sql = "SELECT * FROM blueprint_sections WHERE blueprint_id = ? ORDER BY sort_order";
        List<BlueprintSection> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, blueprintId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapSection(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding sections: " + e.getMessage(), e);
        }
    }

    @Override
    public int insertSection(BlueprintSection section) throws DatabaseException {
        String sql = "INSERT INTO blueprint_sections (blueprint_id, section_name, question_count, total_marks, " +
                     "question_type_filter, difficulty_filter, sort_order) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, section.getBlueprintId());
            ps.setString(2, section.getSectionName());
            ps.setInt(3, section.getQuestionCount());
            ps.setInt(4, section.getTotalMarks());
            ps.setString(5, section.getQuestionTypeFilter());
            ps.setString(6, section.getDifficultyFilter());
            ps.setInt(7, section.getSortOrder());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new DatabaseException("Failed to retrieve generated section ID");
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting section: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateSection(BlueprintSection section) throws DatabaseException {
        String sql = "UPDATE blueprint_sections SET section_name=?, question_count=?, total_marks=?, " +
                     "question_type_filter=?, difficulty_filter=?, sort_order=? WHERE section_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, section.getSectionName());
            ps.setInt(2, section.getQuestionCount());
            ps.setInt(3, section.getTotalMarks());
            ps.setString(4, section.getQuestionTypeFilter());
            ps.setString(5, section.getDifficultyFilter());
            ps.setInt(6, section.getSortOrder());
            ps.setInt(7, section.getSectionId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating section: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteSectionsByBlueprintId(int blueprintId) throws DatabaseException {
        String sql = "DELETE FROM blueprint_sections WHERE blueprint_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, blueprintId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting sections: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BlueprintConstraint> findConstraintsByBlueprintId(int blueprintId) throws DatabaseException {
        String sql = "SELECT * FROM blueprint_constraints WHERE blueprint_id = ?";
        List<BlueprintConstraint> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, blueprintId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapConstraint(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding constraints: " + e.getMessage(), e);
        }
    }

    @Override
    public int insertConstraint(BlueprintConstraint constraint) throws DatabaseException {
        String sql = "INSERT INTO blueprint_constraints (blueprint_id, category, basis, target, required_value, tolerance, priority) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, constraint.getBlueprintId());
            ps.setString(2, constraint.getCategory().name());
            ps.setString(3, constraint.getBasis().name());
            ps.setString(4, constraint.getTarget());
            ps.setDouble(5, constraint.getRequiredValue());
            ps.setDouble(6, constraint.getTolerance());
            ps.setString(7, constraint.getPriority().name());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new DatabaseException("Failed to retrieve generated constraint ID");
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting constraint: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateConstraint(BlueprintConstraint constraint) throws DatabaseException {
        String sql = "UPDATE blueprint_constraints SET category=?, basis=?, target=?, required_value=?, " +
                     "tolerance=?, priority=? WHERE constraint_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, constraint.getCategory().name());
            ps.setString(2, constraint.getBasis().name());
            ps.setString(3, constraint.getTarget());
            ps.setDouble(4, constraint.getRequiredValue());
            ps.setDouble(5, constraint.getTolerance());
            ps.setString(6, constraint.getPriority().name());
            ps.setInt(7, constraint.getConstraintId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating constraint: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteConstraintsByBlueprintId(int blueprintId) throws DatabaseException {
        String sql = "DELETE FROM blueprint_constraints WHERE blueprint_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, blueprintId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting constraints: " + e.getMessage(), e);
        }
    }

    private Blueprint mapBlueprint(ResultSet rs) throws SQLException {
        Blueprint bp = new Blueprint();
        bp.setBlueprintId(rs.getInt("blueprint_id"));
        bp.setExamName(rs.getString("exam_name"));
        bp.setSubject(rs.getString("subject"));
        bp.setTotalMarks(rs.getInt("total_marks"));
        bp.setDurationMinutes(rs.getInt("duration_minutes"));
        bp.setNumberOfQuestions(rs.getInt("number_of_questions"));
        bp.setStatus(BlueprintStatus.valueOf(rs.getString("status")));
        bp.setExposurePolicyType(ExposurePolicyType.valueOf(rs.getString("exposure_policy_type")));
        bp.setExposureWindow(rs.getInt("exposure_window"));
        bp.setCreatedBy(rs.getInt("created_by"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) bp.setCreatedAt(cat.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) bp.setUpdatedAt(uat.toLocalDateTime());
        return bp;
    }

    private BlueprintSection mapSection(ResultSet rs) throws SQLException {
        BlueprintSection s = new BlueprintSection();
        s.setSectionId(rs.getInt("section_id"));
        s.setBlueprintId(rs.getInt("blueprint_id"));
        s.setSectionName(rs.getString("section_name"));
        s.setQuestionCount(rs.getInt("question_count"));
        s.setTotalMarks(rs.getInt("total_marks"));
        s.setQuestionTypeFilter(rs.getString("question_type_filter"));
        s.setDifficultyFilter(rs.getString("difficulty_filter"));
        s.setSortOrder(rs.getInt("sort_order"));
        return s;
    }

    private BlueprintConstraint mapConstraint(ResultSet rs) throws SQLException {
        BlueprintConstraint c = new BlueprintConstraint();
        c.setConstraintId(rs.getInt("constraint_id"));
        c.setBlueprintId(rs.getInt("blueprint_id"));
        c.setCategory(ConstraintCategory.valueOf(rs.getString("category")));
        c.setBasis(ConstraintBasis.valueOf(rs.getString("basis")));
        c.setTarget(rs.getString("target"));
        c.setRequiredValue(rs.getDouble("required_value"));
        c.setTolerance(rs.getDouble("tolerance"));
        c.setPriority(ConstraintPriority.valueOf(rs.getString("priority")));
        return c;
    }
}
