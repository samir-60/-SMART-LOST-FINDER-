package com.findback.ui.admin;

import com.findback.enums.ReportStatus;
import com.findback.model.ItemReport;
import com.findback.model.User;
import com.findback.service.AdminService;
import com.findback.service.ReportService;
import com.findback.service.SessionManager;
import com.findback.ui.FindBackTheme;
import com.findback.ui.MainFrame;
import com.findback.ui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminReportsPanel extends JPanel implements MainFrame.Refreshable {
    private final ReportService reportService = new ReportService();
    private final AdminService adminService = new AdminService();

    private DefaultTableModel tableModel;
    private JTable reportTable;
    private List<ItemReport> allReports;

    public AdminReportsPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(FindBackTheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Campus Reports Management");
        title.setFont(FindBackTheme.getHeaderFont(26));
        title.setForeground(FindBackTheme.TEXT);
        JLabel subtitle = new JLabel("Supervise all lost and found reports, verify legitimacy, and close resolved/duplicate entries.");
        subtitle.setFont(FindBackTheme.getBodyFont(14));
        subtitle.setForeground(new Color(110, 120, 135));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Table Card
        tableModel = new DefaultTableModel(new String[]{
                "ID", "Type", "Title", "Category", "Location", "Reporter", "Event Date", "Status", "Custody"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        reportTable = new JTable(tableModel);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        RoundedPanel tableCard = new RoundedPanel(12, FindBackTheme.CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        tableCard.add(new JScrollPane(reportTable), BorderLayout.CENTER);

        // Action Buttons Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnBar.setOpaque(false);

        JButton viewBtn = FindBackTheme.createStyledButton("View Full Report", FindBackTheme.CARD, FindBackTheme.PRIMARY);
        viewBtn.setBorder(BorderFactory.createLineBorder(FindBackTheme.BORDER));
        viewBtn.addActionListener(e -> viewSelectedReport());
        btnBar.add(viewBtn);

        JButton closeBtn = FindBackTheme.createStyledButton("Close Report", FindBackTheme.CARD, FindBackTheme.WARNING);
        closeBtn.setBorder(BorderFactory.createLineBorder(FindBackTheme.BORDER));
        closeBtn.addActionListener(e -> closeSelectedReport());
        btnBar.add(closeBtn);

        JButton deleteBtn = FindBackTheme.createDangerButton("Delete Report");
        deleteBtn.addActionListener(e -> deleteSelectedReport());
        btnBar.add(deleteBtn);

        tableCard.add(btnBar, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);

        refresh();
    }

    private void deleteSelectedReport() {
        int row = reportTable.getSelectedRow();
        if (row < 0 || allReports == null || row >= allReports.size()) {
            JOptionPane.showMessageDialog(this, "Please select a report to delete.", "Select Report", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ItemReport r = allReports.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete Report #" + r.getId() + " ('" + r.getTitle() + "')?",
                "Confirm Delete Report", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = reportService.deleteReport(r.getId());
            if (ok) {
                User admin = SessionManager.getInstance().getCurrentUser();
                if (admin != null) {
                    adminService.logAction(admin.getId(), "DELETE_REPORT", "ItemReport", r.getId(), "Deleted by administrator");
                }
                JOptionPane.showMessageDialog(this, "Report #" + r.getId() + " deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete report #" + r.getId() + " because matches or claims exist. Marked as Closed instead.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                reportService.closeReport(r.getId());
            }
            refresh();
        }
    }

    private void viewSelectedReport() {
        int row = reportTable.getSelectedRow();
        if (row < 0 || allReports == null || row >= allReports.size()) return;
        ItemReport r = allReports.get(row);
        JOptionPane.showMessageDialog(this,
                "Report #" + r.getId() + " (" + r.getReportType() + ")\n" +
                "Title: " + r.getTitle() + "\n" +
                "Category: " + r.getCategoryName() + "\n" +
                "Location: " + r.getLocationName() + " (" + (r.getSpecificLocation() != null ? r.getSpecificLocation() : "") + ")\n" +
                "Color: " + r.getColor() + "\n" +
                "Date: " + r.getEventDate() + "\n" +
                "Status: " + r.getStatus() + "\n" +
                "Reported By: " + r.getUserName() + "\n\n" +
                "Description:\n" + r.getDescription(),
                "Report Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void closeSelectedReport() {
        int row = reportTable.getSelectedRow();
        if (row < 0 || allReports == null || row >= allReports.size()) return;
        ItemReport r = allReports.get(row);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to close Report #" + r.getId() + " (" + r.getTitle() + ")?",
                "Confirm Report Closure", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            reportService.closeReport(r.getId());
            User admin = SessionManager.getInstance().getCurrentUser();
            if (admin != null) {
                adminService.logAction(admin.getId(), "CLOSE_REPORT", "ItemReport", r.getId(), "Closed by administrator");
            }
            JOptionPane.showMessageDialog(this, "Report #" + r.getId() + " has been closed.", "Success", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        }
    }

    @Override
    public void refresh() {
        allReports = reportService.search("", null, null, null, null);
        tableModel.setRowCount(0);
        for (ItemReport r : allReports) {
            tableModel.addRow(new Object[]{
                    "#" + r.getId(),
                    r.getReportType() != null ? r.getReportType().name() : "LOST",
                    r.getTitle(),
                    r.getCategoryName() != null ? r.getCategoryName() : "-",
                    r.getLocationName() != null ? r.getLocationName() : "-",
                    r.getUserName() != null ? r.getUserName() : "User #" + r.getUserId(),
                    r.getEventDate() != null ? r.getEventDate().toString() : "-",
                    r.getStatus() != null ? r.getStatus().name() : "OPEN",
                    r.getCustodyStatus() != null ? r.getCustodyStatus().name() : "-"
            });
        }
    }
}
