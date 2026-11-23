package com.findback.ui;

import com.findback.model.User;
import com.findback.service.NotificationService;
import com.findback.service.SessionManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Modern SaaS Navigation Sidebar with glassy hover effects and crisp vector icons.
 * Eliminates missing Unicode glyphs (boxes) across all monitor types and operating systems.
 */
public class SidebarPanel extends JPanel {
    private final MainFrame mainFrame;
    private final Map<String, JPanel> navButtons = new LinkedHashMap<>();
    private String activePanel = MainFrame.DASHBOARD;

    public SidebarPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setPreferredSize(new Dimension(230, 0));
        setBackground(FindBackTheme.PRIMARY);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        // Logo area with vector magnifier
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(22, 16, 12, 0));

        JComponent logoIcon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FindBackTheme.SECONDARY);
                g2.fillOval(0, 0, 24, 24);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(5, 5, 8, 8);
                g2.drawLine(12, 12, 17, 17);
                g2.dispose();
            }
            @Override
            public Dimension getPreferredSize() { return new Dimension(24, 24); }
        };

        JLabel logoLabel = new JLabel(" FindBack");
        logoLabel.setFont(FindBackTheme.getHeaderFont(21));
        logoLabel.setForeground(Color.WHITE);

        logoPanel.add(logoIcon);
        logoPanel.add(logoLabel);
        topPanel.add(logoPanel);

        // User info with glass card
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            JPanel userPanel = new JPanel(new BorderLayout(12, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 12));
                    g2.fillRoundRect(12, 0, getWidth() - 24, getHeight(), 10, 10);
                    g2.setColor(new Color(255, 255, 255, 25));
                    g2.drawRoundRect(12, 0, getWidth() - 25, getHeight() - 1, 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            userPanel.setOpaque(false);
            userPanel.setBorder(new EmptyBorder(10, 24, 10, 24));

            // Avatar circle
            JLabel avatar = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(FindBackTheme.SECONDARY);
                    g2.fillOval(0, 0, 36, 36);
                    g2.setColor(Color.WHITE);
                    g2.setFont(FindBackTheme.getHeaderFont(15));
                    String name = user.getFullName();
                    String initial = (name != null && !name.trim().isEmpty())
                            ? name.trim().substring(0, 1).toUpperCase()
                            : "U";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(initial, (36 - fm.stringWidth(initial)) / 2, (36 + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(36, 36);
                }
            };
            userPanel.add(avatar, BorderLayout.WEST);

            JPanel namePanel = new JPanel();
            namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
            namePanel.setOpaque(false);
            JLabel nameLabel = new JLabel(user.getFullName() != null ? user.getFullName() : "User");
            nameLabel.setFont(FindBackTheme.getHeaderFont(13));
            nameLabel.setForeground(Color.WHITE);
            namePanel.add(nameLabel);
            String roleName = user.getRole() != null ? user.getRole().name() : "ROLE_STUDENT";
            JLabel roleLabel = new JLabel(formatRole(roleName));
            roleLabel.setFont(FindBackTheme.getBodyFont(11));
            roleLabel.setForeground(new Color(147, 197, 253));
            namePanel.add(roleLabel);
            userPanel.add(namePanel, BorderLayout.CENTER);

            topPanel.add(userPanel);
        }

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 30));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        topPanel.add(Box.createVerticalStrut(8));
        topPanel.add(sep);
        topPanel.add(Box.createVerticalStrut(10));

        // Navigation buttons with vector icon IDs
        addNavButton(topPanel, "dashboard", "Dashboard", MainFrame.DASHBOARD);
        addNavButton(topPanel, "report_lost", "Report Lost Item", MainFrame.REPORT_LOST);
        addNavButton(topPanel, "report_found", "Report Found Item", MainFrame.REPORT_FOUND);
        addNavButton(topPanel, "browse", "Browse Items", MainFrame.BROWSE);
        addNavButton(topPanel, "matches", "Smart Matches", MainFrame.MATCHES);
        addNavButton(topPanel, "claims", "My Claims", MainFrame.CLAIMS);

        // Notifications with badge
        long unreadCount = 0;
        try {
            if (user != null) {
                NotificationService ns = new NotificationService();
                unreadCount = ns.getUnreadCount(user.getId());
            }
        } catch (Exception ignored) {}
        String notifLabel = "Notifications" + (unreadCount > 0 ? " (" + unreadCount + ")" : "");
        addNavButton(topPanel, "notifications", notifLabel, MainFrame.NOTIFICATIONS);

        addNavButton(topPanel, "profile", "My Profile", MainFrame.PROFILE);

        // Admin section
        if (user != null && user.isStaffOrAdmin()) {
            topPanel.add(Box.createVerticalStrut(12));
            JSeparator sep2 = new JSeparator();
            sep2.setForeground(new Color(255, 255, 255, 30));
            sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            topPanel.add(sep2);
            topPanel.add(Box.createVerticalStrut(8));

            JLabel adminLabel = new JLabel("  ADMINISTRATION");
            adminLabel.setFont(FindBackTheme.getHeaderFont(10));
            adminLabel.setForeground(new Color(147, 197, 253, 180));
            adminLabel.setBorder(new EmptyBorder(0, 18, 6, 0));
            adminLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            topPanel.add(adminLabel);

            addNavButton(topPanel, "admin_dashboard", "Admin Dashboard", MainFrame.ADMIN_DASHBOARD);
            addNavButton(topPanel, "admin_reports", "Manage Reports", MainFrame.ADMIN_REPORTS);
            addNavButton(topPanel, "admin_claims", "Manage Claims", MainFrame.ADMIN_CLAIMS);
            addNavButton(topPanel, "admin_users", "Manage Users", MainFrame.ADMIN_USERS);
            addNavButton(topPanel, "admin_categories", "Categories", MainFrame.ADMIN_CATEGORIES);
            addNavButton(topPanel, "admin_locations", "Locations", MainFrame.ADMIN_LOCATIONS);
            addNavButton(topPanel, "admin_handovers", "Handovers", MainFrame.ADMIN_HANDOVERS);
            addNavButton(topPanel, "admin_analytics", "Analytics", MainFrame.ADMIN_ANALYTICS);
        }

        JScrollPane scrollPane = new JScrollPane(topPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        // Logout button at bottom with glassy red styling and crisp vector icon
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 10, 20, 10));

        JButton logoutBtn = new JButton("Logout") {
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
                int w = getWidth();
                int h = getHeight();

                Color bg = FindBackTheme.DANGER;
                Color topC = pressed ? bg.darker() : (hovered ? new Color(239, 68, 68) : new Color(220, 38, 38));
                Color botC = pressed ? bg.darker().darker() : (hovered ? new Color(185, 28, 28) : new Color(153, 27, 27));

                g2.setPaint(new GradientPaint(0, 0, topC, 0, h, botC));
                g2.fillRoundRect(0, 0, w, h, 12, 12);

                g2.setColor(new Color(255, 255, 255, hovered ? 80 : 40));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

                // Vector power/exit icon
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f));
                int iconX = 42;
                int iconY = (h - 14) / 2;
                g2.drawArc(iconX, iconY + 1, 12, 12, -45, 270);
                g2.drawLine(iconX + 6, iconY - 1, iconX + 6, iconY + 6);

                // Text
                g2.setFont(FindBackTheme.getHeaderFont(13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("Logout", iconX + 22, (h + fm.getAscent() - fm.getDescent()) / 2 + (pressed ? 1 : 0));
                g2.dispose();
            }
        };
        logoutBtn.setPreferredSize(new Dimension(190, 42));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setOpaque(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame,
                    "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                mainFrame.logout();
            }
        });
        bottomPanel.add(logoutBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addNavButton(JPanel parent, String iconKey, String text, String panelName) {
        JPanel btn = new JPanel(new BorderLayout(12, 0)) {
            boolean hovered = false;
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

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        mainFrame.showPanel(panelName);
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isActive = panelName.equals(activePanel);
                int w = getWidth();
                int h = getHeight();

                if (isActive) {
                    // Glassy vibrant selection pill
                    GradientPaint selPaint = new GradientPaint(
                            10, 0, new Color(37, 99, 235, 200),
                            w - 20, 0, new Color(59, 130, 246, 170)
                    );
                    g2.setPaint(selPaint);
                    g2.fillRoundRect(10, 2, w - 20, h - 4, 10, 10);

                    // Accent left indicator
                    g2.setColor(new Color(147, 197, 253));
                    g2.fillRoundRect(2, 6, 4, h - 12, 4, 4);

                    // Delicate glass border
                    g2.setColor(new Color(255, 255, 255, 90));
                    g2.setStroke(new BasicStroke(1.1f));
                    g2.drawRoundRect(10, 2, w - 21, h - 5, 10, 10);
                } else if (hovered) {
                    // Translucent glass hover
                    g2.setColor(new Color(255, 255, 255, 22));
                    g2.fillRoundRect(10, 2, w - 20, h - 4, 10, 10);
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.drawRoundRect(10, 2, w - 21, h - 5, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(230, 42));
        btn.setBorder(new EmptyBorder(0, 24, 0, 12));

        // Crisp vector icon component
        JComponent iconComp = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isActive = panelName.equals(activePanel);
                Color c = isActive ? Color.WHITE : new Color(200, 220, 250);
                g2.setColor(c);
                paintVectorIcon(g2, iconKey, 0, (getHeight() - 18) / 2, 18, 18);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(20, 20);
            }
        };
        btn.add(iconComp, BorderLayout.WEST);

        JLabel label = new JLabel(text);
        label.setFont(FindBackTheme.getBodyFont(13));
        label.setForeground(Color.WHITE);
        btn.add(label, BorderLayout.CENTER);

        navButtons.put(panelName, btn);
        parent.add(btn);
        parent.add(Box.createVerticalStrut(3));
    }

    private void paintVectorIcon(Graphics2D g2, String iconKey, int x, int y, int w, int h) {
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (iconKey) {
            case "dashboard":
                g2.drawRoundRect(x + 1, y + 1, 6, 6, 2, 2);
                g2.drawRoundRect(x + 9, y + 1, 6, 6, 2, 2);
                g2.drawRoundRect(x + 1, y + 9, 6, 6, 2, 2);
                g2.drawRoundRect(x + 9, y + 9, 6, 6, 2, 2);
                break;
            case "report_lost":
                g2.drawRoundRect(x + 2, y + 1, 12, 15, 2, 2);
                g2.drawLine(x + 5, y + 5, x + 11, y + 5);
                g2.drawLine(x + 5, y + 8, x + 11, y + 8);
                g2.drawLine(x + 5, y + 11, x + 9, y + 11);
                break;
            case "report_found":
                g2.drawRect(x + 1, y + 4, 15, 11);
                g2.drawLine(x + 1, y + 8, x + 16, y + 8);
                g2.drawLine(x + 8, y + 8, x + 8, y + 15);
                g2.drawArc(x + 3, y + 1, 5, 4, 0, 180);
                g2.drawArc(x + 8, y + 1, 5, 4, 0, 180);
                break;
            case "browse":
                g2.drawOval(x + 2, y + 2, 9, 9);
                g2.drawLine(x + 9, y + 9, x + 15, y + 15);
                break;
            case "matches":
                Path2D.Float star = new Path2D.Float();
                star.moveTo(x + 8, y + 1);
                star.lineTo(x + 10, y + 6);
                star.lineTo(x + 15, y + 8);
                star.lineTo(x + 10, y + 10);
                star.lineTo(x + 8, y + 15);
                star.lineTo(x + 6, y + 10);
                star.lineTo(x + 1, y + 8);
                star.lineTo(x + 6, y + 6);
                star.closePath();
                g2.draw(star);
                break;
            case "claims":
                g2.drawRoundRect(x + 2, y + 2, 12, 14, 2, 2);
                g2.drawLine(x + 5, y + 10, x + 7, y + 12);
                g2.drawLine(x + 7, y + 12, x + 11, y + 7);
                break;
            case "notifications":
                g2.drawArc(x + 3, y + 2, 10, 10, 0, 180);
                g2.drawLine(x + 3, y + 7, x + 1, y + 12);
                g2.drawLine(x + 13, y + 7, x + 15, y + 12);
                g2.drawLine(x + 1, y + 12, x + 15, y + 12);
                g2.fillOval(x + 6, y + 13, 4, 3);
                break;
            case "profile":
                g2.drawOval(x + 4, y + 2, 8, 8);
                g2.drawArc(x + 1, y + 10, 14, 10, 0, 180);
                break;
            case "admin_dashboard":
                g2.drawArc(x + 1, y + 2, 14, 14, 0, 180);
                g2.drawLine(x + 8, y + 9, x + 12, y + 5);
                g2.drawLine(x + 1, y + 9, x + 15, y + 9);
                break;
            case "admin_reports":
                g2.drawRect(x + 4, y + 1, 10, 12);
                g2.drawRect(x + 1, y + 4, 10, 12);
                break;
            case "admin_claims":
                g2.drawRoundRect(x + 2, y + 1, 12, 13, 4, 4);
                g2.drawLine(x + 5, y + 8, x + 7, y + 10);
                g2.drawLine(x + 7, y + 10, x + 11, y + 5);
                break;
            case "admin_users":
                g2.drawOval(x + 2, y + 2, 6, 6);
                g2.drawArc(x, y + 8, 10, 8, 0, 180);
                g2.drawOval(x + 9, y + 3, 5, 5);
                g2.drawArc(x + 8, y + 9, 8, 7, 0, 180);
                break;
            case "admin_categories":
                g2.drawPolygon(new int[]{x+1, x+7, x+15, x+9, x+1}, new int[]{x+7, x+1, x+9, x+15, x+15}, 5);
                g2.fillOval(x + 3, y + 9, 3, 3);
                break;
            case "admin_locations":
                g2.drawOval(x + 4, y + 1, 8, 8);
                g2.drawLine(x + 8, y + 9, x + 8, y + 15);
                g2.fillOval(x + 6, y + 3, 4, 4);
                break;
            case "admin_handovers":
                g2.drawOval(x + 1, y + 4, 7, 7);
                g2.drawOval(x + 8, y + 4, 7, 7);
                g2.drawLine(x + 4, y + 13, x + 12, y + 13);
                break;
            case "admin_analytics":
                g2.fillRect(x + 2, y + 9, 3, 6);
                g2.fillRect(x + 7, y + 5, 3, 10);
                g2.fillRect(x + 12, y + 2, 3, 13);
                g2.drawLine(x, y + 15, x + 16, y + 15);
                break;
            default:
                g2.fillOval(x + 5, y + 5, 6, 6);
                break;
        }
    }

    public void setActiveButton(String panelName) {
        this.activePanel = panelName;
        repaint();
    }

    private String formatRole(String role) {
        if (role == null || role.trim().isEmpty()) return "Student";
        String clean = role.replace("ROLE_", "").trim();
        if (clean.isEmpty()) return "Student";
        if (clean.length() == 1) return clean.toUpperCase();
        return clean.substring(0, 1).toUpperCase() + clean.substring(1).toLowerCase();
    }
}
