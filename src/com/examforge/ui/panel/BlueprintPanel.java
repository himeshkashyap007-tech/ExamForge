package com.examforge.ui.panel;

import com.examforge.exception.ExamForgeException;
import com.examforge.model.*;
import com.examforge.service.BlueprintService;
import com.examforge.service.QuestionService;
import com.examforge.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BlueprintPanel extends JPanel {
    private final User user;
    private final BlueprintService blueprintService;
    private final QuestionService questionService;
    private JTable blueprintTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JButton editBtn;
    private JButton deleteBtn;
    private JButton deactivateBtn;
    private JButton reactivateBtn;
    private JButton validateBtn;
    private final List<BlueprintStatus> blueprintStatuses = new ArrayList<>();

    private List<BlueprintSection> currentSections = new ArrayList<>();
    private List<BlueprintConstraint> currentConstraints = new ArrayList<>();
    private Runnable backAction;

    public BlueprintPanel(User user) {
        this.user = user;
        this.blueprintService = new BlueprintService();
        this.questionService = new QuestionService();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initUI();
        loadBlueprints();
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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        JButton createBtn = Theme.button("Create Blueprint", Theme.SUCCESS);
        createBtn.addActionListener(e -> showCreateBlueprintDialog());
        buttonPanel.add(createBtn);

        JButton validateBtn = Theme.button("Validate Selected");
        this.validateBtn = validateBtn;
        validateBtn.addActionListener(e -> validateSelected());
        buttonPanel.add(validateBtn);

        JButton editBtn = Theme.button("Edit");
        this.editBtn = editBtn;
        editBtn.addActionListener(e -> editSelected());
        buttonPanel.add(editBtn);

        JButton deleteBtn = Theme.button("Delete", Theme.DANGER);
        this.deleteBtn = deleteBtn;
        deleteBtn.addActionListener(e -> deleteSelected());
        buttonPanel.add(deleteBtn);

        JButton deactivateBtn = Theme.button("Deactivate", Theme.WARNING);
        this.deactivateBtn = deactivateBtn;
        deactivateBtn.addActionListener(e -> deactivateSelected());
        buttonPanel.add(deactivateBtn);

        JButton reactivateBtn = Theme.button("Reactivate", Theme.SUCCESS);
        this.reactivateBtn = reactivateBtn;
        reactivateBtn.addActionListener(e -> reactivateSelected());
        buttonPanel.add(reactivateBtn);

        JButton refreshBtn = Theme.softButton("Refresh");
        refreshBtn.addActionListener(e -> loadBlueprints());
        buttonPanel.add(refreshBtn);

        add(Theme.header("Blueprint Management", createBackButton(), buttonPanel), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new String[]{"ID", "Exam Name", "Subject", "Total Marks", "Questions", "Duration", "Status", "Sections"},
            0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        blueprintTable = new JTable(tableModel);
        blueprintTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Theme.styleTable(blueprintTable);
        blueprintTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) updateActionButtons();
            }
        });
        add(new JScrollPane(blueprintTable), BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(Theme.font(12, Font.PLAIN));
        statusLabel.setForeground(Theme.MUTED);
        statusLabel.setBorder(new EmptyBorder(8, 16, 8, 16));
        add(statusLabel, BorderLayout.SOUTH);

        validateBtn.setEnabled(false);
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        deactivateBtn.setEnabled(false);
        reactivateBtn.setEnabled(false);
    }

    private void loadBlueprints() {
        try {
            List<Blueprint> blueprints = blueprintService.findAll();
            tableModel.setRowCount(0);
            blueprintStatuses.clear();
            for (Blueprint bp : blueprints) {
                tableModel.addRow(new Object[]{
                    bp.getBlueprintId(), bp.getExamName(), bp.getSubject(),
                    bp.getTotalMarks(), bp.getNumberOfQuestions(),
                    bp.getDurationMinutes() + " min",
                    bp.getStatus().getDisplayName(),
                    blueprintService.getSectionCount(bp.getBlueprintId()) + " sections"
                });
                blueprintStatuses.add(bp.getStatus());
            }
            statusLabel.setText("Showing " + blueprints.size() + " blueprints");
            updateActionButtons();
        } catch (ExamForgeException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void validateSelected() {
        int row = blueprintTable.getSelectedRow();
        if (row < 0) {
            statusLabel.setText("Select a blueprint to validate");
            return;
        }
        int blueprintId = (int) tableModel.getValueAt(row, 0);
        try {
            Blueprint bp = blueprintService.findById(blueprintId);
            blueprintService.validateBlueprint(bp);
            List<String> errors = blueprintService.validateBlueprintStructure(bp);
            if (errors.isEmpty()) {
                statusLabel.setText("Blueprint '" + bp.getExamName() + "' is VALID");
                JOptionPane.showMessageDialog(this, "Blueprint is valid!", "Validation Result", JOptionPane.INFORMATION_MESSAGE);
            } else {
                StringBuilder msg = new StringBuilder("Blueprint has issues:\n");
                for (String err : errors) msg.append("- ").append(err).append("\n");
                statusLabel.setText("Blueprint is INVALID");
                JOptionPane.showMessageDialog(this, msg.toString(), "Validation Result", JOptionPane.WARNING_MESSAGE);
            }
            loadBlueprints();
        } catch (ExamForgeException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void updateActionButtons() {
        int row = blueprintTable.getSelectedRow();
        boolean hasRow = row >= 0 && row < blueprintStatuses.size();
        if (!hasRow) {
            validateBtn.setEnabled(false);
            editBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
            deactivateBtn.setEnabled(false);
            reactivateBtn.setEnabled(false);
            return;
        }
        BlueprintStatus status = blueprintStatuses.get(row);
        boolean inactive = status == BlueprintStatus.INACTIVE;
        validateBtn.setEnabled(!inactive);
        editBtn.setEnabled(true);
        deleteBtn.setEnabled(true);
        deactivateBtn.setEnabled(!inactive);
        reactivateBtn.setEnabled(inactive);
    }

    private void editSelected() {
        int row = blueprintTable.getSelectedRow();
        if (row < 0) {
            statusLabel.setText("Select a blueprint to edit");
            return;
        }
        int blueprintId = (int) tableModel.getValueAt(row, 0);
        try {
            Blueprint bp = blueprintService.findById(blueprintId);
            if (bp == null) {
                statusLabel.setText("Blueprint not found");
                return;
            }
            showBlueprintEditorDialog(bp);
        } catch (ExamForgeException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void deleteSelected() {
        int row = blueprintTable.getSelectedRow();
        if (row < 0) {
            statusLabel.setText("Select a blueprint to delete");
            return;
        }
        int blueprintId = (int) tableModel.getValueAt(row, 0);
        String name = String.valueOf(tableModel.getValueAt(row, 1));
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete blueprint '" + name + "'?\nThis action cannot be undone.", "Delete Blueprint",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            statusLabel.setText("Deletion cancelled");
            return;
        }
        try {
            blueprintService.deleteBlueprint(blueprintId);
            loadBlueprints();
            statusLabel.setText("Blueprint deleted successfully");
        } catch (ExamForgeException e) {
            statusLabel.setText(e.getMessage());
            JOptionPane.showMessageDialog(this, e.getMessage(), "Delete Blueprint", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deactivateSelected() {
        int row = blueprintTable.getSelectedRow();
        if (row < 0) {
            statusLabel.setText("Select a blueprint to deactivate");
            return;
        }
        int blueprintId = (int) tableModel.getValueAt(row, 0);
        try {
            blueprintService.deactivateBlueprint(blueprintId);
            loadBlueprints();
            statusLabel.setText("Blueprint deactivated");
        } catch (ExamForgeException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void reactivateSelected() {
        int row = blueprintTable.getSelectedRow();
        if (row < 0) {
            statusLabel.setText("Select a blueprint to reactivate");
            return;
        }
        int blueprintId = (int) tableModel.getValueAt(row, 0);
        try {
            boolean ok = blueprintService.reactivateBlueprint(blueprintId);
            loadBlueprints();
            statusLabel.setText(ok ? "Blueprint reactivated" : "Only inactive blueprints can be reactivated");
        } catch (ExamForgeException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void showCreateBlueprintDialog() {
        showBlueprintEditorDialog(null);
    }

    private void showBlueprintEditorDialog(Blueprint existing) {
        currentSections = new ArrayList<>();
        currentConstraints = new ArrayList<>();
        if (existing != null) {
            for (BlueprintSection s : existing.getSections()) {
                BlueprintSection copy = new BlueprintSection(s.getSectionName(), s.getQuestionCount(), s.getTotalMarks());
                copy.setQuestionTypeFilter(s.getQuestionTypeFilter());
                copy.setDifficultyFilter(s.getDifficultyFilter());
                copy.setSortOrder(s.getSortOrder());
                currentSections.add(copy);
            }
            for (BlueprintConstraint c : existing.getConstraints()) {
                currentConstraints.add(new BlueprintConstraint(c.getCategory(), c.getBasis(), c.getTarget(),
                        c.getRequiredValue(), c.getTolerance(), c.getPriority()));
            }
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Create Blueprint" : "Edit Blueprint", true);
        dialog.setSize(800, 700);
        dialog.setLocationRelativeTo(this);

        JTabbedPane tabs = new JTabbedPane();

        JPanel basicPanel = new JPanel(new GridBagLayout());
        basicPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JTextField examNameField = new JTextField(30);
        JComboBox<String> subjectCombo = new JComboBox<>();
        try {
            for (String s : questionService.findDistinctSubjects()) {
                subjectCombo.addItem(s);
            }
        } catch (ExamForgeException ignored) {}
        JTextField totalMarksField = new JTextField("100", 10);
        JTextField numQuestionsField = new JTextField("30", 10);
        JTextField durationField = new JTextField("180", 10);
        JComboBox<ExposurePolicyType> exposureCombo = new JComboBox<>(ExposurePolicyType.values());
        JTextField exposureWindowField = new JTextField("3", 5);

        if (existing != null) {
            examNameField.setText(existing.getExamName());
            if (existing.getSubject() != null) {
                boolean found = false;
                for (int i = 0; i < subjectCombo.getItemCount(); i++) {
                    if (existing.getSubject().equals(subjectCombo.getItemAt(i))) { found = true; break; }
                }
                if (!found) subjectCombo.addItem(existing.getSubject());
                subjectCombo.setSelectedItem(existing.getSubject());
            }
            totalMarksField.setText(String.valueOf(existing.getTotalMarks()));
            numQuestionsField.setText(String.valueOf(existing.getNumberOfQuestions()));
            durationField.setText(String.valueOf(existing.getDurationMinutes()));
            exposureCombo.setSelectedItem(existing.getExposurePolicyType());
            exposureWindowField.setText(String.valueOf(existing.getExposureWindow()));
        }

        Theme.styleFields(examNameField, subjectCombo, totalMarksField, numQuestionsField,
            durationField, exposureCombo, exposureWindowField);

        int row = 0;
        addFormRow(basicPanel, gbc, row++, "Exam Name:", examNameField);
        addFormRow(basicPanel, gbc, row++, "Subject:", subjectCombo);

        JLabel poolLabel = new JLabel(" ");
        poolLabel.setFont(Theme.font(10, Font.PLAIN));
        poolLabel.setForeground(Theme.MUTED);
        subjectCombo.addActionListener(e -> {
            String sel = (String) subjectCombo.getSelectedItem();
            if (sel == null) { poolLabel.setText(" "); return; }
            try {
                int pool = questionService.countActiveBySubject(sel);
                poolLabel.setText("Available questions in bank: " + pool);
            } catch (ExamForgeException ex) {
                poolLabel.setText(" ");
            }
        });
        if (subjectCombo.getSelectedItem() != null) {
            for (var al : subjectCombo.getActionListeners()) al.actionPerformed(null);
        }
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        basicPanel.add(poolLabel, gbc);
        row++;

        addFormRow(basicPanel, gbc, row++, "Total Marks:", totalMarksField);
        addFormRow(basicPanel, gbc, row++, "Number of Questions:", numQuestionsField);
        addFormRow(basicPanel, gbc, row++, "Duration (minutes):", durationField);
        addFormRow(basicPanel, gbc, row++, "Exposure Policy:", exposureCombo);
        addFormRow(basicPanel, gbc, row++, "Exposure Window:", exposureWindowField);

        tabs.addTab("Basic", basicPanel);

        JPanel sectionPanel = new JPanel(new BorderLayout());
        sectionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        DefaultTableModel sectionModel = new DefaultTableModel(
            new String[]{"Section Name", "Questions", "Marks", "Type Filter", "Diff Filter"}, 0
        );
        JTable sectionTable = new JTable(sectionModel);
        if (existing != null) {
            for (BlueprintSection s : currentSections) {
                sectionModel.addRow(new Object[]{
                    s.getSectionName(), s.getQuestionCount(), s.getTotalMarks(),
                    s.getQuestionTypeFilter() != null ? s.getQuestionTypeFilter() : "",
                    s.getDifficultyFilter() != null ? s.getDifficultyFilter() : ""
                });
            }
        }
        sectionPanel.add(new JScrollPane(sectionTable), BorderLayout.CENTER);

        JPanel sectionButtons = new JPanel(new FlowLayout());
        JButton addSectionBtn = new JButton("Add Section");
        addSectionBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(dialog, "Section Name:");
            if (name == null || name.trim().isEmpty()) return;
            String countStr = JOptionPane.showInputDialog(dialog, "Question Count:");
            if (countStr == null) return;
            String marksStr = JOptionPane.showInputDialog(dialog, "Total Marks:");
            if (marksStr == null) return;
            try {
                BlueprintSection sec = new BlueprintSection(name.trim(),
                    Integer.parseInt(countStr.trim()), Integer.parseInt(marksStr.trim()));
                currentSections.add(sec);
                sectionModel.addRow(new Object[]{name, countStr, marksStr, "", ""});
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number");
            }
        });
        sectionButtons.add(addSectionBtn);

        JButton removeSectionBtn = new JButton("Remove Selected");
        removeSectionBtn.addActionListener(e -> {
            int selRow = sectionTable.getSelectedRow();
            if (selRow >= 0) {
                sectionModel.removeRow(selRow);
                currentSections.remove(selRow);
            }
        });
        sectionButtons.add(removeSectionBtn);
        sectionPanel.add(sectionButtons, BorderLayout.SOUTH);

        tabs.addTab("Sections", sectionPanel);

        JPanel constraintPanel = new JPanel(new BorderLayout());
        constraintPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

DefaultTableModel constraintModel = new DefaultTableModel(
            new String[]{"Category", "Basis", "Target", "Required Value", "Tolerance", "Priority"}, 0
        );
        JTable constraintTable = new JTable(constraintModel);
        if (existing != null) {
            for (BlueprintConstraint c : currentConstraints) {
                constraintModel.addRow(new Object[]{
                    c.getCategory().getDisplayName(), c.getBasis().getDisplayName(),
                    c.getTarget(), c.getRequiredValue(), c.getTolerance(), c.getPriority().getDisplayName()
                });
            }
        }
        constraintPanel.add(new JScrollPane(constraintTable), BorderLayout.CENTER);

        JPanel constraintButtons = new JPanel(new FlowLayout());
        JButton addConstraintBtn = new JButton("Add Constraint");
        addConstraintBtn.addActionListener(e -> {
            JComboBox<ConstraintCategory> catCombo = new JComboBox<>(ConstraintCategory.values());
            JComboBox<ConstraintBasis> basisCombo = new JComboBox<>(ConstraintBasis.values());
            JTextField targetField = new JTextField(15);
            JTextField valueField = new JTextField(10);
            JTextField tolField = new JTextField("0", 5);
            JComboBox<ConstraintPriority> prioCombo = new JComboBox<>(ConstraintPriority.values());

            JPanel cForm = new JPanel(new GridLayout(6, 2, 5, 5));
            cForm.add(new JLabel("Category:")); cForm.add(catCombo);
            cForm.add(new JLabel("Basis:")); cForm.add(basisCombo);
            cForm.add(new JLabel("Target:")); cForm.add(targetField);
            cForm.add(new JLabel("Required Value:")); cForm.add(valueField);
            cForm.add(new JLabel("Tolerance:")); cForm.add(tolField);
            cForm.add(new JLabel("Priority:")); cForm.add(prioCombo);

            int result = JOptionPane.showConfirmDialog(dialog, cForm, "Add Constraint", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    BlueprintConstraint c = new BlueprintConstraint(
                        (ConstraintCategory) catCombo.getSelectedItem(),
                        (ConstraintBasis) basisCombo.getSelectedItem(),
                        targetField.getText().trim(),
                        Double.parseDouble(valueField.getText().trim()),
                        Double.parseDouble(tolField.getText().trim()),
                        (ConstraintPriority) prioCombo.getSelectedItem()
                    );
                    currentConstraints.add(c);
                    constraintModel.addRow(new Object[]{
                        c.getCategory().getDisplayName(), c.getBasis().getDisplayName(),
                        c.getTarget(), c.getRequiredValue(), c.getTolerance(), c.getPriority().getDisplayName()
                    });
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Invalid number");
                }
            }
        });
        constraintButtons.add(addConstraintBtn);

        JButton removeConstraintBtn = new JButton("Remove Selected");
        removeConstraintBtn.addActionListener(e -> {
            int selRow = constraintTable.getSelectedRow();
            if (selRow >= 0) {
                constraintModel.removeRow(selRow);
                currentConstraints.remove(selRow);
            }
        });
        constraintButtons.add(removeConstraintBtn);
        constraintPanel.add(constraintButtons, BorderLayout.SOUTH);

        tabs.addTab("Constraints", constraintPanel);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel formStatus = new JLabel(" ");
        formStatus.setForeground(Color.RED);

        JButton saveBtn = Theme.button(existing == null ? "Create Blueprint" : "Update Blueprint", Theme.SUCCESS);
        saveBtn.addActionListener(e -> {
            try {
                Blueprint bp = existing != null ? existing : new Blueprint();
                bp.setExamName(examNameField.getText().trim());
                bp.setSubject((String) subjectCombo.getSelectedItem());
                bp.setTotalMarks(Integer.parseInt(totalMarksField.getText().trim()));
                bp.setNumberOfQuestions(Integer.parseInt(numQuestionsField.getText().trim()));
                bp.setDurationMinutes(Integer.parseInt(durationField.getText().trim()));
                bp.setExposurePolicyType((ExposurePolicyType) exposureCombo.getSelectedItem());
                bp.setExposureWindow(Integer.parseInt(exposureWindowField.getText().trim()));
                bp.setCreatedBy(user.getUserId());
                bp.setSections(new ArrayList<>(currentSections));
                bp.setConstraints(new ArrayList<>(currentConstraints));

                if (existing != null) {
                    blueprintService.updateBlueprint(bp);
                    statusLabel.setText("Blueprint updated successfully");
                } else {
                    blueprintService.createBlueprint(bp);
                    statusLabel.setText("Blueprint created successfully");
                }
                dialog.dispose();
                loadBlueprints();
            } catch (ExamForgeException ex) {
                formStatus.setText(ex.getMessage());
            } catch (NumberFormatException ex) {
                formStatus.setText("Invalid number format");
            }
        });

        bottomPanel.add(formStatus);
        bottomPanel.add(saveBtn);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabs, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }
}
