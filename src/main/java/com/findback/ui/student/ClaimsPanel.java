package com.findback.ui.student;

import com.findback.model.Claim;
import com.findback.model.User;
import com.findback.service.ClaimService;
import com.findback.service.SessionManager;
import com.findback.ui.FindBackTheme;
import com.findback.ui.MainFrame;
import com.findback.ui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClaimsPanel extends JPanel implements MainFrame.Refreshable {
    private final ClaimService claimService = new ClaimService();

    private DefaultTableModel tableModel;
    private JTable claimsTable;
    private List<Claim> currentClaims;

    public ClaimsPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(FindBackTheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("My Ownership Claims");
        title.setFont(FindBackTheme.getHeaderFont(26));
        title.setForeground(FindBackTheme.TEXT);
        JLabel subtitle = new JLabel("Track the review status of your item claims. Only campus staff can review private evidence.");
        subtitle.setFont(FindBackTheme.getBodyFont(14));
        subtitle.setForeground(new Color(110, 120, 135));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Claims Table Card
        tableModel = new DefaultTableModel(new String[]{
                "Claim ID", "Lost Item", "Found Item", "Status", "Admin Remarks", "Submitted Date"
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

        JButton viewProofBtn = FindBackTheme.createStyledButton("View My Submitted Evidence", FindBackTheme.CARD, FindBackTheme.PRIMARY);
        viewProofBtn.setBorder(BorderFactory.createLineBorder(FindBackTheme.BORDER));
        viewProofBtn.addActionListener(e -> showProofDetails());
        btnBar.add(viewProofBtn);

        tableCard.add(btnBar, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);

        refresh();
    }

    private void showProofDetails() {
        int row = claimsTable.getSelectedRow();
        if (row < 0 || currentClaims == null || row >= currentClaims.size()) {
            JOptionPane.showMessageDialog(this, "Select a claim to view proof details.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Claim c = currentClaims.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("Claim ID: #").append(c.getId()).append("\n");
        sb.append("Status: ").append(c.getStatus()).append("\n");
        sb.append("Lost Item: ").append(c.getLostTitle()).append("\n");
        sb.append("Found Item: ").append(c.getFoundTitle()).append("\n");
        String fPhone = c.getFinderPhone();
        if (fPhone != null && !fPhone.isEmpty()) {
            sb.append("Finder Contact (WhatsApp): ").append(fPhone).append(" (").append(c.getFinderName()).append(")\n");
        }
        sb.append("\nYour Submitted Ownership Evidence:\n").append(c.getProofDescription()).append("\n\n");
        if (c.getAdminRemarks() != null && !c.getAdminRemarks().isEmpty()) {
            sb.append("Campus Staff Verification Remarks:\n").append(c.getAdminRemarks()).append("\n");
        }

        Object[] options = (fPhone != null && !fPhone.isEmpty()) ?
                new Object[]{"Chat on WhatsApp", "Close"} : new Object[]{"Close"};

        int choice = JOptionPane.showOptionDialog(this, sb.toString(), "Claim Details & Verification Status",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0 && fPhone != null && !fPhone.isEmpty()) {
            openWhatsAppChat(fPhone);
        }
    }

    private void openWhatsAppChat(String phone) {
        try {
            String cleanNumber = phone.replaceAll("[^0-9]", "");
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new java.net.URI("https://wa.me/" + cleanNumber));
            } else {
                java.awt.datatransfer.StringSelection sel = new java.awt.datatransfer.StringSelection(phone);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
                JOptionPane.showMessageDialog(this, "WhatsApp number copied to clipboard: " + phone, "WhatsApp Contact", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            java.awt.datatransfer.StringSelection sel = new java.awt.datatransfer.StringSelection(phone);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
            JOptionPane.showMessageDialog(this, "WhatsApp number copied to clipboard: " + phone, "WhatsApp Contact", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void refresh() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        currentClaims = claimService.getByClaimant(user.getId());
        tableModel.setRowCount(0);
        for (Claim c : currentClaims) {
            tableModel.addRow(new Object[]{
                    "#" + c.getId(),
                    c.getLostTitle() != null ? c.getLostTitle() : ("Lost #" + c.getLostReportId()),
                    c.getFoundTitle() != null ? c.getFoundTitle() : ("Found #" + c.getFoundReportId()),
                    c.getStatus() != null ? c.getStatus().name() : "PENDING",
                    c.getAdminRemarks() != null ? c.getAdminRemarks() : "Under verification",
                    c.getCreatedAt() != null ? c.getCreatedAt().toLocalDate().toString() : "-"
            });
        }
    }
}
