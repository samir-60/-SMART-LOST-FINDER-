package com.findback.ui;

import com.findback.service.HandoverService;
import com.findback.service.MatchingService;
import com.findback.service.ReportService;
import com.findback.ui.components.RoundedPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

/**
 * FindBack — Welcome Screen & Landing Portal.
 * Clean, human-written Java Swing interface using FlatLaf and campus branding.
 */
public class WelcomeFrame extends JFrame {

    private static BufferedImage campusImage = null;

    static {
        try {
            InputStream in = WelcomeFrame.class.getClassLoader().getResourceAsStream("images/campus.jpg");
            if (in != null) {
                campusImage = ImageIO.read(in);
            } else {
                File f = new File("src/main/resources/images/campus.jpg");
                if (f.exists()) {
                    campusImage = ImageIO.read(f);
                }
            }
        } catch (Exception ignored) {}
    }

    private JLabel foundValLabel;
    private JLabel matchValLabel;
    private JLabel recValLabel;

    public WelcomeFrame() {
        setTitle("FindBack — Metropolitan University Smart Lost & Found Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1100, 680));

        CampusBackgroundPanel mainPanel = new CampusBackgroundPanel();
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Hero Side (60%)
        gbc.gridx = 0;
        gbc.weightx = 0.60;
        mainPanel.add(createLeftHeroPanel(), gbc);

        // Right Info Side (40%)
        gbc.gridx = 1;
        gbc.weightx = 0.40;
        mainPanel.add(createRightPanel(), gbc);

        setContentPane(mainPanel);
        loadRealStatsAsync();
    }

    private JPanel createLeftHeroPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(30, 48, 30, 24));

