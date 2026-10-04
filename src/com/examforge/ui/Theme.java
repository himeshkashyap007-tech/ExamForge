package com.examforge.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Central visual system for the ExamForge portal.
 * Pure presentation helpers - no behaviour, data or routing lives here.
 */
public final class Theme {

    private Theme() {}

    /* ---------------- Palette ---------------- */
    public static final Color BG         = new Color(244, 246, 251);   // app background
    public static final Color SURFACE    = Color.WHITE;                // cards / surfaces
    public static final Color BORDER     = new Color(226, 231, 240);   // hairline borders
    public static final Color TEXT       = new Color(31, 41, 55);      // primary text
    public static final Color MUTED      = new Color(108, 117, 137);   // secondary text
    public static final Color BRAND      = new Color(52, 83, 154);     // indigo (admin / teacher)
    public static final Color BRAND_DARK = new Color(36, 56, 110);
    public static final Color BRAND_TINT = new Color(233, 238, 249);
    public static final Color ACCENT     = new Color(33, 133, 87);     // emerald (student)
    public static final Color ACCENT_DARK= new Color(20, 95, 60);
    public static final Color ACCENT_TINT= new Color(232, 245, 238);
    public static final Color DANGER     = new Color(198, 78, 70);
    public static final Color WARNING    = new Color(198, 148, 41);
    public static final Color SUCCESS    = new Color(47, 143, 91);
    public static final Color MUTED_OUT  = new Color(150, 158, 170);   // disabled fills

    /* ---------------- Typography ---------------- */
    public static final String FONT_FAMILY = "Segoe UI";

    public static Font font(int px, int style) {
        return new Font(FONT_FAMILY, style, px);
    }

    public static JLabel label(String text, int px, int style, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(font(px, style));
        l.setForeground(fg);
        return l;
    }

    /* ---------------- Headers ---------------- */
    public static JPanel header(String title, JComponent leftExtra, JComponent rightExtra) {
        JPanel band = new JPanel(new BorderLayout());
        band.setBackground(SURFACE);
        band.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(12, 16, 12, 16)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        if (leftExtra != null) left.add(leftExtra);
        left.add(label(title, 19, Font.BOLD, TEXT));
        band.add(left, BorderLayout.WEST);

        if (rightExtra != null) {
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            right.setOpaque(false);
            right.add(rightExtra);
            band.add(right, BorderLayout.EAST);
        }
        return band;
    }

