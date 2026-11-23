package com.findback.ui;

import com.findback.model.User;
import com.findback.service.NotificationService;
import com.findback.service.SessionManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class SidebarPanel extends JPanel {
    private final MainFrame mainFrame;
    private final Map<String, JPanel> navButtons = new LinkedHashMap<>();
    private String activePanel = MainFrame.DASHBOARD;

    public SidebarPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setPreferredSize(new Dimension(220, 0));
        setBackground(FindBackTheme.PRIMARY);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        // Logo area
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(20, 4, 10, 0));
        JLabel logoLabel = new JLabel("\uD83D\uDD0D FindBack");
        logoLabel.setFont(FindBackTheme.getHeaderFont(22));
        logoLabel.setForeground(Color.WHITE);
        logoPanel.add(logoLabel);
        topPanel.add(logoPanel);

        // User info
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            JPanel userPanel = new JPanel(new BorderLayout(10, 0));
            userPanel.setOpaque(false);
            userPanel.setBorder(new EmptyBorder(10, 20, 15, 20));

            // Avatar circle
            JLabel avatar = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(FindBackTheme.SECONDARY);
                    g2.fillOval(0, 0, 36, 36);
                    g2.setColor(Color.WHITE);
                    g2.setFont(FindBackTheme.getHeaderFont(16));
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
            nameLabel.setFont(FindBackTheme.getBodyFont(13));
            nameLabel.setForeground(Color.WHITE);
            namePanel.add(nameLabel);
            String roleName = user.getRole() != null ? user.getRole().name() : "ROLE_STUDENT";
            JLabel roleLabel = new JLabel(formatRole(roleName));
            roleLabel.setFont(FindBackTheme.getBodyFont(11));
            roleLabel.setForeground(new Color(140, 170, 210));
            namePanel.add(roleLabel);
            userPanel.add(namePanel, BorderLayout.CENTER);

            topPanel.add(userPanel);
        }

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 30));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        topPanel.add(sep);
        topPanel.add(Box.createVerticalStrut(10));

        // Navigation buttons
        addNavButton(topPanel, "\uD83C\uDFE0", "Dashboard", MainFrame.DASHBOARD);
        addNavButton(topPanel, "\uD83D\uDCDD", "Report Lost Item", MainFrame.REPORT_LOST);
        addNavButton(topPanel, "\uD83D\uDCE6", "Report Found Item", MainFrame.REPORT_FOUND);
        addNavButton(topPanel, "\uD83D\uDD0D", "Browse Items", MainFrame.BROWSE);
        addNavButton(topPanel, "\u2728", "Smart Matches", MainFrame.MATCHES);
        addNavButton(topPanel, "\uD83D\uDCCB", "My Claims", MainFrame.CLAIMS);

        // Notifications with badge
        long unreadCount = 0;
        try {
            if (user != null) {
                NotificationService ns = new NotificationService();
                unreadCount = ns.getUnreadCount(user.getId());
            }
        } catch (Exception ignored) {}
        String notifLabel = "Notifications" + (unreadCount > 0 ? " (" + unreadCount + ")" : "");
        addNavButton(topPanel, "\uD83D\uDD14", notifLabel, MainFrame.NOTIFICATIONS);

        addNavButton(topPanel, "\uD83D\uDC64", "My Profile", MainFrame.PROFILE);

        // Admin section
        if (user != null && user.isStaffOrAdmin()) {
            topPanel.add(Box.createVerticalStrut(10));
            JSeparator sep2 = new JSeparator();
            sep2.setForeground(new Color(255, 255, 255, 30));
            sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            topPanel.add(sep2);
            topPanel.add(Box.createVerticalStrut(6));

            JLabel adminLabel = new JLabel("  ADMIN");
            adminLabel.setFont(FindBackTheme.getHeaderFont(10));
            adminLabel.setForeground(new Color(100, 130, 170));
            adminLabel.setBorder(new EmptyBorder(0, 16, 6, 0));
            adminLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            topPanel.add(adminLabel);

            addNavButton(topPanel, "\uD83D\uDCCA", "Admin Dashboard", MainFrame.ADMIN_DASHBOARD);
            addNavButton(topPanel, "\uD83D\uDCC4", "Manage Reports", MainFrame.ADMIN_REPORTS);
            addNavButton(topPanel, "\u2705", "Manage Claims", MainFrame.ADMIN_CLAIMS);
            addNavButton(topPanel, "\uD83D\uDC65", "Manage Users", MainFrame.ADMIN_USERS);
            addNavButton(topPanel, "\uD83C\uDFF7\uFE0F", "Categories", MainFrame.ADMIN_CATEGORIES);
            addNavButton(topPanel, "\uD83D\uDCCD", "Locations", MainFrame.ADMIN_LOCATIONS);
            addNavButton(topPanel, "\uD83E\uDD1D", "Handovers", MainFrame.ADMIN_HANDOVERS);
            addNavButton(topPanel, "\uD83D\uDCC8", "Analytics", MainFrame.ADMIN_ANALYTICS);
        }

        JScrollPane scrollPane = new JScrollPane(topPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        // Logout button at bottom
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 10, 20, 10));

        JButton logoutBtn = FindBackTheme.createStyledButton("\u23FB  Logout", FindBackTheme.DANGER, Color.WHITE);
        logoutBtn.setPreferredSize(new Dimension(180, 38));
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

    private void addNavButton(JPanel parent, String icon, String text, String panelName) {
        JPanel btn = new JPanel(new BorderLayout()) {
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
                if (isActive) {
                    g2.setColor(new Color(255, 255, 255, 20));
                    g2.fillRoundRect(8, 0, getWidth() - 16, getHeight(), 8, 8);
                    g2.setColor(FindBackTheme.SECONDARY);
                    g2.fillRoundRect(0, 4, 4, getHeight() - 8, 4, 4);
                } else if (hovered) {
                    g2.setColor(new Color(255, 255, 255, 10));
                    g2.fillRoundRect(8, 0, getWidth() - 16, getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(220, 40));
        btn.setBorder(new EmptyBorder(0, 20, 0, 10));

        JLabel label = new JLabel(icon + "  " + text);
        label.setFont(FindBackTheme.getBodyFont(13));
        label.setForeground(Color.WHITE);
        btn.add(label, BorderLayout.CENTER);

        navButtons.put(panelName, btn);
        parent.add(btn);
        parent.add(Box.createVerticalStrut(2));
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
