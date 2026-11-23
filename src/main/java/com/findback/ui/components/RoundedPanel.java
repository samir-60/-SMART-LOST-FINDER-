package com.findback.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedPanel extends JPanel {
    private int cornerRadius;
    private Color bgColor;
    private boolean drawShadow;

    public RoundedPanel(int cornerRadius, Color bgColor) {
        this(cornerRadius, bgColor, false);
    }

    public RoundedPanel(int cornerRadius, Color bgColor, boolean drawShadow) {
        this.cornerRadius = cornerRadius;
        this.bgColor = bgColor;
        this.drawShadow = drawShadow;
        setOpaque(false);
        setLayout(new BorderLayout());
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (drawShadow) {
            g2.setColor(new Color(0, 0, 0, 15));
            g2.fill(new RoundRectangle2D.Float(2, 2, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius));
            g2.setColor(new Color(0, 0, 0, 8));
            g2.fill(new RoundRectangle2D.Float(4, 4, getWidth() - 4, getHeight() - 4, cornerRadius, cornerRadius));
        }

        g2.setColor(bgColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - (drawShadow ? 4 : 0), getHeight() - (drawShadow ? 4 : 0), cornerRadius, cornerRadius));
        g2.dispose();
        super.paintComponent(g);
    }
}
