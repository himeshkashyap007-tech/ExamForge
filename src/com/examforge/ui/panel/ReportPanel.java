package com.examforge.ui.panel;

import com.examforge.exception.ExamForgeException;
import com.examforge.model.*;
import com.examforge.service.BlueprintService;
import com.examforge.service.PaperService;
import com.examforge.service.QuestionService;
import com.examforge.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportPanel extends JPanel {
    private final User user;
    private final PaperService paperService;
    private final BlueprintService blueprintService;
    private final QuestionService questionService;
    private JTabbedPane tabbedPane;
    private Runnable backAction;

    public ReportPanel(User user) {
        this.user = user;
        this.paperService = new PaperService();
        this.blueprintService = new BlueprintService();
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
        JPanel refreshWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        refreshWrap.setOpaque(false);
        JButton refreshBtn = Theme.softButton("Refresh All");
        refreshBtn.addActionListener(e -> refreshAll());
        refreshWrap.add(refreshBtn);

        add(Theme.header("Reports & History", createBackButton(), refreshWrap), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Theme.SURFACE);
        tabbedPane.setForeground(Theme.TEXT);
        tabbedPane.setFont(Theme.font(13, Font.BOLD));
        tabbedPane.addTab("Examination History", createExamHistoryPanel());
        tabbedPane.addTab("Question Statistics", createQuestionStatsPanel());
        tabbedPane.addTab("Blueprint Summary", createBlueprintSummaryPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createExamHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.SURFACE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Exam ID", "Paper ID", "Exam Name", "Subject", "Total Marks", "Finalized At", "Finalized By"},
            0
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(model);
        Theme.styleTable(table, Theme.BRAND_TINT);

        try {
            List<ExaminationRecord> records = paperService.getExaminationHistory();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (ExaminationRecord r : records) {
                model.addRow(new Object[]{
                    r.getExamId(), r.getPaperId(), r.getExamName(), r.getSubject(),
                    r.getTotalMarks(),
                    r.getFinalizedAt() != null ? r.getFinalizedAt().format(fmt) : "N/A",
                    r.getFinalizedBy()
                });
            }
        } catch (ExamForgeException e) {
            panel.add(new JLabel("Error: " + e.getMessage()));
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createQuestionStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.SURFACE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        try {
            List<String> subjects = questionService.findDistinctSubjects();
            int totalQuestions = questionService.findAll().size();
            int activeQuestions = 0;
            for (String s : subjects) {
                activeQuestions += questionService.countActiveBySubject(s);
            }

            statsPanel.add(createStatCard("Total Questions", String.valueOf(totalQuestions)));
            statsPanel.add(createStatCard("Active Questions", String.valueOf(activeQuestions)));
            statsPanel.add(createStatCard("Subjects", String.valueOf(subjects.size())));

            List<Blueprint> blueprints = blueprintService.findAll();
            long draftCount = blueprints.stream().filter(b -> b.getStatus() == BlueprintStatus.DRAFT).count();
            long validCount = blueprints.stream().filter(b -> b.getStatus() == BlueprintStatus.VALID).count();
            long invalidCount = blueprints.stream().filter(b -> b.getStatus() == BlueprintStatus.INVALID).count();

            statsPanel.add(createStatCard("Draft Blueprints", String.valueOf(draftCount)));
            statsPanel.add(createStatCard("Valid Blueprints", String.valueOf(validCount)));
            statsPanel.add(createStatCard("Invalid Blueprints", String.valueOf(invalidCount)));
        } catch (ExamForgeException e) {
            statsPanel.add(new JLabel("Error: " + e.getMessage()));
        }

        panel.add(statsPanel, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createBlueprintSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.SURFACE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Exam Name", "Subject", "Total Marks", "Questions", "Status", "Sections", "Constraints"},
            0
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(model);
        Theme.styleTable(table, Theme.BRAND_TINT);

        try {
            List<Blueprint> blueprints = blueprintService.findAll();
            for (Blueprint bp : blueprints) {
                model.addRow(new Object[]{
                    bp.getBlueprintId(), bp.getExamName(), bp.getSubject(),
                    bp.getTotalMarks(), bp.getNumberOfQuestions(),
                    bp.getStatus().getDisplayName(),
                    bp.getSections().size(), bp.getConstraints().size()
                });
            }
        } catch (ExamForgeException e) {
            panel.add(new JLabel("Error: " + e.getMessage()));
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatCard(String label, String value) {
        return Theme.statCard(Theme.BRAND, value, label);
    }

    private void refreshAll() {
        removeAll();
        initUI();
        revalidate();
        repaint();
    }
}
