package com.examforge.ui.panel;

import com.examforge.model.User;
import com.examforge.model.UserRole;
import com.examforge.ui.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherDashboard extends JPanel {
    private final User user;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final Runnable logoutCallback;
    private final List<NavItem> navItems = new ArrayList<>();
    private JLabel pageTitle;
    private NavItem activeNav;

    private static final Color SIDEBAR = Theme.BRAND_DARK;
    private static final Color SIDEBAR_ACTIVE = new Color(73, 100, 175);
    private static final Color SIDEBAR_HOVER = new Color(52, 74, 132);

    public TeacherDashboard(User user, Runnable logoutCallback) {
        this.user = user;
        this.logoutCallback = logoutCallback;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(Theme.BG);

        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        JPanel topBar = createTopBar();
        content.add(topBar, BorderLayout.NORTH);
        content.add(mainPanel, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        mainPanel.add(createWelcomePanel(), "WELCOME");

        QuestionBankPanel questionsPanel = new QuestionBankPanel(user);
        questionsPanel.setBackAction(() -> cardLayout.show(mainPanel, "WELCOME"));
        mainPanel.add(questionsPanel, "QUESTIONS");

        BlueprintPanel blueprintPanel = new BlueprintPanel(user);
        blueprintPanel.setBackAction(() -> cardLayout.show(mainPanel, "WELCOME"));
        mainPanel.add(blueprintPanel, "BLUEPRINT");

        PaperGenerationPanel generatePanel = new PaperGenerationPanel(user);
        generatePanel.setBackAction(() -> cardLayout.show(mainPanel, "WELCOME"));
        mainPanel.add(generatePanel, "GENERATE");

        ExposureAnalysisPanel exposurePanel = new ExposureAnalysisPanel(user);
        exposurePanel.setBackAction(() -> cardLayout.show(mainPanel, "WELCOME"));
        mainPanel.add(exposurePanel, "EXPOSURE");

        ReportPanel reportsPanel = new ReportPanel(user);
        reportsPanel.setBackAction(() -> cardLayout.show(mainPanel, "WELCOME"));
        mainPanel.add(reportsPanel, "REPORTS");

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            UserManagementPanel teachersPanel = new UserManagementPanel(user, true, "Teacher Management");
            teachersPanel.setBackAction(() -> cardLayout.show(mainPanel, "WELCOME"));
            mainPanel.add(teachersPanel, "TEACHERS");

            UserManagementPanel studentsPanel = new UserManagementPanel(user, false, "Student Management");
            studentsPanel.setBackAction(() -> cardLayout.show(mainPanel, "WELCOME"));
            mainPanel.add(studentsPanel, "STUDENTS");
        }

        navigateTo(navItems.isEmpty() ? null : navItems.get(0));
    }

    /* ---------------- Sidebar ---------------- */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR);
        sidebar.setPreferredSize(new Dimension(252, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(18, 8, 14, 10));

        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setOpaque(false);
        JLabel name = new JLabel("ExamForge");
        name.setFont(Theme.font(20, Font.BOLD));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        name.setBorder(new EmptyBorder(0, 8, 0, 0));
        brand.add(name);
        JLabel tag = new JLabel("Paper Design System");
        tag.setFont(Theme.font(10, Font.PLAIN));
        tag.setForeground(new Color(168, 182, 216));
        tag.setAlignmentX(Component.LEFT_ALIGNMENT);
        tag.setBorder(new EmptyBorder(0, 8, 0, 0));
        brand.add(tag);
        sidebar.add(brand);

        sidebar.add(Box.createVerticalStrut(22));

        sidebar.add(sectionLabel("WORKSPACE"));
        addNavButton(sidebar, "Dashboard", "WELCOME", Theme.BRAND);
        addNavButton(sidebar, "Question Bank", "QUESTIONS", Theme.BRAND);
        addNavButton(sidebar, "Blueprint Design", "BLUEPRINT", Theme.BRAND);
        addNavButton(sidebar, "Paper Generation", "GENERATE", Theme.BRAND);
        addNavButton(sidebar, "Exposure Analysis", "EXPOSURE", Theme.BRAND);
        addNavButton(sidebar, "Reports & History", "REPORTS", Theme.BRAND);

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            sidebar.add(Box.createVerticalStrut(14));
            sidebar.add(sectionLabel("MANAGEMENT"));
            addNavButton(sidebar, "Teacher Management", "TEACHERS", Theme.DANGER);
            addNavButton(sidebar, "Student Management", "STUDENTS", Theme.DANGER);
        }

        sidebar.add(Box.createVerticalGlue());

        JPanel userChip = new JPanel(new BorderLayout(10, 0));
        userChip.setOpaque(false);
        userChip.setMaximumSize(new Dimension(224, 46));
        JLabel avatar = new JLabel(initials(user.getFullName()));
        avatar.setOpaque(true);
        avatar.setBackground(new Color(96, 121, 194));
        avatar.setForeground(Color.WHITE);
        avatar.setFont(Theme.font(12, Font.BOLD));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(34, 34));
        avatar.setBorder(new MatteBorder(2, 2, 2, 2, new Color(140, 160, 210)));

        JPanel who = new JPanel();
        who.setOpaque(false);
        who.setLayout(new BoxLayout(who, BoxLayout.Y_AXIS));
        JLabel nm = new JLabel(user.getFullName());
        nm.setFont(Theme.font(13, Font.BOLD));
        nm.setForeground(Color.WHITE);
        nm.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel rl = new JLabel(user.getRole().getDisplayName());
        rl.setFont(Theme.font(11, Font.PLAIN));
        rl.setForeground(new Color(166, 180, 214));
        rl.setAlignmentX(Component.LEFT_ALIGNMENT);
        who.add(nm);
        who.add(rl);

        userChip.add(avatar, BorderLayout.WEST);
        userChip.add(who, BorderLayout.CENTER);
        sidebar.add(userChip);

        sidebar.add(Box.createVerticalStrut(12));

        JButton logout = new Theme.RoundedButton("Logout", Theme.DANGER);
        logout.setMaximumSize(new Dimension(224, 40));
        logout.setPreferredSize(new Dimension(224, 40));
        logout.addActionListener(e -> logoutCallback.run());
        sidebar.add(logout);

        return sidebar;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(11, Font.BOLD));
        l.setForeground(new Color(142, 158, 200));
        l.setBorder(new EmptyBorder(2, 8, 8, 0));
        return l;
    }

    private JButton addNavButton(JPanel sidebar, String text, String tag, Color accent) {
        NavItem item = new NavItem(text, tag, accent);
        JButton b = Theme.navButton(text, SIDEBAR, SIDEBAR_ACTIVE, SIDEBAR_HOVER);
        b.setFont(Theme.font(15, Font.PLAIN));
        b.setMaximumSize(new Dimension(224, 44));
        b.setPreferredSize(new Dimension(224, 44));
        b.addActionListener(e -> navigateTo(item));
        item.button = b;
        navItems.add(item);
        sidebar.add(b);
        sidebar.add(Box.createVerticalStrut(6));
        return b;
    }

    private void navigateTo(NavItem item) {
        if (activeNav != null && activeNav.button != null) {
            ((Theme.NavButton) activeNav.button).setActive(false);
            activeNav.button.setFont(Theme.font(15, Font.PLAIN));
        }
        activeNav = item;
        if (item != null) {
            ((Theme.NavButton) item.button).setActive(true);
            item.button.setFont(Theme.font(15, Font.BOLD));
            pageTitle.setText(item.text);
            cardLayout.show(mainPanel, item.tag);
        }
    }

    private String initials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.SURFACE);
        bar.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER),
            new EmptyBorder(10, 18, 10, 18)));
        pageTitle = new JLabel();
        pageTitle.setFont(Theme.font(16, Font.BOLD));
        pageTitle.setForeground(Theme.TEXT);
        bar.add(pageTitle, BorderLayout.WEST);

        JLabel role = Theme.label(user.getRole().getDisplayName(), 11, Font.BOLD, Theme.BRAND);
        role.setBorder(new CompoundBorder(
            new MatteBorder(0, 6, 0, 0, Theme.BRAND),
            new EmptyBorder(0, 8, 0, 0)));
        bar.add(role, BorderLayout.EAST);
        return bar;
    }

    /* ---------------- Welcome ---------------- */
    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(36, 40, 40, 40));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel title = new JLabel("Welcome to ExamForge");
        title.setFont(Theme.font(30, Font.BOLD));
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(title);

        centerPanel.add(Box.createVerticalStrut(10));

        JLabel subtitle = new JLabel("Design constraint-based question papers, analyze exposure and manage your assessment system.");
        subtitle.setFont(Theme.font(14, Font.PLAIN));
        subtitle.setForeground(Theme.MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(subtitle);

        centerPanel.add(Box.createVerticalStrut(30));

        List<FeatureCard> cards = new ArrayList<>();
        cards.add(new FeatureCard("Question Bank", "Manage structured questions with units, topics, difficulty levels and types", "QUESTIONS", Theme.BRAND));
        cards.add(new FeatureCard("Blueprint Design", "Define examination constraints with sections, units, difficulty and type distributions", "BLUEPRINT", Theme.BRAND));
        cards.add(new FeatureCard("Feasibility Analysis", "Check if the question bank can satisfy your blueprint constraints before generation", "GENERATE", Theme.BRAND));
        cards.add(new FeatureCard("Paper Generation", "Auto-generate optimized papers with complete validation", "GENERATE", Theme.BRAND));
        cards.add(new FeatureCard("Exposure Analysis", "Track question usage across examinations and manage exposure levels", "EXPOSURE", Theme.BRAND));
        cards.add(new FeatureCard("Reports & History", "View examination history, question usage reports and generation logs", "REPORTS", Theme.BRAND));

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            cards.add(new FeatureCard("Teacher Management", "Add, remove and manage teacher and super admin accounts", "TEACHERS", Theme.DANGER));
            cards.add(new FeatureCard("Student Management", "Add and manage student accounts", "STUDENTS", Theme.DANGER));
        }

        int cols = 3;
        int rows = (cards.size() + cols - 1) / cols;
        JPanel grid = new JPanel(new GridLayout(rows, cols, 16, 16));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(1000, Integer.MAX_VALUE));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (FeatureCard fc : cards) {
            grid.add(createFeatureCard(fc));
        }

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(grid, BorderLayout.NORTH);
        centerPanel.add(wrap);

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private JComponent createFeatureCard(FeatureCard fc) {
        JButton button = new JButton("<html><body style='width: 210px'>"
            + "<span style='font-size: 14px; font-weight: bold; color: #1F2937;'>" + fc.title + "</span>"
            + "<br>"
            + "<span style='font-size: 12px; color: #6C7589;'>" + fc.description + "</span>"
            + "</body></html>");
        button.setFont(Theme.font(12, Font.PLAIN));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setVerticalAlignment(SwingConstants.TOP);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            new EmptyBorder(16, 16, 16, 16)));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(Theme.SURFACE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText("Click to open " + fc.title);
        button.addChangeListener(e -> {
            ButtonModel m = button.getModel();
            if (m.isPressed()) {
                button.setBackground(Theme.BRAND_TINT);
            } else if (m.isRollover()) {
                button.setBackground(new Color(246, 248, 253));
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(fc.accent),
                    new EmptyBorder(16, 16, 16, 16)));
            } else {
                button.setBackground(Theme.SURFACE);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER),
                    new EmptyBorder(16, 16, 16, 16)));
            }
        });
        button.addActionListener(e -> {
            button.getModel().setRollover(false);
            navigateTo(findNav(fc.tag));
        });
        return button;
    }

    private NavItem findNav(String tag) {
        for (NavItem item : navItems) {
            if (item.tag.equals(tag)) return item;
        }
        return null;
    }

    private static class NavItem {
        final String text;
        final String tag;
        final Color accent;
        JButton button;
        JLabel marker;

        NavItem(String text, String tag, Color accent) {
            this.text = text;
            this.tag = tag;
            this.accent = accent;
        }

        void setMarkerVisibility(boolean visible) {
            if (marker != null) marker.setVisible(visible);
        }
    }

    private static class FeatureCard {
        final String title;
        final String description;
        final String tag;
        final Color accent;

        FeatureCard(String title, String description, String tag, Color accent) {
            this.title = title;
            this.description = description;
            this.tag = tag;
            this.accent = accent;
        }
    }
}