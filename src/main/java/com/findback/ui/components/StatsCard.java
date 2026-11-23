package com.findback.ui.components;

import com.findback.ui.FindBackTheme;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class StatsCard extends JPanel {
    private String icon;
    private String value;
    private String label;
    private Color accentColor;

    public StatsCard(String icon, String value, String label, Color accentColor) {
        this.icon = icon;
        this.value = value;
        this.label = label;
        this.accentColor = accentColor;
        setOpaque(false);
        setPreferredSize(new Dimension(220, 120));
    }

    public void setValue(String value) {
        this.value = value;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Shadow
        g2.setColor(new Color(0, 0, 0, 12));
        g2.fill(new RoundRectangle2D.Float(3, 3, getWidth() - 3, getHeight() - 3, 16, 16));

        // Background
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 16, 16));

        // Accent line at top
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, getWidth() - 4, 4, 16, 16);
        g2.fillRect(0, 4, getWidth() - 4, 2);

        int leftPadding = 20;
        int topPadding = 24;

        // Icon
        g2.setColor(accentColor);
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        g2.drawString(icon, leftPadding, topPadding + 24);

        // Value
        g2.setColor(FindBackTheme.TEXT);
        g2.setFont(FindBackTheme.getHeaderFont(28));
        g2.drawString(value, leftPadding + 45, topPadding + 24);

        // Label
        g2.setColor(new Color(120, 130, 150));
        g2.setFont(FindBackTheme.getBodyFont(13));
        g2.drawString(label, leftPadding, topPadding + 52);

        g2.dispose();
    }
}
