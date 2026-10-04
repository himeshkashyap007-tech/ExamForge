package com.examforge.ui.panel;

import com.examforge.exception.ExamForgeException;
import com.examforge.model.*;
import com.examforge.service.QuestionService;
import com.examforge.service.StudentService;
import com.examforge.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class StudentPracticePanel extends JPanel {
    private final User user;
    private final StudentService studentService;
    private final QuestionService questionService;
    private JComboBox<String> subjectCombo;
    private JComboBox<String> typeCombo;
    private JSpinner numQuestionsSpinner;
    private JTextArea questionArea;
    private ButtonGroup answerGroup;
    private JPanel optionsPanel;
    private JLabel questionLabel;
    private JButton nextBtn;
    private JButton submitBtn;
    private List<Question> questions;
    private int currentIndex;
    private Map<Integer, String> answers;
    private long startTime;
    private Runnable backAction;

    public StudentPracticePanel(User user) {
        this.user = user;
        this.studentService = new StudentService();
        this.questionService = new QuestionService();
        setLayout(new BorderLayout());
        setBackground(Theme.SURFACE);
        initUI();
    }

    public void setBackAction(Runnable backAction) {
        this.backAction = backAction;
    }

    private JButton createBackButton() {
        JButton backBtn = Theme.backButton();
        backBtn.addActionListener(e -> {
            if (backAction != null) backAction.run();
        });
        return backBtn;
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(Theme.ACCENT_TINT);
        topPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

        topPanel.add(createBackButton());
        topPanel.add(Theme.label("Subject:", 12, Font.BOLD, Theme.MUTED));
        subjectCombo = new JComboBox<>();
        Theme.styleField(subjectCombo);
        try {
            for (String s : questionService.findDistinctSubjects()) subjectCombo.addItem(s);
        } catch (ExamForgeException ignored) {}
        topPanel.add(subjectCombo);

        topPanel.add(Theme.label("Type:", 12, Font.BOLD, Theme.MUTED));
        typeCombo = new JComboBox<>(new String[]{"Random", "Balanced", "Weak-Area"});
        Theme.styleField(typeCombo);
        topPanel.add(typeCombo);

        topPanel.add(Theme.label("Questions:", 12, Font.BOLD, Theme.MUTED));
        numQuestionsSpinner = new JSpinner(new SpinnerNumberModel(10, 5, 50, 1));
        Theme.styleField(numQuestionsSpinner);
        topPanel.add(numQuestionsSpinner);

        JButton startBtn = Theme.button("Start Practice", Theme.SUCCESS);
        startBtn.addActionListener(e -> startPractice());
        topPanel.add(startBtn);

        add(topPanel, BorderLayout.NORTH);

        JPanel quizPanel = new JPanel(new BorderLayout());
        quizPanel.setBackground(Theme.SURFACE);
        quizPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setOpaque(false);
        progressPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        questionLabel = new JLabel("Start a practice session to begin");
        questionLabel.setFont(Theme.font(14, Font.BOLD));
        questionLabel.setForeground(Theme.TEXT);
        progressPanel.add(questionLabel, BorderLayout.WEST);
        add(progressPanel, BorderLayout.CENTER);

        optionsPanel = new JPanel();
        optionsPanel.setBackground(Theme.SURFACE);
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navPanel.setOpaque(false);
        nextBtn = Theme.softButton("Next");
        nextBtn.setEnabled(false);
        nextBtn.addActionListener(e -> showNextQuestion());
        submitBtn = Theme.button("Submit Quiz", Theme.WARNING);
        submitBtn.setEnabled(false);
        submitBtn.addActionListener(e -> submitQuiz());
        navPanel.add(nextBtn);
        navPanel.add(submitBtn);

        quizPanel.add(optionsPanel, BorderLayout.CENTER);
        quizPanel.add(navPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, progressPanel, quizPanel);
        splitPane.setDividerLocation(40);
        add(splitPane, BorderLayout.SOUTH);

        answers = new HashMap<>();
    }

    private void startPractice() {
        String subject = (String) subjectCombo.getSelectedItem();
        if (subject == null) {
            JOptionPane.showMessageDialog(this, "Please select a subject");
            return;
        }
        int numQuestions = (int) numQuestionsSpinner.getValue();
        String type = (String) typeCombo.getSelectedItem();

        try {
            switch (type) {
                case "Random" -> questions = studentService.startRandomPractice(subject, numQuestions);
                case "Balanced" -> questions = studentService.startBalancedPractice(subject, numQuestions);
                case "Weak-Area" -> questions = studentService.startWeakAreaPractice(subject, numQuestions, user.getUserId());
                default -> questions = studentService.startRandomPractice(subject, numQuestions);
            }

            if (questions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No questions available for this subject");
                return;
            }

            currentIndex = 0;
            answers.clear();
            startTime = System.currentTimeMillis();
            nextBtn.setEnabled(true);
            submitBtn.setEnabled(questions.size() > 0);
            showQuestion(0);
        } catch (ExamForgeException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showQuestion(int index) {
        if (index >= questions.size()) return;
        Question q = questions.get(index);
        questionLabel.setText("Question " + (index + 1) + " of " + questions.size() +
            " [" + q.getMarks() + " marks] - " + q.getDifficulty().getDisplayName());

        optionsPanel.removeAll();
        JLabel qText = new JLabel("<html><b>" + q.getQuestionText() + "</b></html>");
        qText.setFont(Theme.font(14, Font.PLAIN));
        qText.setForeground(Theme.TEXT);
        optionsPanel.add(qText);
        optionsPanel.add(Box.createVerticalStrut(10));

        answerGroup = new ButtonGroup();
        String[] opts = {"A", "B", "C", "D"};
        String[] optTexts = {q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()};

        if (q.getQuestionType() == QuestionType.MCQ) {
            for (int i = 0; i < 4; i++) {
                if (optTexts[i] != null && !optTexts[i].isEmpty()) {
                    JRadioButton rb = new JRadioButton(opts[i] + ". " + optTexts[i]);
                    rb.setFont(Theme.font(13, Font.PLAIN));
                    rb.setBackground(Theme.SURFACE);
                    rb.setActionCommand(opts[i]);
                    if (answers.containsKey(q.getQuestionId())) {
                        rb.setSelected(answers.get(q.getQuestionId()).equals(opts[i]));
                    }
                    answerGroup.add(rb);
                    optionsPanel.add(rb);
                    optionsPanel.add(Box.createVerticalStrut(5));
                }
            }
        } else {
            JTextArea answerArea = new JTextArea(4, 50);
            answerArea.setLineWrap(true);
            answerArea.setWrapStyleWord(true);
            answerArea.setFont(Theme.font(13, Font.PLAIN));
            answerArea.setBorder(new EmptyBorder(6, 6, 6, 6));
            optionsPanel.add(Theme.label("Your Answer:", 12, Font.BOLD, Theme.MUTED));
            optionsPanel.add(answerArea);
        }

        optionsPanel.revalidate();
        optionsPanel.repaint();
    }

    private void showNextQuestion() {
        if (currentIndex < questions.size()) {
            saveCurrentAnswer();
            currentIndex++;
            if (currentIndex < questions.size()) {
                showQuestion(currentIndex);
            } else {
                nextBtn.setEnabled(false);
                JOptionPane.showMessageDialog(this, "All questions answered. Click Submit to finish.");
            }
        }
    }

    private void saveCurrentAnswer() {
        if (currentIndex >= questions.size()) return;
        Question q = questions.get(currentIndex);
        if (q.getQuestionType() == QuestionType.MCQ && answerGroup.getSelection() != null) {
            answers.put(q.getQuestionId(), answerGroup.getSelection().getActionCommand());
        }
    }

    private void submitQuiz() {
        saveCurrentAnswer();
        int confirm = JOptionPane.showConfirmDialog(this, "Submit quiz with " + answers.size() + "/" + questions.size() + " answers?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int timeSpent = (int) ((System.currentTimeMillis() - startTime) / 1000);
        String subject = (String) subjectCombo.getSelectedItem();
        String type = (String) typeCombo.getSelectedItem();

        try {
            StudentAttempt attempt = studentService.submitQuiz(
                user.getUserId(), type != null ? type : "Random",
                subject, questions, answers, timeSpent);

            JOptionPane.showMessageDialog(this,
                "Quiz submitted!\n" +
                "Score: " + attempt.getMarksObtained() + "/" + attempt.getTotalMarks() +
                " (" + String.format("%.1f", attempt.getPercentage()) + "%)\n" +
                "Correct: " + attempt.getCorrectAnswers() + "/" + attempt.getTotalQuestions(),
                "Quiz Results", JOptionPane.INFORMATION_MESSAGE);

            currentIndex = 0;
            answers.clear();
            nextBtn.setEnabled(false);
            submitBtn.setEnabled(false);
            optionsPanel.removeAll();
            questionLabel.setText("Practice session completed. Start a new one!");
            optionsPanel.revalidate();
            optionsPanel.repaint();
        } catch (ExamForgeException e) {
            JOptionPane.showMessageDialog(this, "Error submitting: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