        // 1. Campus Badge
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(37, 99, 235, 140));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(new Color(147, 197, 253, 180));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setMaximumSize(new Dimension(420, 30));
        JLabel textLabel = new JLabel("METROPOLITAN UNIVERSITY • SYLHET, BANGLADESH");
        textLabel.setFont(FindBackTheme.getHeaderFont(11));
        textLabel.setForeground(new Color(224, 242, 254));
        badge.add(textLabel);
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(badge);
        content.add(Box.createVerticalStrut(14));

        // 2. University Header
        JLabel uniHeader = new JLabel("WELCOME TO METROPOLITAN UNIVERSITY");
        uniHeader.setFont(FindBackTheme.getHeaderFont(22));
        uniHeader.setForeground(new Color(224, 242, 254));
        uniHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(uniHeader);
        content.add(Box.createVerticalStrut(8));

        // 3. FindBack Main Title
        JLabel title = new JLabel("FindBack");
        title.setFont(FindBackTheme.getHeaderFont(48));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);

        // 4. Subtitle
        JLabel subtitle = new JLabel("Smart Lost & Found Portal");
        subtitle.setFont(FindBackTheme.getHeaderFont(18));
        subtitle.setForeground(new Color(147, 197, 253));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(subtitle);
        content.add(Box.createVerticalStrut(12));

        // 5. Hero Accent Line
        JLabel hero = new JLabel("Lost something? Find it back.");
        hero.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hero.setForeground(new Color(254, 240, 138));
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(hero);
        content.add(Box.createVerticalStrut(8));

        // 6. Supporting Description
        JLabel desc = new JLabel("<html><div style='width:460px; color:#dbeafe; font-size:13px; line-height:1.45;'>"
                + "Official intelligent recovery portal for Metropolitan University students and faculty. "
                + "Report lost items, discover found belongings, and let our explainable Smart Matching "
                + "Engine reconnect you with what matters.</div></html>");
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(desc);
        content.add(Box.createVerticalStrut(22));

        // 7. Belonging Category Pills (Clean text chips without emojis)
        JPanel categoryGrid = createCategoryGrid();
        categoryGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(categoryGrid);
        content.add(Box.createVerticalStrut(26));

        // 8. Action Buttons (Login & Register - Glassy styled buttons)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = FindBackTheme.createStyledButton("Login", FindBackTheme.SECONDARY, Color.WHITE);
        loginBtn.setPreferredSize(new Dimension(160, 44));
        loginBtn.setFont(FindBackTheme.getHeaderFont(15));
        loginBtn.addActionListener(e -> openLoginDialog());

        JButton registerBtn = FindBackTheme.createStyledButton("Register", new Color(255, 255, 255, 40), Color.WHITE);
        registerBtn.setPreferredSize(new Dimension(160, 44));
        registerBtn.setFont(FindBackTheme.getHeaderFont(15));
        registerBtn.addActionListener(e -> openRegisterDialog());

        buttonsPanel.add(loginBtn);
        buttonsPanel.add(registerBtn);
        content.add(buttonsPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(content, gbc);
        return panel;
    }

    private JPanel createCategoryGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 4, 10, 10));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(480, 84));

        String[] items = {
                "Backpack", "Wallet", "Keys", "Headphones",
                "Phone", "Books", "ID Card", "Watch"
        };

        for (String itemName : items) {
            JPanel cell = new JPanel(new GridBagLayout()) {
                private boolean hovered = false;
                {
                    addMouseListener(new MouseAdapter() {
                        @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                        @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                        @Override public void mouseClicked(MouseEvent e) { openLoginDialog(); }
                    });
                }
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = hovered ? new Color(255, 255, 255, 50) : new Color(255, 255, 255, 22);
                    g2.setColor(bg);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.setColor(hovered ? new Color(147, 197, 253, 200) : new Color(255, 255, 255, 45));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            cell.setOpaque(false);
            cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cell.setPreferredSize(new Dimension(108, 36));

            JLabel nameLabel = new JLabel(itemName, SwingConstants.CENTER);
            nameLabel.setFont(FindBackTheme.getHeaderFont(12));
            nameLabel.setForeground(new Color(241, 245, 249));
            cell.add(nameLabel);

            grid.add(cell);
        }
        return grid;
    }

    private JPanel createRightPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        RoundedPanel card = new RoundedPanel(18, new Color(255, 255, 255, 240));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 32, 32, 32));
        card.setMaximumSize(new Dimension(460, 560));

        JLabel header = new JLabel("How It Works");
        header.setFont(FindBackTheme.getHeaderFont(24));
        header.setForeground(FindBackTheme.TEXT);
        card.add(header);

        JLabel sub = new JLabel("Fast, transparent lost & found process");
        sub.setFont(FindBackTheme.getBodyFont(13));
        sub.setForeground(new Color(100, 115, 135));
        card.add(sub);
        card.add(Box.createVerticalStrut(20));

        String[][] steps = {
                {"1", "Report Belonging", "Submit a lost or found report with details, category, location, and photo."},
                {"2", "Smart Matching", "Our weighted rule engine compares category, location, date, color, and description."},
                {"3", "In-App Notification", "Get instant alerts whenever a high-confidence match is discovered."},
                {"4", "Verified Handover", "Submit ownership proof, receive administrative review, and retrieve item."}
        };

        for (int i = 0; i < steps.length; i++) {
            card.add(createStepRow(steps[i][0], steps[i][1], steps[i][2]));
            if (i < steps.length - 1) card.add(Box.createVerticalStrut(12));
        }

        card.add(Box.createVerticalStrut(22));

        // Live Real Database Stats
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(420, 75));

        foundValLabel = new JLabel("...", SwingConstants.CENTER);
        matchValLabel = new JLabel("...", SwingConstants.CENTER);
        recValLabel = new JLabel("...", SwingConstants.CENTER);

        statsPanel.add(createStatMiniBox(foundValLabel, "Items Found"));
        statsPanel.add(createStatMiniBox(matchValLabel, "Smart Matches"));
        statsPanel.add(createStatMiniBox(recValLabel, "Recovered"));

        card.add(statsPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 20, 20, 40);
        wrapper.add(card, gbc);
        return wrapper;
    }

    private JPanel createStepRow(String stepNum, String title, String desc) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);

        JLabel numBadge = new JLabel(stepNum, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(new Color(37, 99, 235));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        numBadge.setFont(FindBackTheme.getHeaderFont(12));
        numBadge.setForeground(FindBackTheme.SECONDARY);
        numBadge.setPreferredSize(new Dimension(28, 28));
        row.add(numBadge, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FindBackTheme.getHeaderFont(13));
        titleLabel.setForeground(FindBackTheme.TEXT);
        textPanel.add(titleLabel);

        JLabel descLabel = new JLabel("<html><div style='width:280px; color:#64748b; font-size:11px;'>" + desc + "</div></html>");
        descLabel.setFont(FindBackTheme.getBodyFont(11));
        textPanel.add(descLabel);

        row.add(textPanel, BorderLayout.CENTER);
        return row;
    }

    private JPanel createStatMiniBox(JLabel valueLabel, String lbl) {
        RoundedPanel box = new RoundedPanel(10, FindBackTheme.BG);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(10, 8, 10, 8));

        valueLabel.setFont(FindBackTheme.getHeaderFont(18));
        valueLabel.setForeground(FindBackTheme.SECONDARY);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(valueLabel);

        JLabel lLabel = new JLabel(lbl, SwingConstants.CENTER);
        lLabel.setFont(FindBackTheme.getBodyFont(10));
        lLabel.setForeground(new Color(100, 115, 135));
        lLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(lLabel);
        return box;
    }

    private void loadRealStatsAsync() {
        SwingWorker<long[], Void> worker = new SwingWorker<>() {
            @Override
            protected long[] doInBackground() {
                try {
                    ReportService rs = new ReportService();
                    MatchingService ms = new MatchingService();
                    HandoverService hs = new HandoverService();
                    return new long[]{
                            rs.countFound(),
                            ms.getAllMatches().size(),
                            hs.countRecovered()
                    };
                } catch (Exception e) {
                    return new long[]{0, 0, 0};
                }
            }

            @Override
            protected void done() {
                try {
                    long[] res = get();
                    if (foundValLabel != null) foundValLabel.setText(String.valueOf(res[0]));
                    if (matchValLabel != null) matchValLabel.setText(String.valueOf(res[1]));
                    if (recValLabel != null) recValLabel.setText(String.valueOf(res[2]));
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private static class CampusBackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth();
            int h = getHeight();

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            if (campusImage != null) {
                double imgAspect = (double) campusImage.getWidth() / campusImage.getHeight();
                double panelAspect = (double) w / h;
                int drawW, drawH, drawX, drawY;
                if (panelAspect > imgAspect) {
                    drawW = w;
                    drawH = (int) (w / imgAspect);
                    drawX = 0;
                    drawY = (h - drawH) / 2;
                } else {
                    drawH = h;
                    drawW = (int) (h * imgAspect);
                    drawX = (w - drawW) / 2;
                    drawY = 0;
                }
                g2.drawImage(campusImage, drawX, drawY, drawW, drawH, null);
            } else {
                g2.setColor(FindBackTheme.PRIMARY);
                g2.fillRect(0, 0, w, h);
            }

            // Atmospheric gradient overlay: deep navy on the left for text contrast, translucent on the right
            LinearGradientPaint overlay = new LinearGradientPaint(
                    0, 0, w, 0,
                    new float[]{0.0f, 0.55f, 0.85f, 1.0f},
                    new Color[]{
                            new Color(10, 25, 47, 235), // Deep navy on left
                            new Color(15, 35, 65, 210),
                            new Color(10, 25, 47, 160),
                            new Color(10, 20, 38, 190)  // Translucent on right
                    }
            );
            g2.setPaint(overlay);
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
    }

    private void openLoginDialog() {
        new LoginDialog(this).setVisible(true);
    }

    private void openRegisterDialog() {
        new RegisterDialog(this).setVisible(true);
    }
}
