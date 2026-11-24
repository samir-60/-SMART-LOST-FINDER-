package com.findback.ui.student;

import com.findback.enums.ReportType;
import com.findback.model.ItemReport;
import com.findback.model.Match;
import com.findback.model.User;
import com.findback.service.ClaimService;
import com.findback.service.HandoverService;
import com.findback.service.MatchingService;
import com.findback.service.ReportService;
import com.findback.service.SessionManager;
import com.findback.ui.FindBackTheme;
import com.findback.ui.MainFrame;
import com.findback.ui.components.RoundedPanel;
import com.findback.ui.components.StatsCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel implements MainFrame.Refreshable {
    private final ReportService reportService = new ReportService();
    private final MatchingService matchingService = new MatchingService();
    private final ClaimService claimService = new ClaimService();
    private final HandoverService handoverService = new HandoverService();

    private StatsCard lostCard;
    private StatsCard foundCard;
    private StatsCard matchesCard;
    private StatsCard recoveredCard;

    private DefaultTableModel recentLostModel;
    private DefaultTableModel recentFoundModel;
    private DefaultTableModel matchesModel;

    private JTable lostTable;
    private JTable foundTable;

    private List<ItemReport> currentLostItems;
    private List<ItemReport> currentFoundItems;

    public DashboardPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(FindBackTheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Student Dashboard");
        titleLabel.setFont(FindBackTheme.getHeaderFont(26));
        titleLabel.setForeground(FindBackTheme.TEXT);

        User user = SessionManager.getInstance().getCurrentUser();
        String greeting = user != null ? "Welcome back, " + user.getFullName() : "Welcome to FindBack";
        JLabel subtitleLabel = new JLabel(greeting + " • Metropolitan University Smart Portal");
        subtitleLabel.setFont(FindBackTheme.getBodyFont(14));
        subtitleLabel.setForeground(new Color(110, 120, 135));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Center Scrollable Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Stats Cards Row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        lostCard = new StatsCard("box", "0", "Total Lost Items", FindBackTheme.DANGER);
        foundCard = new StatsCard("search", "0", "Total Found Items", FindBackTheme.SECONDARY);
        matchesCard = new StatsCard("target", "0", "Potential Matches", FindBackTheme.WARNING);
        recoveredCard = new StatsCard("check", "0", "Recovered Items", FindBackTheme.SUCCESS);

        statsRow.add(lostCard);
        statsRow.add(foundCard);
        statsRow.add(matchesCard);
        statsRow.add(recoveredCard);
        contentPanel.add(statsRow);

        contentPanel.add(Box.createVerticalStrut(24));

        // Two-column section: Recent Lost & Recent Found
        JPanel tablesRow = new JPanel(new GridLayout(1, 2, 20, 0));
        tablesRow.setOpaque(false);
        tablesRow.setPreferredSize(new Dimension(0, 340));

        // Recent Lost Panel
        RoundedPanel lostPanel = new RoundedPanel(12, FindBackTheme.CARD);
        lostPanel.setLayout(new BorderLayout(10, 10));
        lostPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel lostTitle = new JLabel("Recent Lost Reports");
        lostTitle.setFont(FindBackTheme.getHeaderFont(16));
        lostTitle.setForeground(FindBackTheme.TEXT);
        lostPanel.add(lostTitle, BorderLayout.NORTH);

        recentLostModel = new DefaultTableModel(new String[]{"Item Title", "Category", "Location", "Date", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        lostTable = new JTable(recentLostModel);
        lostTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lostPanel.add(new JScrollPane(lostTable), BorderLayout.CENTER);

        JPanel lostActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        lostActions.setOpaque(false);
        JButton deleteLostBtn = FindBackTheme.createDangerButton("Delete Report");
        deleteLostBtn.setPreferredSize(new Dimension(130, 32));
        deleteLostBtn.addActionListener(e -> deleteSelectedReport(true));
        lostActions.add(deleteLostBtn);
        lostPanel.add(lostActions, BorderLayout.SOUTH);

        tablesRow.add(lostPanel);

        // Recent Found Panel
        RoundedPanel foundPanel = new RoundedPanel(12, FindBackTheme.CARD);
        foundPanel.setLayout(new BorderLayout(10, 10));
        foundPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel foundTitle = new JLabel("Recent Found Items");
        foundTitle.setFont(FindBackTheme.getHeaderFont(16));
        foundTitle.setForeground(FindBackTheme.TEXT);
        foundPanel.add(foundTitle, BorderLayout.NORTH);

        recentFoundModel = new DefaultTableModel(new String[]{"Item Title", "Category", "Location", "Date", "Custody"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        foundTable = new JTable(recentFoundModel);
        foundTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        foundPanel.add(new JScrollPane(foundTable), BorderLayout.CENTER);

        JPanel foundActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        foundActions.setOpaque(false);
        JButton deleteFoundBtn = FindBackTheme.createDangerButton("Delete Report");
        deleteFoundBtn.setPreferredSize(new Dimension(130, 32));
        deleteFoundBtn.addActionListener(e -> deleteSelectedReport(false));
        foundActions.add(deleteFoundBtn);
        foundPanel.add(foundActions, BorderLayout.SOUTH);

        tablesRow.add(foundPanel);

        contentPanel.add(tablesRow);

        contentPanel.add(Box.createVerticalStrut(24));

        // High Confidence Matches
        RoundedPanel matchSection = new RoundedPanel(12, FindBackTheme.CARD);
        matchSection.setLayout(new BorderLayout(10, 10));
        matchSection.setBorder(new EmptyBorder(16, 16, 16, 16));
        matchSection.setPreferredSize(new Dimension(0, 240));

        JLabel matchHeader = new JLabel("High Confidence Smart Matches (AI Rule-Based Engine)");
        matchHeader.setFont(FindBackTheme.getHeaderFont(16));
        matchHeader.setForeground(FindBackTheme.TEXT);
        matchSection.add(matchHeader, BorderLayout.NORTH);

        matchesModel = new DefaultTableModel(new String[]{"Lost Item", "Found Item", "Category", "Score", "Confidence", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable matchTable = new JTable(matchesModel);
        matchSection.add(new JScrollPane(matchTable), BorderLayout.CENTER);
        contentPanel.add(matchSection);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        refresh();
    }

    private void deleteSelectedReport(boolean isLost) {
        JTable table = isLost ? lostTable : foundTable;
        List<ItemReport> items = isLost ? currentLostItems : currentFoundItems;
        int row = table.getSelectedRow();
        if (row < 0 || items == null || row >= items.size()) {
            JOptionPane.showMessageDialog(this, "Please select a report to delete.", "Select Report", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ItemReport report = items.get(row);
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        if (!user.isStaffOrAdmin() && !user.getId().equals(report.getUserId())) {
            JOptionPane.showMessageDialog(this, "You can only delete reports that you reported.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete report: '" + report.getTitle() + "'?",
                "Confirm Delete Report", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = reportService.deleteReport(report.getId());
            if (deleted) {
                refresh();
                JOptionPane.showMessageDialog(this, "Report deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete report.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void refresh() {
        lostCard.setValue(String.valueOf(reportService.countLost()));
        foundCard.setValue(String.valueOf(reportService.countFound()));
        recoveredCard.setValue(String.valueOf(handoverService.countRecovered()));

        User user = SessionManager.getInstance().getCurrentUser();
        List<Match> userMatches = (user != null) ? matchingService.getMatchesForUser(user.getId()) : java.util.Collections.emptyList();
        List<Match> displayMatches = !userMatches.isEmpty() ? userMatches : matchingService.getAllMatches();
        matchesCard.setValue(String.valueOf(displayMatches.size()));

        // Populate Lost Table
        recentLostModel.setRowCount(0);
        currentLostItems = reportService.getRecentLost(10);
        for (ItemReport r : currentLostItems) {
            recentLostModel.addRow(new Object[]{
                    r.getTitle(),
                    r.getCategoryName() != null ? r.getCategoryName() : "General",
                    r.getLocationName() != null ? r.getLocationName() : "Campus",
                    r.getEventDate() != null ? r.getEventDate().toString() : "-",
                    r.getStatus() != null ? r.getStatus().name() : "OPEN"
            });
        }

        // Populate Found Table
        recentFoundModel.setRowCount(0);
        currentFoundItems = reportService.getRecentFound(10);
        for (ItemReport r : currentFoundItems) {
            recentFoundModel.addRow(new Object[]{
                    r.getTitle(),
                    r.getCategoryName() != null ? r.getCategoryName() : "General",
                    r.getLocationName() != null ? r.getLocationName() : "Campus",
                    r.getEventDate() != null ? r.getEventDate().toString() : "-",
                    r.getCustodyStatus() != null ? r.getCustodyStatus().name() : "WITH_FINDER"
            });
        }

        // Populate Matches Table
        matchesModel.setRowCount(0);
        for (Match m : displayMatches) {
            String conf = m.getTotalScore() >= 85 ? "Strong Match" : "Possible Match";
            matchesModel.addRow(new Object[]{
                    m.getLostTitle() != null ? m.getLostTitle() : ("Lost #" + m.getLostReportId()),
                    m.getFoundTitle() != null ? m.getFoundTitle() : ("Found #" + m.getFoundReportId()),
                    m.getLostCategory() != null ? m.getLostCategory() : "-",
                    String.format("%.1f%%", m.getTotalScore()),
                    conf,
                    m.getStatus() != null ? m.getStatus().name() : "POTENTIAL"
            });
        }
    }
}
