package com.examforge.ui.panel;

import com.examforge.engine.FeasibilityResult;
import com.examforge.exception.ExamForgeException;
import com.examforge.model.*;
import com.examforge.service.BlueprintService;
import com.examforge.service.PaperService;
import com.examforge.validator.ValidationCheckResult;
import com.examforge.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PaperGenerationPanel extends JPanel {
    private final User user;
    private final BlueprintService blueprintService;
    private final PaperService paperService;
    private JComboBox<String> blueprintCombo;
    private JTable paperTable;
    private DefaultTableModel paperTableModel;
    private JTextArea feasibilityArea;
    private JTextArea validationArea;
    private JLabel statusLabel;
    private Paper currentPaper;
    private Blueprint currentBlueprint;
    private Runnable backAction;

    public PaperGenerationPanel(User user) {
        this.user = user;
        this.blueprintService = new BlueprintService();
        this.paperService = new PaperService();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
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
        add(Theme.header("Paper Generation", createBackButton(), null), BorderLayout.NORTH);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplit.setDividerLocation(200);

        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(Theme.SURFACE);
        controlPanel.setBorder(new EmptyBorder(16, 16, 10, 16));

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        selectionPanel.setOpaque(false);
        selectionPanel.add(Theme.label("Select Blueprint:", 12, Font.BOLD, Theme.MUTED));
        blueprintCombo = new JComboBox<>();
        blueprintCombo.setPreferredSize(new Dimension(320, 32));
        Theme.styleField(blueprintCombo);
        loadBlueprints();
        selectionPanel.add(blueprintCombo);

        JButton checkFeasibilityBtn = Theme.button("Check Feasibility");
        checkFeasibilityBtn.addActionListener(e -> checkFeasibility());
        selectionPanel.add(checkFeasibilityBtn);

        JButton generateBtn = Theme.button("Generate Paper", Theme.SUCCESS);
        generateBtn.addActionListener(e -> generatePaper());
        selectionPanel.add(generateBtn);

        JButton validateBtn = Theme.button("Validate Paper", Theme.WARNING);
        validateBtn.addActionListener(e -> validatePaper());
        selectionPanel.add(validateBtn);

        JButton finalizeBtn = Theme.button("Finalize Paper", Theme.DANGER);
        finalizeBtn.addActionListener(e -> finalizePaper());
        selectionPanel.add(finalizeBtn);

        controlPanel.add(selectionPanel, BorderLayout.NORTH);

        feasibilityArea = new JTextArea(5, 50);
        feasibilityArea.setEditable(false);
        feasibilityArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        feasibilityArea.setForeground(Theme.TEXT);
        validationArea = new JTextArea(5, 50);
        validationArea.setEditable(false);
        validationArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        validationArea.setForeground(Theme.TEXT);

        JSplitPane textSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            new JScrollPane(feasibilityArea), new JScrollPane(validationArea));
        textSplit.setDividerLocation(350);
        textSplit.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Theme.BORDER), "Feasibility | Validation",
            javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
            Theme.font(12, Font.BOLD), Theme.BRAND_DARK));

        controlPanel.add(textSplit, BorderLayout.CENTER);
        mainSplit.setTopComponent(controlPanel);

        paperTableModel = new DefaultTableModel(
            new String[]{"Q#", "Section", "Subject", "Unit", "Topic", "Marks", "Difficulty", "Type", "Question Text"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        paperTable = new JTable(paperTableModel);
        Theme.styleTable(paperTable);
        JPanel paperPanel = new JPanel(new BorderLayout());
        paperPanel.setBackground(Theme.SURFACE);
        paperPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Theme.BORDER), "Generated Paper",
            javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
            Theme.font(12, Font.BOLD), Theme.BRAND_DARK));
        paperPanel.add(new JScrollPane(paperTable), BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(Theme.font(12, Font.PLAIN));
        statusLabel.setForeground(Theme.MUTED);
        statusLabel.setBorder(new EmptyBorder(6, 16, 8, 16));
        paperPanel.add(statusLabel, BorderLayout.SOUTH);

        mainSplit.setBottomComponent(paperPanel);
        add(mainSplit, BorderLayout.CENTER);
    }

    private void loadBlueprints() {
        try {
            List<Blueprint> blueprints = blueprintService.findAll();
            blueprintCombo.removeAllItems();
            for (Blueprint bp : blueprints) {
                blueprintCombo.addItem(bp.getBlueprintId() + " - " + bp.getExamName() + " (" + bp.getSubject() + ")");
            }
        } catch (ExamForgeException e) {
            statusLabel.setText("Error loading blueprints: " + e.getMessage());
        }
    }

    private Blueprint getSelectedBlueprint() {
        try {
            String selected = (String) blueprintCombo.getSelectedItem();
            if (selected == null) return null;
            int id = Integer.parseInt(selected.split(" - ")[0].trim());
            return blueprintService.findById(id);
        } catch (Exception e) {
            return null;
        }
    }

    private void checkFeasibility() {
        Blueprint bp = getSelectedBlueprint();
        if (bp == null) {
            feasibilityArea.setText("Please select a blueprint");
            return;
        }

        feasibilityArea.setText("Checking feasibility...\n");
        SwingWorker<FeasibilityResult, Void> worker = new SwingWorker<>() {
            @Override
            protected FeasibilityResult doInBackground() throws Exception {
                return paperService.analyzeFeasibility(bp);
            }

            @Override
            protected void done() {
                try {
                    FeasibilityResult result = get();
                    feasibilityArea.setText("Feasibility Analysis Result\n");
                    feasibilityArea.append("========================\n");
                    feasibilityArea.append("Status: " + result.getStatus().getDisplayName() + "\n");
                    feasibilityArea.append("Message: " + result.getMessage() + "\n\n");
                    feasibilityArea.append("Blueprint: " + bp.getExamName() + "\n");
                    feasibilityArea.append("Subject: " + bp.getSubject() + "\n");
                    feasibilityArea.append("Total Marks: " + bp.getTotalMarks() + "\n");
                    feasibilityArea.append("Questions: " + bp.getNumberOfQuestions() + "\n");
                    feasibilityArea.append("Sections: " + bp.getSections().size() + "\n");
                    feasibilityArea.append("Constraints: " + bp.getConstraints().size() + "\n");
                } catch (Exception e) {
                    feasibilityArea.setText("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void generatePaper() {
        Blueprint bp = getSelectedBlueprint();
        if (bp == null) {
            statusLabel.setText("Please select a blueprint");
            return;
        }

        statusLabel.setText("Generating paper...");
        SwingWorker<Paper, Void> worker = new SwingWorker<>() {
            @Override
            protected Paper doInBackground() throws Exception {
                return paperService.generatePaper(bp, user.getUserId());
            }

            @Override
            protected void done() {
                try {
                    currentPaper = get();
                    currentBlueprint = bp;
                    statusLabel.setText("Paper generated successfully! Paper ID: " + currentPaper.getPaperId());
                    displayPaper(currentPaper);
                    feasibilityArea.append("\n\nPaper Generated Successfully!");
                } catch (Exception e) {
                    statusLabel.setText("Generation failed: " + e.getMessage());
                    feasibilityArea.append("\n\nGeneration Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void validatePaper() {
        if (currentPaper == null || currentBlueprint == null) {
            statusLabel.setText("Generate a paper first");
            return;
        }

        List<ValidationCheckResult> results = paperService.validatePaper(currentPaper, currentBlueprint);
        StringBuilder sb = new StringBuilder("Paper Validation Results\n");
        sb.append("========================\n");
        int passed = 0, failed = 0;
        for (ValidationCheckResult r : results) {
            sb.append(r.getStatus().getDisplayName()).append(" | ");
            sb.append(r.getConstraintName()).append(" | ");
            sb.append("Expected: ").append(r.getExpectedValue()).append(" | ");
            sb.append("Actual: ").append(r.getActualValue()).append("\n");
            if (r.getStatus() == com.examforge.model.ValidationCheckStatus.PASS) passed++;
            else failed++;
        }
        sb.append("\n").append(passed).append(" passed, ").append(failed).append(" failed\n");
        validationArea.setText(sb.toString());
        statusLabel.setText("Validation complete: " + passed + " passed, " + failed + " failed");
    }

    private void finalizePaper() {
        if (currentPaper == null) {
            statusLabel.setText("No paper to finalize");
            return;
        }
        if (currentPaper.getStatus() != PaperStatus.VALIDATED) {
            statusLabel.setText("Paper must be validated before finalizing");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to finalize this paper? This action cannot be undone.",
            "Confirm Finalization", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = paperService.finalizePaper(currentPaper, user.getUserId());
                if (success) {
                    statusLabel.setText("Paper finalized successfully!");
                    validationArea.append("\n\nPaper FINALIZED. Official examination record created.");
                }
            } catch (ExamForgeException e) {
                statusLabel.setText("Finalization failed: " + e.getMessage());
            }
        }
    }

    private void displayPaper(Paper paper) {
        paperTableModel.setRowCount(0);
        int qNum = 1;
        for (PaperQuestion pq : paper.getQuestions()) {
            Question q = pq.getQuestion();
            if (q != null) {
                paperTableModel.addRow(new Object[]{
                    qNum++,
                    "Section " + (pq.getSectionIndex() + 1),
                    q.getSubject(), q.getUnit(), q.getTopic(),
                    q.getMarks(), q.getDifficulty().getDisplayName(),
                    q.getQuestionType().getDisplayName(),
                    q.getQuestionText()
                });
            }
        }
    }
}
