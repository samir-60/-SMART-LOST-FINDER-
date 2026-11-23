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

        Color c = baseColor;
        if (pressed) {
            c = c.darker();
        } else if (hovered) {
            c = c.brighter();
        }

        g2.setColor(c);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

        g2.setColor(textColor);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
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
