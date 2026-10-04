package com.examforge.ui.panel;

import com.examforge.exception.ExamForgeException;
import com.examforge.model.User;
import com.examforge.service.QuestionService;
import com.examforge.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StudentPerformancePanel extends JPanel {
    private final User user;
    private final StudentService studentService;
    private final QuestionService questionService;
    private JPanel reportPanel;
    private Runnable backAction;

    private static final Color GREEN = new Color(40, 120, 80);
    private static final Color CARD_BG = new Color(252, 254, 252);
    private static final Color BORDER = new Color(222, 232, 224);
    private static final Color MUTED = new Color(90, 110, 90);
    private static final Color TEXT_HEAD = new Color(60, 80, 60);

    public StudentPerformancePanel(User user) {
        this.user = user;
        this.studentService = new StudentService();
        this.questionService = new QuestionService();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initUI();
        loadPerformance();
    }

    public void setBackAction(Runnable backAction) {
        this.backAction = backAction;
    }

    private JButton createBackButton() {
        JButton backBtn = new JButton("Back");
        Color backBase = new Color(40, 120, 80);
        backBtn.setBackground(backBase);
        backBtn.setForeground(Color.WHITE);
        backBtn.setContentAreaFilled(false);
        backBtn.setOpaque(true);
        backBtn.setFocusPainted(false);
        backBtn.addChangeListener(e -> {
            ButtonModel m = backBtn.getModel();
            backBtn.setBackground(m.isPressed() ? backBase.darker()
                                : m.isRollover() ? backBase.brighter() : backBase);
        });
        backBtn.addActionListener(e -> {
            if (backAction != null) backAction.run();
        });
        return backBtn;
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(new Color(248, 252, 248));
        JLabel title = new JLabel("Performance Analysis");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(40, 120, 80));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(createBackButton());
        leftPanel.add(title);
        topPanel.add(leftPanel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadPerformance());
        topPanel.add(refreshBtn, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        reportPanel = new JPanel();
        reportPanel.setBackground(Color.WHITE);
        reportPanel.setLayout(new BoxLayout(reportPanel, BoxLayout.Y_AXIS));
        reportPanel.setBorder(new EmptyBorder(18, 24, 24, 24));

        JScrollPane scroll = new JScrollPane(reportPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadPerformance() {
        reportPanel.removeAll();
        try {
            int totalAttempts = studentService.getTotalAttempts(user.getUserId());
            double avgScore = studentService.getAverageScore(user.getUserId());

            String rating;
            if (avgScore >= 80) rating = "EXCELLENT";
            else if (avgScore >= 60) rating = "GOOD";
            else if (avgScore >= 40) rating = "AVERAGE";
            else rating = "NEEDS IMPROVEMENT";

            reportPanel.add(headerBlock());
            reportPanel.add(Box.createVerticalStrut(18));
            reportPanel.add(statsRow(totalAttempts, avgScore));
            reportPanel.add(Box.createVerticalStrut(18));

            if (totalAttempts == 0) {
                reportPanel.add(emptyState());
            } else {
                addSection("Performance Rating", ratingCard("Performance Rating", rating));
                addSection("Subject-wise Performance", subjectSection());
                addSection("Recommendations", recommendationsCard(buildRecommendations(avgScore)));
            }
        } catch (ExamForgeException e) {
            reportPanel.add(errorCard("Error loading performance: " + e.getMessage()));
        }
        reportPanel.revalidate();
        reportPanel.repaint();
        reportPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JComponent headerBlock() {
        JPanel h = new JPanel();
        h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
        h.setBackground(Color.WHITE);
        JLabel big = new JLabel("Performance Analysis");
        big.setFont(new Font("Segoe UI", Font.BOLD, 22));
        big.setForeground(new Color(40, 120, 80));
        JLabel sub = new JLabel("Your Practice Performance");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(MUTED);
        h.add(big);
        h.add(Box.createVerticalStrut(3));
        h.add(sub);
        return h;
    }

    private JComponent statsRow(int totalAttempts, double avgScore) {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setBackground(Color.WHITE);
        row.add(statCard("Total Practice Attempts", String.valueOf(totalAttempts)));
        row.add(statCard("Average Score", String.format("%.1f%%", avgScore)));
        return row;
    }

    private JPanel statCard(String caption, String value) {
        JPanel c = card();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        JLabel cap = new JLabel(caption);
        cap.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cap.setForeground(MUTED);
        cap.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 30));
        val.setForeground(new Color(40, 120, 80));
        val.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.add(Box.createVerticalStrut(2));
        c.add(cap);
        c.add(Box.createVerticalStrut(6));
        c.add(val);
        return c;
    }

    private JPanel ratingCard(String caption, String rating) {
        JPanel c = card();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        JLabel cap = new JLabel(caption);
        cap.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cap.setForeground(MUTED);
        cap.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel val = new JLabel(rating);
        val.setFont(new Font("Segoe UI", Font.BOLD, 22));
        val.setForeground(ratingColor(rating));
        val.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.add(Box.createVerticalStrut(2));
        c.add(cap);
        c.add(Box.createVerticalStrut(8));
        c.add(val);
        return c;
    }

    private Color ratingColor(String rating) {
        if ("EXCELLENT".equals(rating)) return new Color(40, 120, 80);
        if ("GOOD".equals(rating)) return new Color(90, 150, 90);
        if ("AVERAGE".equals(rating)) return new Color(200, 150, 40);
        return new Color(205, 90, 70);
    }

    private JComponent subjectSection() throws ExamForgeException {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        List<String> subjects = questionService.findDistinctSubjects();
        for (String subject : subjects) {
            double subjectAvg = studentService.getAverageScore(user.getUserId());
            wrap.add(subjectRow(subject, subjectAvg));
            wrap.add(Box.createVerticalStrut(10));
        }
        return wrap;
    }

    private JPanel subjectRow(String subject, double pct) {
        JPanel row = card();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        JPanel line = new JPanel(new BorderLayout());
        line.setOpaque(false);
        JLabel name = new JLabel(subject);
        name.setFont(new Font("Segoe UI", Font.BOLD, 14));
        name.setForeground(TEXT_HEAD);
        JLabel score = new JLabel(String.format("%.1f%%", pct));
        score.setFont(new Font("Segoe UI", Font.BOLD, 13));
        score.setForeground(new Color(40, 120, 80));
        line.add(name, BorderLayout.WEST);
        line.add(score, BorderLayout.EAST);
        row.add(line);
        row.add(Box.createVerticalStrut(8));
        row.add(new ProgressBar(pct));
        return row;
    }

    private JComponent recommendationsCard(List<String> recommendations) {
        JPanel c = card();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        for (String recommendation : recommendations) {
            JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 1));
            line.setOpaque(false);
            JLabel dot = new JLabel("\u2022");
            dot.setFont(new Font("Segoe UI", Font.BOLD, 14));
            dot.setForeground(new Color(40, 120, 80));
            JLabel text = new JLabel(recommendation);
            text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            text.setForeground(TEXT_HEAD);
            line.add(dot);
            line.add(text);
            c.add(line);
            c.add(Box.createVerticalStrut(4));
        }
        return c;
    }

    private JPanel emptyState() {
        JPanel c = card();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        JLabel msg = new JLabel("No practice data available yet.");
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(MUTED);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.add(Box.createVerticalStrut(6));
        c.add(msg);
        c.add(Box.createVerticalStrut(6));
        return c;
    }

    private JPanel errorCard(String message) {
        JPanel c = card();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        JLabel msg = new JLabel(message);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        msg.setForeground(new Color(205, 90, 70));
        msg.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(msg);
        return c;
    }

    private void addSection(String title, JComponent body) {
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 15));
        t.setForeground(new Color(40, 120, 80));
        reportPanel.add(t);
        reportPanel.add(Box.createVerticalStrut(8));
        reportPanel.add(body);
        reportPanel.add(Box.createVerticalStrut(18));
    }

    private List<String> buildRecommendations(double avgScore) {
        List<String> list = new ArrayList<>();
        if (avgScore < 50) {
            list.add("Focus on basics. Start with Random Practice.");
            list.add("Review incorrect answers carefully.");
        } else if (avgScore < 70) {
            list.add("Good progress! Try Balanced Practice.");
            list.add("Focus on weak areas for improvement.");
        } else {
            list.add("Excellent performance!");
            list.add("Challenge yourself with Weak-Area Practice.");
        }
        return list;
    }

    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1),
            new EmptyBorder(14, 16, 14, 16)));
        return p;
    }

    private static class ProgressBar extends JPanel {
        final double fraction;

        ProgressBar(double percentage) {
            this.fraction = Math.min(1.0, Math.max(0.0, percentage / 100.0));
            setOpaque(false);
            setPreferredSize(new Dimension(240, 18));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int h = getHeight();
            g2.setColor(new Color(230, 238, 232));
            g2.fillRoundRect(0, 0, getWidth(), h, h, h);
            g2.setColor(new Color(40, 120, 80));
            int w = (int) Math.round(getWidth() * fraction);
            if (w > 0) g2.fillRoundRect(0, 0, w, h, h, h);
            g2.dispose();
        }
    }
}