    public static JPanel filterBar(JComponent... items) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(SURFACE);
        bar.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(4, 16, 8, 16)));
        for (JComponent c : items) bar.add(c);
        return bar;
    }

    /* ---------------- Buttons ---------------- */
    public static JButton button(String text) {
        return new RoundedButton(text, BRAND);
    }

    public static JButton button(String text, Color base) {
        return new RoundedButton(text, base);
    }

    public static JButton button(String text, Color base, Runnable action) {
        JButton btn = new RoundedButton(text, base);
        btn.addActionListener(e -> action.run());
        return btn;
    }

    public static JButton softButton(String text) {
        return new RoundedButton(text, new Color(238, 241, 247), Color.WHITE, true);
    }

    public static JButton backButton() {
        JButton b = new RoundedButton("\u2190  Back", BRAND_DARK, Color.WHITE, false);
        b.setFont(font(15, Font.BOLD));
        b.setMargin(new Insets(11, 20, 11, 20));
        return b;
    }

    /* ---------------- Sidebar navigation buttons ---------------- */
    public static JButton navButton(String text, Color base, Color activeBg, Color hover) {
        return new NavButton(text, base, activeBg, hover);
    }

    /* ---------------- Fields / combos ---------------- */
    public static void styleField(JComponent c) {
        Border normal = new CompoundBorder(new LineBorder(BORDER),
            new EmptyBorder(7, 9, 7, 9));
        Border focused = new CompoundBorder(new LineBorder(BRAND),
            new EmptyBorder(7, 9, 7, 9));
        c.setFont(font(13, Font.PLAIN));
        c.setBackground(SURFACE);
        c.setBorder(normal);
        c.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { c.setBorder(focused); }
            @Override public void focusLost(FocusEvent e) { c.setBorder(normal); }
        });
    }

    public static void styleFields(JComponent... fields) {
        for (JComponent f : fields) styleField(f);
    }

    /* ---------------- Tables ---------------- */
    public static void styleTable(JTable table) {
        styleTable(table, BRAND_TINT);
    }

    public static void styleTable(JTable table, Color selectionTint) {
        table.setRowHeight(30);
        table.setFont(font(13, Font.PLAIN));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(242, 244, 250));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(selectionTint);
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);

        DefaultTableCellRenderer cell = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(0, 9, 0, 9));
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? new Color(250, 251, 254) : SURFACE);
                    setForeground(TEXT);
                }
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, cell);

        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 34));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                l.setBackground(new Color(234, 238, 248));
                l.setForeground(BRAND_DARK);
                l.setFont(font(12, Font.BOLD));
                l.setBorder(new EmptyBorder(0, 9, 0, 9));
                return l;
            }
        });
    }

    /* ---------------- Cards / stat cards ---------------- */
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(SURFACE);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER),
            new EmptyBorder(14, 16, 14, 16)));
        return p;
    }

    public static JPanel statCard(Color accent, String value, String label) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(SURFACE);
        card.setBorder(new CompoundBorder(
            new MatteBorder(0, 4, 0, 0, accent),
            new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(16, 16, 16, 16))));
        JLabel val = new JLabel(value);
        val.setFont(font(27, Font.BOLD));
        val.setForeground(accent);
        JLabel name = new JLabel(label);
        name.setFont(font(12, Font.PLAIN));
        name.setForeground(MUTED);
        card.add(val, BorderLayout.CENTER);
        card.add(name, BorderLayout.SOUTH);
        return card;
    }

    /* ---------------- Rounded button ---------------- */
    public static class RoundedButton extends JButton {
        private Color currentBg;
        private final Color base;
        private final Color over;
        private final Color pressed;
        private final Color fg;

        public RoundedButton(String text, Color base) {
            this(text, base, Color.WHITE, false);
        }

        public RoundedButton(String text, Color base, Color foreground, boolean outline) {
            super(text);
            this.base = base;
            this.fg = foreground;
            this.over = blend(base, outline ? new Color(52, 83, 154) : Color.WHITE, outline ? 0.10 : 0.14);
            this.pressed = blend(base, Color.BLACK, 0.16);
            this.currentBg = base;
            setForeground(foreground);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(true);
            setBorder(outline
                ? BorderFactory.createLineBorder(base)
                : BorderFactory.createEmptyBorder());
            setFocusPainted(false);
            setFont(font(14, Font.BOLD));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(10, 18, 10, 18));
            addChangeListener(e -> {
                ButtonModel m = getModel();
                if (!isEnabled()) currentBg = new Color(226, 230, 238);
                else if (m.isPressed()) currentBg = pressed;
                else if (m.isRollover()) currentBg = over;
                else currentBg = base;
                repaint();
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /* ---------------- Sidebar nav item ---------------- */
    public static class NavButton extends JButton {
        private final Color base;
        private final Color activeBg;
        private final Color hover;
        private final Color pressed;
        private final Color fg;
        private Color currentBg;
        private boolean active;

        public NavButton(String text, Color base, Color activeBg, Color hover) {
            super(text);
            this.base = base;
            this.activeBg = activeBg;
            this.hover = hover;
            this.pressed = blend(base, Color.BLACK, 0.16);
            this.fg = Color.WHITE;
            this.currentBg = base;
            setForeground(fg);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(0, 16, 0, 10));
            addChangeListener(e -> {
                ButtonModel m = getModel();
                if (active) currentBg = activeBg;
                else if (m.isPressed()) currentBg = pressed;
                else if (m.isRollover()) currentBg = hover;
                else currentBg = base;
                repaint();
            });
        }

        public void setActive(boolean a) {
            this.active = a;
            currentBg = a ? activeBg : base;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /* ---------------- Helpers ---------------- */
    private static Color blend(Color c, Color target, double f) {
        return new Color(
            (int) Math.round(c.getRed() + (target.getRed() - c.getRed()) * f),
            (int) Math.round(c.getGreen() + (target.getGreen() - c.getGreen()) * f),
            (int) Math.round(c.getBlue() + (target.getBlue() - c.getBlue()) * f));
    }

    public static Color ratingColor(String rating) {
        if ("EXCELLENT".equals(rating)) return ACCENT;
        if ("GOOD".equals(rating)) return new Color(90, 150, 90);
        if ("AVERAGE".equals(rating)) return WARNING;
        return DANGER;
    }
}