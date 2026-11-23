package com.findback.ui;

import com.findback.ui.components.RoundedPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

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
        } catch (Exception e) {
            System.err.println("Could not load campus image: " + e.getMessage());
        }
    }

    public WelcomeFrame() {
        setTitle("FindBack — Metropolitan University Smart Lost & Found Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1180, 720));

        // Root container with full campus background & atmospheric overlay
        CampusBackgroundPanel mainPanel = new CampusBackgroundPanel();
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Hero Side (62% width)
        gbc.gridx = 0;
        gbc.weightx = 0.62;
        mainPanel.add(createLeftHeroPanel(), gbc);

        // Right Info Side (38% width)
        gbc.gridx = 1;
        gbc.weightx = 0.38;
        mainPanel.add(createRightPanel(), gbc);

        setContentPane(mainPanel);
    }

    /**
     * Left panel featuring the animated Metropolitan University text,
     * portal branding, lost item icons, and action buttons.
     */
    private JPanel createLeftHeroPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(40, 50, 40, 30));

        // 1. University Badge
        JPanel badge = createUniversityBadge();
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(badge);
        content.add(Box.createVerticalStrut(14));

        // 2. Animated Unique Typography for "WELCOME TO METROPOLITAN UNIVERSITY"
        AnimatedUniversityHeader animatedHeader = new AnimatedUniversityHeader();
        animatedHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(animatedHeader);
        content.add(Box.createVerticalStrut(16));

        // 3. FindBack Main Title
        JLabel title = new JLabel("FindBack");
        title.setFont(getUniqueHeaderFont(48));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(4));

        // 4. Subtitle
        JLabel subtitle = new JLabel("Smart Lost & Found Portal");
        subtitle.setFont(getUniqueHeaderFont(20));
        subtitle.setForeground(new Color(147, 197, 253));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(subtitle);
        content.add(Box.createVerticalStrut(14));

        // 5. Hero message
        JLabel hero = new JLabel("Lost something? Find it back.");
        hero.setFont(new Font("Segoe UI", Font.BOLD, 17));
        hero.setForeground(new Color(254, 240, 138)); // Warm golden highlight
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(hero);
        content.add(Box.createVerticalStrut(8));

        // 6. Supporting Description
        JLabel desc = new JLabel("<html><div style='width:450px; color:#dbeafe; font-size:13px; line-height:1.4;'>"
                + "Official intelligent recovery portal for Metropolitan University students and faculty. "
                + "Report lost items, discover found belongings, and let our explainable Smart Matching "
                + "Engine reconnect you with what matters.</div></html>");
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(desc);
        content.add(Box.createVerticalStrut(24));

        // 7. Item icons grid with campus lost belongings theme
        JPanel iconsGrid = createItemIconsGrid();
        iconsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(iconsGrid);
        content.add(Box.createVerticalStrut(30));

        // 8. Action Buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton getStartedBtn = FindBackTheme.createStyledButton("Get Started", FindBackTheme.SECONDARY, Color.WHITE);
        getStartedBtn.setPreferredSize(new Dimension(150, 46));
        getStartedBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        getStartedBtn.addActionListener(e -> openRegisterDialog());

        JButton loginBtn = FindBackTheme.createStyledButton("Login", new Color(255, 255, 255, 45), Color.WHITE);
        loginBtn.setPreferredSize(new Dimension(130, 46));
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.addActionListener(e -> openLoginDialog());

        JButton registerBtn = FindBackTheme.createOutlineButton("Register");
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setPreferredSize(new Dimension(150, 46));
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerBtn.addActionListener(e -> openRegisterDialog());

        buttonsPanel.add(getStartedBtn);
        buttonsPanel.add(loginBtn);
        buttonsPanel.add(registerBtn);
        content.add(buttonsPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(content, gbc);
        return panel;
    }

    private JPanel createUniversityBadge() {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(37, 99, 235, 120));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(new Color(147, 197, 253, 180));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(2, 10, 2, 12));
        badge.setMaximumSize(new Dimension(420, 32));

        JLabel iconLabel = new JLabel("🎓");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        JLabel textLabel = new JLabel("METROPOLITAN UNIVERSITY • SYLHET, BANGLADESH");
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        textLabel.setForeground(new Color(224, 242, 254));

        badge.add(iconLabel);
        badge.add(textLabel);
        return badge;
    }

    /**
     * Animated header component that provides:
     * 1. A typewriter character-by-character reveal for "WELCOME TO METROPOLITAN UNIVERSITY".
     * 2. A smooth animated living cyan-amber shimmer across the text.
     */
    private static class AnimatedUniversityHeader extends JComponent {
        private static final String FULL_TEXT = "WELCOME TO METROPOLITAN UNIVERSITY";
        private int revealedChars = 0;
        private float shimmerOffset = 0.0f;
        private boolean typingDone = false;
        private boolean showCursor = true;
        private final Timer animationTimer;

        public AnimatedUniversityHeader() {
            setPreferredSize(new Dimension(580, 50));
            setMaximumSize(new Dimension(650, 50));

            animationTimer = new Timer(35, e -> {
                // Typewriter effect
                if (!typingDone) {
                    if (revealedChars < FULL_TEXT.length()) {
                        revealedChars++;
                    } else {
                        typingDone = true;
                    }
                }
                // Continuous shimmer wave
                shimmerOffset += 0.035f;
                if (shimmerOffset > 2.0f) {
                    shimmerOffset = 0.0f;
                }
                // Cursor blink
                if (System.currentTimeMillis() % 600 < 300) {
                    showCursor = true;
                } else {
                    showCursor = false;
                }
                repaint();
            });
            animationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            Font font = getUniqueHeaderFont(24);
            g2.setFont(font);

            String textToDraw = FULL_TEXT.substring(0, Math.min(revealedChars, FULL_TEXT.length()));

            // Text shadow for legibility and depth
            g2.setColor(new Color(0, 0, 0, 160));
            g2.drawString(textToDraw, 2, 34);

            // Shimmer gradient across the letters
            float width = Math.max(1, getWidth());
            float x1 = (shimmerOffset - 0.5f) * width;
            float x2 = x1 + width * 0.6f;

            if (x2 <= x1) x2 = x1 + 100;

            Color[] colors = new Color[]{
                    new Color(255, 255, 255),
                    new Color(96, 165, 250),
                    new Color(254, 240, 138),
                    new Color(255, 255, 255)
            };
            float[] fractions = new float[]{0.0f, 0.45f, 0.75f, 1.0f};

            LinearGradientPaint gradient = new LinearGradientPaint(
                    Math.max(0, x1), 20, Math.min(width, x2), 20,
                    fractions, colors, MultipleGradientPaint.CycleMethod.REPEAT
            );
            g2.setPaint(gradient);
            g2.drawString(textToDraw, 0, 32);

            // Animated typing cursor
            if (!typingDone && showCursor) {
                FontMetrics fm = g2.getFontMetrics();
                int curX = fm.stringWidth(textToDraw) + 4;
                g2.setColor(new Color(254, 240, 138));
                g2.fillRect(curX, 14, 3, 20);
            }

            g2.dispose();
        }
    }

    private static Font getUniqueHeaderFont(int size) {
        // Try elegant modern geometric fonts first
        String[] fontNames = {"Century Gothic", "Segoe UI Variable Display", "Segoe UI Semibold", "Segoe UI", "Trebuchet MS"};
        for (String name : fontNames) {
            Font f = new Font(name, Font.BOLD, size);
            if (f.getFamily().equalsIgnoreCase(name) || f.getName().equalsIgnoreCase(name)) {
                return f;
            }
        }
        return new Font("Segoe UI", Font.BOLD, size);
    }

    private JPanel createItemIconsGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 4, 12, 12));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(460, 130));

        String[][] items = {
                {"\uD83C\uDF92", "Backpack"}, {"\uD83D\uDCB0", "Wallet"},
                {"\uD83D\uDD11", "Keys"}, {"\uD83C\uDFA7", "Headphones"},
                {"\uD83D\uDCF1", "Phone"}, {"\uD83D\uDCDA", "Books"},
                {"\uD83E\uDEAA", "ID Card"}, {"\u231A", "Watch"}
        };

        for (String[] item : items) {
            JPanel cell = new JPanel(new BorderLayout(0, 4)) {
                private boolean hovered = false;

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
                    });
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Frosted glass effect
                    Color bg = hovered ? new Color(255, 255, 255, 45) : new Color(255, 255, 255, 20);
                    g2.setColor(bg);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                    // Delicate border
                    g2.setColor(hovered ? new Color(147, 197, 253, 160) : new Color(255, 255, 255, 40));
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            cell.setOpaque(false);
            cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cell.setBorder(new EmptyBorder(8, 6, 8, 6));

            JLabel iconLabel = new JLabel(item[0], SwingConstants.CENTER);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
            cell.add(iconLabel, BorderLayout.CENTER);

            JLabel nameLabel = new JLabel(item[1], SwingConstants.CENTER);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            nameLabel.setForeground(new Color(224, 242, 254));
            cell.add(nameLabel, BorderLayout.SOUTH);

            grid.add(cell);
        }
        return grid;
    }

    /**
     * Right-hand glassmorphism card displaying how the portal works and campus recovery stats.
     */
    private JPanel createRightPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        // Glassmorphism Container Card
        RoundedPanel card = new RoundedPanel(18, new Color(255, 255, 255, 235));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 36, 40, 36));
        card.setMaximumSize(new Dimension(460, 620));

        JLabel header = new JLabel("How It Works");
        header.setFont(getUniqueHeaderFont(24));
        header.setForeground(FindBackTheme.TEXT);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(header);

        JLabel sub = new JLabel("Fast, transparent lost & found process");
        sub.setFont(FindBackTheme.getBodyFont(13));
        sub.setForeground(new Color(100, 115, 135));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sub);
        card.add(Box.createVerticalStrut(24));

        String[][] steps = {
                {"\uD83D\uDCDD", "1. Report Belonging", "Submit a lost or found report with details, category, location, and photo."},
                {"\u2728", "2. Smart Matching", "Our weighted rule engine compares category, location, date, color, and description."},
                {"\uD83D\uDD14", "3. In-App Notification", "Get instant alerts whenever a high-confidence match is discovered."},
                {"\uD83E\uDD1D", "4. Verified Handover", "Submit ownership proof, receive administrative review, and retrieve item."}
        };

        for (int i = 0; i < steps.length; i++) {
            card.add(createStepRow(steps[i][0], steps[i][1], steps[i][2]));
            if (i < steps.length - 1) card.add(Box.createVerticalStrut(14));
        }

        card.add(Box.createVerticalStrut(26));

        // University Campus Stats Row
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        statsPanel.setOpaque(false);
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsPanel.setMaximumSize(new Dimension(420, 75));

        statsPanel.add(createStatMiniBox("500+", "Items Found"));
        statsPanel.add(createStatMiniBox("88%", "Match Rate"));
        statsPanel.add(createStatMiniBox("<24h", "Avg Return"));

        card.add(statsPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 20, 20, 40);
        wrapper.add(card, gbc);
        return wrapper;
    }

    private JPanel createStepRow(String icon, String title, String desc) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        row.add(iconLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(FindBackTheme.TEXT);
        textPanel.add(titleLabel);

        JLabel descLabel = new JLabel("<html><div style='width:280px; color:#64748b; font-size:11px;'>" + desc + "</div></html>");
        descLabel.setFont(FindBackTheme.getBodyFont(11));
        textPanel.add(descLabel);

        row.add(textPanel, BorderLayout.CENTER);
        return row;
    }

    private JPanel createStatMiniBox(String val, String lbl) {
        RoundedPanel box = new RoundedPanel(10, FindBackTheme.BG);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(10, 8, 10, 8));

        JLabel vLabel = new JLabel(val, SwingConstants.CENTER);
        vLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        vLabel.setForeground(FindBackTheme.SECONDARY);
        vLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(vLabel);

        JLabel lLabel = new JLabel(lbl, SwingConstants.CENTER);
        lLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lLabel.setForeground(new Color(100, 115, 135));
        lLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(lLabel);

        return box;
    }

    /**
     * Root background panel that renders the Metropolitan University photo
     * with an atmospheric dark sapphire overlay for contrast.
     */
    private static class CampusBackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth();
            int h = getHeight();

            if (campusImage != null) {
                // Cover-crop aspect ratio calculation
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
                // Fallback deep gradient if image is not present
                g2.setColor(FindBackTheme.PRIMARY);
                g2.fillRect(0, 0, w, h);
            }

            // Cinematic atmospheric gradient overlay
            // Deep navy on the left to preserve high text contrast, with soft vignette on the right
            LinearGradientPaint overlay = new LinearGradientPaint(
                    0, 0, w, 0,
                    new float[]{0.0f, 0.55f, 0.85f, 1.0f},
                    new Color[]{
                            new Color(10, 25, 47, 235), // Deep navy
                            new Color(15, 35, 65, 215),
                            new Color(10, 25, 47, 180),
                            new Color(10, 20, 38, 220)
                    }
            );
            g2.setPaint(overlay);
            g2.fillRect(0, 0, w, h);

            g2.dispose();
        }
    }

    private void openLoginDialog() {
        LoginDialog dialog = new LoginDialog(this);
        dialog.setVisible(true);
    }

    private void openRegisterDialog() {
        RegisterDialog dialog = new RegisterDialog(this);
        dialog.setVisible(true);
    }
}

