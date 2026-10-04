package com.examforge.ui;

import com.examforge.exception.ExamForgeException;
import com.examforge.model.User;
import com.examforge.model.UserRole;
import com.examforge.service.UserService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {
    private final boolean studentLogin;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    private final UserService userService;
    private LoginListener loginListener;
    private Runnable backAction;

    public interface LoginListener {
        void onLoginSuccess(User user);
    }

    public LoginPanel(boolean studentLogin) {
        this.studentLogin = studentLogin;
        userService = new UserService();

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(245, 247, 252));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        topBar.setOpaque(false);
        JButton backButton = Theme.backButton();
        backButton.addActionListener(e -> {
            if (backAction != null) backAction.run();
        });
        topBar.add(backButton);
        add(topBar, BorderLayout.NORTH);

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(createCard());
        add(centerWrap, BorderLayout.CENTER);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int h = Math.max(getHeight(), 1);
        g2.setPaint(new GradientPaint(0, 0, new Color(240, 243, 250), 0, h, new Color(248, 249, 253)));
        g2.fillRect(0, 0, getWidth(), h);
        g2.dispose();
    }

    private JComponent createCard() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(430, 430));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            new javax.swing.border.EmptyBorder(30, 42, 30, 42)));

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("ExamForge");
        brand.setFont(Theme.font(27, Font.BOLD));
        brand.setForeground(studentLogin ? Theme.ACCENT_DARK : Theme.BRAND_DARK);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(brand);

        panel.add(Box.createVerticalStrut(4));

        JLabel badge = new JLabel(studentLogin ? "STUDENT PORTAL" : "ADMIN  /  TEACHER PORTAL");
        badge.setFont(Theme.font(10, Font.BOLD));
        badge.setForeground(studentLogin ? Theme.ACCENT : Theme.BRAND);
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        badge.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, studentLogin ? Theme.ACCENT : Theme.BRAND),
            new javax.swing.border.EmptyBorder(0, 0, 8, 0)));
        panel.add(badge);

        panel.add(Box.createVerticalStrut(22));

        JLabel welcome = new JLabel("Welcome Back");
        welcome.setFont(Theme.font(19, Font.BOLD));
        welcome.setForeground(Theme.TEXT);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(welcome);

        panel.add(Box.createVerticalStrut(3));

        JLabel subtitle = new JLabel(studentLogin ? "Sign in to your student account" : "Sign in to continue");
        subtitle.setFont(Theme.font(12, Font.PLAIN));
        subtitle.setForeground(Theme.MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(subtitle);

        panel.add(Box.createVerticalStrut(22));

        panel.add(fieldRow("Username", usernameField = new JTextField(22)));
        panel.add(Box.createVerticalStrut(10));
        panel.add(fieldRow("Password", passwordField = new JPasswordField(22)));
        panel.add(Box.createVerticalStrut(4));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(Theme.font(12, Font.PLAIN));
        statusLabel.setForeground(Theme.DANGER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(statusLabel);

        panel.add(Box.createVerticalStrut(8));

        loginButton = new Theme.RoundedButton("Login", studentLogin ? Theme.ACCENT : Theme.BRAND);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(300, 42));
        loginButton.setPreferredSize(new Dimension(300, 42));
        panel.add(loginButton);

        if (studentLogin) {
            panel.add(Box.createVerticalStrut(10));
            registerButton = new Theme.RoundedButton("Register New Account", Theme.SUCCESS);
            registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            registerButton.setMaximumSize(new Dimension(300, 42));
            registerButton.setPreferredSize(new Dimension(300, 42));
            registerButton.addActionListener(e -> showRegistrationDialog());
            panel.add(registerButton);
        } else {
            registerButton = null;
        }

        loginButton.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin());

        Theme.styleField(usernameField);
        Theme.styleField(passwordField);

        return panel;
    }

    private JComponent fieldRow(String title, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        JLabel l = new JLabel(title);
        l.setFont(Theme.font(12, Font.BOLD));
        l.setForeground(Theme.MUTED);
        row.add(l, BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    public void setLoginListener(LoginListener listener) {
        this.loginListener = listener;
    }

    public void setBackAction(Runnable backAction) {
        this.backAction = backAction;
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password");
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setForeground(Theme.MUTED);
        statusLabel.setText("Logging in...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return userService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null && loginListener != null) {
                        loginListener.onLoginSuccess(user);
                    } else {
                        statusLabel.setForeground(Theme.DANGER);
                        statusLabel.setText("Invalid credentials");
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(Theme.DANGER);
                    statusLabel.setText("Login failed: " + ex.getMessage());
                }
                loginButton.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void showRegistrationDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register", true);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new javax.swing.border.EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField regUser = new JTextField(20);
        JPasswordField regPass = new JPasswordField(20);
        JTextField regName = new JTextField(20);
        JLabel regStatus = new JLabel(" ");
        regStatus.setForeground(Theme.DANGER);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(Theme.label("Username:", 12, Font.BOLD, Theme.MUTED), gbc);
        gbc.gridx = 1; panel.add(regUser, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(Theme.label("Password:", 12, Font.BOLD, Theme.MUTED), gbc);
        gbc.gridx = 1; panel.add(regPass, gbc);
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(Theme.label("Full Name:", 12, Font.BOLD, Theme.MUTED), gbc);
        gbc.gridx = 1; panel.add(regName, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JButton submitBtn = new Theme.RoundedButton("Register", Theme.SUCCESS);
        panel.add(submitBtn, gbc);
        gbc.gridy = 4;
        panel.add(regStatus, gbc);

        Theme.styleFields(regUser, regPass, regName);

        submitBtn.addActionListener(e -> {
            try {
                userService.register(
                    regUser.getText().trim(),
                    new String(regPass.getPassword()),
                    regName.getText().trim(),
                    UserRole.STUDENT
                );

                regStatus.setForeground(Theme.SUCCESS);
                regStatus.setText("Registration successful!");

                SwingUtilities.invokeLater(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}

                    dialog.dispose();
                });

            } catch (ExamForgeException ex) {
                regStatus.setForeground(Theme.DANGER);
                regStatus.setText(ex.getMessage());
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }
}