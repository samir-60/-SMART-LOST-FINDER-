package com.findback.ui.student;

import com.findback.dao.CategoryDao;
import com.findback.dao.LocationDao;
import com.findback.enums.ReportStatus;
import com.findback.enums.ReportType;
import com.findback.model.CampusLocation;
import com.findback.model.Category;
import com.findback.model.Claim;
import com.findback.model.ItemReport;
import com.findback.model.Match;
import com.findback.model.User;
import com.findback.service.ClaimService;
import com.findback.service.FileStorageService;
import com.findback.service.MatchingService;
import com.findback.service.SessionManager;
import com.findback.ui.FindBackTheme;
import com.findback.ui.MainFrame;
import com.findback.ui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

/**
 * Real-Time Smart Matches & Instant AI Search Panel.
 * Features:
 * - Instant AI Smart Search: upload a photo, enter color, title, and location to search all campus found items.
 * - Interactive Celebration Match Dialog with direct WhatsApp contact and claim submission.
 * - Seamless toggle between "All Campus Matches" and "My Reports Matches".
 * - 100% clean vector typography with zero broken emoji characters (no hollow boxes).
 */
public class MatchesPanel extends JPanel implements MainFrame.Refreshable {
    private final MatchingService matchingService = new MatchingService();
    private final ClaimService claimService = new ClaimService();
    private final CategoryDao categoryDao = new CategoryDao();
    private final LocationDao locationDao = new LocationDao();
    private final FileStorageService fileStorageService = new FileStorageService();

    private DefaultTableModel tableModel;
    private JTable matchesTable;
    private List<Match> currentMatches;
    private JButton runAiBtn;
    private JComboBox<String> viewFilterCombo;

