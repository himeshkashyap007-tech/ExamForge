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

    public static class Session {
        private final StudentAttempt attempt;
        private final List<Question> questions;
        private final boolean resumed;

        public Session(StudentAttempt attempt, List<Question> questions, boolean resumed) {
            this.attempt = attempt;
            this.questions = questions;
            this.resumed = resumed;
        }

        public StudentAttempt getAttempt() { return attempt; }
        public List<Question> getQuestions() { return questions; }
        public boolean isResumed() { return resumed; }
    }

    public Session startSession(int studentId, String practiceType, String subject, int numQuestions) throws ExamForgeException {
        try {
            List<Question> pool = questionDAO.findActiveBySubject(subject);
            if (numQuestions > pool.size()) {
                throw new ExamForgeException("You cannot select more questions than are available in the question bank (pool: " +
                    pool.size() + ", requested: " + numQuestions + ").");
            }

            StudentAttempt open = studentDAO.findOpenAttempt(studentId, practiceType, subject);
            if (open != null) {
                List<Question> restored = restoreAssignedQuestions(open);
                if (restored.size() == open.getTotalQuestions()) {
                    return new Session(open, restored, true);
                }
            }

            String type = practiceType == null ? "Random" : practiceType;
            List<Question> selected;
            switch (type) {
                case "Balanced" -> selected = startBalancedPractice(subject, numQuestions);
                case "Weak-Area" -> selected = startWeakAreaPractice(subject, numQuestions, studentId);
                default -> selected = startRandomPractice(subject, numQuestions);
            }

            StudentAttempt attempt = new StudentAttempt(studentId, type, subject, selected.size(),
                selected.stream().mapToInt(Question::getMarks).sum(), 0);
            attempt.setStatus("IN_PROGRESS");
            attempt.setAssignedQuestionIds(toCsv(selected));
            int attemptId = studentDAO.insertAttempt(attempt);
            attempt.setAttemptId(attemptId);
            return new Session(attempt, selected, false);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to start session: " + e.getMessage(), e);
        }
    }

    public void abandonSession(int attemptId) throws ExamForgeException {
        try {
            studentDAO.deleteAttempt(attemptId);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to abandon session: " + e.getMessage(), e);
        }
    }

    public StudentAttempt submitSession(int attemptId, int studentId, String practiceType, String subject,
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
        attempt.setAttemptId(attemptId);
        attempt.setCorrectAnswers(correctAnswers);
        attempt.setTimeSpentSeconds(timeSpentSeconds);

        try {
            studentDAO.completeAttempt(attemptId, correctAnswers, marksObtained, timeSpentSeconds);

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
            throw new ExamForgeException("Failed to submit session: " + e.getMessage(), e);
        }
    }

    private List<Question> restoreAssignedQuestions(StudentAttempt attempt) throws DatabaseException {
        String csv = attempt.getAssignedQuestionIds();
        if (csv == null || csv.trim().isEmpty()) return new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        for (String part : csv.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) ids.add(Integer.parseInt(t));
        }
        return questionDAO.findByIdsInOrder(ids);
    }

    private String toCsv(List<Question> questions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < questions.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(questions.get(i).getQuestionId());
        }
        return sb.toString();
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
