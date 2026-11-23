package com.findback.ui.components;

import com.findback.ui.FindBackTheme;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Premium Modern Statistics Card.
 * Uses 100% Java2D vector geometric icons instead of emoji characters,
 * ensuring no unrendered hollow square boxes across any OS or Java version.
 */
public class StatsCard extends JPanel {
    private String iconKey;
    private String value;
    private String label;
    private Color accentColor;

    public StatsCard(String iconKey, String value, String label, Color accentColor) {
        this.iconKey = iconKey != null ? iconKey.toLowerCase() : "box";
        this.value = value != null ? value : "0";
        this.label = label != null ? label : "";
        this.accentColor = accentColor != null ? accentColor : FindBackTheme.SECONDARY;
        setOpaque(false);
        setPreferredSize(new Dimension(220, 110));
    }

    public void setValue(String value) {
        this.value = value != null ? value : "0";
        repaint();
    }

    public void setLabel(String label) {
        this.label = label;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth();
        int h = getHeight();

        // Soft drop shadow
        g2.setColor(new Color(0, 0, 0, 10));
        g2.fill(new RoundRectangle2D.Float(2, 3, w - 4, h - 4, 16, 16));

        // Card surface
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w - 2, h - 2, 16, 16));

        // Delicate border
        g2.setColor(new Color(226, 232, 240));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 3, h - 3, 16, 16));

        // Top accent stripe
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, w - 2, 4, 16, 16);
        g2.fillRect(0, 3, w - 2, 2);

        // Vector Icon Pill Circle
        int iconX = 18;
        int iconY = 20;
        int pillSize = 38;

        g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 26));
        g2.fillOval(iconX, iconY, pillSize, pillSize);

        g2.setColor(accentColor);
        paintVectorIcon(g2, iconKey, iconX + 10, iconY + 10, 18);

        // Value text
        g2.setColor(FindBackTheme.TEXT);
        g2.setFont(FindBackTheme.getHeaderFont(26));
        g2.drawString(value, iconX + pillSize + 14, iconY + 28);

        // Label text
        g2.setColor(new Color(100, 115, 130));
        g2.setFont(FindBackTheme.getBodyFont(13));
        g2.drawString(label, iconX, iconY + pillSize + 22);

        g2.dispose();
    }

    private void paintVectorIcon(Graphics2D g2, String key, int x, int y, int size) {
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (key.contains("search") || key.contains("found") || key.contains("browse")) {
            // Magnifying Glass
            int r = size - 7;
            g2.drawOval(x, y, r, r);
            g2.drawLine(x + r - 1, y + r - 1, x + size - 1, y + size - 1);
        } else if (key.contains("target") || key.contains("match")) {
            // Target Crosshair
            g2.drawOval(x, y, size - 2, size - 2);
            g2.drawOval(x + 4, y + 4, size - 10, size - 10);
            g2.drawLine(x + size / 2 - 1, y - 1, x + size / 2 - 1, y + 3);
            g2.drawLine(x + size / 2 - 1, y + size - 5, x + size / 2 - 1, y + size - 1);
            g2.drawLine(x - 1, y + size / 2 - 1, x + 3, y + size / 2 - 1);
            g2.drawLine(x + size - 5, y + size / 2 - 1, x + size - 1, y + size / 2 - 1);
        } else if (key.contains("check") || key.contains("recover") || key.contains("success")) {
            // Checkmark Circle
            g2.drawOval(x, y, size - 2, size - 2);
            g2.drawLine(x + 4, y + size / 2, x + size / 2 - 1, y + size - 6);
            g2.drawLine(x + size / 2 - 1, y + size - 6, x + size - 5, y + 5);
        } else if (key.contains("user") || key.contains("profile") || key.contains("student")) {
            // User Silhouette
            g2.drawOval(x + 4, y, size - 10, size - 10);
            g2.drawArc(x + 1, y + size - 7, size - 4, 12, 0, 180);
        } else if (key.contains("lock") || key.contains("claim")) {
            // Padlock
            g2.drawRoundRect(x + 2, y + 6, size - 6, size - 7, 4, 4);
            g2.drawArc(x + 4, y, size - 10, 12, 0, 180);
        } else {
            // Package / Box (Default)
            g2.drawRect(x + 1, y + 5, size - 4, size - 6);
            g2.drawLine(x + 1, y + 5, x + size / 2 - 1, y);
            g2.drawLine(x + size - 3, y + 5, x + size / 2 - 1, y);
            g2.drawLine(x + size / 2 - 1, y + 5, x + size / 2 - 1, y + size - 2);
        }
    }
}
