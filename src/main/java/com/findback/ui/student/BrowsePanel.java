package com.findback.ui.student;

import com.findback.dao.CategoryDao;
import com.findback.dao.LocationDao;
import com.findback.enums.ReportType;
import com.findback.model.CampusLocation;
import com.findback.model.Category;
import com.findback.model.Claim;
import com.findback.model.ItemReport;
import com.findback.model.User;
import com.findback.service.ClaimService;
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

public class BrowsePanel extends JPanel implements MainFrame.Refreshable {
    private final ReportService reportService = new ReportService();
    private final ClaimService claimService = new ClaimService();
    private final CategoryDao categoryDao = new CategoryDao();
    private final LocationDao locationDao = new LocationDao();

    private JTextField searchField;
    private JComboBox<String> typeCombo;
    private JComboBox<LostReportPanel.CategoryItem> categoryCombo;
    private JComboBox<LostReportPanel.LocationItem> locationCombo;
    private JTextField colorFilterField;

    private DefaultTableModel tableModel;
    private JTable itemTable;
    private List<ItemReport> currentResults;

    public BrowsePanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(FindBackTheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Top Header & Filters
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        JLabel title = new JLabel("Browse & Search Campus Items");
        title.setFont(FindBackTheme.getHeaderFont(26));
        title.setForeground(FindBackTheme.TEXT);
        topPanel.add(title);
        topPanel.add(Box.createVerticalStrut(16));

        // Filter Bar Card
        RoundedPanel filterCard = new RoundedPanel(12, FindBackTheme.CARD);
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        filterCard.setBorder(new EmptyBorder(6, 10, 6, 10));

        searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(180, 36));
        searchField.putClientProperty("JTextField.placeholderText", "Search keywords...");
        filterCard.add(searchField);

        typeCombo = new JComboBox<>(new String[]{"All Reports", "LOST Items", "FOUND Items"});
        typeCombo.setPreferredSize(new Dimension(130, 36));
        filterCard.add(typeCombo);

        categoryCombo = new JComboBox<>();
        categoryCombo.setPreferredSize(new Dimension(140, 36));
        filterCard.add(categoryCombo);

        locationCombo = new JComboBox<>();
        locationCombo.setPreferredSize(new Dimension(150, 36));
        filterCard.add(locationCombo);

        colorFilterField = new JTextField(8);
        colorFilterField.setPreferredSize(new Dimension(100, 36));
        colorFilterField.putClientProperty("JTextField.placeholderText", "Color");
        filterCard.add(colorFilterField);

        JButton searchBtn = FindBackTheme.createPrimaryButton("\uD83D\uDD0D Search");
        searchBtn.addActionListener(e -> applySearch());
        filterCard.add(searchBtn);

        JButton resetBtn = FindBackTheme.createStyledButton("Reset", FindBackTheme.CARD, FindBackTheme.TEXT);
        resetBtn.setBorder(BorderFactory.createLineBorder(FindBackTheme.BORDER));
        resetBtn.addActionListener(e -> resetFilters());
        filterCard.add(resetBtn);

        topPanel.add(filterCard);
        add(topPanel, BorderLayout.NORTH);

