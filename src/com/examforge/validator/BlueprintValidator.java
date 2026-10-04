package com.examforge.validator;

import com.examforge.model.*;

import java.util.ArrayList;
import java.util.List;

public class BlueprintValidator {

    public List<String> validate(Blueprint blueprint) {
        List<String> errors = new ArrayList<>();

        if (blueprint.getExamName() == null || blueprint.getExamName().trim().isEmpty()) {
            errors.add("Exam name is required");
        }
        if (blueprint.getSubject() == null || blueprint.getSubject().trim().isEmpty()) {
            errors.add("Subject is required");
        }
        if (blueprint.getTotalMarks() <= 0) {
            errors.add("Total marks must be positive");
        }
        if (blueprint.getNumberOfQuestions() <= 0) {
            errors.add("Number of questions must be positive");
        }
        if (blueprint.getDurationMinutes() <= 0) {
            errors.add("Duration must be positive");
        }

        validateSectionConsistency(blueprint, errors);
        validateConstraintConsistency(blueprint, errors);

        return errors;
    }

    private void validateSectionConsistency(Blueprint blueprint, List<String> errors) {
        if (blueprint.getSections().isEmpty()) {
            errors.add("At least one section is required");
            return;
        }

        int sectionMarksTotal = blueprint.getSectionMarksTotal();
        if (sectionMarksTotal != blueprint.getTotalMarks()) {
            errors.add("Section marks total (" + sectionMarksTotal + ") does not equal paper total marks (" + blueprint.getTotalMarks() + ")");
        }

        int sectionCountTotal = blueprint.getSectionCountTotal();
        if (sectionCountTotal != blueprint.getNumberOfQuestions()) {
            errors.add("Section question count total (" + sectionCountTotal + ") does not equal paper question count (" + blueprint.getNumberOfQuestions() + ")");
        }

        for (BlueprintSection section : blueprint.getSections()) {
            if (section.getSectionName() == null || section.getSectionName().trim().isEmpty()) {
                errors.add("Section name cannot be empty");
            }
            if (section.getQuestionCount() <= 0) {
                errors.add("Section '" + section.getSectionName() + "' must have at least 1 question");
            }
            if (section.getTotalMarks() <= 0) {
                errors.add("Section '" + section.getSectionName() + "' must have positive marks");
            }
            if (section.getQuestionCount() > 0 && section.getTotalMarks() > 0) {
                double avgMarks = (double) section.getTotalMarks() / section.getQuestionCount();
                if (avgMarks < 0.5) {
                    errors.add("Section '" + section.getSectionName() + "' average marks per question is too low (" + String.format("%.1f", avgMarks) + ")");
                }
            }
        }
    }

    private void validateConstraintConsistency(Blueprint blueprint, List<String> errors) {
        double totalHardMarks = 0;
        boolean hasGlobalDifficultyConstraint = false;

        for (BlueprintConstraint c : blueprint.getConstraints()) {
            if (c.getCategory() == ConstraintCategory.DIFFICULTY && c.getBasis() == ConstraintBasis.MARKS) {
                if (c.getPriority() == ConstraintPriority.HARD) {
                    totalHardMarks += c.getRequiredValue();
                }
                hasGlobalDifficultyConstraint = true;
            }

            if (c.getBasis() == ConstraintBasis.PERCENTAGE) {
                // Percentage constraints are always evaluated against question count
                // (see DefaultPaperStrategy/PaperValidator, which compute percentage as
                // matchingCount * 100.0 / totalQuestionCount), so the target here must
                // use the same denominator for the integer-target check below to be meaningful.
                double targetValue = blueprint.getNumberOfQuestions() * c.getRequiredValue() / 100.0;

                if (c.getTolerance() == 0) {
                    if (targetValue != Math.floor(targetValue)) {
                        errors.add("Constraint '" + c + "' produces non-integer target (" +
                                   String.format("%.2f", targetValue) + ") with EXACT_INTEGER policy");
                    }
                }
            }
        }

        if (hasGlobalDifficultyConstraint && totalHardMarks > blueprint.getTotalMarks()) {
            errors.add("Total hard difficulty marks constraints (" + totalHardMarks +
                       ") exceed paper total marks (" + blueprint.getTotalMarks() + ")");
        }
    }
}
