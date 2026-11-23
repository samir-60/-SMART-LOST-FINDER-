package com.findback.ui.admin;

import com.findback.dao.ClaimDao;
import com.findback.enums.ClaimStatus;
import com.findback.model.Claim;
import com.findback.model.User;
import com.findback.service.AdminService;
import com.findback.service.ClaimService;
import com.findback.service.HandoverService;
import com.findback.service.SessionManager;
import com.findback.ui.FindBackTheme;
import com.findback.ui.MainFrame;
import com.findback.ui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminClaimsPanel extends JPanel implements MainFrame.Refreshable {
    private final ClaimService claimService = new ClaimService();
    private final ClaimDao claimDao = new ClaimDao();
    private final HandoverService handoverService = new HandoverService();
    private final AdminService adminService = new AdminService();

    private DefaultTableModel tableModel;
    private JTable claimsTable;
    private List<Claim> allClaims;

    public AdminClaimsPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(FindBackTheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Ownership Claim Verification");
        title.setFont(FindBackTheme.getHeaderFont(26));
        title.setForeground(FindBackTheme.TEXT);
        JLabel subtitle = new JLabel("Review confidential evidence, approve legitimate ownership claims, or schedule physical handover.");
        subtitle.setFont(FindBackTheme.getBodyFont(14));
        subtitle.setForeground(new Color(110, 120, 135));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Claims Table Card
        tableModel = new DefaultTableModel(new String[]{
                "Claim ID", "Claimant", "Lost Item", "Found Item", "Status", "Admin Remarks", "Date"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        claimsTable = new JTable(tableModel);
        claimsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        RoundedPanel tableCard = new RoundedPanel(12, FindBackTheme.CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        tableCard.add(new JScrollPane(claimsTable), BorderLayout.CENTER);

        // Action Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnBar.setOpaque(false);

        JButton reviewBtn = FindBackTheme.createStyledButton("Inspect Private Evidence", FindBackTheme.CARD, FindBackTheme.PRIMARY);
        reviewBtn.setBorder(BorderFactory.createLineBorder(FindBackTheme.BORDER));
        reviewBtn.addActionListener(e -> inspectEvidence());
        btnBar.add(reviewBtn);

        JButton approveBtn = FindBackTheme.createSuccessButton("Approve Claim");
        approveBtn.addActionListener(e -> approveClaim());
        btnBar.add(approveBtn);

        JButton rejectBtn = FindBackTheme.createDangerButton("Reject Claim");
        rejectBtn.addActionListener(e -> rejectClaim());
        btnBar.add(rejectBtn);

        JButton handoverBtn = FindBackTheme.createPrimaryButton("Execute Handover");
        handoverBtn.addActionListener(e -> executeHandover());
        btnBar.add(handoverBtn);

        tableCard.add(btnBar, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);

        refresh();
    }

    private void inspectEvidence() {
        int row = claimsTable.getSelectedRow();
        if (row < 0 || allClaims == null || row >= allClaims.size()) return;
        Claim c = allClaims.get(row);
        JOptionPane.showMessageDialog(this,
                "Claim #" + c.getId() + " by " + c.getClaimantName() + "\n" +
                "Lost Item: " + c.getLostTitle() + "\n" +
                "Found Item: " + c.getFoundTitle() + "\n" +
                "Status: " + c.getStatus() + "\n\n" +
                "CONFIDENTIAL EVIDENCE SUBMITTED:\n" + c.getProofDescription(),
                "Confidential Ownership Evidence", JOptionPane.INFORMATION_MESSAGE);
    }

    private void approveClaim() {
        int row = claimsTable.getSelectedRow();
        if (row < 0 || allClaims == null || row >= allClaims.size()) return;
        Claim c = allClaims.get(row);

        String remarks = JOptionPane.showInputDialog(this, "Enter admin remarks for approval:", "Approve Claim #" + c.getId(), JOptionPane.QUESTION_MESSAGE);
        if (remarks == null) return;

        User admin = SessionManager.getInstance().getCurrentUser();
        claimService.reviewClaim(c.getId(), ClaimStatus.APPROVED, remarks.trim(), admin != null ? admin.getId() : 1L);
        JOptionPane.showMessageDialog(this, "Claim #" + c.getId() + " approved! Handover can now be scheduled.", "Claim Approved", JOptionPane.INFORMATION_MESSAGE);
        refresh();
    }

    private void rejectClaim() {
        int row = claimsTable.getSelectedRow();
        if (row < 0 || allClaims == null || row >= allClaims.size()) return;
        Claim c = allClaims.get(row);

        String remarks = JOptionPane.showInputDialog(this, "Enter reason for rejection:", "Reject Claim #" + c.getId(), JOptionPane.WARNING_MESSAGE);
        if (remarks == null || remarks.trim().isEmpty()) return;

        User admin = SessionManager.getInstance().getCurrentUser();
        claimService.reviewClaim(c.getId(), ClaimStatus.REJECTED, remarks.trim(), admin != null ? admin.getId() : 1L);
        JOptionPane.showMessageDialog(this, "Claim #" + c.getId() + " rejected.", "Claim Rejected", JOptionPane.INFORMATION_MESSAGE);
        refresh();
    }

    private void executeHandover() {
        int row = claimsTable.getSelectedRow();
        if (row < 0 || allClaims == null || row >= allClaims.size()) return;
        Claim c = allClaims.get(row);

        if (c.getStatus() != ClaimStatus.APPROVED) {
            JOptionPane.showMessageDialog(this, "Handover can only be executed for APPROVED claims.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String venue = JOptionPane.showInputDialog(this, "Handover Venue:", "Central Campus Security & Proctor Office");
        if (venue == null || venue.trim().isEmpty()) return;

        User admin = SessionManager.getInstance().getCurrentUser();
        try {
            var handover = handoverService.executeHandover(c.getId(), admin != null ? admin.getId() : 1L, venue.trim(), "Physical verification verified.");
            JOptionPane.showMessageDialog(this,
                    "Handover successfully completed!\nOfficial Receipt Code: " + handover.getReceiptCode() + "\n" +
                    "Both parties have received in-portal recovery notifications.",
                    "Recovery Completed", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error executing handover: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refresh() {
        allClaims = claimDao.findAll();
        tableModel.setRowCount(0);
        for (Claim c : allClaims) {
            tableModel.addRow(new Object[]{
                    "#" + c.getId(),
                    c.getClaimantName() != null ? c.getClaimantName() : "User #" + c.getClaimantId(),
                    c.getLostTitle() != null ? c.getLostTitle() : ("#" + c.getLostReportId()),
                    c.getFoundTitle() != null ? c.getFoundTitle() : ("#" + c.getFoundReportId()),
                    c.getStatus() != null ? c.getStatus().name() : "PENDING",
                    c.getAdminRemarks() != null ? c.getAdminRemarks() : "-",
                    c.getCreatedAt() != null ? c.getCreatedAt().toLocalDate().toString() : "-"
            });
        }
    }
}
