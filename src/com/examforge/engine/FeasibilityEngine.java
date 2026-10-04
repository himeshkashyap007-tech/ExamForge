package com.examforge.engine;

import com.examforge.config.SearchConfig;
import com.examforge.dao.QuestionDAO;
import com.examforge.dao.impl.QuestionDAOImpl;
import com.examforge.exception.DatabaseException;
import com.examforge.exception.ExamForgeException;
import com.examforge.model.*;
import com.examforge.util.ExposureScorer;

import java.util.*;
import java.util.stream.Collectors;

public class FeasibilityEngine {
    private final QuestionDAO questionDAO;
    private final ExposureScorer exposureScorer;
    private final SearchConfig searchConfig;
    private int combinationCount;

    public FeasibilityEngine() {
        this.questionDAO = new QuestionDAOImpl();
        this.exposureScorer = ExposureScorer.getInstance();
        this.searchConfig = SearchConfig.getInstance();
    }

    public FeasibilityResult analyzeFeasibility(Blueprint blueprint) throws ExamForgeException {
        try {
            List<Question> allQuestions = questionDAO.findActiveBySubject(blueprint.getSubject());

            if (blueprint.getExposurePolicyType() == ExposurePolicyType.STRICT_BLOCK) {
                allQuestions = applyStrictBlock(allQuestions, blueprint.getExposureWindow());
            }

            if (allQuestions.isEmpty()) {
                return new FeasibilityResult(FeasibilityStatus.PROVEN_INFEASIBLE,
                    "No active questions available for subject: " + blueprint.getSubject());
            }

            if (!checkBasicCounts(blueprint, allQuestions)) {
                return new FeasibilityResult(FeasibilityStatus.PROVEN_INFEASIBLE,
                    "Insufficient questions available to meet basic requirements");
            }

            if (!checkMarkCombinations(blueprint, allQuestions)) {
                return new FeasibilityResult(FeasibilityStatus.PROVEN_INFEASIBLE,
                    "Cannot achieve required mark distribution with available question marks");
            }

            List<String> structuralErrors = checkStructuralConstraints(blueprint, allQuestions);
            if (!structuralErrors.isEmpty()) {
                return new FeasibilityResult(FeasibilityStatus.PROVEN_INFEASIBLE,
                    "Structural constraint violations: " + String.join("; ", structuralErrors));
            }

            return new FeasibilityResult(FeasibilityStatus.FEASIBLE,
                "Blueprint appears feasible. " + allQuestions.size() + " candidate questions available.");
        } catch (DatabaseException e) {
            throw new ExamForgeException("Feasibility analysis failed: " + e.getMessage(), e);
        }
    }

    private List<Question> applyStrictBlock(List<Question> questions, int recentWindow) {
        return questions.stream().filter(q -> {
            try {
                com.examforge.dao.ExposureDAO expDAO = new com.examforge.dao.impl.ExposureDAOImpl();
                com.examforge.model.QuestionExposure exposure = expDAO.findByQuestionId(q.getQuestionId());
                if (exposure == null) return true;
                if (exposure.getRecentUsageCount() < recentWindow) return true;
                if (exposure.getLastOfficialUse() == null) return true;
                long daysSinceLast = java.time.Duration.between(exposure.getLastOfficialUse(), java.time.LocalDateTime.now()).toDays();
                return daysSinceLast > recentWindow * 90L;
            } catch (DatabaseException e) {
                return true;
            }
        }).collect(Collectors.toList());
    }

