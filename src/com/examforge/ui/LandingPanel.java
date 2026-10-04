package com.examforge.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class LandingPanel extends JPanel {

    public interface ChoiceListener {
        void onStudentLogin();
        void onAdminLogin();
    }

    private ChoiceListener listener;

    public LandingPanel() {
        setLayout(new GridBagLayout());
        setOpaque(true);
        setBackground(new Color(245, 247, 252));

        JPanel card = createCard();
        add(card);
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

    private JPanel createCard() {
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
        panel.setPreferredSize(new Dimension(520, 430));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            new javax.swing.border.EmptyBorder(36, 52, 36, 52)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("ExamForge");
        brand.setFont(Theme.font(30, Font.BOLD));
        brand.setForeground(Theme.BRAND_DARK);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(brand);

        panel.add(Box.createVerticalStrut(6));

        JLabel subtitle = new JLabel("Constraint-Based Paper Design System");
        subtitle.setFont(Theme.font(13, Font.PLAIN));
        subtitle.setForeground(Theme.MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(subtitle);

        panel.add(Box.createVerticalStrut(26));

        JLabel rule = new JLabel();
        rule.setPreferredSize(new Dimension(200, 2));
        rule.setMaximumSize(new Dimension(200, 2));
        rule.setOpaque(true);
        rule.setBackground(new Color(223, 228, 240));
        rule.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(rule);

        panel.add(Box.createVerticalStrut(24));

        JLabel prompt = new JLabel("Choose how you want to continue");
        prompt.setFont(Theme.font(15, Font.BOLD));
        prompt.setForeground(Theme.TEXT);
        prompt.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(prompt);

        panel.add(Box.createVerticalStrut(18));

        JButton studentButton = new Theme.RoundedButton("Student Login", Theme.ACCENT);
        studentButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        studentButton.setMaximumSize(new Dimension(330, 50));
        studentButton.setPreferredSize(new Dimension(330, 50));
        studentButton.addActionListener(e -> {
            if (listener != null) listener.onStudentLogin();
        });
        panel.add(studentButton);

        panel.add(Box.createVerticalStrut(12));

        JButton adminButton = new Theme.RoundedButton("Admin / Teacher Login", Theme.BRAND);
        adminButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        adminButton.setMaximumSize(new Dimension(330, 50));
        adminButton.setPreferredSize(new Dimension(330, 50));
        adminButton.addActionListener(e -> {
            if (listener != null) listener.onAdminLogin();
        });
        panel.add(adminButton);

        return panel;
    }

    public void setChoiceListener(ChoiceListener listener) {
        this.listener = listener;
    }
}