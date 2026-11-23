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

    public MatchesPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(FindBackTheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Smart Matches (AI Rule-Based Engine)");
        title.setFont(FindBackTheme.getHeaderFont(26));
        title.setForeground(FindBackTheme.TEXT);
        JLabel subtitle = new JLabel("Explainable 0–100 matching: Category 25%, Location 25%, Date 20%, Color 15%, Description 15%.");
        subtitle.setFont(FindBackTheme.getBodyFont(14));
        subtitle.setForeground(new Color(110, 120, 135));
        header.add(title, BorderLayout.NORTH);
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

        RoundedPanel tableCard = new RoundedPanel(12, FindBackTheme.CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        tableCard.add(new JScrollPane(matchesTable), BorderLayout.CENTER);

        // Action Buttons Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnBar.setOpaque(false);

        JButton runAiBtn = FindBackTheme.createStyledButton("✨ Run AI Match Engine", new Color(243, 232, 255), new Color(124, 58, 237));
        runAiBtn.setFont(FindBackTheme.getHeaderFont(13));
        runAiBtn.setToolTipText("Run free offline AI matching model across all open lost and found reports");
        runAiBtn.addActionListener(e -> runAiMatching());
        btnBar.add(runAiBtn);

        JButton breakdownBtn = FindBackTheme.createStyledButton("Explain Match Score", FindBackTheme.CARD, FindBackTheme.PRIMARY);
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
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            int newMatches = matchingService.runMatchingEngine();
            refresh();
            if (newMatches > 0) {
                JOptionPane.showMessageDialog(this,
                        "AI Matching Engine executed successfully!\n" +
                        "Discovered " + newMatches + " new matching pair(s) using our explainable similarity model.\n" +
                        "Review the list below and click 'Send Request to Get Item' to initiate your claim.",
                        "AI Matching Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "AI Matching Engine executed successfully!\n" +
                        "All reports are up to date. No additional matches found at this time.",
                        "AI Matching Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error running matching engine: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void showExplanation() {
        int row = matchesTable.getSelectedRow();
        if (row < 0 || currentMatches == null || row >= currentMatches.size()) {
            JOptionPane.showMessageDialog(this, "Select a match from the table to view the scoring breakdown.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Match m = currentMatches.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("SMART MATCH BREAKDOWN — TOTAL SCORE: ").append(String.format("%.1f", m.getTotalScore())).append(" / 100\n\n");
        sb.append("• Category Match:     ").append(String.format("%.1f", m.getCategoryScore())).append(" / 25 pts\n");
        sb.append("• Location Proximity: ").append(String.format("%.1f", m.getLocationScore())).append(" / 25 pts\n");
        sb.append("• Date Chronology:    ").append(String.format("%.1f", m.getDateScore())).append(" / 20 pts\n");
        sb.append("• Color Similarity:   ").append(String.format("%.1f", m.getColorScore())).append(" / 15 pts\n");
        sb.append("• Description Match:  ").append(String.format("%.1f", m.getDescriptionScore())).append(" / 15 pts\n\n");
        sb.append("EXPLANATION:\n").append(m.getExplanation() != null ? m.getExplanation() : "Computed based on campus item feature similarity.");

        JOptionPane.showMessageDialog(this, sb.toString(), "Explainable Smart Match Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void submitClaimForMatch() {
        int row = matchesTable.getSelectedRow();
        if (row < 0 || currentMatches == null || row >= currentMatches.size()) {
            JOptionPane.showMessageDialog(this, "Select a match to submit an ownership claim.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
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
