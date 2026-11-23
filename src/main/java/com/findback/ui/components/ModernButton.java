package com.findback.ui.components;

import com.findback.ui.FindBackTheme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class ModernButton extends JButton {
    private Color baseColor;
    private Color textColor;
    private boolean hovered = false;
    private boolean pressed = false;
    private int cornerRadius = 10;

    public ModernButton(String text) {
        this(text, FindBackTheme.SECONDARY, Color.WHITE);
    }

    public ModernButton(String text, Color baseColor, Color textColor) {
        super(text);
        this.baseColor = baseColor;
        this.textColor = textColor;
        setFont(FindBackTheme.getBodyFont(14));
        setForeground(textColor);
        setBackground(baseColor);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(10, 24, 10, 24));

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

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth();
        int h = getHeight();

        Color topColor;
        Color bottomColor;

        if (pressed) {
            topColor = baseColor.darker();
            bottomColor = baseColor.darker().darker();
        } else if (hovered) {
            topColor = new Color(
                    Math.min(255, baseColor.getRed() + 25),
                    Math.min(255, baseColor.getGreen() + 25),
                    Math.min(255, baseColor.getBlue() + 30)
            );
            bottomColor = baseColor;
        } else {
            topColor = new Color(
                    Math.min(255, baseColor.getRed() + 10),
                    Math.min(255, baseColor.getGreen() + 10),
                    Math.min(255, baseColor.getBlue() + 15)
            );
            bottomColor = new Color(
                    Math.max(0, baseColor.getRed() - 15),
                    Math.max(0, baseColor.getGreen() - 15),
                    Math.max(0, baseColor.getBlue() - 15)
            );
        }

        // Render glass body
        GradientPaint glassPaint = new GradientPaint(0, 0, topColor, 0, h, bottomColor);
        g2.setPaint(glassPaint);
        g2.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);

        // Top glossy specular highlight
        GradientPaint highlightPaint = new GradientPaint(
                0, 0, new Color(255, 255, 255, hovered ? 90 : 50),
                0, h / 2.0f, new Color(255, 255, 255, 0)
        );
        g2.setPaint(highlightPaint);
        g2.fillRoundRect(1, 1, w - 2, h / 2, cornerRadius, cornerRadius);

        // Glassy translucent border
        Color borderColor = hovered ? new Color(255, 255, 255, 140) : new Color(255, 255, 255, 60);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);

        // Text
        g2.setColor(textColor);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (w - fm.stringWidth(getText())) / 2;
        int y = (h + fm.getAscent() - fm.getDescent()) / 2 + (pressed ? 1 : 0);
        g2.drawString(getText(), x, y);
        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = Math.max(d.height, 40);
        return d;
    }
}
