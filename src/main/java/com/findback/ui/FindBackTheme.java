package com.findback.ui;

import com.formdev.flatlaf.FlatIntelliJLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FindBackTheme {
    public static final Color PRIMARY = new Color(20, 45, 78);
    public static final Color SECONDARY = new Color(37, 99, 235);
    public static final Color BG = new Color(245, 247, 251);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(23, 43, 77);
    public static final Color SUCCESS = new Color(22, 163, 74);
    public static final Color WARNING = new Color(245, 158, 11);
    public static final Color DANGER = new Color(220, 38, 38);

    public static final Color PRIMARY_LIGHT = new Color(40, 70, 120);
    public static final Color BORDER = new Color(220, 225, 235);

    public static void setupLookAndFeel() {
        try {
            FlatIntelliJLaf.setup();
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("TabbedPane.selectedBackground", CARD);
            UIManager.put("Table.alternateRowColor", new Color(248, 250, 252));
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.rowHeight", 40);
            UIManager.put("TableHeader.height", 40);
            UIManager.put("TableHeader.background", BG);
            UIManager.put("TableHeader.foreground", TEXT);
            UIManager.put("TextField.placeholderForeground", new Color(150, 160, 175));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Font getHeaderFont(float size) {
        return new Font("Segoe UI", Font.BOLD, (int) size);
    }

    public static Font getBodyFont(float size) {
        return new Font("Segoe UI", Font.PLAIN, (int) size);
    }

    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text) {
            private boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = hovered ? bg.brighter() : bg;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(fg);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        button.setFont(getBodyFont(14));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 24, 10, 24));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, 40));
        return button;
    }

    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, SECONDARY, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createStyledButton(text, DANGER, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, SUCCESS, Color.WHITE);
    }

    public static JButton createWarningButton(String text) {
        return createStyledButton(text, WARNING, Color.WHITE);
    }

    public static JButton createOutlineButton(String text) {
        JButton button = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hovered) {
                    g2.setColor(new Color(SECONDARY.getRed(), SECONDARY.getGreen(), SECONDARY.getBlue(), 20));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.setColor(SECONDARY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        button.setFont(getBodyFont(14));
        button.setForeground(SECONDARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 24, 10, 24));
        return button;
    }
}
