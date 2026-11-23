package com.findback.ui.admin;

import com.findback.dao.HandoverDao;
import com.findback.model.Handover;
import com.findback.ui.FindBackTheme;
import com.findback.ui.MainFrame;
import com.findback.ui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminHandoversPanel extends JPanel implements MainFrame.Refreshable {
    private final HandoverDao handoverDao = new HandoverDao();

    private DefaultTableModel tableModel;
    private JTable handoverTable;
    private List<Handover> allHandovers;

    public AdminHandoversPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(FindBackTheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Official Recovery & Handover Certificates");
        title.setFont(FindBackTheme.getHeaderFont(26));
        title.setForeground(FindBackTheme.TEXT);
        JLabel subtitle = new JLabel("Tamper-evident recovery receipt codes, staff witness validation, and physical return records.");
        subtitle.setFont(FindBackTheme.getBodyFont(14));
        subtitle.setForeground(new Color(110, 120, 135));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Table Card
        tableModel = new DefaultTableModel(new String[]{
                "Receipt Code", "Lost Item", "Found Item", "Returned To", "Handed By", "Staff Witness", "Venue", "Handover Date"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        handoverTable = new JTable(tableModel);
        handoverTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        RoundedPanel tableCard = new RoundedPanel(12, FindBackTheme.CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        tableCard.add(new JScrollPane(handoverTable), BorderLayout.CENTER);

        // Action Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnBar.setOpaque(false);

        JButton certBtn = FindBackTheme.createPrimaryButton("View Official Recovery Certificate");
        certBtn.addActionListener(e -> viewCertificate());
        btnBar.add(certBtn);

        tableCard.add(btnBar, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);

        refresh();
    }

    private void viewCertificate() {
        int row = handoverTable.getSelectedRow();
        if (row < 0 || allHandovers == null || row >= allHandovers.size()) {
            JOptionPane.showMessageDialog(this, "Select a handover record to view its certificate.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Handover h = allHandovers.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("     METROPOLITAN UNIVERSITY, SYLHET\n");
        sb.append("   OFFICIAL ITEM RECOVERY & HANDOVER CERTIFICATE\n");
        sb.append("====================================================\n\n");
        sb.append("Receipt Code:    ").append(h.getReceiptCode()).append("\n");
        sb.append("Lost Item:       ").append(h.getLostTitle()).append("\n");
        sb.append("Found Item:      ").append(h.getFoundTitle()).append("\n");
        sb.append("Verified Owner:  ").append(h.getReceiverName()).append("\n");
        sb.append("Honest Finder:   ").append(h.getHandedByName() != null ? h.getHandedByName() : "Campus Staff").append("\n");
        sb.append("Staff Witness:   ").append(h.getWitnessName()).append("\n");
        sb.append("Handover Venue:  ").append(h.getVenue()).append("\n");
        sb.append("Timestamp:       ").append(h.getHandoverDate().toString().replace("T", " ")).append("\n");
        if (h.getRemarks() != null) sb.append("Remarks:         ").append(h.getRemarks()).append("\n");
        sb.append("\nStatus: VERIFIED & COMPLETED\n");

        JOptionPane.showMessageDialog(this, sb.toString(), "Recovery Certificate — " + h.getReceiptCode(), JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void refresh() {
        allHandovers = handoverDao.findAll();
        tableModel.setRowCount(0);
        for (Handover h : allHandovers) {
            tableModel.addRow(new Object[]{
                    h.getReceiptCode(),
                    h.getLostTitle() != null ? h.getLostTitle() : ("#" + h.getLostReportId()),
                    h.getFoundTitle() != null ? h.getFoundTitle() : ("#" + h.getFoundReportId()),
                    h.getReceiverName() != null ? h.getReceiverName() : "User #" + h.getReceivedById(),
                    h.getHandedByName() != null ? h.getHandedByName() : "-",
                    h.getWitnessName() != null ? h.getWitnessName() : "Proctor Office",
                    h.getVenue(),
                    h.getHandoverDate() != null ? h.getHandoverDate().toLocalDate().toString() : "-"
            });
        }
    }
}
