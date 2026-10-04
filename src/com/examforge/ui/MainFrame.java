package com.examforge.ui;

import com.examforge.model.User;
import com.examforge.model.UserRole;
import com.examforge.ui.panel.*;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final LandingPanel landingPanel;
    private final LoginPanel studentLoginPanel;
    private final LoginPanel adminLoginPanel;
    private TeacherDashboard teacherDashboard;
    private StudentDashboard studentDashboard;

    private static final String LANDING_CARD = "LANDING";
    private static final String STUDENT_LOGIN_CARD = "STUDENT_LOGIN";
    private static final String ADMIN_LOGIN_CARD = "ADMIN_LOGIN";
    private static final String TEACHER_CARD = "TEACHER";
    private static final String STUDENT_CARD = "STUDENT";

    public MainFrame() {
        super("ExamForge - Constraint-Based Paper Design System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        landingPanel = new LandingPanel();
        landingPanel.setChoiceListener(new LandingPanel.ChoiceListener() {
            @Override public void onStudentLogin() {
                cardLayout.show(contentPanel, STUDENT_LOGIN_CARD);
            }
            @Override public void onAdminLogin() {
                cardLayout.show(contentPanel, ADMIN_LOGIN_CARD);
            }
        });
        contentPanel.add(landingPanel, LANDING_CARD);

        studentLoginPanel = new LoginPanel(true);
        studentLoginPanel.setLoginListener(this::handleLogin);
        studentLoginPanel.setBackAction(() -> cardLayout.show(contentPanel, LANDING_CARD));
        contentPanel.add(studentLoginPanel, STUDENT_LOGIN_CARD);

        adminLoginPanel = new LoginPanel(false);
        adminLoginPanel.setLoginListener(this::handleLogin);
        adminLoginPanel.setBackAction(() -> cardLayout.show(contentPanel, LANDING_CARD));
        contentPanel.add(adminLoginPanel, ADMIN_LOGIN_CARD);

        add(contentPanel);
        cardLayout.show(contentPanel, LANDING_CARD);
    }

    private void handleLogin(User user) {
        contentPanel.removeAll();
        if (user.getRole() == UserRole.TEACHER || user.getRole() == UserRole.SUPER_ADMIN) {
            teacherDashboard = new TeacherDashboard(user, this::handleLogout);
            contentPanel.add(teacherDashboard, TEACHER_CARD);
            cardLayout.show(contentPanel, TEACHER_CARD);
        } else {
            studentDashboard = new StudentDashboard(user, this::handleLogout);
            contentPanel.add(studentDashboard, STUDENT_CARD);
            cardLayout.show(contentPanel, STUDENT_CARD);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void handleLogout() {
        contentPanel.removeAll();
        contentPanel.add(landingPanel, LANDING_CARD);
        contentPanel.add(studentLoginPanel, STUDENT_LOGIN_CARD);
        contentPanel.add(adminLoginPanel, ADMIN_LOGIN_CARD);
        cardLayout.show(contentPanel, LANDING_CARD);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}