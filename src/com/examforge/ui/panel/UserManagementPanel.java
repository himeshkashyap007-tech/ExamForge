package com.examforge.ui.panel;

import com.examforge.exception.ExamForgeException;
import com.examforge.model.User;
import com.examforge.model.UserRole;
import com.examforge.service.UserService;
import com.examforge.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel {
    private final User actor;
    private final boolean managingTeachers;
    private final String titleText;
    private final UserService userService;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private Runnable backAction;

    public UserManagementPanel(User actor, boolean managingTeachers, String title) {
        this.actor = actor;
        this.managingTeachers = managingTeachers;
        this.titleText = title;
        this.userService = new UserService();
        setLayout(new BorderLayout());
        setBackground(Theme.SURFACE);
        initUI();
        loadUsers();
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

    private JButton createStyledButton(String text, Color base, Runnable action) {
        return Theme.button(text, base, action);
    }

    private void initUI() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(createStyledButton(managingTeachers ? "+ Add Teacher" : "+ Add Student",
            Theme.SUCCESS, this::showAddUserDialog));
        buttonPanel.add(createStyledButton("Remove", Theme.DANGER, this::removeSelected));

        add(Theme.header(titleText, createBackButton(), buttonPanel), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new String[]{"ID", "Username", "Full Name", "Role", "Active"}, 0
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        userTable = new JTable(tableModel);
        Theme.styleTable(userTable, Theme.BRAND_TINT);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(Theme.font(12, Font.PLAIN));
        statusLabel.setForeground(Theme.MUTED);
        statusLabel.setBorder(new EmptyBorder(8, 16, 8, 16));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void loadUsers() {
        try {
            List<User> users = userService.findManageableUsers(actor, managingTeachers ? UserRole.TEACHER : UserRole.STUDENT);
            tableModel.setRowCount(0);
            for (User u : users) {
                tableModel.addRow(new Object[]{
                    u.getUserId(), u.getUsername(), u.getFullName(),
                    u.getRole().getDisplayName(), u.isActive() ? "Active" : "Inactive"
                });
            }
            statusLabel.setText("Showing " + users.size() + " account(s)");
        } catch (ExamForgeException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            managingTeachers ? "Add Teacher" : "Add Student", true);
        dialog.setSize(400, managingTeachers ? 330 : 280);
        dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField addUser = new JTextField(20);
        JPasswordField addPass = new JPasswordField(20);
        JTextField addName = new JTextField(20);
        JComboBox<UserRole> roleCombo = null;
        JLabel roleLabel = null;
        if (managingTeachers) {
            roleCombo = new JComboBox<>(new UserRole[]{UserRole.TEACHER, UserRole.SUPER_ADMIN});
            roleLabel = new JLabel("Role:");
        }
        JLabel addStatus = new JLabel(" ");

        Theme.styleFields(addUser, addPass, addName);
        if (roleCombo != null) Theme.styleField(roleCombo);
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(Theme.label("Username:", 12, Font.BOLD, Theme.MUTED), gbc);
        gbc.gridx = 1; panel.add(addUser, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(Theme.label("Password:", 12, Font.BOLD, Theme.MUTED), gbc);
        gbc.gridx = 1; panel.add(addPass, gbc);
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(Theme.label("Full Name:", 12, Font.BOLD, Theme.MUTED), gbc);
        gbc.gridx = 1; panel.add(addName, gbc);
        int nextRow = 3;
        if (managingTeachers) {
            gbc.gridx = 0; gbc.gridy = nextRow;
            panel.add(roleLabel, gbc);
            gbc.gridx = 1; panel.add(roleCombo, gbc);
            nextRow++;
        }
        gbc.gridx = 0; gbc.gridy = nextRow; gbc.gridwidth = 2;
        final JComboBox<UserRole> roleSelection = roleCombo;
        JButton submitBtn = Theme.button("Create Account", Theme.SUCCESS);
        panel.add(submitBtn, gbc);
        gbc.gridy = nextRow + 1;
        panel.add(addStatus, gbc);

        submitBtn.addActionListener(e -> {
            try {
                UserRole role = roleSelection != null ? (UserRole) roleSelection.getSelectedItem() : UserRole.STUDENT;
                userService.createUser(actor,
                    addUser.getText().trim(),
                    new String(addPass.getPassword()),
                    addName.getText().trim(),
                    role);
                addStatus.setForeground(Theme.SUCCESS);
                addStatus.setText("Account created successfully!");
                SwingUtilities.invokeLater(() -> {
                    try {
                        Thread.sleep(800);
                    } catch (InterruptedException ignored) {}
                    dialog.dispose();
                    loadUsers();
                });
            } catch (ExamForgeException ex) {
                addStatus.setForeground(Theme.DANGER);
                addStatus.setText(ex.getMessage());
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void removeSelected() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            statusLabel.setText("Select an account to remove");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to remove the account \"" + username + "\"?\n"
                + "It will be deactivated and will no longer be able to log in.",
            "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            userService.deactivateUser(actor, id);
            statusLabel.setText("Account removed: " + username);
            loadUsers();
        } catch (ExamForgeException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }
}