package com.examforge.ui.panel;

import com.examforge.exception.ExamForgeException;
import com.examforge.model.ExposureLevel;
import com.examforge.model.Question;
import com.examforge.model.QuestionExposure;
import com.examforge.dao.ExposureDAO;
import com.examforge.dao.impl.ExposureDAOImpl;
import com.examforge.service.QuestionService;
import com.examforge.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExposureAnalysisPanel extends JPanel {
    private final com.examforge.model.User user;
    private final QuestionService questionService;
    private final ExposureDAO exposureDAO;
    private JTable exposureTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JComboBox<String> subjectFilter;
    private JComboBox<String> levelFilter;
    private Runnable backAction;

    public ExposureAnalysisPanel(com.examforge.model.User user) {
        this.user = user;
        this.questionService = new QuestionService();
        this.exposureDAO = new ExposureDAOImpl();
        setLayout(new BorderLayout());
        setBackground(Theme.SURFACE);
        initUI();
        loadExposureData();
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
        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);

        JButton refreshBtn = Theme.softButton("Refresh");
        refreshBtn.addActionListener(e -> loadExposureData());

        JPanel refreshWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        refreshWrap.setOpaque(false);
        refreshWrap.add(refreshBtn);
        north.add(Theme.header("Question Exposure Analysis", createBackButton(), refreshWrap), BorderLayout.NORTH);

        subjectFilter = new JComboBox<>();
        subjectFilter.addItem("All");
        try {
            for (String s : questionService.findDistinctSubjects()) {
                subjectFilter.addItem(s);
            }
        } catch (ExamForgeException ignored) {}
        subjectFilter.addActionListener(e -> loadExposureData());

        levelFilter = new JComboBox<>(new String[]{"All", "LOW", "MEDIUM", "HIGH"});
        levelFilter.addActionListener(e -> loadExposureData());
        Theme.styleFields(subjectFilter, levelFilter);

        north.add(Theme.filterBar(
            Theme.label("Subject:", 12, Font.BOLD, Theme.MUTED), subjectFilter,
            Theme.label("Exposure Level:", 12, Font.BOLD, Theme.MUTED), levelFilter), BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new String[]{"Q ID", "Subject", "Unit", "Topic", "Difficulty", "Marks",
                         "Total Usage", "Recent Usage", "Exposure Level", "Last Used", "Exposure Score"},
            0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        exposureTable = new JTable(tableModel);
        Theme.styleTable(exposureTable, Theme.ACCENT_TINT);
        exposureTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        exposureTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        exposureTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        add(new JScrollPane(exposureTable), BorderLayout.CENTER);

        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(new EmptyBorder(8, 16, 8, 16));
        JLabel legend = Theme.label("LOW = 0-1 uses   |   MEDIUM = 2-3 uses   |   HIGH = 4+ uses", 12, Font.PLAIN, Theme.MUTED);
        JLabel penalty = Theme.label("Penalty: Last exam=100, 2nd=70, 3rd=40, Older=10, Never=0", 12, Font.PLAIN, Theme.MUTED);
        JLabel lowChip = Theme.label("\u25CF LOW", 12, Font.BOLD, Theme.SUCCESS);
        JLabel medChip = Theme.label("\u25CF MEDIUM", 12, Font.BOLD, Theme.WARNING);
        JLabel highChip = Theme.label("\u25CF HIGH", 12, Font.BOLD, Theme.DANGER);
        summaryPanel.add(lowChip);
        summaryPanel.add(Box.createHorizontalStrut(12));
        summaryPanel.add(medChip);
        summaryPanel.add(Box.createHorizontalStrut(12));
        summaryPanel.add(highChip);
        summaryPanel.add(Box.createHorizontalStrut(24));
        summaryPanel.add(legend);
        summaryPanel.add(Box.createHorizontalStrut(16));
        summaryPanel.add(penalty);
        add(summaryPanel, BorderLayout.SOUTH);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(Theme.font(12, Font.PLAIN));
        statusLabel.setForeground(Theme.MUTED);
        statusLabel.setBorder(new EmptyBorder(8, 16, 8, 16));
    }

    private void loadExposureData() {
        try {
            String selectedSubject = subjectFilter.getSelectedItem() != null ? subjectFilter.getSelectedItem().toString() : "All";
            String selectedLevel = levelFilter.getSelectedItem() != null ? levelFilter.getSelectedItem().toString() : "All";

            List<Question> questions;
            if (!selectedSubject.equals("All")) {
                questions = questionService.findActiveBySubject(selectedSubject);
            } else {
                questions = questionService.findAll();
            }

            tableModel.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (Question q : questions) {
                QuestionExposure exp = exposureDAO.findByQuestionId(q.getQuestionId());
                if (exp == null) {
                    exp = new QuestionExposure(q.getQuestionId());
                }

                ExposureLevel level = exp.getExposureLevel();
                if (!selectedLevel.equals("All") && level != ExposureLevel.valueOf(selectedLevel)) {
                    continue;
                }

                String lastUsed = exp.getLastOfficialUse() != null ? exp.getLastOfficialUse().format(fmt) : "Never";

                tableModel.addRow(new Object[]{
                    q.getQuestionId(), q.getSubject(), q.getUnit(), q.getTopic(),
                    q.getDifficulty().getDisplayName(), q.getMarks(),
                    exp.getTotalOfficialUsage(), exp.getRecentUsageCount(),
                    level.getDisplayName(), lastUsed,
                    String.format("%.1f", exp.getExposureScore())
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