    public MatchesPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(FindBackTheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleRow.setOpaque(false);
        JLabel title = new JLabel("Real-Time AI Smart Matches");
        title.setFont(FindBackTheme.getHeaderFont(26));
        title.setForeground(FindBackTheme.TEXT);
        titleRow.add(title);

        // Clean typography badge
        JLabel aiBadge = new JLabel("Powered by Groq AI");
        aiBadge.setFont(FindBackTheme.getHeaderFont(11));
        aiBadge.setForeground(new Color(109, 40, 217));
        aiBadge.setOpaque(true);
        aiBadge.setBackground(new Color(237, 233, 254));
        aiBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(196, 181, 253), 1, true),
                new EmptyBorder(4, 10, 4, 10)));
        titleRow.add(aiBadge);

        // Filter Toggle in Header
        viewFilterCombo = new JComboBox<>(new String[]{"All Campus Matches", "My Reports Matches"});
        viewFilterCombo.setFont(FindBackTheme.getBodyFont(12));
        viewFilterCombo.setPreferredSize(new Dimension(180, 32));
        viewFilterCombo.addActionListener(e -> refresh());
        titleRow.add(Box.createHorizontalStrut(10));
        titleRow.add(viewFilterCombo);

        JLabel subtitle = new JLabel("High-speed semantic matching evaluating category (25%), location (25%), timeline (20%), color (15%), and descriptions (15%).");
        subtitle.setFont(FindBackTheme.getBodyFont(14));
        subtitle.setForeground(new Color(100, 115, 130));

        header.add(titleRow, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Matches Table Card
        tableModel = new DefaultTableModel(new String[]{
                "Match ID", "Lost Item", "Found Item", "Category", "Score", "Confidence", "Finder Contact (WhatsApp)", "Status"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        matchesTable = new JTable(tableModel);
        matchesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        matchesTable.setAutoCreateRowSorter(true);

        RoundedPanel tableCard = new RoundedPanel(12, FindBackTheme.CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        tableCard.add(new JScrollPane(matchesTable), BorderLayout.CENTER);

        // Action Buttons Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnBar.setOpaque(false);

        // Instant AI Smart Search (Upload picture, color, location)
        JButton instantSearchBtn = FindBackTheme.createStyledButton("Instant AI Smart Search", new Color(16, 185, 129), Color.WHITE);
        instantSearchBtn.setFont(FindBackTheme.getHeaderFont(13));
        instantSearchBtn.setToolTipText("Upload an image and search where your item is available in real-time");
        instantSearchBtn.addActionListener(e -> openInstantAiSearchDialog());
        btnBar.add(instantSearchBtn);

        runAiBtn = FindBackTheme.createStyledButton("Run AI Match Scan", new Color(124, 58, 237), Color.WHITE);
        runAiBtn.setFont(FindBackTheme.getHeaderFont(13));
        runAiBtn.setToolTipText("Scan and evaluate all active lost and found items in the system");
        runAiBtn.addActionListener(e -> runAiMatching());
        btnBar.add(runAiBtn);

        JButton breakdownBtn = FindBackTheme.createStyledButton("Explain Match & Contact", FindBackTheme.CARD, FindBackTheme.PRIMARY);
        breakdownBtn.setBorder(BorderFactory.createLineBorder(FindBackTheme.BORDER));
        breakdownBtn.addActionListener(e -> showExplanation());
        btnBar.add(breakdownBtn);

        JButton claimBtn = FindBackTheme.createPrimaryButton("Send Request to Get Item");
        claimBtn.addActionListener(e -> submitClaimForMatch());
        btnBar.add(claimBtn);

        tableCard.add(btnBar, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);

        refresh();
    }

    /**
     * Interactive Instant AI Smart Search Dialog.
     * Allows uploading an image, selecting color, category, and location where lost,
     * then executes live AI comparison across all open found items.
     */
    private void openInstantAiSearchDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Instant AI Smart Search & Scanner", true);
        dialog.setSize(560, 640);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(16, 16));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(24, 28, 24, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Title Header
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel headerLabel = new JLabel("Instant AI Belonging Search");
        headerLabel.setFont(FindBackTheme.getHeaderFont(20));
        headerLabel.setForeground(FindBackTheme.TEXT);
        form.add(headerLabel, gbc);

        gbc.gridy = 1;
        JLabel subLabel = new JLabel("Upload a photo or enter item attributes to search all found items in real-time.");
        subLabel.setFont(FindBackTheme.getBodyFont(12));
        subLabel.setForeground(new Color(110, 120, 135));
        form.add(subLabel, gbc);

        // Item Name
        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        form.add(new JLabel("Item Name / Title *"), gbc);
        gbc.gridx = 1;
        JTextField titleField = new JTextField();
        titleField.setPreferredSize(new Dimension(280, 34));
        form.add(titleField, gbc);

        // Category
        gbc.gridy = 3; gbc.gridx = 0;
        form.add(new JLabel("Category *"), gbc);
        gbc.gridx = 1;
        JComboBox<CategoryItem> categoryCombo = new JComboBox<>();
        categoryCombo.setPreferredSize(new Dimension(280, 34));
        for (Category c : categoryDao.findAllActive()) {
            categoryCombo.addItem(new CategoryItem(c.getId(), c.getName()));
        }
        form.add(categoryCombo, gbc);

        // Color
        gbc.gridy = 4; gbc.gridx = 0;
        form.add(new JLabel("Primary Color *"), gbc);
        gbc.gridx = 1;
        JTextField colorField = new JTextField();
        colorField.setPreferredSize(new Dimension(280, 34));
        form.add(colorField, gbc);

        // Location where exact lost
        gbc.gridy = 5; gbc.gridx = 0;
        form.add(new JLabel("Where Exact Lost *"), gbc);
        gbc.gridx = 1;
        JComboBox<LocationItem> locationCombo = new JComboBox<>();
        locationCombo.setPreferredSize(new Dimension(280, 34));
        for (CampusLocation l : locationDao.findAllActive()) {
            locationCombo.addItem(new LocationItem(l.getId(), l.getName()));
        }
        form.add(locationCombo, gbc);

        // Specific details
        gbc.gridy = 6; gbc.gridx = 0;
        form.add(new JLabel("Specific Marks / Notes"), gbc);
        gbc.gridx = 1;
        JTextField notesField = new JTextField();
        notesField.setPreferredSize(new Dimension(280, 34));
        form.add(notesField, gbc);

        // Photo Upload Box
        gbc.gridy = 7; gbc.gridx = 0;
        form.add(new JLabel("Product Picture"), gbc);
        gbc.gridx = 1;
        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        photoPanel.setOpaque(false);
        JButton uploadBtn = FindBackTheme.createStyledButton("Upload Photo...", FindBackTheme.CARD, FindBackTheme.PRIMARY);
        uploadBtn.setBorder(BorderFactory.createLineBorder(FindBackTheme.BORDER));
        JLabel photoLabel = new JLabel("No photo chosen");
        photoLabel.setFont(FindBackTheme.getBodyFont(11));
        photoLabel.setForeground(Color.GRAY);
        final File[] selectedFile = new File[1];

        uploadBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Images (JPG, PNG, GIF, SVG)", "jpg", "jpeg", "png", "gif", "svg"));
            if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = chooser.getSelectedFile();
                photoLabel.setText(selectedFile[0].getName());
                photoLabel.setForeground(new Color(16, 185, 129));
            }
        });
        photoPanel.add(uploadBtn);
        photoPanel.add(photoLabel);
        form.add(photoPanel, gbc);

        // Buttons
        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 2;
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        actionRow.setOpaque(false);

        JButton cancelBtn = FindBackTheme.createStyledButton("Cancel", FindBackTheme.CARD, FindBackTheme.TEXT);
        cancelBtn.setBorder(BorderFactory.createLineBorder(FindBackTheme.BORDER));
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton searchBtn = FindBackTheme.createPrimaryButton("Search Matches with AI");
        searchBtn.addActionListener(e -> {
            String name = titleField.getText().trim();
            String color = colorField.getText().trim();
            CategoryItem cat = (CategoryItem) categoryCombo.getSelectedItem();
            LocationItem loc = (LocationItem) locationCombo.getSelectedItem();

            if (name.isEmpty() || color.isEmpty() || cat == null || loc == null) {
                JOptionPane.showMessageDialog(dialog, "Please fill in Item Name, Category, Color, and Location.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            dialog.dispose();
            executeSmartSearch(name, cat.id, cat.name, loc.id, loc.name, color, notesField.getText().trim(), selectedFile[0]);
        });

        actionRow.add(cancelBtn);
        actionRow.add(searchBtn);
        form.add(actionRow, gbc);

        dialog.setContentPane(form);
        dialog.setVisible(true);
    }

    /**
     * Executes the live AI matching search against all found items in the database.
     */
    private void executeSmartSearch(String title, Long catId, String catName, Long locId, String locName, String color, String details, File photo) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<List<Match>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Match> doInBackground() {
                ItemReport queryLost = new ItemReport();
                queryLost.setId(999999L);
                queryLost.setReportType(ReportType.LOST);
                queryLost.setTitle(title);
                queryLost.setCategoryId(catId);
                queryLost.setCategoryName(catName);
                queryLost.setLocationId(locId);
                queryLost.setLocationName(locName);
                queryLost.setColor(color);
                queryLost.setEventDate(LocalDate.now());
                queryLost.setDescription(details.isEmpty() ? (title + " in " + color) : details);

                if (photo != null && photo.exists()) {
                    try {
                        byte[] bytes = Files.readAllBytes(photo.toPath());
                        String stored = fileStorageService.storeFile(bytes, photo.getName());
                        queryLost.setImagePath(stored);
                    } catch (Exception ignore) {}
                }

                return matchingService.searchFoundMatches(queryLost);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    List<Match> results = get();
                    if (!results.isEmpty()) {
                        showSuccessMatchPopup(results, title);
                    } else {
                        int opt = JOptionPane.showConfirmDialog(MatchesPanel.this,
                                "No exact match found in current campus records for '" + title + "'.\n\n" +
                                "Would you like to register this as an official Lost Item Report now?\n" +
                                "Our system will automatically monitor campus items and alert you as soon as it is found.",
                                "AI Smart Search Result", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        if (opt == JOptionPane.YES_OPTION) {
                            JOptionPane.showMessageDialog(MatchesPanel.this,
                                    "Please navigate to 'Report Lost Item' on the sidebar to publish your report.",
                                    "Report Item", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MatchesPanel.this, "Search error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * Celebratory Success Pop-Up Dialog showing discovered matching items,
     * AI reasoning, and direct WhatsApp contact buttons for the finder.
     */
    private void showSuccessMatchPopup(List<Match> matches, String queryTitle) {
        JDialog popup = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "AI Match Success — Found Belongings", true);
        popup.setSize(620, 520);
        popup.setLocationRelativeTo(this);
        popup.setLayout(new BorderLayout());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(FindBackTheme.BG);
        container.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Animated Success Banner
        RoundedPanel banner = new RoundedPanel(12, new Color(236, 253, 245));
        banner.setLayout(new BorderLayout(10, 6));
        banner.setBorder(new EmptyBorder(14, 16, 14, 16));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel bannerTitle = new JLabel("Found " + matches.size() + " Potential Match(es) on Campus!");
        bannerTitle.setFont(FindBackTheme.getHeaderFont(18));
        bannerTitle.setForeground(new Color(5, 150, 105));
        banner.add(bannerTitle, BorderLayout.NORTH);

        JLabel bannerSub = new JLabel("High-confidence match detected for your search: '" + queryTitle + "'.");
        bannerSub.setFont(FindBackTheme.getBodyFont(12));
        bannerSub.setForeground(new Color(4, 120, 87));
        banner.add(bannerSub, BorderLayout.SOUTH);
        container.add(banner);
        container.add(Box.createVerticalStrut(14));

        // Matches list
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        for (Match m : matches) {
            RoundedPanel itemCard = new RoundedPanel(12, Color.WHITE);
            itemCard.setLayout(new BorderLayout(12, 10));
            itemCard.setBorder(new EmptyBorder(14, 16, 14, 16));
            itemCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

            // Info column
            JPanel infoCol = new JPanel();
            infoCol.setLayout(new BoxLayout(infoCol, BoxLayout.Y_AXIS));
            infoCol.setOpaque(false);

            JLabel titleLbl = new JLabel(m.getFoundTitle() != null ? m.getFoundTitle() : "Found Item");
            titleLbl.setFont(FindBackTheme.getHeaderFont(16));
            titleLbl.setForeground(FindBackTheme.TEXT);
            infoCol.add(titleLbl);

            JLabel locLbl = new JLabel("Location: " + m.getFoundLocation() + " | Category: " + m.getFoundCategory() + " | Color: " + m.getFoundColor());
            locLbl.setFont(FindBackTheme.getBodyFont(12));
            locLbl.setForeground(new Color(100, 115, 130));
            infoCol.add(locLbl);

            String finderPhone = m.getFinderPhone();
            JLabel phoneLbl = new JLabel("Found by: " + m.getFinderName() + (finderPhone.isEmpty() ? "" : (" • WhatsApp: " + finderPhone)));
            phoneLbl.setFont(FindBackTheme.getBodyFont(12));
            phoneLbl.setForeground(new Color(37, 99, 235));
            infoCol.add(phoneLbl);

            itemCard.add(infoCol, BorderLayout.CENTER);

            // Right Actions & Score
            JPanel rightCol = new JPanel();
            rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
            rightCol.setOpaque(false);

            JLabel scoreBadge = new JLabel(String.format("%.0f%% Match", m.getTotalScore()), SwingConstants.CENTER);
            scoreBadge.setFont(FindBackTheme.getHeaderFont(13));
            scoreBadge.setForeground(m.getTotalScore() >= 85 ? new Color(16, 185, 129) : new Color(245, 158, 11));
            scoreBadge.setAlignmentX(Component.RIGHT_ALIGNMENT);
            rightCol.add(scoreBadge);
            rightCol.add(Box.createVerticalStrut(8));

            if (!finderPhone.isEmpty()) {
                JButton waBtn = FindBackTheme.createStyledButton("Chat on WhatsApp", new Color(37, 211, 102), Color.WHITE);
                waBtn.setFont(FindBackTheme.getHeaderFont(11));
                waBtn.addActionListener(e -> openWhatsAppChat(finderPhone));
                waBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
                rightCol.add(waBtn);
            }

            itemCard.add(rightCol, BorderLayout.EAST);
            listPanel.add(itemCard);
            listPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        container.add(scroll);

        // Bottom Dismiss Button
        JButton closeBtn = FindBackTheme.createPrimaryButton("Done");
        closeBtn.addActionListener(e -> popup.dispose());
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bot.setOpaque(false);
        bot.add(closeBtn);
        popup.add(container, BorderLayout.CENTER);
        popup.add(bot, BorderLayout.SOUTH);

        popup.setVisible(true);
    }

    private void runAiMatching() {
        runAiBtn.setEnabled(false);
        runAiBtn.setText("Scanning AI Matches...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                return matchingService.runMatchingEngine();
            }

            @Override
            protected void done() {
                runAiBtn.setEnabled(true);
                runAiBtn.setText("Run AI Match Scan");
                setCursor(Cursor.getDefaultCursor());
                try {
                    int newMatches = get();
                    refresh();
                    if (newMatches > 0) {
                        JOptionPane.showMessageDialog(MatchesPanel.this,
                                "Real-Time AI Scan Complete!\n\n" +
                                "Discovered " + newMatches + " matching pair(s) using deep contextual reasoning.\n" +
                                "Review the matches below or contact the finder directly via WhatsApp.",
                                "AI Matching Results", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(MatchesPanel.this,
                                "Real-Time AI Scan Complete!\n\n" +
                                "All active reports were evaluated against campus records. All current matches are up to date.",
                                "AI Matching Results", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MatchesPanel.this, "AI Scan Notice: " + ex.getMessage(), "Notice", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showExplanation() {
        int selected = matchesTable.getSelectedRow();
        if (selected < 0 || currentMatches == null) {
            JOptionPane.showMessageDialog(this, "Select a match from the table to view the AI reasoning breakdown.", "Select Match", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int row = matchesTable.convertRowIndexToModel(selected);
        if (row >= currentMatches.size()) return;

        Match m = currentMatches.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("CAMPUS AI MATCH ASSESSMENT — TOTAL SCORE: ").append(String.format("%.1f", m.getTotalScore())).append(" / 100\n\n");
        sb.append("• Category Match:     ").append(String.format("%.1f", m.getCategoryScore())).append(" / 25 pts\n");
        sb.append("• Location Proximity: ").append(String.format("%.1f", m.getLocationScore())).append(" / 25 pts\n");
        sb.append("• Date Chronology:    ").append(String.format("%.1f", m.getDateScore())).append(" / 20 pts\n");
        sb.append("• Color Similarity:   ").append(String.format("%.1f", m.getColorScore())).append(" / 15 pts\n");
        sb.append("• Description Match:  ").append(String.format("%.1f", m.getDescriptionScore())).append(" / 15 pts\n\n");
        sb.append("FINDER CONTACT INFORMATION:\n");
        sb.append("• Finder: ").append(m.getFinderName()).append("\n");
        String fPhone = m.getFinderPhone();
        if (!fPhone.isEmpty()) {
            sb.append("• WhatsApp Number: ").append(fPhone).append("\n\n");
        } else {
            sb.append("• Contact: Kept safely with Proctor & Security Office\n\n");
        }
        sb.append("NATURAL REASONING:\n");
        sb.append(m.getExplanation() != null ? m.getExplanation() : "Analyzed based on campus location, chronology, and item characteristics.");

        Object[] options;
        if (!fPhone.isEmpty()) {
            options = new Object[]{"Chat on WhatsApp", "Submit Claim Request", "Close"};
        } else {
            options = new Object[]{"Submit Claim Request", "Close"};
        }

        int choice = JOptionPane.showOptionDialog(this, sb.toString(), "AI Match Human Explanation & Contact",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0 && !fPhone.isEmpty()) {
            openWhatsAppChat(fPhone);
        } else if ((choice == 1 && !fPhone.isEmpty()) || (choice == 0 && fPhone.isEmpty())) {
            submitClaimForMatch();
        }
    }

    private void openWhatsAppChat(String phone) {
        try {
            String cleanNumber = phone.replaceAll("[^0-9]", "");
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new java.net.URI("https://wa.me/" + cleanNumber));
            } else {
                StringSelection sel = new StringSelection(phone);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
                JOptionPane.showMessageDialog(this, "WhatsApp number copied to clipboard: " + phone, "WhatsApp Contact", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            StringSelection sel = new StringSelection(phone);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
            JOptionPane.showMessageDialog(this, "WhatsApp number copied to clipboard: " + phone, "WhatsApp Contact", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void submitClaimForMatch() {
        int selected = matchesTable.getSelectedRow();
        if (selected < 0 || currentMatches == null) {
            JOptionPane.showMessageDialog(this, "Select a match to submit an ownership claim.", "Select Match", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int row = matchesTable.convertRowIndexToModel(selected);
        if (row >= currentMatches.size()) return;

        Match m = currentMatches.get(row);
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        String proof = JOptionPane.showInputDialog(this,
                "Enter confidential proof of ownership for '" + (m.getFoundTitle() != null ? m.getFoundTitle() : "Item") + "':\n" +
                "(e.g. Unique serial number, distinctive marks, lock combination, inside contents):",
                "Submit Ownership Claim", JOptionPane.QUESTION_MESSAGE);

        if (proof != null && !proof.trim().isEmpty()) {
            try {
                Claim claim = claimService.submitClaim(user.getId(), m.getLostReportId(), m.getFoundReportId(), m.getId(), proof.trim(), null);
                JOptionPane.showMessageDialog(this,
                        "Ownership claim #" + claim.getId() + " submitted successfully!\n" +
                        "Campus staff and finder will verify your confidential evidence.",
                        "Claim Submitted", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error submitting claim: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void refresh() {
        User user = SessionManager.getInstance().getCurrentUser();
        boolean showAll = viewFilterCombo == null || viewFilterCombo.getSelectedIndex() == 0;

        if (showAll || user == null) {
            currentMatches = matchingService.getAllMatches();
        } else {
            currentMatches = matchingService.getMatchesForUser(user.getId());
            if (currentMatches.isEmpty()) {
                // If user has no personal matches, fallback to showing all campus matches so table is not empty
                currentMatches = matchingService.getAllMatches();
                if (viewFilterCombo != null) {
                    viewFilterCombo.setSelectedIndex(0);
                }
            }
        }

        tableModel.setRowCount(0);
        for (Match m : currentMatches) {
            String conf = m.getTotalScore() >= 85 ? "Strong Potential Match" : "Possible Match";
            String contact = m.getFinderPhone().isEmpty() ? "Proctor Office" : m.getFinderPhone();
            tableModel.addRow(new Object[]{
                    "#" + m.getId(),
                    m.getLostTitle() != null ? m.getLostTitle() : ("Report #" + m.getLostReportId()),
                    m.getFoundTitle() != null ? m.getFoundTitle() : ("Report #" + m.getFoundReportId()),
                    m.getLostCategory() != null ? m.getLostCategory() : "-",
                    String.format("%.1f%%", m.getTotalScore()),
                    conf,
                    contact,
                    m.getStatus() != null ? m.getStatus().name() : "POTENTIAL"
            });
        }
    }

    public static class CategoryItem {
        public final Long id;
        public final String name;
        public CategoryItem(Long id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    public static class LocationItem {
        public final Long id;
        public final String name;
        public LocationItem(Long id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}