    private boolean checkBasicCounts(Blueprint blueprint, List<Question> questions) {
        if (questions.size() < blueprint.getNumberOfQuestions()) {
            return false;
        }

        for (BlueprintConstraint c : blueprint.getConstraints()) {
            if (c.getBasis() == ConstraintBasis.COUNT && c.getPriority() == ConstraintPriority.HARD) {
                long matchingCount = questions.stream()
                    .filter(q -> matchesConstraintTarget(q, c))
                    .count();
                if (matchingCount + c.getTolerance() < c.getRequiredValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkMarkCombinations(Blueprint blueprint, List<Question> questions) {
        for (BlueprintConstraint c : blueprint.getConstraints()) {
            if (c.getBasis() == ConstraintBasis.MARKS && c.getPriority() == ConstraintPriority.HARD) {
                List<Integer> availableMarks = questions.stream()
                    .filter(q -> matchesConstraintTarget(q, c))
                    .map(Question::getMarks)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

                if (availableMarks.isEmpty() && c.getRequiredValue() > 0) {
                    return false;
                }

                Set<Integer> achievableSums = new HashSet<>();
                achievableSums.add(0);
                for (int mark : availableMarks) {
                    Set<Integer> newSums = new HashSet<>();
                    for (int sum : achievableSums) {
                        newSums.add(sum + mark);
                    }
                    achievableSums.addAll(newSums);
                }

                boolean found = false;
                double required = c.getRequiredValue();
                double tol = c.getTolerance();
                for (int sum : achievableSums) {
                    if (Math.abs(sum - required) <= tol) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
        }
        return true;
    }

    private List<String> checkStructuralConstraints(Blueprint blueprint, List<Question> questions) {
        List<String> errors = new ArrayList<>();

        int sectionMarksTotal = blueprint.getSectionMarksTotal();
        if (sectionMarksTotal != blueprint.getTotalMarks()) {
            errors.add("Section marks total (" + sectionMarksTotal + ") != paper total (" + blueprint.getTotalMarks() + ")");
        }

        for (BlueprintSection section : blueprint.getSections()) {
            List<Question> sectionPool = questions.stream()
                .filter(q -> matchesSectionFilter(q, section))
                .collect(Collectors.toList());
            if (sectionPool.size() < section.getQuestionCount()) {
                errors.add("Section '" + section.getSectionName() + "' requires " + section.getQuestionCount() +
                           " questions but only " + sectionPool.size() + " eligible candidates available");
            }
        }

        for (BlueprintConstraint c : blueprint.getConstraints()) {
            if (c.getPriority() != ConstraintPriority.HARD) continue;

            if (c.getCategory() == ConstraintCategory.DIFFICULTY && c.getBasis() == ConstraintBasis.MARKS) {
                Difficulty diff = Difficulty.fromString(c.getTarget());
                int avail = questions.stream()
                    .filter(q -> q.getDifficulty() == diff)
                    .mapToInt(Question::getMarks)
                    .sum();
                if (avail + c.getTolerance() < c.getRequiredValue()) {
                    errors.add("Insufficient " + diff.getDisplayName() + " marks: " + avail + " available, " +
                               c.getRequiredValue() + " required");
                }
            }
        }

        return errors;
    }

    private boolean matchesConstraintTarget(Question q, BlueprintConstraint c) {
        switch (c.getCategory()) {
            case UNIT -> {
                return c.getTarget().equals(q.getUnit());
            }
            case DIFFICULTY -> {
                return Difficulty.fromString(c.getTarget()) == q.getDifficulty();
            }
            case QUESTION_TYPE -> {
                return QuestionType.fromString(c.getTarget()) == q.getQuestionType();
            }
            default -> {
                return true;
            }
        }
    }

    private boolean matchesSectionFilter(Question q, BlueprintSection section) {
        if (section.getQuestionTypeFilter() != null && !section.getQuestionTypeFilter().isEmpty()) {
            try {
                QuestionType filterType = QuestionType.fromString(section.getQuestionTypeFilter());
                if (q.getQuestionType() != filterType) return false;
            } catch (IllegalArgumentException ignored) {}
        }
        if (section.getDifficultyFilter() != null && !section.getDifficultyFilter().isEmpty()) {
            try {
                Difficulty filterDiff = Difficulty.fromString(section.getDifficultyFilter());
                if (q.getDifficulty() != filterDiff) return false;
            } catch (IllegalArgumentException ignored) {}
        }
        return true;
    }
}
