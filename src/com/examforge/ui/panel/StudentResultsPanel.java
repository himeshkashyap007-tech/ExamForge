package com.examforge.ui.panel;

import com.examforge.exception.ExamForgeException;
import com.examforge.model.*;
import com.examforge.service.StudentService;
import com.examforge.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class StudentResultsPanel extends JPanel {
    private final User user;
    private final StudentService studentService;
    private JTable attemptsTable;
    private DefaultTableModel tableModel;
    private JTextArea detailArea;
    private JLabel statusLabel;
    private Runnable backAction;
    private List<StudentAttempt> allAttempts = new ArrayList<>();
    private JComboBox<String> subjectFilter;
    private JButton dateFilterButton;
    private LocalDate selectedDate;
    private final Set<LocalDate> availableDates = new LinkedHashSet<>();
    private JComboBox<String> typeFilter;

    public StudentResultsPanel(User user) {
        this.user = user;
        this.studentService = new StudentService();
        setLayout(new BorderLayout());
        setBackground(Theme.SURFACE);
        initUI();
        loadResults();
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
        subjectFilter.addItem("All Subjects");
        Theme.styleField(subjectFilter);

        dateFilterButton = Theme.softButton("All Dates");

        typeFilter = new JComboBox<>();
        typeFilter.addItem("All Types");
        typeFilter.addItem("Random");
        typeFilter.addItem("Balanced");
        typeFilter.addItem("Weak-Area");
        Theme.styleField(typeFilter);

        JButton clearBtn = Theme.softButton("Clear Filters");
        clearBtn.addActionListener(e -> clearFilters());

        subjectFilter.addActionListener(e -> applyFilters());
        dateFilterButton.addActionListener(e -> showDatePicker());
        typeFilter.addActionListener(e -> applyFilters());

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(Theme.header("My Practice Results", createBackButton(), null), BorderLayout.NORTH);
        north.add(Theme.filterBar(
            Theme.label("Subject:", 12, Font.BOLD, Theme.MUTED), subjectFilter,
            Theme.label("Date:", 12, Font.BOLD, Theme.MUTED), dateFilterButton,
            Theme.label("Type:", 12, Font.BOLD, Theme.MUTED), typeFilter,
            Theme.label("", 12, Font.BOLD, Theme.MUTED), clearBtn), BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new String[]{"Attempt ID", "Type", "Subject", "Questions", "Correct", "Score", "Percentage", "Date"},
            0
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        attemptsTable = new JTable(tableModel);
        Theme.styleTable(attemptsTable, Theme.ACCENT_TINT);
        attemptsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attemptsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && attemptsTable.getSelectedRow() >= 0) {
                viewSelectedDetails();
            }
        });

        detailArea = new JTextArea(10, 50);
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        detailArea.setForeground(Theme.TEXT);
        detailArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(attemptsTable), new JScrollPane(detailArea));
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(Theme.font(12, Font.PLAIN));
        statusLabel.setForeground(Theme.MUTED);
        statusLabel.setBorder(new EmptyBorder(8, 16, 8, 16));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void loadResults() {
        try {
            allAttempts = studentService.getAttempts(user.getUserId());
            populateFilterOptions();
            applyFilters();
        } catch (ExamForgeException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void populateFilterOptions() {
        String prevSubject = subjectFilter.getSelectedItem() != null ? subjectFilter.getSelectedItem().toString() : null;

        subjectFilter.removeAllItems();
        subjectFilter.addItem("All Subjects");
        Set<String> subjects = new LinkedHashSet<>();
        for (StudentAttempt a : allAttempts) {
            if (a.getSubject() != null && !a.getSubject().trim().isEmpty()) subjects.add(a.getSubject());
        }
        for (String s : subjects) subjectFilter.addItem(s);
        if (prevSubject != null && indexOfItem(subjectFilter, prevSubject) >= 0) {
            subjectFilter.setSelectedItem(prevSubject);
        }

        availableDates.clear();
        for (StudentAttempt a : allAttempts) {
            if (a.getAttemptedAt() != null) availableDates.add(a.getAttemptedAt().toLocalDate());
        }
        if (selectedDate != null && !availableDates.contains(selectedDate)) {
            selectedDate = null;
        }
        dateFilterButton.setText(selectedDate == null ? "All Dates" : selectedDate.toString());

        typeFilter.removeAllItems();
        typeFilter.addItem("All Types");
        typeFilter.addItem("Random");
        typeFilter.addItem("Balanced");
        typeFilter.addItem("Weak-Area");
    }

    private int indexOfItem(JComboBox<String> combo, String value) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (value.equals(combo.getItemAt(i))) return i;
        }
        return -1;
    }

    private void applyFilters() {
        String subj = (String) subjectFilter.getSelectedItem();
        String type = (String) typeFilter.getSelectedItem();

        LocalDate selDate = selectedDate;

        List<StudentAttempt> filtered = new ArrayList<>();
        for (StudentAttempt a : allAttempts) {
            boolean subjectOk = subj == null || "All Subjects".equals(subj) || subj.equals(a.getSubject());
            boolean dateOk = selDate == null || (a.getAttemptedAt() != null
                && selDate.equals(a.getAttemptedAt().toLocalDate()));
            boolean typeOk = type == null || "All Types".equals(type) || type.equals(a.getPracticeType());
            if (subjectOk && dateOk && typeOk) filtered.add(a);
        }
        populateTable(filtered);
        if (filtered.isEmpty() && !allAttempts.isEmpty()) {
            statusLabel.setText("No results match the selected filters.");
        } else {
            statusLabel.setText("Showing " + filtered.size() + " of " + allAttempts.size() + " attempts");
        }
    }

    private void clearFilters() {
        subjectFilter.setSelectedItem("All Subjects");
        selectedDate = null;
        dateFilterButton.setText("All Dates");
        typeFilter.setSelectedItem("All Types");
        applyFilters();
    }

    private void showDatePicker() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Select Date", Dialog.ModalityType.MODELESS);
        dialog.setUndecorated(true);
        dialog.setAlwaysOnTop(true);
        YearMonth month = selectedDate != null ? YearMonth.from(selectedDate) : YearMonth.now();
        buildCalendarDialog(dialog, month);
        dialog.pack();
        Point loc = dateFilterButton.getLocationOnScreen();
        dialog.setLocation(loc.x, loc.y + dateFilterButton.getHeight());
        dialog.addWindowFocusListener(new WindowAdapter() {
            @Override public void windowLostFocus(WindowEvent e) {
                dialog.dispose();
            }
        });
        dialog.setVisible(true);
    }

    private void buildCalendarDialog(JDialog dialog, YearMonth month) {
        dialog.getContentPane().removeAll();
        JPanel cal = new JPanel(new BorderLayout());
        cal.setBackground(Color.WHITE);
        cal.setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        JButton prev = new JButton("<");
        prev.setFocusPainted(false);
        prev.addActionListener(e -> buildCalendarDialog(dialog, month.minusMonths(1)));
        JLabel title = new JLabel(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")), SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(new Color(40, 120, 80));
        JButton next = new JButton(">");
        next.setFocusPainted(false);
        next.addActionListener(e -> buildCalendarDialog(dialog, month.plusMonths(1)));
        header.add(prev, BorderLayout.WEST);
        header.add(title, BorderLayout.CENTER);
        header.add(next, BorderLayout.EAST);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        legend.setBackground(Color.WHITE);
        JLabel availLbl = new JLabel("\u25CF Available");
        availLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        availLbl.setForeground(new Color(40, 120, 80));
        JLabel unavailLbl = new JLabel("\u25CB Unavailable");
        unavailLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        unavailLbl.setForeground(new Color(150, 160, 150));
        legend.add(availLbl);
        legend.add(unavailLbl);

        JPanel north = new JPanel(new GridLayout(2, 1, 0, 0));
        north.setBackground(Color.WHITE);
        north.add(header);
        north.add(legend);
        cal.add(north, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(7, 7, 2, 2));
        grid.setBackground(Color.WHITE);
        for (String d : new String[]{"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"}) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(new Font("Segoe UI", Font.BOLD, 10));
            l.setForeground(new Color(100, 120, 100));
            grid.add(l);
        }
        LocalDate first = month.atDay(1);
        int offset = first.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < offset; i++) grid.add(new JLabel(" "));
        for (int d = 1; d <= month.lengthOfMonth(); d++) {
            LocalDate date = month.atDay(d);
            JButton day = new JButton(String.valueOf(d));
            day.setFocusPainted(false);
            day.setOpaque(true);
            if (availableDates.contains(date)) {
                day.setBorderPainted(true);
                day.setBorder(BorderFactory.createLineBorder(new Color(40, 120, 80)));
                if (date.equals(selectedDate)) {
                    day.setBackground(new Color(20, 95, 60));
                    day.setForeground(Color.WHITE);
                    day.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    day.setBorder(BorderFactory.createLineBorder(new Color(10, 70, 45)));
                } else {
                    day.setBackground(new Color(222, 240, 228));
                    day.setForeground(new Color(25, 95, 60));
                    day.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    day.addMouseListener(new MouseAdapter() {
                        @Override public void mouseEntered(MouseEvent e) {
                            day.setBackground(new Color(40, 120, 80));
                            day.setForeground(Color.WHITE);
                        }
                        @Override public void mouseExited(MouseEvent e) {
                            day.setBackground(new Color(222, 240, 228));
                            day.setForeground(new Color(25, 95, 60));
                        }
                    });
                }
                day.addActionListener(e -> {
                    selectedDate = date;
                    dateFilterButton.setText(date.toString());
                    dialog.dispose();
                    applyFilters();
                });
            } else {
                day.setBorderPainted(false);
                day.setBackground(new Color(238, 240, 236));
                day.setForeground(new Color(150, 160, 150));
                day.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                day.setEnabled(false);
            }
            grid.add(day);
        }
        cal.add(grid, BorderLayout.CENTER);

        JButton allDatesBtn = new JButton("All Dates");
        allDatesBtn.setFocusPainted(false);
        allDatesBtn.setBackground(new Color(248, 252, 248));
        allDatesBtn.addActionListener(e -> {
            selectedDate = null;
            dateFilterButton.setText("All Dates");
            dialog.dispose();
            applyFilters();
        });
        cal.add(allDatesBtn, BorderLayout.SOUTH);

        dialog.setContentPane(cal);
        dialog.pack();
        dialog.revalidate();
        dialog.repaint();
    }

    private void populateTable(List<StudentAttempt> attempts) {
        tableModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (StudentAttempt a : attempts) {
            tableModel.addRow(new Object[]{
                a.getAttemptId(), a.getPracticeType(), a.getSubject(),
                a.getTotalQuestions(), a.getCorrectAnswers(),
                a.getMarksObtained() + "/" + a.getTotalMarks(),
                String.format("%.1f%%", a.getPercentage()),
                a.getAttemptedAt() != null ? a.getAttemptedAt().format(fmt) : "N/A"
            });
        }
    }

    private void viewSelectedDetails() {
        int row = attemptsTable.getSelectedRow();
        if (row < 0) {
            statusLabel.setText("Select an attempt to view details");
            return;
        }
        int attemptId = (int) tableModel.getValueAt(row, 0);
        try {
            List<StudentResponse> responses = studentService.getAttemptResponses(attemptId);
            StringBuilder sb = new StringBuilder();
            sb.append("Attempt #").append(attemptId).append(" - Detailed Results\n");
            sb.append("=".repeat(50)).append("\n\n");

            int qNum = 1;
            for (StudentResponse r : responses) {
                Question q = r.getQuestion();
                sb.append("Q").append(qNum++).append(": ");
                if (q != null) {
                    String text = q.getQuestionText();
                    if (text != null && text.length() > 60) text = text.substring(0, 60) + "...";
                    sb.append(text).append("\n");
                    sb.append("  Your Answer: ").append(r.getSelectedOption()).append(" | ");
                    sb.append("Correct: ").append(q.getCorrectOption()).append(" | ");
                    sb.append(r.isCorrect() ? "CORRECT" : "WRONG").append(" | ");
                    sb.append("Marks: ").append(r.getMarksObtained()).append("/").append(q.getMarks()).append("\n\n");
                }
            }
            detailArea.setText(sb.toString());
            detailArea.setCaretPosition(0);
        } catch (ExamForgeException e) {
            detailArea.setText("Error loading details: " + e.getMessage());
        }
    }
}
