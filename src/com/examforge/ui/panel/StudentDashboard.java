package com.examforge.ui.panel;

import com.examforge.model.User;
import com.examforge.ui.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDashboard extends JPanel {
    private final User user;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final Runnable logoutCallback;
    private final StudentPracticePanel practicePanel;
    private final List<NavItem> navItems = new ArrayList<>();
    private JLabel pageTitle;
    private NavItem activeNav;

    private static final Color SIDEBAR = Theme.ACCENT_DARK;
    private static final Color SIDEBAR_ACTIVE = new Color(43, 150, 102);
    private static final Color SIDEBAR_HOVER = new Color(30, 116, 78);

    public StudentDashboard(User user, Runnable logoutCallback) {
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
        content.add(createTopBar(), BorderLayout.NORTH);
        content.add(mainPanel, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        mainPanel.add(createWelcomePanel(), "WELCOME");

        practicePanel = new StudentPracticePanel(user);
        practicePanel.setBackAction(() -> cardLayout.show(mainPanel, "WELCOME"));
        mainPanel.add(practicePanel, "PRACTICE");

        StudentResultsPanel resultsPanel = new StudentResultsPanel(user);
        resultsPanel.setBackAction(() -> cardLayout.show(mainPanel, "WELCOME"));
        mainPanel.add(resultsPanel, "RESULTS");

        StudentPerformancePanel performancePanel = new StudentPerformancePanel(user);
        performancePanel.setBackAction(() -> cardLayout.show(mainPanel, "WELCOME"));
        mainPanel.add(performancePanel, "PERFORMANCE");

        navigateTo(navItems.isEmpty() ? null : navItems.get(0));
    }

    /* ---------------- Sidebar ---------------- */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR);
        sidebar.setPreferredSize(new Dimension(252, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(18, 14, 14, 14));

        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setOpaque(false);
        JLabel name = new JLabel("ExamForge");
        name.setFont(Theme.font(20, Font.BOLD));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(name);
        JLabel tag = new JLabel("Student Practice Portal");
        tag.setFont(Theme.font(10, Font.PLAIN));
        tag.setForeground(new Color(164, 215, 188));
        tag.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(tag);
        sidebar.add(brand);

        sidebar.add(Box.createVerticalStrut(22));

        sidebar.add(sectionLabel("MENU"));
        addNavButton(sidebar, "Dashboard", "WELCOME");
        addNavButton(sidebar, "Practice", "PRACTICE");
        addNavButton(sidebar, "My Results", "RESULTS");
        addNavButton(sidebar, "Performance", "PERFORMANCE");

        sidebar.add(Box.createVerticalGlue());

        JPanel userChip = new JPanel(new BorderLayout(10, 0));
        userChip.setOpaque(false);
        userChip.setMaximumSize(new Dimension(224, 46));
        JLabel avatar = new JLabel(initials(user.getFullName()));
        avatar.setOpaque(true);
        avatar.setBackground(new Color(74, 165, 126));
        avatar.setForeground(Color.WHITE);
        avatar.setFont(Theme.font(12, Font.BOLD));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(34, 34));
        avatar.setBorder(new MatteBorder(2, 2, 2, 2, new Color(140, 205, 172)));

        JPanel who = new JPanel();
        who.setOpaque(false);
        who.setLayout(new BoxLayout(who, BoxLayout.Y_AXIS));
        JLabel nm = new JLabel(user.getFullName());
        nm.setFont(Theme.font(13, Font.BOLD));
        nm.setForeground(Color.WHITE);
        nm.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel rl = new JLabel(user.getRole().getDisplayName());
        rl.setFont(Theme.font(11, Font.PLAIN));
        rl.setForeground(new Color(164, 215, 188));
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
        l.setForeground(new Color(150, 208, 177));
        l.setBorder(new EmptyBorder(2, 10, 8, 0));
        return l;
    }

    private void addNavButton(JPanel sidebar, String text, String tag) {
        NavItem item = new NavItem(text, tag);
        JButton b = Theme.navButton(text, SIDEBAR, SIDEBAR_ACTIVE, SIDEBAR_HOVER);
        b.setFont(Theme.font(15, Font.PLAIN));
        b.setMaximumSize(new Dimension(224, 44));
        b.setPreferredSize(new Dimension(224, 44));
        b.addActionListener(e -> navigateTo(item));
        item.button = b;
        navItems.add(item);
        sidebar.add(b);
        sidebar.add(Box.createVerticalStrut(6));
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

        JLabel role = Theme.label(user.getRole().getDisplayName(), 11, Font.BOLD, Theme.ACCENT);
        role.setBorder(new CompoundBorder(
            new MatteBorder(0, 6, 0, 0, Theme.ACCENT),
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

        JLabel title = new JLabel("Welcome to Student Practice Mode");
        title.setFont(Theme.font(30, Font.BOLD));
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(title);

        centerPanel.add(Box.createVerticalStrut(10));

        JLabel subtitle = new JLabel("Practice with ExamForge's Question Bank, review your results and track your performance.");
        subtitle.setFont(Theme.font(14, Font.PLAIN));
        subtitle.setForeground(Theme.MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(subtitle);

        centerPanel.add(Box.createVerticalStrut(30));

        JPanel grid = new JPanel(new GridLayout(1, 3, 16, 16));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(1000, 180));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        grid.add(createFeatureCard("Practice",
            "Practice with questions from the question bank", "PRACTICE", null));
        grid.add(createFeatureCard("My Performance",
            "Analyze your practice performance and progress over time", "PERFORMANCE", null));
        grid.add(createFeatureCard("My Results",
            "View your past practice results and detailed score breakdowns", "RESULTS", null));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(grid, BorderLayout.NORTH);
        centerPanel.add(wrap);

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Pre-selects the "Type" dropdown (Random / Balanced / Weak-Area) inside the
     * practice screen so each dashboard button opens the mode it is named after.
     */
    private boolean selectPracticeType(Container root, String type) {
        for (Component c : root.getComponents()) {
            if (c instanceof JComboBox<?> combo) {
                for (int i = 0; i < combo.getItemCount(); i++) {
                    if (type.equals(combo.getItemAt(i))) {
                        combo.setSelectedIndex(i);
                        return true;
                    }
                }
            }
            if (c instanceof Container child && selectPracticeType(child, type)) {
                return true;
            }
        }
        return false;
    }

    private JComponent createFeatureCard(String title, String desc, String targetTag, String practiceMode) {
        JButton button = new JButton("<html><body style='width: 210px'>"
            + "<span style='font-size: 14px; font-weight: bold; color: #1F2937;'>" + title + "</span>"
            + "<br>"
            + "<span style='font-size: 12px; color: #6C7589;'>" + desc + "</span>"
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
        button.setToolTipText("Click to open " + title);
        button.addChangeListener(e -> {
            ButtonModel m = button.getModel();
            if (m.isPressed()) {
                button.setBackground(Theme.ACCENT_TINT);
            } else if (m.isRollover()) {
                button.setBackground(new Color(246, 252, 248));
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.ACCENT),
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
            navigateTo(findNav(targetTag));
            if (practiceMode != null) {
                selectPracticeType(practicePanel, practiceMode);
            }
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
        JButton button;

        NavItem(String text, String tag) {
            this.text = text;
            this.tag = tag;
        }
    }
}