        // Results Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Type", "Title", "Category", "Location", "Color", "Date", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        itemTable = new JTable(tableModel);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showItemDetails();
                }
            }
        });

        RoundedPanel tableCard = new RoundedPanel(12, FindBackTheme.CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        tableCard.add(new JScrollPane(itemTable), BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottomBar.setOpaque(false);
        JButton viewBtn = FindBackTheme.createStyledButton("View Item Details", FindBackTheme.CARD, FindBackTheme.PRIMARY);
        viewBtn.setBorder(BorderFactory.createLineBorder(FindBackTheme.BORDER));
        viewBtn.addActionListener(e -> showItemDetails());
        bottomBar.add(viewBtn);

        JButton claimBtn = FindBackTheme.createPrimaryButton("Send Claim / Request Item");
        claimBtn.addActionListener(e -> claimSelectedItem());
        bottomBar.add(claimBtn);

        tableCard.add(bottomBar, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);

        refresh();
    }

    private void claimSelectedItem() {
        int selected = itemTable.getSelectedRow();
        if (selected < 0 || currentResults == null || selected >= currentResults.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table to submit an ownership claim.", "Select Item", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ItemReport r = currentResults.get(selected);
        if (r.getReportType() != ReportType.FOUND) {
            JOptionPane.showMessageDialog(this, "Claims can only be filed against FOUND items held on campus.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Please log in to submit a claim.", "Authentication Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String proof = JOptionPane.showInputDialog(this,
                "Enter confidential proof of ownership for '" + r.getTitle() + "':\n" +
                "(e.g. Serial number, distinctive marks, passcode/lock, or detailed inside contents)",
                "Submit Ownership Claim", JOptionPane.QUESTION_MESSAGE);

        if (proof != null && !proof.trim().isEmpty()) {
            try {
                Claim claim = claimService.submitClaim(user.getId(), null, r.getId(), null, proof.trim(), null);
                JOptionPane.showMessageDialog(this,
                        "Ownership Claim #" + claim.getId() + " submitted successfully!\n" +
                        "Campus authorities will review your evidence. You can track progress under the Claims tab.",
                        "Claim Submitted", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error submitting claim: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applySearch() {
        String kw = searchField.getText().trim();
        int typeIdx = typeCombo.getSelectedIndex();
        ReportType type = null;
        if (typeIdx == 1) type = ReportType.LOST;
        else if (typeIdx == 2) type = ReportType.FOUND;

        LostReportPanel.CategoryItem cat = (LostReportPanel.CategoryItem) categoryCombo.getSelectedItem();
        Long catId = (cat != null && cat.id > 0) ? cat.id : null;

        LostReportPanel.LocationItem loc = (LostReportPanel.LocationItem) locationCombo.getSelectedItem();
        Long locId = (loc != null && loc.id > 0) ? loc.id : null;

        String color = colorFilterField.getText().trim();

        currentResults = reportService.search(kw, catId, locId, type, color);
        populateTable(currentResults);
    }

    private void resetFilters() {
        searchField.setText("");
        typeCombo.setSelectedIndex(0);
        if (categoryCombo.getItemCount() > 0) categoryCombo.setSelectedIndex(0);
        if (locationCombo.getItemCount() > 0) locationCombo.setSelectedIndex(0);
        colorFilterField.setText("");
        applySearch();
    }

    private void populateTable(List<ItemReport> items) {
        tableModel.setRowCount(0);
        for (ItemReport r : items) {
            tableModel.addRow(new Object[]{
                    r.getId(),
                    r.getReportType() != null ? r.getReportType().name() : "LOST",
                    r.getTitle(),
                    r.getCategoryName() != null ? r.getCategoryName() : "-",
                    r.getLocationName() != null ? r.getLocationName() : "-",
                    r.getColor() != null ? r.getColor() : "-",
                    r.getEventDate() != null ? r.getEventDate().toString() : "-",
                    r.getStatus() != null ? r.getStatus().name() : "OPEN"
            });
        }
    }

    private void showItemDetails() {
        int selected = itemTable.getSelectedRow();
        if (selected < 0 || currentResults == null || selected >= currentResults.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table to view details.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ItemReport r = currentResults.get(selected);
        StringBuilder sb = new StringBuilder();
        sb.append("Report ID: #").append(r.getId()).append("\n");
        sb.append("Type: ").append(r.getReportType()).append("\n");
        sb.append("Title: ").append(r.getTitle()).append("\n");
        sb.append("Category: ").append(r.getCategoryName()).append("\n");
        sb.append("Location: ").append(r.getLocationName()).append("\n");
        if (r.getSpecificLocation() != null) sb.append("Specific: ").append(r.getSpecificLocation()).append("\n");
        sb.append("Color: ").append(r.getColor()).append("\n");
        sb.append("Date: ").append(r.getEventDate()).append("\n");
        sb.append("Status: ").append(r.getStatus()).append("\n");
        if (r.getCustodyStatus() != null) sb.append("Custody: ").append(r.getCustodyStatus()).append("\n");
        sb.append("\nDescription:\n").append(r.getDescription());

        JOptionPane.showMessageDialog(this, sb.toString(), "Item Report Details", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void refresh() {
        categoryCombo.removeAllItems();
        categoryCombo.addItem(new LostReportPanel.CategoryItem(0L, "All Categories"));
        for (Category c : categoryDao.findAllActive()) {
            categoryCombo.addItem(new LostReportPanel.CategoryItem(c.getId(), c.getName()));
        }

        locationCombo.removeAllItems();
        locationCombo.addItem(new LostReportPanel.LocationItem(0L, "All Locations"));
        for (CampusLocation l : locationDao.findAllActive()) {
            locationCombo.addItem(new LostReportPanel.LocationItem(l.getId(), l.getName()));
        }

        applySearch();
    }
}
