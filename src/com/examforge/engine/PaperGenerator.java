package com.examforge.engine;

import com.examforge.config.ScoringConfig;
import com.examforge.config.SearchConfig;
import com.examforge.dao.PaperDAO;
import com.examforge.dao.impl.PaperDAOImpl;
import com.examforge.exception.DatabaseException;
import com.examforge.exception.ExamForgeException;
import com.examforge.exception.PaperGenerationException;
import com.examforge.model.*;
import com.examforge.strategy.PaperGenerationStrategy;
import com.examforge.strategy.DefaultPaperStrategy;
import com.examforge.util.ExposureScorer;

import java.util.*;
import java.util.stream.Collectors;

public class PaperGenerator {
    private final PaperDAO paperDAO;
    private final PaperGenerationStrategy strategy;
    private final SearchConfig searchConfig;

    public PaperGenerator() {
        this.paperDAO = new PaperDAOImpl();
        this.strategy = new DefaultPaperStrategy();
        this.searchConfig = SearchConfig.getInstance();
    }

    public Paper generatePaper(Blueprint blueprint, int userId) throws ExamForgeException {
        List<Question> candidates = loadCandidates(blueprint);
        if (candidates.isEmpty()) {
            throw new PaperGenerationException("No eligible questions available for paper generation");
        }

        FeasibilityEngine feasibilityEngine = new FeasibilityEngine();
        FeasibilityResult feasibilityResult = feasibilityEngine.analyzeFeasibility(blueprint);

        if (feasibilityResult.getStatus() == FeasibilityStatus.PROVEN_INFEASIBLE) {
            throw new PaperGenerationException("Cannot generate paper: " + feasibilityResult.getMessage());
        }
        if (feasibilityResult.getStatus() == FeasibilityStatus.SEARCH_LIMIT_REACHED) {
            throw new PaperGenerationException("Feasibility inconclusive: " + feasibilityResult.getMessage());
        }

        List<Question> selected = strategy.selectQuestions(blueprint, candidates);

        if (selected.isEmpty()) {
            throw new PaperGenerationException("Paper generation failed: no valid combination found");
        }

        Paper paper = buildPaper(blueprint, selected, userId);
        paper = savePaper(paper);
        return paper;
    }

    private List<Question> loadCandidates(Blueprint blueprint) throws ExamForgeException {
        try {
            com.examforge.dao.QuestionDAO qDAO = new com.examforge.dao.impl.QuestionDAOImpl();
            List<Question> questions = qDAO.findActiveBySubject(blueprint.getSubject());

            if (blueprint.getExposurePolicyType() == ExposurePolicyType.STRICT_BLOCK) {
                com.examforge.dao.ExposureDAO expDAO = new com.examforge.dao.impl.ExposureDAOImpl();
                questions = questions.stream().filter(q -> {
                    try {
                        QuestionExposure exp = expDAO.findByQuestionId(q.getQuestionId());
                        if (exp == null) return true;
                        if (exp.getRecentUsageCount() < blueprint.getExposureWindow()) return true;
                        if (exp.getLastOfficialUse() == null) return true;
                        long days = java.time.Duration.between(exp.getLastOfficialUse(), java.time.LocalDateTime.now()).toDays();
                        return days > blueprint.getExposureWindow() * 90L;
                    } catch (DatabaseException e) {
                        return true;
                    }
                }).collect(Collectors.toList());
            }
            return questions;
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to load candidates: " + e.getMessage(), e);
        }
    }

    private Paper buildPaper(Blueprint blueprint, List<Question> selected, int userId) {
        Paper paper = new Paper();
        paper.setBlueprintId(blueprint.getBlueprintId());
        paper.setExamName(blueprint.getExamName());
        paper.setCreatedBy(userId);
        paper.setTotalMarks(blueprint.getTotalMarks());
        paper.setQuestionCount(blueprint.getNumberOfQuestions());

        int sectionIndex = 0;
        int sortOrder = 0;
        Map<Integer, List<Question>> sectionAllocation = allocateToSections(blueprint, selected);

        for (Map.Entry<Integer, List<Question>> entry : sectionAllocation.entrySet()) {
            int sIdx = entry.getKey();
            for (Question q : entry.getValue()) {
                PaperQuestion pq = new PaperQuestion(0, q.getQuestionId(), sIdx, sortOrder++);
                pq.setQuestion(q);
                paper.addQuestion(pq);
            }
            sectionIndex = Math.max(sectionIndex, sIdx + 1);
        }

        return paper;
    }

    private Map<Integer, List<Question>> allocateToSections(Blueprint blueprint, List<Question> selected) {
        Map<Integer, List<Question>> allocation = new TreeMap<>();
        List<Question> remaining = new ArrayList<>(selected);

        for (int i = 0; i < blueprint.getSections().size(); i++) {
            BlueprintSection section = blueprint.getSections().get(i);
            List<Question> sectionQuestions = new ArrayList<>();

            Iterator<Question> it = remaining.iterator();
            while (it.hasNext() && sectionQuestions.size() < section.getQuestionCount()) {
                Question q = it.next();
                if (matchesSectionFilter(q, section)) {
                    sectionQuestions.add(q);
                    it.remove();
                }
            }
            allocation.put(i, sectionQuestions);
        }

        if (!remaining.isEmpty() && allocation.containsKey(0)) {
            allocation.get(0).addAll(remaining);
        }

        return allocation;
    }

    private boolean matchesSectionFilter(Question q, BlueprintSection section) {
        if (section.getQuestionTypeFilter() != null && !section.getQuestionTypeFilter().isEmpty()) {
            try {
                if (q.getQuestionType() != QuestionType.fromString(section.getQuestionTypeFilter())) return false;
            } catch (IllegalArgumentException ignored) {}
        }
        if (section.getDifficultyFilter() != null && !section.getDifficultyFilter().isEmpty()) {
            try {
                if (q.getDifficulty() != Difficulty.fromString(section.getDifficultyFilter())) return false;
            } catch (IllegalArgumentException ignored) {}
        }
        return true;
    }

    private Paper savePaper(Paper paper) throws ExamForgeException {
        try {
            int paperId = paperDAO.insert(paper);
            paper.setPaperId(paperId);
            for (PaperQuestion pq : paper.getQuestions()) {
                pq.setPaperId(paperId);
                paperDAO.insertPaperQuestion(pq);
            }
            return paperDAO.findById(paperId);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to save paper: " + e.getMessage(), e);
        }
    }
}
