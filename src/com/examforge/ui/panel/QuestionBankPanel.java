package com.examforge.ui.panel;

import com.examforge.exception.ExamForgeException;
import com.examforge.model.*;
import com.examforge.service.QuestionService;
import com.examforge.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class QuestionBankPanel extends JPanel {
    private final User user;
    private final QuestionService questionService;
    private JTable questionTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> subjectFilter;
    private JComboBox<String> unitFilter;
    private JComboBox<String> difficultyFilter;
    private JComboBox<String> typeFilter;
    private JLabel statusLabel;
    private Runnable backAction;

    public QuestionBankPanel(User user) {
        this.user = user;
        this.questionService = new QuestionService();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initUI();
        loadQuestions();
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
        subjectFilter = new JComboBox<>();
        subjectFilter.addItem("All");
        subjectFilter.addActionListener(e -> { loadUnitFilter(); loadQuestions(); });

        unitFilter = new JComboBox<>();
        unitFilter.addItem("All");
        unitFilter.addActionListener(e -> loadQuestions());

        difficultyFilter = new JComboBox<>(new String[]{"All", "EASY", "MEDIUM", "HARD"});
        difficultyFilter.addActionListener(e -> loadQuestions());

        typeFilter = new JComboBox<>(new String[]{"All", "MCQ", "SHORT_ANSWER", "LONG_ANSWER"});
        typeFilter.addActionListener(e -> loadQuestions());
        Theme.styleFields(subjectFilter, unitFilter, difficultyFilter, typeFilter);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        JButton addBtn = Theme.button("+  Add Question", Theme.SUCCESS);
        addBtn.addActionListener(e -> showAddQuestionDialog());
        buttonPanel.add(addBtn);

        JButton editBtn = Theme.button("Edit Selected");
        editBtn.addActionListener(e -> editSelectedQuestion());
        buttonPanel.add(editBtn);

        JButton deactivateBtn = Theme.button("Deactivate", Theme.DANGER);
        deactivateBtn.addActionListener(e -> deactivateSelected());
        buttonPanel.add(deactivateBtn);

        JButton refreshBtn = Theme.softButton("Refresh");
        refreshBtn.addActionListener(e -> loadQuestions());
        buttonPanel.add(refreshBtn);

        JPanel filterPanel = Theme.filterBar(
            Theme.label("Subject:", 12, Font.BOLD, Theme.MUTED), subjectFilter,
            Theme.label("Unit:", 12, Font.BOLD, Theme.MUTED), unitFilter,
            Theme.label("Difficulty:", 12, Font.BOLD, Theme.MUTED), difficultyFilter,
            Theme.label("Type:", 12, Font.BOLD, Theme.MUTED), typeFilter);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(Theme.header("Question Bank Management", createBackButton(), buttonPanel), BorderLayout.NORTH);
        north.add(filterPanel, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new String[]{"ID", "Subject", "Unit", "Topic", "Marks", "Difficulty", "Type", "Active", "Text (Preview)"},
            0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        questionTable = new JTable(tableModel);
        questionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Theme.styleTable(questionTable);
        questionTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        questionTable.getColumnModel().getColumn(8).setPreferredWidth(300);
        add(new JScrollPane(questionTable), BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(Theme.font(12, Font.PLAIN));
        statusLabel.setForeground(Theme.MUTED);
        statusLabel.setBorder(new EmptyBorder(8, 16, 8, 16));
        add(statusLabel, BorderLayout.SOUTH);

        loadSubjectFilter();
        loadUnitFilter();
    }

    private void loadSubjectFilter() {
        try {
            List<String> subjects = questionService.findDistinctSubjects();
            subjectFilter.removeAllItems();
            subjectFilter.addItem("All");
            for (String s : subjects) subjectFilter.addItem(s);
        } catch (ExamForgeException e) {
            statusLabel.setText("Error loading subjects: " + e.getMessage());
        }
    }

    private void loadUnitFilter() {
        try {
            String subject = getFilterValue(subjectFilter);
            Set<String> units = new TreeSet<>();
            if (subject.equals("All")) {
                for (int i = 0; i < subjectFilter.getItemCount(); i++) {
                    String s = subjectFilter.getItemAt(i);
                    if (s.equals("All")) continue;
                    units.addAll(questionService.findDistinctUnits(s));
                }
            } else {
                units.addAll(questionService.findDistinctUnits(subject));
            }

            // Temporarily detach the listener so repopulating the combo doesn't
            // trigger loadQuestions() repeatedly mid-population.
            ActionListener[] listeners = unitFilter.getActionListeners();
            for (ActionListener l : listeners) unitFilter.removeActionListener(l);

            unitFilter.removeAllItems();
            unitFilter.addItem("All");
            for (String u : units) unitFilter.addItem(u);
            unitFilter.setSelectedItem("All");

            for (ActionListener l : listeners) unitFilter.addActionListener(l);
        } catch (ExamForgeException e) {
            statusLabel.setText("Error loading units: " + e.getMessage());
        }
    }

    private void loadQuestions() {
        try {
            String subject = getFilterValue(subjectFilter);
            String unit = getFilterValue(unitFilter);
            String diff = getFilterValue(difficultyFilter);
            String type = getFilterValue(typeFilter);

            List<Question> questions;
            if (subject.equals("All") && unit.equals("All") && diff.equals("All") && type.equals("All")) {
                questions = questionService.findAll();
            } else {
                questions = questionService.findByFilters(
                    subject.equals("All") ? null : subject,
                    unit.equals("All") ? null : unit,
                    diff.equals("All") ? null : diff,
                    type.equals("All") ? null : type
                );
            }

            tableModel.setRowCount(0);
            for (Question q : questions) {
                String preview = q.getQuestionText();
                if (preview != null && preview.length() > 80) {
                    preview = preview.substring(0, 80) + "...";
                }
                tableModel.addRow(new Object[]{
                    q.getQuestionId(), q.getSubject(), q.getUnit(), q.getTopic(),
                    q.getMarks(), q.getDifficulty().getDisplayName(),
                    q.getQuestionType().getDisplayName(),
                    q.isActiveStatus() ? "Yes" : "No",
                    preview
                });
            }
            statusLabel.setText("Showing " + questions.size() + " questions");
        } catch (ExamForgeException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private String getFilterValue(JComboBox<String> combo) {
        Object selected = combo.getSelectedItem();
        return selected != null ? selected.toString() : "All";
    }

    private void showAddQuestionDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Question", true);
        dialog.setSize(600, 550);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JTextField textField = new JTextField(40);
        JTextField subjectField = new JTextField(20);
        JTextField unitField = new JTextField(20);
        JTextField topicField = new JTextField(20);
        JTextField marksField = new JTextField(5);
        JComboBox<Difficulty> diffCombo = new JComboBox<>(Difficulty.values());
        JComboBox<QuestionType> typeCombo = new JComboBox<>(QuestionType.values());

        JTextField optA = new JTextField(30);
        JTextField optB = new JTextField(30);
        JTextField optC = new JTextField(30);
        JTextField optD = new JTextField(30);
        JComboBox<String> correctCombo = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        JTextArea explanationArea = new JTextArea(3, 30);

        Theme.styleFields(textField, subjectField, unitField, topicField, marksField,
            optA, optB, optC, optD, correctCombo, explanationArea);

        int row = 0;
        addFormRow(form, gbc, row++, "Question Text:", new JScrollPane(textField));
        addFormRow(form, gbc, row++, "Subject:", subjectField);
        addFormRow(form, gbc, row++, "Unit:", unitField);
        addFormRow(form, gbc, row++, "Topic:", topicField);
        addFormRow(form, gbc, row++, "Marks:", marksField);
        addFormRow(form, gbc, row++, "Difficulty:", diffCombo);
        addFormRow(form, gbc, row++, "Type:", typeCombo);
        addFormRow(form, gbc, row++, "Option A:", optA);
        addFormRow(form, gbc, row++, "Option B:", optB);
        addFormRow(form, gbc, row++, "Option C:", optC);
        addFormRow(form, gbc, row++, "Option D:", optD);
        addFormRow(form, gbc, row++, "Correct Option:", correctCombo);
        addFormRow(form, gbc, row++, "Explanation:", new JScrollPane(explanationArea));

        JLabel formStatus = new JLabel(" ");
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        form.add(formStatus, gbc);

        JButton saveBtn = Theme.button("Save Question", Theme.SUCCESS);
        gbc.gridy = ++row;
        form.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            try {
                Question q = new Question();
                q.setQuestionText(textField.getText().trim());
                q.setSubject(subjectField.getText().trim());
                q.setUnit(unitField.getText().trim());
                q.setTopic(topicField.getText().trim());
                q.setMarks(Integer.parseInt(marksField.getText().trim()));
                q.setDifficulty((Difficulty) diffCombo.getSelectedItem());
                q.setQuestionType((QuestionType) typeCombo.getSelectedItem());
                q.setOptionA(optA.getText().trim());
                q.setOptionB(optB.getText().trim());
                q.setOptionC(optC.getText().trim());
                q.setOptionD(optD.getText().trim());
                q.setCorrectOption((String) correctCombo.getSelectedItem());
                q.setExplanation(explanationArea.getText().trim());
                q.setCreatedBy(user.getUserId());

                questionService.addQuestion(q);
                dialog.dispose();
                loadSubjectFilter();
                loadUnitFilter();
                loadQuestions();
                statusLabel.setText("Question added successfully");
            } catch (ExamForgeException ex) {
                formStatus.setForeground(Color.RED);
                formStatus.setText(ex.getMessage());
            } catch (NumberFormatException ex) {
                formStatus.setForeground(Color.RED);
                formStatus.setText("Marks must be a number");
            }
        });

        dialog.add(new JScrollPane(form));
        dialog.setVisible(true);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void editSelectedQuestion() {
        int row = questionTable.getSelectedRow();
        if (row < 0) {
            statusLabel.setText("Select a question to edit");
            return;
        }
        int questionId = (int) tableModel.getValueAt(row, 0);
        try {
            Question q = questionService.findById(questionId);
            if (q == null) {
                statusLabel.setText("Question not found");
                return;
            }
            showEditQuestionDialog(q);
        } catch (ExamForgeException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void showEditQuestionDialog(Question q) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Question", true);
        dialog.setSize(600, 550);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JTextField textField = new JTextField(q.getQuestionText(), 40);
        JTextField subjectField = new JTextField(q.getSubject(), 20);
        JTextField unitField = new JTextField(q.getUnit(), 20);
        JTextField topicField = new JTextField(q.getTopic(), 20);
        JTextField marksField = new JTextField(String.valueOf(q.getMarks()), 5);
        JComboBox<Difficulty> diffCombo = new JComboBox<>(Difficulty.values());
        diffCombo.setSelectedItem(q.getDifficulty());
        JComboBox<QuestionType> typeCombo = new JComboBox<>(QuestionType.values());
        typeCombo.setSelectedItem(q.getQuestionType());
        JTextField optA = new JTextField(q.getOptionA() != null ? q.getOptionA() : "", 30);
        JTextField optB = new JTextField(q.getOptionB() != null ? q.getOptionB() : "", 30);
        JTextField optC = new JTextField(q.getOptionC() != null ? q.getOptionC() : "", 30);
        JTextField optD = new JTextField(q.getOptionD() != null ? q.getOptionD() : "", 30);
        JComboBox<String> correctCombo = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        if (q.getCorrectOption() != null) correctCombo.setSelectedItem(q.getCorrectOption());
        JTextArea explanationArea = new JTextArea(q.getExplanation() != null ? q.getExplanation() : "", 3, 30);

        Theme.styleFields(textField, subjectField, unitField, topicField, marksField,
            optA, optB, optC, optD, correctCombo, explanationArea);

        int row = 0;
        addFormRow(form, gbc, row++, "Question Text:", new JScrollPane(textField));
        addFormRow(form, gbc, row++, "Subject:", subjectField);
        addFormRow(form, gbc, row++, "Unit:", unitField);
        addFormRow(form, gbc, row++, "Topic:", topicField);
        addFormRow(form, gbc, row++, "Marks:", marksField);
        addFormRow(form, gbc, row++, "Difficulty:", diffCombo);
        addFormRow(form, gbc, row++, "Type:", typeCombo);
        addFormRow(form, gbc, row++, "Option A:", optA);
        addFormRow(form, gbc, row++, "Option B:", optB);
        addFormRow(form, gbc, row++, "Option C:", optC);
        addFormRow(form, gbc, row++, "Option D:", optD);
        addFormRow(form, gbc, row++, "Correct Option:", correctCombo);
        addFormRow(form, gbc, row++, "Explanation:", new JScrollPane(explanationArea));

        JLabel formStatus = new JLabel(" ");
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        form.add(formStatus, gbc);

        JButton saveBtn = Theme.button("Update Question");
        gbc.gridy = ++row;
        form.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            try {
                q.setQuestionText(textField.getText().trim());
                q.setSubject(subjectField.getText().trim());
                q.setUnit(unitField.getText().trim());
                q.setTopic(topicField.getText().trim());
                q.setMarks(Integer.parseInt(marksField.getText().trim()));
                q.setDifficulty((Difficulty) diffCombo.getSelectedItem());
                q.setQuestionType((QuestionType) typeCombo.getSelectedItem());
                q.setOptionA(optA.getText().trim());
                q.setOptionB(optB.getText().trim());
                q.setOptionC(optC.getText().trim());
                q.setOptionD(optD.getText().trim());
                q.setCorrectOption((String) correctCombo.getSelectedItem());
                q.setExplanation(explanationArea.getText().trim());

                questionService.updateQuestion(q);
                dialog.dispose();
                loadSubjectFilter();
                loadUnitFilter();
                loadQuestions();
                statusLabel.setText("Question updated successfully");
            } catch (ExamForgeException ex) {
                formStatus.setForeground(Color.RED);
                formStatus.setText(ex.getMessage());
            } catch (NumberFormatException ex) {
                formStatus.setForeground(Color.RED);
                formStatus.setText("Marks must be a number");
            }
        });

        dialog.add(new JScrollPane(form));
        dialog.setVisible(true);
    }

    private void deactivateSelected() {
        int row = questionTable.getSelectedRow();
        if (row < 0) {
            statusLabel.setText("Select a question to deactivate");
            return;
        }
        int questionId = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to deactivate this question?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                questionService.deactivateQuestion(questionId);
                loadQuestions();
                statusLabel.setText("Question deactivated");
            } catch (ExamForgeException e) {
                statusLabel.setText("Error: " + e.getMessage());
            }
        }
    }
}
