package com.examforge.util;

import com.examforge.model.Question;
import com.examforge.model.QuestionType;
import java.util.ArrayList;
import java.util.List;

public class ValidationUtil {

    public static List<String> validateQuestion(Question question) {
        List<String> errors = new ArrayList<>();

        if (question.getQuestionText() == null || question.getQuestionText().trim().isEmpty()) {
            errors.add("Question text cannot be empty");
        }
        if (question.getSubject() == null || question.getSubject().trim().isEmpty()) {
            errors.add("Subject cannot be empty");
        }
        if (question.getUnit() == null || question.getUnit().trim().isEmpty()) {
            errors.add("Unit cannot be empty");
        }
        if (question.getTopic() == null || question.getTopic().trim().isEmpty()) {
            errors.add("Topic cannot be empty");
        }
        if (question.getMarks() <= 0) {
            errors.add("Marks must be a positive integer");
        }
        if (question.getDifficulty() == null) {
            errors.add("Difficulty must be specified");
        }
        if (question.getQuestionType() == null) {
            errors.add("Question type must be specified");
        }

        if (question.getQuestionType() == QuestionType.MCQ) {
            if (question.getOptionA() == null || question.getOptionA().trim().isEmpty()) {
                errors.add("Option A cannot be empty for MCQ");
            }
            if (question.getOptionB() == null || question.getOptionB().trim().isEmpty()) {
                errors.add("Option B cannot be empty for MCQ");
            }
            if (question.getOptionC() == null || question.getOptionC().trim().isEmpty()) {
                errors.add("Option C cannot be empty for MCQ");
            }
            if (question.getOptionD() == null || question.getOptionD().trim().isEmpty()) {
                errors.add("Option D cannot be empty for MCQ");
            }
            if (question.getCorrectOption() == null || question.getCorrectOption().trim().isEmpty()) {
                errors.add("Correct option must be specified for MCQ");
            } else {
                String correct = question.getCorrectOption().trim().toUpperCase();
                if (!correct.equals("A") && !correct.equals("B") && !correct.equals("C") && !correct.equals("D")) {
                    errors.add("Correct option must be A, B, C, or D");
                }
            }
        }

        return errors;
    }

    public static boolean isQuestionValid(Question question) {
        return validateQuestion(question).isEmpty();
    }
}
