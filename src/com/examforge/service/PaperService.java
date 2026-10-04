package com.examforge.service;
import com.examforge.engine.FeasibilityResult;
import com.examforge.validator.ValidationCheckResult;
import com.examforge.config.DatabaseConfig;
import com.examforge.dao.PaperDAO;
import com.examforge.dao.impl.PaperDAOImpl;
import com.examforge.exception.DatabaseException;
import com.examforge.exception.ExamForgeException;
import com.examforge.exception.PaperGenerationException;
import com.examforge.model.*;
import com.examforge.engine.FeasibilityEngine;
import com.examforge.engine.PaperGenerator;
import com.examforge.validator.PaperValidator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PaperService {
    private final PaperDAO paperDAO;
    private final PaperGenerator paperGenerator;
    private final PaperValidator paperValidator;
    private final FeasibilityEngine feasibilityEngine;

    public PaperService() {
        this.paperDAO = new PaperDAOImpl();
        this.paperGenerator = new PaperGenerator();
        this.paperValidator = new PaperValidator();
        this.feasibilityEngine = new FeasibilityEngine();
    }

    public FeasibilityResult analyzeFeasibility(Blueprint blueprint) throws ExamForgeException {
        return feasibilityEngine.analyzeFeasibility(blueprint);
    }

    public Paper generatePaper(Blueprint blueprint, int userId) throws ExamForgeException {
        Paper paper = paperGenerator.generatePaper(blueprint, userId);
        List<ValidationCheckResult> results = paperValidator.validate(paper, blueprint);
        boolean allPassed = results.stream().allMatch(r -> r.getStatus() == ValidationCheckStatus.PASS);
        if (!allPassed) {
            StringBuilder sb = new StringBuilder("Paper validation failed:\n");
            for (ValidationCheckResult r : results) {
                if (r.getStatus() == ValidationCheckStatus.FAIL) {
                    sb.append("- ").append(r.getMessage()).append("\n");
                }
            }
            throw new PaperGenerationException(sb.toString());
        }
        paper.setStatus(PaperStatus.VALIDATED);
        try {
            paperDAO.updateStatus(paper.getPaperId(), PaperStatus.VALIDATED);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to update paper status: " + e.getMessage(), e);
        }
        return paper;
    }

    public List<ValidationCheckResult> validatePaper(Paper paper, Blueprint blueprint) {
        return paperValidator.validate(paper, blueprint);
    }

    public boolean replaceQuestion(Paper paper, int oldQuestionId, int newQuestionId, int sectionIndex) throws ExamForgeException {
        try {
            for (PaperQuestion pq : paper.getQuestions()) {
                if (pq.getQuestionId() == oldQuestionId && pq.getSectionIndex() == sectionIndex) {
                    pq.setQuestionId(newQuestionId);
                    paperDAO.deletePaperQuestionsByPaperId(paper.getPaperId());
                    for (PaperQuestion p : paper.getQuestions()) {
                        paperDAO.insertPaperQuestion(p);
                    }
                    return true;
                }
            }
            return false;
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to replace question: " + e.getMessage(), e);
        }
    }

    public boolean finalizePaper(Paper paper, int userId) throws ExamForgeException {
        if (paper.getStatus() != PaperStatus.VALIDATED) {
            throw new ExamForgeException("Only validated papers can be finalized");
        }

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement checkPs = conn.prepareStatement(
                "SELECT status FROM papers WHERE paper_id = ? AND status = 'VALIDATED'");
            checkPs.setInt(1, paper.getPaperId());
            var rs = checkPs.executeQuery();
            if (!rs.next()) {
                conn.rollback();
                throw new ExamForgeException("Paper is no longer in validated state");
            }
            checkPs.close();

            PreparedStatement examPs = conn.prepareStatement(
                "INSERT INTO examination_records (paper_id, exam_name, subject, total_marks, finalized_by) VALUES (?, ?, ?, ?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS);
            examPs.setInt(1, paper.getPaperId());
            examPs.setString(2, paper.getExamName());
            examPs.setString(3, paper.getBlueprint() != null ? paper.getBlueprint().getSubject() : "");
            examPs.setInt(4, paper.getTotalMarks());
            examPs.setInt(5, userId);
            examPs.executeUpdate();
            examPs.close();

            PreparedStatement updatePs = conn.prepareStatement(
                "UPDATE papers SET status = 'FINALIZED', updated_at = CURRENT_TIMESTAMP WHERE paper_id = ?");
            updatePs.setInt(1, paper.getPaperId());
            updatePs.executeUpdate();
            updatePs.close();

            for (PaperQuestion pq : paper.getQuestions()) {
                PreparedStatement expPs = conn.prepareStatement(
                    "INSERT INTO question_exposure (question_id, total_official_usage, last_official_use, recent_usage_count, exposure_score) " +
                    "VALUES (?, 1, CURRENT_TIMESTAMP, 1, 0) " +
                    "ON DUPLICATE KEY UPDATE total_official_usage = total_official_usage + 1, " +
                    "last_official_use = CURRENT_TIMESTAMP, recent_usage_count = recent_usage_count + 1");
                expPs.setInt(1, pq.getQuestionId());
                expPs.executeUpdate();
                expPs.close();
            }

            conn.commit();
            paper.setStatus(PaperStatus.FINALIZED);
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new ExamForgeException("Finalization failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public Paper findById(int id) throws ExamForgeException {
        try {
            return paperDAO.findById(id);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error finding paper: " + e.getMessage(), e);
        }
    }

    public List<Paper> findAll() throws ExamForgeException {
        try {
            return paperDAO.findAll();
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error listing papers: " + e.getMessage(), e);
        }
    }

    public List<Paper> findByStatus(PaperStatus status) throws ExamForgeException {
        try {
            return paperDAO.findByStatus(status);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error listing papers: " + e.getMessage(), e);
        }
    }

    public List<ExaminationRecord> getExaminationHistory() throws ExamForgeException {
        try {
            return paperDAO.findAllExaminationRecords();
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error listing exam history: " + e.getMessage(), e);
        }
    }
}
