package com.findback.ui.admin;

import com.findback.service.AdminService;
import com.findback.ui.FindBackTheme;
import com.findback.ui.MainFrame;
import com.findback.ui.components.RoundedPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class AdminAnalyticsPanel extends JPanel implements MainFrame.Refreshable {
    private final AdminService adminService = new AdminService();

    private JPanel pieChartPanel;
    private JPanel barChartPanel;

    public AdminAnalyticsPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(FindBackTheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Campus Lost & Found Visual Analytics (JFreeChart)");
        title.setFont(FindBackTheme.getHeaderFont(26));
        title.setForeground(FindBackTheme.TEXT);
        JLabel subtitle = new JLabel("Interactive chart breakdowns by category and campus location distribution.");
        subtitle.setFont(FindBackTheme.getBodyFont(14));
        subtitle.setForeground(new Color(110, 120, 135));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Center Split for Charts
        JPanel chartsGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsGrid.setOpaque(false);

        // Category Pie Chart Card
        RoundedPanel pieCard = new RoundedPanel(12, FindBackTheme.CARD);
        pieCard.setLayout(new BorderLayout());
        pieCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        pieChartPanel = new JPanel(new BorderLayout());
        pieChartPanel.setOpaque(false);
        pieCard.add(pieChartPanel, BorderLayout.CENTER);
        chartsGrid.add(pieCard);

        // Location Bar Chart Card
        RoundedPanel barCard = new RoundedPanel(12, FindBackTheme.CARD);
        barCard.setLayout(new BorderLayout());
        barCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        barChartPanel = new JPanel(new BorderLayout());
        barChartPanel.setOpaque(false);
        barCard.add(barChartPanel, BorderLayout.CENTER);
        chartsGrid.add(barCard);

        add(chartsGrid, BorderLayout.CENTER);

        refresh();
    }

    @Override
    public void refresh() {
        Map<String, Object> stats = adminService.getDashboardStats();

        // 1. Category Pie Chart
        DefaultPieDataset<String> pieDataset = new DefaultPieDataset<>();
        @SuppressWarnings("unchecked")
        Map<String, Long> catMap = (Map<String, Long>) stats.get("reportsByCategory");
        if (catMap != null) {
            catMap.forEach(pieDataset::setValue);
        }
        JFreeChart pieChart = ChartFactory.createPieChart("Campus Reports by Category", pieDataset, true, true, false);
        pieChart.setBackgroundPaint(Color.WHITE);
        pieChartPanel.removeAll();
        pieChartPanel.add(new ChartPanel(pieChart), BorderLayout.CENTER);
        pieChartPanel.revalidate();
        pieChartPanel.repaint();

        // 2. Location Bar Chart
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        @SuppressWarnings("unchecked")
        Map<String, Long> locMap = (Map<String, Long>) stats.get("reportsByLocation");
        if (locMap != null) {
            locMap.forEach((location, count) -> barDataset.addValue(count, "Reports", location));
        }
        JFreeChart barChart = ChartFactory.createBarChart(
                "Incident Distribution by Campus Location",
                "Campus Location",
                "Report Count",
                barDataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );
        barChart.setBackgroundPaint(Color.WHITE);
        barChartPanel.removeAll();
        barChartPanel.add(new ChartPanel(barChart), BorderLayout.CENTER);
        barChartPanel.revalidate();
        barChartPanel.repaint();
    }
}
