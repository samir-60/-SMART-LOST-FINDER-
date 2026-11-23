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
            private boolean pressed = false;

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
                        pressed = false;
                        repaint();
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        pressed = true;
                        repaint();
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        pressed = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                int w = getWidth();
                int h = getHeight();
                int arc = 12;

                // Base glassy gradient
                Color topColor;
                Color bottomColor;

                if (pressed) {
                    topColor = bg.darker();
                    bottomColor = bg.darker().darker();
                } else if (hovered) {
                    topColor = new Color(
                            Math.min(255, bg.getRed() + 25),
                            Math.min(255, bg.getGreen() + 25),
                            Math.min(255, bg.getBlue() + 30)
                    );
                    bottomColor = bg;
                } else {
                    topColor = new Color(
                            Math.min(255, bg.getRed() + 10),
                            Math.min(255, bg.getGreen() + 10),
                            Math.min(255, bg.getBlue() + 15)
                    );
                    bottomColor = new Color(
                            Math.max(0, bg.getRed() - 15),
                            Math.max(0, bg.getGreen() - 15),
                            Math.max(0, bg.getBlue() - 15)
                    );
                }

                // Render glass body
                GradientPaint glassPaint = new GradientPaint(0, 0, topColor, 0, h, bottomColor);
                g2.setPaint(glassPaint);
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                // Top glossy specular highlight
                GradientPaint highlightPaint = new GradientPaint(
                        0, 0, new Color(255, 255, 255, hovered ? 90 : 50),
                        0, h / 2.0f, new Color(255, 255, 255, 0)
                );
                g2.setPaint(highlightPaint);
                g2.fillRoundRect(1, 1, w - 2, h / 2, arc, arc);

                // Glassy translucent border
                Color borderColor = hovered ? new Color(255, 255, 255, 140) : new Color(255, 255, 255, 60);
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

                // Text with subtle shadow for readability
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = (h + fm.getAscent() - fm.getDescent()) / 2 + (pressed ? 1 : 0);

                if (fg.equals(Color.WHITE)) {
                    g2.setColor(new Color(0, 0, 0, 80));
                    g2.drawString(getText(), x, y + 1);
                }
                g2.setColor(fg);
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        button.setFont(getHeaderFont(13));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 24, 10, 24));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, 42));
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
            private boolean pressed = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hovered = false; pressed = false; repaint(); }
                    @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                    @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                int w = getWidth();
                int h = getHeight();
                int arc = 12;

                // Glass backdrop
                Color glassBg = pressed ? new Color(255, 255, 255, 30)
                        : (hovered ? new Color(255, 255, 255, 20) : new Color(255, 255, 255, 8));
                g2.setColor(glassBg);
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                // Glossy reflection line
                GradientPaint shine = new GradientPaint(
                        0, 0, new Color(255, 255, 255, hovered ? 60 : 30),
                        0, h / 2.0f, new Color(255, 255, 255, 0)
                );
                g2.setPaint(shine);
                g2.fillRoundRect(1, 1, w - 2, h / 2, arc, arc);

                // Border
                Color strokeColor = hovered ? new Color(147, 197, 253, 220) : new Color(255, 255, 255, 80);
                g2.setColor(strokeColor);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = (h + fm.getAscent() - fm.getDescent()) / 2 + (pressed ? 1 : 0);
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        button.setFont(getHeaderFont(13));
        button.setForeground(SECONDARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 24, 10, 24));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, 42));
        return button;
    }
}
