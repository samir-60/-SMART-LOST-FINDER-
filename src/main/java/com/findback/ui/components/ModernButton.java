package com.findback.ui.components;

import com.findback.ui.FindBackTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Premium Glassy Action Button component.
 * Features realistic specular glass sheen, translucent border glow,
 * keyboard accessibility, tactile press offset, icon + text rendering,
 * and robust enabled/disabled states.
 */
public class ModernButton extends JButton {
    private Color baseColor;
    private Color textColor;
    private boolean hovered = false;
    private boolean pressed = false;
    private int cornerRadius = 12;

    public ModernButton() {
        this("", FindBackTheme.SECONDARY, Color.WHITE);
    }

    public ModernButton(String text) {
        this(text, FindBackTheme.SECONDARY, Color.WHITE);
    }

    public ModernButton(String text, Icon icon) {
        this(text, FindBackTheme.SECONDARY, Color.WHITE);
        setIcon(icon);
    }

    public ModernButton(String text, Color baseColor, Color textColor) {
        super(text);
        this.baseColor = baseColor != null ? baseColor : FindBackTheme.SECONDARY;
        this.textColor = textColor != null ? textColor : Color.WHITE;

        setFont(FindBackTheme.getHeaderFont(13));
        setForeground(this.textColor);
        setBackground(this.baseColor);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(10, 24, 10, 24));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    hovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                pressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    pressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    public Color getBaseColor() {
        return baseColor;
    }

    public void setBaseColor(Color baseColor) {
        this.baseColor = baseColor;
        repaint();
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
        setForeground(textColor);
        repaint();
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        this.baseColor = bg;
        repaint();
    }

    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        this.textColor = fg;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth();
        int h = getHeight();

        // Disabled state
        if (!isEnabled()) {
            Color disabledBg = new Color(226, 232, 240);
            g2.setColor(disabledBg);
            g2.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);
            g2.setColor(new Color(203, 213, 225));
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);

            paintContent(g2, w, h, new Color(148, 163, 184), 0);
            g2.dispose();
            return;
        }

        // Active glassy gradients
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

        // Base glass gradient fill
        GradientPaint glassPaint = new GradientPaint(0, 0, topColor, 0, h, bottomColor);
        g2.setPaint(glassPaint);
        g2.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);

        // Top glossy specular highlight
        GradientPaint highlightPaint = new GradientPaint(
                0, 0, new Color(255, 255, 255, hovered ? 95 : 55),
                0, h / 2.0f, new Color(255, 255, 255, 0)
        );
        g2.setPaint(highlightPaint);
        g2.fillRoundRect(1, 1, w - 2, h / 2, cornerRadius, cornerRadius);

        // Translucent glass border
        Color borderColor = hovered ? new Color(255, 255, 255, 150) : new Color(255, 255, 255, 65);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);

        // Optional focus border
        if (hasFocus()) {
            g2.setColor(new Color(255, 255, 255, 180));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(2, 2, w - 5, h - 5, cornerRadius - 2, cornerRadius - 2);
        }

        int pressOffset = pressed ? 1 : 0;
        paintContent(g2, w, h, textColor, pressOffset);

        g2.dispose();
    }

    private void paintContent(Graphics2D g2, int w, int h, Color renderColor, int pressOffset) {
        String text = getText();
        Icon icon = getIcon();

        Font font = getFont();
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics(font);

        int textWidth = (text != null && !text.isEmpty()) ? fm.stringWidth(text) : 0;
        int iconWidth = (icon != null) ? icon.getIconWidth() : 0;
        int iconHeight = (icon != null) ? icon.getIconHeight() : 0;
        int gap = (iconWidth > 0 && textWidth > 0) ? getIconTextGap() : 0;

        int totalWidth = iconWidth + gap + textWidth;
        int startX = (w - totalWidth) / 2;

        // Draw icon if present
        if (icon != null) {
            int iconY = (h - iconHeight) / 2 + pressOffset;
            icon.paintIcon(this, g2, startX, iconY);
            startX += iconWidth + gap;
        }

        // Draw text
        if (text != null && !text.isEmpty()) {
            int textY = (h + fm.getAscent() - fm.getDescent()) / 2 + pressOffset;

            // Subtle text shadow for high-contrast legibility
            if (renderColor.equals(Color.WHITE)) {
                g2.setColor(new Color(0, 0, 0, 70));
                g2.drawString(text, startX, textY + 1);
            }

            g2.setColor(renderColor);
            g2.drawString(text, startX, textY);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = Math.max(d.height, 42);
        return d;
    }
}
