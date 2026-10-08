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
    private ButtonGroup answerGroup;
    private JPanel optionsPanel;
    private JPanel questionNavPanel;
    private JPanel examHeaderPanel;
    private JPanel setupPanel;
    private JPanel examPanel;
    private CardLayout examCardLayout;
    private JLabel questionLabel;
    private JLabel timerLabel;
    private JLabel examNameLabel;
    private JButton prevBtn;
    private JButton nextBtn;
    private JButton submitBtn;
    private List<Question> questions;
    private int currentIndex;
    private int currentAttemptId;
    private Map<Integer, String> answers;
    private long startTime;
    private Runnable backAction;
    private Runnable exitExamModeAction;
    private Runnable enterExamModeAction;
    private boolean examMode;
    private javax.swing.Timer countdownTimer;
    private int timeRemaining;
    private String currentSubject;
    private String currentType;
    private final List<JButton> navButtons = new ArrayList<>();

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

    public void setExitExamModeAction(Runnable exitExamModeAction) {
        this.exitExamModeAction = exitExamModeAction;
    }

    public void setEnterExamModeAction(Runnable enterExamModeAction) {
        this.enterExamModeAction = enterExamModeAction;
    }

    private void initUI() {
        examCardLayout = new CardLayout();
        setLayout(examCardLayout);

        setupPanel = createSetupPanel();
        examPanel = createExamPanel();

        add(setupPanel, "SETUP");
        add(examPanel, "EXAM");

        examCardLayout.show(this, "SETUP");
    }

    private JPanel createSetupPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.SURFACE);

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

        JButton startBtn = Theme.button("Start Exam", Theme.SUCCESS);
        startBtn.setMargin(new Insets(12, 28, 12, 28));
        startBtn.setFont(Theme.font(14, Font.BOLD));
        startBtn.addActionListener(e -> startPractice());
        topPanel.add(startBtn);

        panel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Theme.SURFACE);
        centerPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(Theme.label("Practice Setup", 22, Font.BOLD, Theme.TEXT));
        infoPanel.add(Box.createVerticalStrut(12));
        infoPanel.add(Theme.label("Select a subject, practice type, and number of questions to begin.", 14, Font.PLAIN, Theme.MUTED));
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(Theme.label("Each practice session generates a unique set of randomized questions.", 14, Font.PLAIN, Theme.MUTED));

        centerPanel.add(infoPanel);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createExamPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.SURFACE);

        examHeaderPanel = new JPanel(new BorderLayout());
        examHeaderPanel.setBackground(Theme.ACCENT_DARK);
        examHeaderPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        examNameLabel = new JLabel("Practice Exam");
        examNameLabel.setFont(Theme.font(14, Font.BOLD));
        examNameLabel.setForeground(Color.WHITE);
        examHeaderPanel.add(examNameLabel, BorderLayout.WEST);

        questionLabel = new JLabel("Question 1 of 10");
        questionLabel.setFont(Theme.font(14, Font.BOLD));
        questionLabel.setForeground(Color.WHITE);
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        examHeaderPanel.add(questionLabel, BorderLayout.CENTER);

        timerLabel = new JLabel("Time: 00:00");
        timerLabel.setFont(Theme.font(14, Font.BOLD));
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        examHeaderPanel.add(timerLabel, BorderLayout.EAST);

        panel.add(examHeaderPanel, BorderLayout.NORTH);

        optionsPanel = new JPanel();
        optionsPanel.setBackground(Theme.SURFACE);
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JScrollPane scrollPane = new JScrollPane(optionsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        questionNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        questionNavPanel.setBackground(Theme.BG);
        questionNavPanel.setBorder(new EmptyBorder(6, 10, 0, 10));
        questionNavPanel.setVisible(false);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        navPanel.setBackground(Theme.BG);
        navPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        prevBtn = Theme.button("\u2190 Previous", Theme.BRAND);
        prevBtn.setMargin(new Insets(12, 24, 12, 24));
        prevBtn.setFont(Theme.font(14, Font.BOLD));
        prevBtn.setEnabled(false);
        prevBtn.addActionListener(e -> showPreviousQuestion());
        navPanel.add(prevBtn);

        nextBtn = Theme.button("Next \u2192", Theme.BRAND);
        nextBtn.setMargin(new Insets(12, 24, 12, 24));
        nextBtn.setFont(Theme.font(14, Font.BOLD));
        nextBtn.setEnabled(false);
        nextBtn.addActionListener(e -> showNextQuestion());
        navPanel.add(nextBtn);

        submitBtn = Theme.button("Submit Exam", Theme.WARNING);
        submitBtn.setMargin(new Insets(12, 24, 12, 24));
        submitBtn.setFont(Theme.font(14, Font.BOLD));
        submitBtn.setEnabled(false);
        submitBtn.setVisible(false);
        submitBtn.addActionListener(e -> submitQuiz());
        navPanel.add(submitBtn);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(Theme.BG);
        bottomPanel.add(questionNavPanel);
        bottomPanel.add(navPanel);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        answers = new HashMap<>();
        currentAttemptId = 0;

        return panel;
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
            StudentService.Session session = studentService.startSession(user.getUserId(), type, subject, numQuestions);
            if (session.isResumed()) {
                int choice = JOptionPane.showConfirmDialog(this,
                    "You have an unfinished " + type + " session for '" + subject + "'.\nContinue where you left off?",
                    "Resume Session", JOptionPane.YES_NO_OPTION);
                if (choice != javax.swing.JOptionPane.YES_OPTION) {
                    studentService.abandonSession(session.getAttempt().getAttemptId());
                    session = studentService.startSession(user.getUserId(), type, subject, numQuestions);
                }
            }
            questions = session.getQuestions();
            currentAttemptId = session.getAttempt().getAttemptId();
            currentSubject = subject;
            currentType = type;

            if (questions.isEmpty()) {
                currentAttemptId = 0;
                questions = new ArrayList<>();
                return;
            }

            currentIndex = 0;
            answers.clear();
            startTime = System.currentTimeMillis();

            timeRemaining = Math.max(60, questions.size() * 120);
            startTimer();

            examMode = true;
            examNameLabel.setText(type + " Practice \u2014 " + subject);
            buildQuestionNav();
            showQuestion(0);
            updateNavButtons();

            examCardLayout.show(this, "EXAM");
            if (enterExamModeAction != null) enterExamModeAction.run();
        } catch (ExamForgeException e) {
            currentAttemptId = 0;
            questions = new ArrayList<>();
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                optionsPanel.removeAll();
                optionsPanel.add(Theme.label(e.getMessage(), 12, Font.BOLD, Theme.DANGER));
                optionsPanel.revalidate();
                optionsPanel.repaint();
            }
        }
    }

    private void startTimer() {
        if (countdownTimer != null) countdownTimer.stop();
        if (timeRemaining <= 0) timeRemaining = 1;
        countdownTimer = new javax.swing.Timer(1000, e -> {
            timeRemaining--;
            if (timeRemaining <= 0) {
                timeRemaining = 0;
                countdownTimer.stop();
                submitQuiz();
                return;
            }
            updateTimerLabel();
        });
        countdownTimer.start();
        updateTimerLabel();
    }

    private void updateTimerLabel() {
        int mins = timeRemaining / 60;
        int secs = timeRemaining % 60;
        timerLabel.setText(String.format("Time: %02d:%02d", mins, secs));
    }

    private void buildQuestionNav() {
        questionNavPanel.removeAll();
        questionNavPanel.setVisible(true);
        navButtons.clear();
        for (int i = 0; i < questions.size(); i++) {
            final int idx = i;
            String label = String.valueOf(i + 1);
            if (label.length() == 1) label = " " + label + " ";
            else if (label.length() == 2) label = " " + label;
            else label = label + " ";
            JButton btn = new JButton(label);
            btn.setFont(Theme.font(12, Font.BOLD));
            btn.setMargin(new Insets(6, 10, 6, 10));
            btn.setPreferredSize(new Dimension(36, 30));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
            btn.setContentAreaFilled(true);
            btn.setOpaque(true);
            btn.setRolloverEnabled(true);
            btn.addChangeListener(e -> {
                ButtonModel m = btn.getModel();
                if (!btn.isEnabled()) {
                    btn.setBackground(new Color(226, 230, 238));
                    btn.setForeground(new Color(150, 158, 170));
                } else if (m.isPressed()) {
                    btn.setBackground(Theme.BRAND_DARK);
                    btn.setForeground(Color.WHITE);
                } else if (m.isRollover()) {
                    btn.setBackground(Theme.BRAND);
                    btn.setForeground(Color.WHITE);
                } else if (idx == currentIndex) {
                    btn.setBackground(Theme.BRAND);
                    btn.setForeground(Color.WHITE);
                } else if (isAnswered(idx)) {
                    btn.setBackground(new Color(220, 240, 230));
                    btn.setForeground(Theme.ACCENT_DARK);
                } else {
                    btn.setBackground(Theme.BRAND_TINT);
                    btn.setForeground(Theme.BRAND_DARK);
                }
                btn.repaint();
            });
            btn.addActionListener(e -> jumpToQuestion(idx));
            navButtons.add(btn);
            questionNavPanel.add(btn);
        }
        questionNavPanel.revalidate();
        questionNavPanel.repaint();
    }

    private boolean isAnswered(int idx) {
        if (idx < 0 || idx >= questions.size()) return false;
        return answers.containsKey(questions.get(idx).getQuestionId());
    }

    private void jumpToQuestion(int idx) {
        if (idx < 0 || idx >= questions.size()) return;
        saveCurrentAnswer();
        currentIndex = idx;
        showQuestion(idx);
        updateNavButtons();
    }

    private void showQuestion(int index) {
        if (index >= questions.size()) return;
        Question q = questions.get(index);
        questionLabel.setText("Question " + (index + 1) + " of " + questions.size());

        optionsPanel.removeAll();
        optionsPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel qPanel = new JPanel();
        qPanel.setLayout(new BoxLayout(qPanel, BoxLayout.Y_AXIS));
        qPanel.setOpaque(false);
        qPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        qPanel.add(Theme.label("Question " + (index + 1), 11, Font.BOLD, Theme.ACCENT));
        qPanel.add(Box.createVerticalStrut(6));

        JTextArea qText = new JTextArea(q.getQuestionText());
        qText.setFont(Theme.font(16, Font.BOLD));
        qText.setForeground(Theme.TEXT);
        qText.setLineWrap(true);
        qText.setWrapStyleWord(true);
        qText.setEditable(false);
        qText.setOpaque(false);
        qText.setBorder(null);
        qText.setAlignmentX(Component.LEFT_ALIGNMENT);
        qPanel.add(qText);
        qPanel.add(Box.createVerticalStrut(16));

        answerGroup = new ButtonGroup();
        String[] opts = {"A", "B", "C", "D"};
        String[] optTexts = {q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()};

        if (q.getQuestionType() == QuestionType.MCQ) {
            for (int i = 0; i < 4; i++) {
                if (optTexts[i] != null && !optTexts[i].isEmpty()) {
                    JRadioButton rb = new JRadioButton(opts[i] + ".  " + optTexts[i]);
                    rb.setFont(Theme.font(15, Font.PLAIN));
                    rb.setBackground(Theme.SURFACE);
                    rb.setActionCommand(opts[i]);
                    rb.setBorder(new EmptyBorder(8, 12, 8, 12));
                    if (answers.containsKey(q.getQuestionId())) {
                        rb.setSelected(answers.get(q.getQuestionId()).equals(opts[i]));
                    }
                    answerGroup.add(rb);
                    qPanel.add(rb);
                    qPanel.add(Box.createVerticalStrut(4));
                }
            }
        } else {
            JTextArea answerArea = new JTextArea(4, 50);
            answerArea.setLineWrap(true);
            answerArea.setWrapStyleWord(true);
            answerArea.setFont(Theme.font(14, Font.PLAIN));
            answerArea.setBorder(new EmptyBorder(10, 10, 10, 10));
            qPanel.add(Theme.label("Your Answer:", 13, Font.BOLD, Theme.MUTED));
            qPanel.add(Box.createVerticalStrut(6));
            qPanel.add(answerArea);
        }

        optionsPanel.add(qPanel);
        optionsPanel.revalidate();
        optionsPanel.repaint();

        updateNavButtons();
    }

    private void updateNavButtons() {
        if (prevBtn != null) prevBtn.setEnabled(currentIndex > 0);
        if (nextBtn != null) {
            boolean isLast = currentIndex >= questions.size() - 1;
            nextBtn.setVisible(!isLast);
            nextBtn.setEnabled(!isLast);
        }
        if (submitBtn != null) {
            boolean isLast = currentIndex >= questions.size() - 1;
            submitBtn.setVisible(isLast);
            submitBtn.setEnabled(isLast);
        }
        for (int i = 0; i < navButtons.size(); i++) {
            navButtons.get(i).repaint();
        }
    }

    private void showPreviousQuestion() {
        if (currentIndex > 0) {
            saveCurrentAnswer();
            currentIndex--;
            showQuestion(currentIndex);
        }
    }

    private void showNextQuestion() {
        if (currentIndex < questions.size() - 1) {
            saveCurrentAnswer();
            currentIndex++;
            showQuestion(currentIndex);
        }
    }

    private void saveCurrentAnswer() {
        if (currentIndex >= questions.size()) return;
        Question q = questions.get(currentIndex);
        if (q.getQuestionType() == QuestionType.MCQ && answerGroup != null && answerGroup.getSelection() != null) {
            answers.put(q.getQuestionId(), answerGroup.getSelection().getActionCommand());
        }
    }

    private void submitQuiz() {
        if (countdownTimer != null) countdownTimer.stop();
        saveCurrentAnswer();
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
            "Submit exam with " + answers.size() + "/" + questions.size() + " answers?",
            "Confirm Submission", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            startTimer();
            return;
        }

        int timeSpent = (int) ((System.currentTimeMillis() - startTime) / 1000);
        String subject = currentSubject;
        String type = currentType;

        try {
            StudentAttempt attempt = studentService.submitSession(
                currentAttemptId, user.getUserId(), type != null ? type : "Random",
                subject, questions, answers, timeSpent);

            javax.swing.JOptionPane.showMessageDialog(this,
                "Exam submitted!\n" +
                "Score: " + attempt.getMarksObtained() + "/" + attempt.getTotalMarks() +
                " (" + String.format("%.1f", attempt.getPercentage()) + "%)\n" +
                "Correct: " + attempt.getCorrectAnswers() + "/" + attempt.getTotalQuestions(),
                "Exam Results", javax.swing.JOptionPane.INFORMATION_MESSAGE);

            exitExamMode();
        } catch (ExamForgeException e) {
            javax.swing.JOptionPane.showConfirmDialog(this, "Error submitting: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exitExamMode() {
        if (countdownTimer != null) countdownTimer.stop();
        examMode = false;
        currentIndex = 0;
        currentAttemptId = 0;
        answers.clear();
        questions = new ArrayList<>();
        examCardLayout.show(this, "SETUP");
        if (exitExamModeAction != null) exitExamModeAction.run();
    }

    private JButton createBackButton() {
        JButton backBtn = Theme.backButton();
        backBtn.addActionListener(e -> {
            if (backAction != null) backAction.run();
        });
        return backBtn;
    }

    public boolean isExamMode() {
        return examMode;
    }
}
