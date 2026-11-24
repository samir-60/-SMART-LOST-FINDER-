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
 * Features:
 * - High-resolution Metropolitan University campus aerial background with cinematic sapphire overlay.
 * - Animated living typography for "WELCOME TO METROPOLITAN UNIVERSITY".
 * - Glassmorphism hero cards and category visual badges.
 * - Crystal clear glassy action buttons (Login & Register).
 * - Real-time live campus statistics queried directly from the database.
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
        } catch (Exception e) {
            System.err.println("Could not load campus image: " + e.getMessage());
        }
    }

    private JLabel foundValLabel;
    private JLabel matchValLabel;
    private JLabel recValLabel;

    public WelcomeFrame() {
        setTitle("FindBack — Metropolitan University Smart Lost & Found Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1160, 700));

        // Root container with full campus background & atmospheric overlay
        CampusBackgroundPanel mainPanel = new CampusBackgroundPanel();
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Hero Side (60% width)
        gbc.gridx = 0;
        gbc.weightx = 0.60;
        mainPanel.add(createLeftHeroPanel(), gbc);

        // Right Info Side (40% width)
        gbc.gridx = 1;
        gbc.weightx = 0.40;
        mainPanel.add(createRightPanel(), gbc);

        setContentPane(mainPanel);

        // Load live statistics in background thread to avoid freezing UI
        loadRealStatsAsync();
    }

    /**
     * Left hero panel featuring the university badge, animated header,
     * portal branding, lost belongings interactive icons, and glassy buttons.
     */
    private JPanel createLeftHeroPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(30, 48, 30, 24));

        // 1. University Badge
        JPanel badge = createUniversityBadge();
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(badge);
        content.add(Box.createVerticalStrut(14));

        // 2. Animated Unique Typography for "WELCOME TO METROPOLITAN UNIVERSITY"
        AnimatedUniversityHeader animatedHeader = new AnimatedUniversityHeader();
        animatedHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(animatedHeader);
        content.add(Box.createVerticalStrut(14));

        // 3. FindBack Main Title
        JLabel title = new JLabel("FindBack");
        title.setFont(getUniqueHeaderFont(50));
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
        content.add(Box.createVerticalStrut(12));

        // 5. Hero Accent Line
        JLabel hero = new JLabel("Lost something? Find it back.");
        hero.setFont(new Font("Segoe UI", Font.BOLD, 17));
        hero.setForeground(new Color(254, 240, 138)); // Warm golden amber highlight
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

        // 7. Interactive Lost Belongings Grid (Backpacks, Wallets, Keys, Phone...)
        JPanel iconsGrid = createItemIconsGrid();
        iconsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(iconsGrid);
        content.add(Box.createVerticalStrut(26));

        // 8. Action Buttons (Login & Register - Clear Glassy Premium Styling)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = createGlassWelcomeButton("Login", true);
        loginBtn.setPreferredSize(new Dimension(165, 46));
        loginBtn.setToolTipText("Access your student or administrator account");
        loginBtn.addActionListener(e -> openLoginDialog());

        JButton registerBtn = createGlassWelcomeButton("Register", false);
        registerBtn.setPreferredSize(new Dimension(165, 46));
        registerBtn.setToolTipText("Create a new student account at Metropolitan University");
        registerBtn.addActionListener(e -> openRegisterDialog());

        buttonsPanel.add(loginBtn);
        buttonsPanel.add(registerBtn);
        content.add(buttonsPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(content, gbc);
        return panel;
    }

    /**
     * Creates a premium glassy button with specular highlights,
     * high-contrast antialiased typography, and tactile press states.
     */
    private JButton createGlassWelcomeButton(String text, boolean primary) {
        JButton button = new JButton(text) {
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
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                int w = getWidth();
                int h = getHeight();
                int arc = 14;

                if (primary) {
                    // Vibrant Royal Blue Glass (Login)
                    Color topC = pressed ? new Color(29, 78, 216, 250) : (hovered ? new Color(59, 130, 246, 240) : new Color(37, 99, 235, 220));
                    Color botC = pressed ? new Color(30, 58, 138, 255) : (hovered ? new Color(37, 99, 235, 250) : new Color(29, 78, 216, 235));
                    g2.setPaint(new GradientPaint(0, 0, topC, 0, h, botC));
                    g2.fillRoundRect(0, 0, w, h, arc, arc);

                    // Specular glass highlight reflection
                    g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, hovered ? 120 : 75), 0, h * 0.5f, new Color(255, 255, 255, 0)));
                    g2.fillRoundRect(1, 1, w - 2, h / 2, arc, arc);

                    // Luminous neon border
                    g2.setColor(hovered ? new Color(255, 255, 255, 220) : new Color(147, 197, 253, 160));
                    g2.setStroke(new BasicStroke(1.3f));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                } else {
                    // Frosted White Crystal Glass (Register)
                    Color bg = pressed ? new Color(255, 255, 255, 60) : (hovered ? new Color(255, 255, 255, 45) : new Color(255, 255, 255, 25));
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, w, h, arc, arc);

                    // Specular reflection
                    g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, hovered ? 90 : 50), 0, h * 0.5f, new Color(255, 255, 255, 0)));
                    g2.fillRoundRect(1, 1, w - 2, h / 2, arc, arc);

                    // Glass border
                    g2.setColor(hovered ? new Color(255, 255, 255, 220) : new Color(224, 242, 254, 110));
                    g2.setStroke(new BasicStroke(1.3f));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                }

                // High-contrast, bold, readable text with drop shadow
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = (h + fm.getAscent() - fm.getDescent()) / 2 + (pressed ? 1 : 0);

                // Deep text shadow for clarity against any background
                g2.setColor(new Color(0, 0, 0, 140));
                g2.drawString(getText(), x, y + 1);

                g2.setColor(Color.WHITE);
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createUniversityBadge() {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(37, 99, 235, 130));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(new Color(147, 197, 253, 190));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(4, 14, 4, 14));
        badge.setMaximumSize(new Dimension(420, 32));

        JLabel textLabel = new JLabel("METROPOLITAN UNIVERSITY • SYLHET, BANGLADESH");
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        textLabel.setForeground(new Color(224, 242, 254));

        badge.add(textLabel);
        return badge;
    }

    /**
     * Animated header component:
     * - Typewriter character-by-character reveal for "WELCOME TO METROPOLITAN UNIVERSITY".
     * - Continuous smooth cyan-amber-white shimmer wave with mathematically safe gradient coordinates.
     */
    private static class AnimatedUniversityHeader extends JComponent {
        private static final String FULL_TEXT = "WELCOME TO METROPOLITAN UNIVERSITY";
        private int revealedChars = 0;
        private float shimmerOffset = 0.0f;
        private boolean typingDone = false;
        private boolean showCursor = true;
        private final Timer animationTimer;

        public AnimatedUniversityHeader() {
            setPreferredSize(new Dimension(580, 48));
            setMaximumSize(new Dimension(650, 48));

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
                showCursor = (System.currentTimeMillis() % 600 < 300);
                repaint();
            });
            animationTimer.start();
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }
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

            // Legible shadow behind the text
            g2.setColor(new Color(0, 0, 0, 180));
            g2.drawString(textToDraw, 2, 34);

            // Shimmer gradient across the letters (strictly non-equal start and end points)
            float width = Math.max(100f, getWidth());
            float startX = (shimmerOffset - 0.5f) * width;
            float endX = startX + Math.max(120f, width * 0.6f);

            Color[] colors = new Color[]{
                    new Color(255, 255, 255),
                    new Color(96, 165, 250),
                    new Color(254, 240, 138),
                    new Color(255, 255, 255)
            };
            float[] fractions = new float[]{0.0f, 0.45f, 0.75f, 1.0f};

            LinearGradientPaint gradient = new LinearGradientPaint(
                    startX, 20, endX, 20,
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
        String[] fontNames = {"Century Gothic", "Segoe UI Variable Display", "Segoe UI Semibold", "Segoe UI", "Trebuchet MS"};
        for (String name : fontNames) {
            Font f = new Font(name, Font.BOLD, size);
            if (f.getFamily().equalsIgnoreCase(name) || f.getName().equalsIgnoreCase(name)) {
                return f;
            }
        }
        return new Font("Segoe UI", Font.BOLD, size);
    }

    /**
     * Interactive grid showing campus belonging categories.
     * Clean typography glassy pills with zero emojis or icon clutter.
     * Clicking any category invites the user to sign in or register to browse it.
     */
    private JPanel createItemIconsGrid() {
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
                            openLoginDialog();
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
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    // Luminous glass border
                    g2.setColor(hovered ? new Color(147, 197, 253, 200) : new Color(255, 255, 255, 40));
                    g2.setStroke(new BasicStroke(1.1f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            cell.setOpaque(false);
            cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cell.setPreferredSize(new Dimension(108, 36));

            JLabel nameLabel = new JLabel(itemName, SwingConstants.CENTER);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nameLabel.setForeground(new Color(241, 245, 249));
            cell.add(nameLabel);

            grid.add(cell);
        }
        return grid;
    }

    /**
     * Right-hand card displaying the 4-step recovery process and live database stats.
     */
    private JPanel createRightPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        // Glassmorphism Container Card
        RoundedPanel card = new RoundedPanel(18, new Color(255, 255, 255, 235));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 32, 32, 32));
        card.setMaximumSize(new Dimension(460, 600));

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

        // University Campus Live Real Stats Row
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        statsPanel.setOpaque(false);
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Circular numbered badge
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
        numBadge.setForeground(new Color(37, 99, 235));
        numBadge.setPreferredSize(new Dimension(28, 28));
        row.add(numBadge, BorderLayout.WEST);

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

    private JPanel createStatMiniBox(JLabel valueLabel, String lbl) {
        RoundedPanel box = new RoundedPanel(10, FindBackTheme.BG);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(10, 8, 10, 8));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(FindBackTheme.SECONDARY);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(valueLabel);

        JLabel lLabel = new JLabel(lbl, SwingConstants.CENTER);
        lLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lLabel.setForeground(new Color(100, 115, 135));
        lLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(lLabel);

        return box;
    }

    /**
     * Asynchronously queries real live statistics from the database
     * without blocking UI startup or event handling.
     */
    private void loadRealStatsAsync() {
        SwingWorker<long[], Void> worker = new SwingWorker<>() {
            @Override
            protected long[] doInBackground() {
                long found = 0;
                long matches = 0;
                long recovered = 0;
                try {
                    ReportService rs = new ReportService();
                    MatchingService ms = new MatchingService();
                    HandoverService hs = new HandoverService();
                    found = rs.countFound();
                    matches = ms.getHighConfidenceMatches().size();
                    recovered = hs.countRecovered();
                } catch (Exception ignore) {}
                return new long[]{found, matches, recovered};
            }

            @Override
            protected void done() {
                try {
                    long[] res = get();
                    if (foundValLabel != null) foundValLabel.setText(String.valueOf(res[0]));
                    if (matchValLabel != null) matchValLabel.setText(String.valueOf(res[1]));
                    if (recValLabel != null) recValLabel.setText(String.valueOf(res[2]));
                } catch (Exception ignore) {}
            }
        };
        worker.execute();
    }

    /**
     * Root background panel that renders the Metropolitan University aerial photo
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
                // High-fidelity cover-crop aspect ratio calculation
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

            // Cinematic atmospheric gradient overlay: deep navy on the left for text contrast
            LinearGradientPaint overlay = new LinearGradientPaint(
                    0, 0, w, 0,
                    new float[]{0.0f, 0.55f, 0.85f, 1.0f},
                    new Color[]{
                            new Color(10, 25, 47, 235), // Deep sapphire navy
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
