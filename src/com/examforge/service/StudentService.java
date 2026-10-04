package com.examforge.service;

import com.examforge.dao.QuestionDAO;
import com.examforge.dao.StudentDAO;
import com.examforge.dao.impl.QuestionDAOImpl;
import com.examforge.dao.impl.StudentDAOImpl;
import com.examforge.exception.DatabaseException;
import com.examforge.exception.ExamForgeException;
import com.examforge.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class StudentService {
    private final StudentDAO studentDAO;
    private final QuestionDAO questionDAO;

    public StudentService() {
        this.studentDAO = new StudentDAOImpl();
        this.questionDAO = new QuestionDAOImpl();
    }

    public List<Question> startRandomPractice(String subject, int numQuestions) throws ExamForgeException {
        try {
            List<Question> all = questionDAO.findActiveBySubject(subject);
            if (all.size() < numQuestions) {
                numQuestions = all.size();
            }
            Collections.shuffle(all);
            return all.subList(0, numQuestions);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to start practice: " + e.getMessage(), e);
        }
    }

    public List<Question> startBalancedPractice(String subject, int numQuestions) throws ExamForgeException {
        try {
            List<Question> all = questionDAO.findActiveBySubject(subject);
            if (all.isEmpty()) return new ArrayList<>();
            if (all.size() < numQuestions) numQuestions = all.size();

            Map<String, List<Question>> byDifficulty = new HashMap<>();
            for (Question q : all) {
                byDifficulty.computeIfAbsent(q.getDifficulty().name(), k -> new ArrayList<>()).add(q);
            }

            List<Question> selected = new ArrayList<>();
            int perDifficulty = numQuestions / 3;
            int remainder = numQuestions % 3;

            for (String diff : List.of("EASY", "MEDIUM", "HARD")) {
                List<Question> pool = byDifficulty.getOrDefault(diff, new ArrayList<>());
                int count = perDifficulty + (remainder-- > 0 ? 1 : 0);
                count = Math.min(count, pool.size());
                Collections.shuffle(pool);
                selected.addAll(pool.subList(0, count));
            }

            while (selected.size() < numQuestions && selected.size() < all.size()) {
                for (Question q : all) {
                    if (!selected.contains(q) && selected.size() < numQuestions) {
                        selected.add(q);
                    }
                }
            }
            return selected;
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to start balanced practice: " + e.getMessage(), e);
        }
    }

    public List<Question> startWeakAreaPractice(String subject, int numQuestions, int studentId) throws ExamForgeException {
        try {
            List<String> weakTopics = studentDAO.findWeakTopics(studentId, subject);
            List<Question> all = questionDAO.findActiveBySubject(subject);

            List<Question> weakQuestions = all.stream()
                .filter(q -> weakTopics.contains(q.getTopic()))
                .collect(Collectors.toList());

            Collections.shuffle(weakQuestions);
            if (weakQuestions.size() >= numQuestions) {
                return weakQuestions.subList(0, numQuestions);
            }

            List<Question> remaining = all.stream()
                .filter(q -> !weakQuestions.contains(q))
                .collect(Collectors.toList());
            Collections.shuffle(remaining);
            int needed = numQuestions - weakQuestions.size();
            weakQuestions.addAll(remaining.subList(0, Math.min(needed, remaining.size())));
            return weakQuestions;
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to start weak-area practice: " + e.getMessage(), e);
        }
    }

    public StudentAttempt submitQuiz(int studentId, String practiceType, String subject,
                                      List<Question> questions, Map<Integer, String> answers,
                                      int timeSpentSeconds) throws ExamForgeException {
        int totalMarks = questions.stream().mapToInt(Question::getMarks).sum();
        int marksObtained = 0;
        int correctAnswers = 0;

        for (Question q : questions) {
            String answer = answers.get(q.getQuestionId());
            if (answer != null && answer.equalsIgnoreCase(q.getCorrectOption())) {
                marksObtained += q.getMarks();
                correctAnswers++;
            }
        }

        StudentAttempt attempt = new StudentAttempt(studentId, practiceType, subject,
            questions.size(), totalMarks, marksObtained);
        attempt.setCorrectAnswers(correctAnswers);
        attempt.setTimeSpentSeconds(timeSpentSeconds);

        try {
            int attemptId = studentDAO.insertAttempt(attempt);
            attempt.setAttemptId(attemptId);

            for (Question q : questions) {
                StudentResponse response = new StudentResponse(attemptId, q.getQuestionId(),
                    answers.getOrDefault(q.getQuestionId(), ""));
                String correct = q.getCorrectOption();
                boolean isCorrect = correct != null && correct.equalsIgnoreCase(response.getSelectedOption());
                response.setCorrect(isCorrect);
                response.setMarksObtained(isCorrect ? q.getMarks() : 0);
                studentDAO.insertResponse(response);
            }
            return attempt;
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to submit quiz: " + e.getMessage(), e);
        }
    }

    public List<StudentAttempt> getAttempts(int studentId) throws ExamForgeException {
        try {
            return studentDAO.findAttemptsByStudentId(studentId);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error listing attempts: " + e.getMessage(), e);
        }
    }

    public List<StudentResponse> getAttemptResponses(int attemptId) throws ExamForgeException {
        try {
            return studentDAO.findResponsesByAttemptId(attemptId);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error listing responses: " + e.getMessage(), e);
        }
    }

    public double getAverageScore(int studentId) throws ExamForgeException {
        try {
            return studentDAO.getAverageScore(studentId);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error calculating average: " + e.getMessage(), e);
        }
    }

    public int getTotalAttempts(int studentId) throws ExamForgeException {
        try {
            return studentDAO.getTotalAttempts(studentId);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error counting attempts: " + e.getMessage(), e);
        }
    }
}
