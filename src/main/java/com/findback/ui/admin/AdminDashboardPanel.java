package com.findback.ui.admin;

import com.findback.model.AdminAction;
import com.findback.service.AdminService;
import com.findback.ui.FindBackTheme;
import com.findback.ui.MainFrame;
import com.findback.ui.components.RoundedPanel;
import com.findback.ui.components.StatsCard;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class AdminDashboardPanel extends JPanel implements MainFrame.Refreshable {
    private final AdminService adminService = new AdminService();

    private StatsCard usersCard;
    private StatsCard lostCard;
    private StatsCard foundCard;
    private StatsCard matchesCard;
    private StatsCard claimsCard;
    private StatsCard recoveredCard;

    private JPanel chartHolder;
    private DefaultTableModel auditTableModel;

    public AdminDashboardPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(FindBackTheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Administrative & Security Control Center");
        title.setFont(FindBackTheme.getHeaderFont(26));
        title.setForeground(FindBackTheme.TEXT);
        JLabel subtitle = new JLabel("Campus-wide monitoring, verification analytics, and tamper-evident audit log.");
        subtitle.setFont(FindBackTheme.getBodyFont(14));
        subtitle.setForeground(new Color(110, 120, 135));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Center Content Box
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // 6 Stats Cards (2 rows of 3)
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 16, 14));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        usersCard = new StatsCard("\uD83D\uDC65", "0", "Total Users", FindBackTheme.PRIMARY);
        lostCard = new StatsCard("\uD83D\uDCE6", "0", "Lost Reports", FindBackTheme.DANGER);
        foundCard = new StatsCard("\uD83D\uDD0E", "0", "Found Reports", FindBackTheme.SECONDARY);
        matchesCard = new StatsCard("\uD83C\uDFAF", "0", "Smart Matches", FindBackTheme.WARNING);
        claimsCard = new StatsCard("\uD83D\uDD12", "0", "Pending Claims", new Color(139, 92, 246));
        recoveredCard = new StatsCard("\u2705", "0", "Recovered Items", FindBackTheme.SUCCESS);

        statsGrid.add(usersCard);
        statsGrid.add(lostCard);
        statsGrid.add(foundCard);
        statsGrid.add(matchesCard);
        statsGrid.add(claimsCard);
        statsGrid.add(recoveredCard);
        center.add(statsGrid);

        center.add(Box.createVerticalStrut(20));

        // Middle Section: JFreeChart on Left, Audit Logs on Right
        JPanel middleSplit = new JPanel(new GridLayout(1, 2, 20, 0));
        middleSplit.setOpaque(false);
        middleSplit.setPreferredSize(new Dimension(0, 320));

        // Chart Card
        RoundedPanel chartCard = new RoundedPanel(12, FindBackTheme.CARD);
        chartCard.setLayout(new BorderLayout());
        chartCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        chartHolder = new JPanel(new BorderLayout());
        chartHolder.setOpaque(false);
        chartCard.add(chartHolder, BorderLayout.CENTER);
        middleSplit.add(chartCard);

        // Audit Log Card
        RoundedPanel auditCard = new RoundedPanel(12, FindBackTheme.CARD);
        auditCard.setLayout(new BorderLayout(10, 10));
        auditCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel auditTitle = new JLabel("Recent Administrative Audit Logs");
        auditTitle.setFont(FindBackTheme.getHeaderFont(15));
        auditTitle.setForeground(FindBackTheme.TEXT);
        auditCard.add(auditTitle, BorderLayout.NORTH);

        auditTableModel = new DefaultTableModel(new String[]{"Action", "Target", "Admin", "Timestamp", "Details"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable auditTable = new JTable(auditTableModel);
        auditCard.add(new JScrollPane(auditTable), BorderLayout.CENTER);
        middleSplit.add(auditCard);

        center.add(middleSplit);

        JScrollPane scroll = new JScrollPane(center);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    @Override
    public void refresh() {
        Map<String, Object> stats = adminService.getDashboardStats();
        usersCard.setValue(String.valueOf(stats.getOrDefault("totalUsers", 0)));
        lostCard.setValue(String.valueOf(stats.getOrDefault("totalLost", 0)));
        foundCard.setValue(String.valueOf(stats.getOrDefault("totalFound", 0)));
        matchesCard.setValue(String.valueOf(stats.getOrDefault("potentialMatches", 0)));
        claimsCard.setValue(String.valueOf(stats.getOrDefault("pendingClaims", 0)));
        recoveredCard.setValue(String.valueOf(stats.getOrDefault("recoveredItems", 0)));

        // Update Category Pie Chart
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        @SuppressWarnings("unchecked")
        Map<String, Long> catMap = (Map<String, Long>) stats.get("reportsByCategory");
        if (catMap != null) {
            catMap.forEach(dataset::setValue);
        }
        JFreeChart chart = ChartFactory.createPieChart("Reports by Category", dataset, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        chartHolder.removeAll();
        chartHolder.add(new ChartPanel(chart), BorderLayout.CENTER);
        chartHolder.revalidate();
        chartHolder.repaint();

        // Update Audit Table
        auditTableModel.setRowCount(0);
        List<AdminAction> logs = adminService.getRecentAuditLogs();
        for (AdminAction a : logs) {
            auditTableModel.addRow(new Object[]{
                    a.getActionType(),
                    a.getTargetEntity() + " #" + (a.getTargetId() != null ? a.getTargetId() : ""),
                    a.getAdminName() != null ? a.getAdminName() : "Admin",
                    a.getCreatedAt() != null ? a.getCreatedAt().toString().replace("T", " ") : "-",
                    a.getDetails()
            });
        }
    }
}
