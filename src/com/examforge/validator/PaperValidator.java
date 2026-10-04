package com.examforge.validator;

import com.examforge.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class PaperValidator {

    public List<ValidationCheckResult> validate(Paper paper, Blueprint blueprint) {
        List<ValidationCheckResult> results = new ArrayList<>();

        validateTotalMarks(paper, blueprint, results);
        validateQuestionCount(paper, blueprint, results);
        validateNoDuplicates(paper, results);
        validateAllQuestionsActive(paper, results);
        validateSectionStructure(paper, blueprint, results);
        validateHardConstraints(paper, blueprint, results);

        return results;
    }

    private void validateTotalMarks(Paper paper, Blueprint blueprint, List<ValidationCheckResult> results) {
        int actualMarks = paper.getQuestions().stream()
            .mapToInt(pq -> pq.getQuestion() != null ? pq.getQuestion().getMarks() : 0)
            .sum();
        if (actualMarks == blueprint.getTotalMarks()) {
            results.add(ValidationCheckResult.pass("Total Marks",
                String.valueOf(blueprint.getTotalMarks()), String.valueOf(actualMarks)));
        } else {
            results.add(ValidationCheckResult.fail("Total Marks",
                String.valueOf(blueprint.getTotalMarks()), String.valueOf(actualMarks),
                "Paper total marks (" + actualMarks + ") does not equal required (" + blueprint.getTotalMarks() + ")"));
        }
    }

    private void validateQuestionCount(Paper paper, Blueprint blueprint, List<ValidationCheckResult> results) {
        int count = paper.getQuestions().size();
        if (count == blueprint.getNumberOfQuestions()) {
            results.add(ValidationCheckResult.pass("Question Count",
                String.valueOf(blueprint.getNumberOfQuestions()), String.valueOf(count)));
        } else {
            results.add(ValidationCheckResult.fail("Question Count",
                String.valueOf(blueprint.getNumberOfQuestions()), String.valueOf(count),
                "Paper question count (" + count + ") does not equal required (" + blueprint.getNumberOfQuestions() + ")"));
        }
    }

    private void validateNoDuplicates(Paper paper, List<ValidationCheckResult> results) {
        Set<Integer> ids = new HashSet<>();
        boolean hasDup = false;
        for (PaperQuestion pq : paper.getQuestions()) {
            if (!ids.add(pq.getQuestionId())) {
                hasDup = true;
                break;
            }
        }
        if (!hasDup) {
            results.add(ValidationCheckResult.pass("No Duplicates", "0", "0"));
        } else {
            results.add(ValidationCheckResult.fail("No Duplicates", "0", "1",
                "Paper contains duplicate questions"));
        }
    }

    private void validateAllQuestionsActive(Paper paper, List<ValidationCheckResult> results) {
        boolean allActive = paper.getQuestions().stream()
            .allMatch(pq -> pq.getQuestion() != null && pq.getQuestion().isActiveStatus());
        if (allActive) {
            results.add(ValidationCheckResult.pass("Active Status", "All Active", "All Active"));
        } else {
            results.add(ValidationCheckResult.fail("Active Status", "All Active", "Some Inactive",
                "Paper contains inactive questions"));
        }
    }

    private void validateSectionStructure(Paper paper, Blueprint blueprint, List<ValidationCheckResult> results) {
        if (blueprint.getSections().isEmpty()) return;

        Map<Integer, List<PaperQuestion>> bySection = new HashMap<>();
        for (PaperQuestion pq : paper.getQuestions()) {
            bySection.computeIfAbsent(pq.getSectionIndex(), k -> new ArrayList<>()).add(pq);
        }

        for (int i = 0; i < blueprint.getSections().size(); i++) {
            BlueprintSection bs = blueprint.getSections().get(i);
            List<PaperQuestion> sectionPqs = bySection.getOrDefault(i, new ArrayList<>());

            if (sectionPqs.size() == bs.getQuestionCount()) {
                results.add(ValidationCheckResult.pass("Section '" + bs.getSectionName() + "' Count",
                    String.valueOf(bs.getQuestionCount()), String.valueOf(sectionPqs.size())));
            } else {
                results.add(ValidationCheckResult.fail("Section '" + bs.getSectionName() + "' Count",
                    String.valueOf(bs.getQuestionCount()), String.valueOf(sectionPqs.size()),
                    "Section question count mismatch"));
            }

            int sectionMarks = sectionPqs.stream()
                .mapToInt(pq -> pq.getQuestion() != null ? pq.getQuestion().getMarks() : 0)
                .sum();
            if (sectionMarks == bs.getTotalMarks()) {
                results.add(ValidationCheckResult.pass("Section '" + bs.getSectionName() + "' Marks",
                    String.valueOf(bs.getTotalMarks()), String.valueOf(sectionMarks)));
            } else {
                results.add(ValidationCheckResult.fail("Section '" + bs.getSectionName() + "' Marks",
                    String.valueOf(bs.getTotalMarks()), String.valueOf(sectionMarks),
                    "Section marks mismatch"));
            }
        }
    }

    private void validateHardConstraints(Paper paper, Blueprint blueprint, List<ValidationCheckResult> results) {
        List<BlueprintConstraint> hardConstraints = blueprint.getConstraints().stream()
            .filter(c -> c.getPriority() == ConstraintPriority.HARD)
            .toList();

        for (BlueprintConstraint constraint : hardConstraints) {
            switch (constraint.getCategory()) {
                case DIFFICULTY -> validateDifficultyConstraint(paper, constraint, results);
                case QUESTION_TYPE -> validateTypeConstraint(paper, constraint, results);
                case UNIT -> validateUnitConstraint(paper, constraint, results);
                default -> {}
            }
        }
    }

    private void validateDifficultyConstraint(Paper paper, BlueprintConstraint constraint,
                                               List<ValidationCheckResult> results) {
        Difficulty diff = Difficulty.fromString(constraint.getTarget());
        double actual;
        if (constraint.getBasis() == ConstraintBasis.COUNT) {
            actual = paper.getQuestions().stream()
                .filter(pq -> pq.getQuestion() != null && pq.getQuestion().getDifficulty() == diff)
                .count();
        } else if (constraint.getBasis() == ConstraintBasis.PERCENTAGE) {
            long matchingCount = paper.getQuestions().stream()
                .filter(pq -> pq.getQuestion() != null && pq.getQuestion().getDifficulty() == diff)
                .count();
            actual = paper.getQuestions().isEmpty() ? 0 : (matchingCount * 100.0) / paper.getQuestions().size();
        } else {
            actual = paper.getQuestions().stream()
                .filter(pq -> pq.getQuestion() != null && pq.getQuestion().getDifficulty() == diff)
                .mapToInt(pq -> pq.getQuestion().getMarks())
                .sum();
        }

        double required = constraint.getRequiredValue();
        double tolerance = constraint.getTolerance();
        String name = "Difficulty " + diff.getDisplayName() + " " + constraint.getBasis().getDisplayName();

        if (Math.abs(actual - required) <= tolerance) {
            results.add(ValidationCheckResult.pass(name, String.valueOf(required), String.valueOf(actual)));
        } else {
            results.add(ValidationCheckResult.fail(name, String.valueOf(required), String.valueOf(actual),
                name + " constraint violated: required " + required + " +/- " + tolerance + ", got " + actual));
        }
    }

    private void validateTypeConstraint(Paper paper, BlueprintConstraint constraint,
                                         List<ValidationCheckResult> results) {
        QuestionType type = QuestionType.fromString(constraint.getTarget());
        double actual;
        if (constraint.getBasis() == ConstraintBasis.COUNT) {
            actual = paper.getQuestions().stream()
                .filter(pq -> pq.getQuestion() != null && pq.getQuestion().getQuestionType() == type)
                .count();
        } else if (constraint.getBasis() == ConstraintBasis.PERCENTAGE) {
            long matchingCount = paper.getQuestions().stream()
                .filter(pq -> pq.getQuestion() != null && pq.getQuestion().getQuestionType() == type)
                .count();
            actual = paper.getQuestions().isEmpty() ? 0 : (matchingCount * 100.0) / paper.getQuestions().size();
        } else {
            actual = paper.getQuestions().stream()
                .filter(pq -> pq.getQuestion() != null && pq.getQuestion().getQuestionType() == type)
                .mapToInt(pq -> pq.getQuestion().getMarks())
                .sum();
        }

        double required = constraint.getRequiredValue();
        double tolerance = constraint.getTolerance();
        String name = "Type " + type.getDisplayName() + " " + constraint.getBasis().getDisplayName();

        if (Math.abs(actual - required) <= tolerance) {
            results.add(ValidationCheckResult.pass(name, String.valueOf(required), String.valueOf(actual)));
        } else {
            results.add(ValidationCheckResult.fail(name, String.valueOf(required), String.valueOf(actual),
                name + " constraint violated"));
        }
    }

    private void validateUnitConstraint(Paper paper, BlueprintConstraint constraint,
                                         List<ValidationCheckResult> results) {
        double actual;
        if (constraint.getBasis() == ConstraintBasis.COUNT) {
            actual = paper.getQuestions().stream()
                .filter(pq -> pq.getQuestion() != null && constraint.getTarget().equals(pq.getQuestion().getUnit()))
                .count();
        } else if (constraint.getBasis() == ConstraintBasis.PERCENTAGE) {
            long matchingCount = paper.getQuestions().stream()
                .filter(pq -> pq.getQuestion() != null && constraint.getTarget().equals(pq.getQuestion().getUnit()))
                .count();
            actual = paper.getQuestions().isEmpty() ? 0 : (matchingCount * 100.0) / paper.getQuestions().size();
        } else {
            actual = paper.getQuestions().stream()
                .filter(pq -> pq.getQuestion() != null && constraint.getTarget().equals(pq.getQuestion().getUnit()))
                .mapToInt(pq -> pq.getQuestion().getMarks())
                .sum();
        }

        double required = constraint.getRequiredValue();
        double tolerance = constraint.getTolerance();
        String name = "Unit " + constraint.getTarget() + " " + constraint.getBasis().getDisplayName();

        if (Math.abs(actual - required) <= tolerance) {
            results.add(ValidationCheckResult.pass(name, String.valueOf(required), String.valueOf(actual)));
        } else {
            results.add(ValidationCheckResult.fail(name, String.valueOf(required), String.valueOf(actual),
                name + " constraint violated"));
        }
    }
}
