package com.examforge.strategy;

import com.examforge.dao.ExposureDAO;
import com.examforge.dao.impl.ExposureDAOImpl;
import com.examforge.exception.DatabaseException;
import com.examforge.exception.ExamForgeException;
import com.examforge.model.Blueprint;
import com.examforge.model.BlueprintConstraint;
import com.examforge.model.BlueprintSection;
import com.examforge.model.ConstraintBasis;
import com.examforge.model.ConstraintCategory;
import com.examforge.model.ConstraintPriority;
import com.examforge.model.Difficulty;
import com.examforge.model.ExposurePolicyType;
import com.examforge.model.Question;
import com.examforge.model.QuestionExposure;
import com.examforge.model.QuestionType;
import com.examforge.util.ExposureScorer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultPaperStrategy implements PaperGenerationStrategy {

    /**
     * Upper bound on DP table cells (candidates x count x marks) before we fall back
     * to a best-effort greedy selection instead of the exact subset-sum search. This
     * keeps generation fast/bounded for unusually large blueprints or question banks.
     */
    private static final long MAX_DP_CELLS = 6_000_000L;

    private final ExposureDAO exposureDAO = new ExposureDAOImpl();
    private final ExposureScorer exposureScorer = ExposureScorer.getInstance();

    @Override
    public List<Question> selectQuestions(
            Blueprint blueprint,
            List<Question> candidates) throws ExamForgeException {

        if (blueprint == null) {
            throw new ExamForgeException("Blueprint cannot be null");
        }

        if (candidates == null || candidates.isEmpty()) {
            throw new ExamForgeException("No candidate questions available");
        }

        int requiredCount = blueprint.getNumberOfQuestions();

        if (requiredCount <= 0) {
            throw new ExamForgeException("Blueprint question count must be greater than zero");
        }

        List<Question> available = new ArrayList<>();
        for (Question question : candidates) {
            if (question != null && question.isActiveStatus()) {
                available.add(question);
            }
        }

        if (available.size() < requiredCount) {
            throw new ExamForgeException(
                    "Not enough candidate questions: required "
                            + requiredCount
                            + ", available "
                            + available.size());
        }

        List<Question> selected = new ArrayList<>();
        Set<Integer> selectedIds = new HashSet<>();

        if (!blueprint.getSections().isEmpty()) {
            /*
             * Satisfy each blueprint section with exactly the section's question
             * count AND exactly the section's target marks, so the generated paper
             * doesn't fail total/section marks validation.
             */
            for (BlueprintSection section : blueprint.getSections()) {

                int requiredForSection = section.getQuestionCount();
                if (requiredForSection <= 0) {
                    continue;
                }

                List<Question> sectionCandidates = new ArrayList<>();
                for (Question question : available) {
                    if (selectedIds.contains(question.getQuestionId())) {
                        continue;
                    }
                    if (matchesSection(question, section)) {
                        sectionCandidates.add(question);
                    }
                }

                List<Question> chosen = selectExact(
                        sectionCandidates,
                        requiredForSection,
                        section.getTotalMarks(),
                        blueprint);

                if (chosen == null) {
                    throw new ExamForgeException(
                            "Unable to satisfy section '"
                                    + section.getSectionName()
                                    + "': no combination of "
                                    + requiredForSection
                                    + " question(s) totalling exactly "
                                    + section.getTotalMarks()
                                    + " marks could be found among "
                                    + sectionCandidates.size()
                                    + " eligible candidate(s).");
                }

                for (Question q : chosen) {
                    if (selectedIds.add(q.getQuestionId())) {
                        selected.add(q);
                    }
                }
            }
        } else {
            /*
             * No sections defined: pick exactly requiredCount questions summing to
             * exactly the blueprint's total marks.
             */
            List<Question> chosen = selectExact(available, requiredCount, blueprint.getTotalMarks(), blueprint);
            if (chosen == null) {
                throw new ExamForgeException(
                        "Unable to select "
                                + requiredCount
                                + " question(s) totalling exactly "
                                + blueprint.getTotalMarks()
                                + " marks from "
                                + available.size()
                                + " candidate(s).");
            }
            selected.addAll(chosen);
            for (Question q : chosen) {
                selectedIds.add(q.getQuestionId());
            }
        }

        if (selected.size() != requiredCount) {
            throw new ExamForgeException(
                    "Unable to generate exactly "
                            + requiredCount
                            + " questions");
        }

        validateHardConstraints(selected, blueprint.getConstraints());

        return selected;
    }

    /**
     * Selects exactly {@code targetCount} questions from {@code pool} whose marks sum to
     * exactly {@code targetMarks}, preferring (in order) questions that satisfy more HARD
     * blueprint constraints and, among ties, questions with lower exposure (when the
     * blueprint's exposure policy is SOFT_AVOID). Returns null if no exact combination exists.
     */
    private List<Question> selectExact(
            List<Question> pool,
            int targetCount,
            int targetMarks,
            Blueprint blueprint) {

        if (targetCount == 0) {
            return new ArrayList<>();
        }
        if (pool.size() < targetCount || targetMarks < 0) {
            return null;
        }

        List<BlueprintConstraint> constraints = blueprint.getConstraints();

        List<Question> ranked = new ArrayList<>(pool);
        ranked.sort(
                Comparator
                        .comparingInt((Question q) -> constraintScore(q, constraints))
                        .reversed()
                        .thenComparingDouble((Question q) -> exposurePenalty(q, blueprint))
                        .thenComparingInt(Question::getQuestionId)
        );

        int n = ranked.size();
        int[] marks = new int[n];
        for (int i = 0; i < n; i++) {
            marks[i] = ranked.get(i).getMarks();
        }

        long cells = (long) (n + 1) * (targetCount + 1) * (targetMarks + 1);
        if (cells <= 0 || cells > MAX_DP_CELLS) {
            // Blueprint/pool too large for exact DP within our time budget; fall back
            // to a best-effort greedy pick (preference order, first-fit on marks).
            return greedyFallback(ranked, targetCount, targetMarks);
        }

        // canReach[i][c][m] = true if items ranked[i..n-1] can supply exactly c
        // questions summing to exactly m marks.
        boolean[][][] canReach = new boolean[n + 1][targetCount + 1][targetMarks + 1];
        canReach[n][0][0] = true;

        for (int i = n - 1; i >= 0; i--) {
            for (int c = 0; c <= targetCount; c++) {
                for (int m = 0; m <= targetMarks; m++) {
                    boolean skip = canReach[i + 1][c][m];
                    boolean take = false;
                    if (!skip && c > 0 && m >= marks[i]) {
                        take = canReach[i + 1][c - 1][m - marks[i]];
                    }
                    canReach[i][c][m] = skip || take;
                }
            }
        }

        if (!canReach[0][targetCount][targetMarks]) {
            return null;
        }

        List<Question> result = new ArrayList<>();
        int c = targetCount;
        int m = targetMarks;
        for (int i = 0; i < n && !(c == 0 && m == 0); i++) {
            if (c > 0 && m >= marks[i] && canReach[i + 1][c - 1][m - marks[i]]) {
                result.add(ranked.get(i));
                c--;
                m -= marks[i];
            }
            // else: skip ranked.get(i); canReach[i+1][c][m] is guaranteed true here
        }

        return result;
    }

    /**
     * Best-effort selection used only when the exact DP would be too large to run.
     * Does not guarantee hitting targetMarks exactly; the caller's final paper
     * validation step will surface any mismatch.
     */
    private List<Question> greedyFallback(List<Question> ranked, int targetCount, int targetMarks) {
        List<Question> result = new ArrayList<>();
        Set<Integer> used = new HashSet<>();
        int remainingMarks = targetMarks;

        // First pass: fill within the marks budget, in preference order.
        for (Question q : ranked) {
            if (result.size() >= targetCount) break;
            if (q.getMarks() <= remainingMarks) {
                result.add(q);
                used.add(q.getQuestionId());
                remainingMarks -= q.getMarks();
            }
        }

        // Top up with remaining preference-ordered candidates if still short on count.
        if (result.size() < targetCount) {
            for (Question q : ranked) {
                if (result.size() >= targetCount) break;
                if (!used.contains(q.getQuestionId())) {
                    result.add(q);
                    used.add(q.getQuestionId());
                }
            }
        }

        return result.size() == targetCount ? result : null;
    }

    private double exposurePenalty(Question question, Blueprint blueprint) {
        if (blueprint.getExposurePolicyType() != ExposurePolicyType.SOFT_AVOID) {
            return 0.0;
        }
        try {
            QuestionExposure exposure = exposureDAO.findByQuestionId(question.getQuestionId());
            if (exposure == null) {
                return 0.0;
            }
            int totalUsage = exposure.getTotalOfficialUsage();
            if (totalUsage <= 0) {
                return 0.0;
            }
            int recent = Math.min(Math.max(exposure.getRecentUsageCount(), 0), totalUsage);
            List<Integer> recencyPositions = new ArrayList<>();
            for (int i = 0; i < recent; i++) {
                recencyPositions.add(0); // used within the recent exposure window
            }
            for (int i = recent; i < totalUsage; i++) {
                recencyPositions.add(3); // older usage, outside the recent window
            }
            return exposureScorer.calculatePenalty(recencyPositions, totalUsage);
        } catch (DatabaseException e) {
            // Exposure lookup failing shouldn't block generation; treat as unknown/never used.
            return 0.0;
        }
    }

    private boolean matchesSection(
            Question question,
            BlueprintSection section) {

        String typeFilter = section.getQuestionTypeFilter();

        if (typeFilter != null && !typeFilter.isBlank()) {
            try {
                QuestionType requiredType =
                        QuestionType.fromString(typeFilter);

                if (question.getQuestionType() != requiredType) {
                    return false;
                }
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        String difficultyFilter = section.getDifficultyFilter();

        if (difficultyFilter != null && !difficultyFilter.isBlank()) {
            try {
                Difficulty requiredDifficulty =
                        Difficulty.fromString(difficultyFilter);

                if (question.getDifficulty() != requiredDifficulty) {
                    return false;
                }
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        return true;
    }

    private int constraintScore(
            Question question,
            List<BlueprintConstraint> constraints) {

        if (constraints == null) {
            return 0;
        }

        int score = 0;

        for (BlueprintConstraint constraint : constraints) {

            if (constraint.getPriority() != ConstraintPriority.HARD) {
                continue;
            }

            boolean matches = matchesConstraint(question, constraint);

            if (matches) {
                score += 10;

                if (constraint.getBasis() == ConstraintBasis.MARKS) {
                    score += question.getMarks();
                }
            }
        }

        return score;
    }

    private boolean matchesConstraint(
            Question question,
            BlueprintConstraint constraint) {

        ConstraintCategory category = constraint.getCategory();

        if (category == null) {
            return false;
        }

        String target = constraint.getTarget();

        if (target == null) {
            return false;
        }

        return switch (category) {

            case UNIT ->
                    target.equalsIgnoreCase(question.getUnit());

            case DIFFICULTY -> {
                try {
                    yield Difficulty.fromString(target)
                            == question.getDifficulty();
                } catch (IllegalArgumentException e) {
                    yield false;
                }
            }

            case QUESTION_TYPE -> {
                try {
                    yield QuestionType.fromString(target)
                            == question.getQuestionType();
                } catch (IllegalArgumentException e) {
                    yield false;
                }
            }

            case SECTION, EXPOSURE ->
                    true;
        };
    }

    private void validateHardConstraints(
            List<Question> selected,
            List<BlueprintConstraint> constraints)
            throws ExamForgeException {

        if (constraints == null) {
            return;
        }

        for (BlueprintConstraint constraint : constraints) {

            if (constraint.getPriority() != ConstraintPriority.HARD) {
                continue;
            }

            int matchingCount = 0;
            int matchingMarks = 0;

            for (Question question : selected) {
                if (matchesConstraint(question, constraint)) {
                    matchingCount++;
                    matchingMarks += question.getMarks();
                }
            }

            double required = constraint.getRequiredValue();
            double tolerance = constraint.getTolerance();

            boolean satisfied;

            switch (constraint.getBasis()) {

                case COUNT:
                    // Symmetric tolerance band, consistent with how PaperValidator
                    // independently re-checks the finalized paper against the blueprint
                    // (both too few AND too many matches should fail a HARD constraint).
                    satisfied =
                            Math.abs(matchingCount - required) <= tolerance;
                    break;

                case MARKS:
                    satisfied =
                            Math.abs(matchingMarks - required)
                                    <= tolerance;
                    break;

                case PERCENTAGE:
                    double percentage =
                            selected.isEmpty()
                                    ? 0
                                    : (matchingCount * 100.0)
                                      / selected.size();

                    satisfied =
                            Math.abs(percentage - required)
                                    <= tolerance;
                    break;

                default:
                    satisfied = true;
            }

            if (!satisfied) {
                throw new ExamForgeException(
                        "Hard constraint could not be satisfied: "
                                + constraint);
            }
        }
    }
}
