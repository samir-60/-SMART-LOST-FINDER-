package com.findback.ui.student;

import com.findback.model.Claim;
import com.findback.model.Match;
import com.findback.model.User;
import com.findback.service.ClaimService;
import com.findback.service.MatchingService;
import com.findback.service.SessionManager;
import com.findback.ui.FindBackTheme;
import com.findback.ui.MainFrame;
import com.findback.ui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MatchesPanel extends JPanel implements MainFrame.Refreshable {
    private final MatchingService matchingService = new MatchingService();
    private final ClaimService claimService = new ClaimService();

    private DefaultTableModel tableModel;
    private JTable matchesTable;
    private List<Match> currentMatches;
    private JButton runAiBtn;

    public MatchesPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(FindBackTheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);
        JLabel title = new JLabel("Real-Time AI Smart Matches");
        title.setFont(FindBackTheme.getHeaderFont(26));
        title.setForeground(FindBackTheme.TEXT);
        titleRow.add(title);

        JLabel aiBadge = new JLabel(" ⚡ Powered by Groq Real-Time AI ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(237, 233, 254));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(139, 92, 246));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        aiBadge.setFont(FindBackTheme.getHeaderFont(11));
        aiBadge.setForeground(new Color(109, 40, 217));
        aiBadge.setBorder(new EmptyBorder(3, 8, 3, 8));
        titleRow.add(aiBadge);

        JLabel subtitle = new JLabel("High-speed semantic matching evaluating category (25%), location (25%), timeline (20%), color (15%), and descriptions (15%).");
        subtitle.setFont(FindBackTheme.getBodyFont(14));
        subtitle.setForeground(new Color(100, 115, 130));
        
        header.add(titleRow, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Matches Table Card
        tableModel = new DefaultTableModel(new String[]{
                "Match ID", "Lost Item", "Found Item", "Category", "Score", "Confidence", "Status"
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

        runAiBtn = FindBackTheme.createStyledButton("⚡ Groq Real-Time AI Match", new Color(124, 58, 237), Color.WHITE);
        runAiBtn.setFont(FindBackTheme.getHeaderFont(13));
        runAiBtn.setToolTipText("Run ultra-fast Groq AI semantic reasoning across open lost and found reports");
        runAiBtn.addActionListener(e -> runAiMatching());
        btnBar.add(runAiBtn);

        JButton breakdownBtn = FindBackTheme.createStyledButton("Explain Match (Human Reason)", FindBackTheme.CARD, FindBackTheme.PRIMARY);
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

    private void runAiMatching() {
        runAiBtn.setEnabled(false);
        runAiBtn.setText("⏳ AI Scanning...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                return matchingService.runMatchingEngine();
            }

            @Override
            protected void done() {
                runAiBtn.setEnabled(true);
                runAiBtn.setText("⚡ Groq Real-Time AI Match");
                setCursor(Cursor.getDefaultCursor());
                try {
                    int newMatches = get();
                    refresh();
                    if (newMatches > 0) {
                        JOptionPane.showMessageDialog(MatchesPanel.this,
                                "Real-Time AI Scan Complete!\n\n" +
                                "Discovered " + newMatches + " matching pair(s) using deep contextual reasoning.\n" +
                                "Review the matches below and click 'Send Request to Get Item' to initiate your claim.",
                                "AI Matching Results", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(MatchesPanel.this,
                                "Real-Time AI Scan Complete!\n\n" +
                                "All active reports were evaluated against campus records. No new matches at this time.",
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
        sb.append("NATURAL REASONING:\n");
        sb.append(m.getExplanation() != null ? m.getExplanation() : "Analyzed based on campus location, chronology, and item characteristics.");

        JOptionPane.showMessageDialog(this, sb.toString(), "AI Match Human Explanation", JOptionPane.INFORMATION_MESSAGE);
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
                        "Campus staff will verify your confidential evidence.",
                        "Claim Submitted", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error submitting claim: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void refresh() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            currentMatches = matchingService.getMatchesForUser(user.getId());
        } else {
            currentMatches = matchingService.getHighConfidenceMatches();
        }

        tableModel.setRowCount(0);
        for (Match m : currentMatches) {
            String conf = m.getTotalScore() >= 85 ? "Strong Potential Match" : "Possible Match";
            tableModel.addRow(new Object[]{
                    "#" + m.getId(),
                    m.getLostTitle() != null ? m.getLostTitle() : ("Report #" + m.getLostReportId()),
                    m.getFoundTitle() != null ? m.getFoundTitle() : ("Report #" + m.getFoundReportId()),
                    m.getLostCategory() != null ? m.getLostCategory() : "-",
                    String.format("%.1f%%", m.getTotalScore()),
                    conf,
                    m.getStatus() != null ? m.getStatus().name() : "POTENTIAL"
            });
        }
    }
}
