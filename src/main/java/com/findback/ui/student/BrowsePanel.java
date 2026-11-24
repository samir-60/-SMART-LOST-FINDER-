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

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * Browse and search campus lost and found items.
 * Clean, human-written Java Swing interface with search filters, WhatsApp contact, and delete actions.
 */
public class BrowsePanel extends JPanel implements MainFrame.Refreshable {
    private final ReportService reportService = new ReportService();
    private final ClaimService claimService = new ClaimService();
    private final CategoryDao categoryDao = new CategoryDao();
    private final LocationDao locationDao = new LocationDao();

    private JTextField searchField;
    private JComboBox<String> typeCombo;
    private JComboBox<FilterItem> categoryCombo;
    private JComboBox<FilterItem> locationCombo;
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

        JButton searchBtn = FindBackTheme.createPrimaryButton("Search");
        searchBtn.addActionListener(e -> applySearch());
        filterCard.add(searchBtn);

        JButton resetBtn = FindBackTheme.createStyledButton("Reset", FindBackTheme.CARD, FindBackTheme.TEXT);
        resetBtn.setBorder(BorderFactory.createLineBorder(FindBackTheme.BORDER));
        resetBtn.addActionListener(e -> resetFilters());
        filterCard.add(resetBtn);

        topPanel.add(filterCard);
        add(topPanel, BorderLayout.NORTH);

        // Results Table
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Type", "Title", "Category", "Location", "Color", "Date", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        itemTable = new JTable(tableModel);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.setAutoCreateRowSorter(true);
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

        JButton deleteBtn = FindBackTheme.createDangerButton("Delete Report");
        deleteBtn.addActionListener(e -> deleteSelectedItem());
        bottomBar.add(deleteBtn);

        tableCard.add(bottomBar, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);

        refresh();
    }

    private void deleteSelectedItem() {
        int selected = itemTable.getSelectedRow();
        if (selected < 0 || currentResults == null) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table to delete.", "Select Item", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = itemTable.convertRowIndexToModel(selected);
        if (modelRow >= currentResults.size()) return;
        ItemReport r = currentResults.get(modelRow);

        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Please log in to manage reports.", "Authentication Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!user.isStaffOrAdmin() && !user.getId().equals(r.getUserId())) {
            JOptionPane.showMessageDialog(this, "You can only delete your own reports.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete '" + r.getTitle() + "'?",
                "Confirm Delete Report", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = reportService.deleteReport(r.getId());
            if (deleted) {
                refresh();
                JOptionPane.showMessageDialog(this, "Report deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete report.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void claimSelectedItem() {
        int selected = itemTable.getSelectedRow();
        if (selected < 0 || currentResults == null) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table to submit an ownership claim.",
                    "Select Item", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = itemTable.convertRowIndexToModel(selected);
        if (modelRow >= currentResults.size()) return;
        ItemReport r = currentResults.get(modelRow);

        submitClaimDirectly(r);
    }

    private void applySearch() {
        String kw = searchField.getText().trim();
        int typeIdx = typeCombo.getSelectedIndex();
        ReportType type = null;
        if (typeIdx == 1) type = ReportType.LOST;
        else if (typeIdx == 2) type = ReportType.FOUND;

        FilterItem cat = (FilterItem) categoryCombo.getSelectedItem();
        Long catId = (cat != null && cat.id > 0) ? cat.id : null;

        FilterItem loc = (FilterItem) locationCombo.getSelectedItem();
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
        if (items == null) return;
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
        if (selected < 0 || currentResults == null) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table to view details.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = itemTable.convertRowIndexToModel(selected);
        if (modelRow >= currentResults.size()) return;
        ItemReport r = currentResults.get(modelRow);

        User currentUser = SessionManager.getInstance().getCurrentUser();
        boolean isOwnerOrAdmin = currentUser != null && (currentUser.isStaffOrAdmin() || currentUser.getId().equals(r.getUserId()));

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Item Report #" + r.getId() + " Details", true);
        dialog.setSize(520, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(16, 16));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 24, 20, 24));
        content.setBackground(Color.WHITE);

        // Header Title
        JLabel titleLbl = new JLabel(r.getTitle() != null ? r.getTitle() : "Item Details");
        titleLbl.setFont(FindBackTheme.getHeaderFont(20));
        titleLbl.setForeground(FindBackTheme.TEXT);
        content.add(titleLbl);
        content.add(Box.createVerticalStrut(12));

        // Optional Photo Preview
        if (r.getImagePath() != null && !r.getImagePath().trim().isEmpty()) {
            File imgFile = new File(r.getImagePath());
            if (imgFile.exists()) {
                try {
                    BufferedImage bimg = ImageIO.read(imgFile);
                    if (bimg != null) {
                        Image scaled = bimg.getScaledInstance(220, 140, Image.SCALE_SMOOTH);
                        JLabel imgLbl = new JLabel(new ImageIcon(scaled));
                        imgLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                        imgLbl.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
                        content.add(imgLbl);
                        content.add(Box.createVerticalStrut(12));
                    }
                } catch (Exception ignored) {}
            }
        }

        // Details Grid
        JPanel infoGrid = new JPanel(new GridLayout(0, 2, 8, 8));
        infoGrid.setOpaque(false);
        infoGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        addDetailRow(infoGrid, "Report Type:", r.getReportType() != null ? r.getReportType().name() : "LOST");
        addDetailRow(infoGrid, "Category:", r.getCategoryName() != null ? r.getCategoryName() : "General");
        addDetailRow(infoGrid, "Location:", r.getLocationName() != null ? r.getLocationName() : "Campus");
        if (r.getSpecificLocation() != null && !r.getSpecificLocation().isEmpty()) {
            addDetailRow(infoGrid, "Specific Spot:", r.getSpecificLocation());
        }
        addDetailRow(infoGrid, "Color:", r.getColor() != null ? r.getColor() : "-");
        addDetailRow(infoGrid, "Event Date:", r.getEventDate() != null ? r.getEventDate().toString() : "-");
        addDetailRow(infoGrid, "Status:", r.getStatus() != null ? r.getStatus().name() : "OPEN");
        if (r.getCustodyStatus() != null) {
            addDetailRow(infoGrid, "Custody:", r.getCustodyStatus().name());
        }

        String contact = r.getContactPhone();
        if (contact != null && !contact.trim().isEmpty()) {
            addDetailRow(infoGrid, "Contact / WhatsApp:", contact);
        }

        content.add(infoGrid);
        content.add(Box.createVerticalStrut(14));

        // Description Box
        JLabel descHeader = new JLabel("Description & Marks:");
        descHeader.setFont(FindBackTheme.getHeaderFont(13));
        descHeader.setForeground(FindBackTheme.TEXT);
        content.add(descHeader);
        content.add(Box.createVerticalStrut(4));

        JTextArea descArea = new JTextArea(r.getDescription() != null ? r.getDescription() : "No details provided.");
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setBackground(new Color(248, 250, 252));
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        content.add(new JScrollPane(descArea));

        dialog.add(content, BorderLayout.CENTER);

        // Buttons Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnBar.setBackground(FindBackTheme.BG);

        if (contact != null && !contact.trim().isEmpty()) {
            JButton waBtn = FindBackTheme.createStyledButton("Chat on WhatsApp", new Color(37, 211, 102), Color.WHITE);
            waBtn.addActionListener(e -> openWhatsAppChat(contact));
            btnBar.add(waBtn);
        }

        if (r.isFound() && (currentUser == null || !currentUser.getId().equals(r.getUserId()))) {
            JButton claimBtn = FindBackTheme.createPrimaryButton("Submit Claim");
            claimBtn.addActionListener(e -> {
                dialog.dispose();
                submitClaimDirectly(r);
            });
            btnBar.add(claimBtn);
        }

        if (isOwnerOrAdmin) {
            JButton deleteBtn = FindBackTheme.createDangerButton("Delete Report");
            deleteBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(dialog,
                        "Are you sure you want to permanently delete '" + r.getTitle() + "'?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    reportService.deleteReport(r.getId());
                    dialog.dispose();
                    refresh();
                    JOptionPane.showMessageDialog(BrowsePanel.this, "Report deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            btnBar.add(deleteBtn);
        }

        JButton closeBtn = FindBackTheme.createStyledButton("Close", Color.WHITE, FindBackTheme.TEXT);
        closeBtn.setBorder(BorderFactory.createLineBorder(FindBackTheme.BORDER));
        closeBtn.addActionListener(e -> dialog.dispose());
        btnBar.add(closeBtn);

        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(FindBackTheme.getHeaderFont(12));
        l.setForeground(new Color(100, 115, 130));

        JLabel v = new JLabel(value);
        v.setFont(FindBackTheme.getBodyFont(12));
        v.setForeground(FindBackTheme.TEXT);

        panel.add(l);
        panel.add(v);
    }

    private void openWhatsAppChat(String phone) {
        try {
            String cleanNumber = phone.replaceAll("[^0-9]", "");
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("https://wa.me/" + cleanNumber));
            } else {
                StringSelection sel = new StringSelection(phone);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
                JOptionPane.showMessageDialog(this, "WhatsApp number copied to clipboard: " + phone, "WhatsApp Contact", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            StringSelection sel = new StringSelection(phone);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
            JOptionPane.showMessageDialog(this, "WhatsApp number copied to clipboard: " + phone, "WhatsApp Contact", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void submitClaimDirectly(ItemReport r) {
        if (!r.isFound()) {
            JOptionPane.showMessageDialog(this, "Claims can only be filed against items reported as Found.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Please log in to submit an ownership claim.", "Authentication Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (r.getUserId() != null && user.getId().equals(r.getUserId())) {
            JOptionPane.showMessageDialog(this, "You reported finding this item yourself. You cannot claim your own found submission.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String proof = JOptionPane.showInputDialog(this,
                "Enter confidential ownership evidence for '" + r.getTitle() + "':\n(e.g. Serial number, specific engravings, markings, or inside contents)",
                "Claim Found Belonging", JOptionPane.QUESTION_MESSAGE);

        if (proof != null && !proof.trim().isEmpty()) {
            try {
                Claim claim = claimService.submitClaim(user.getId(), null, r.getId(), null, proof.trim(), null);
                JOptionPane.showMessageDialog(this,
                        "Ownership claim #" + claim.getId() + " submitted successfully!\nCampus staff and the finder will verify.",
                        "Claim Submitted", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Claim error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void refresh() {
        categoryCombo.removeAllItems();
        categoryCombo.addItem(new FilterItem(0L, "All Categories"));
        for (Category c : categoryDao.findAllActive()) {
            categoryCombo.addItem(new FilterItem(c.getId(), c.getName()));
        }

        locationCombo.removeAllItems();
        locationCombo.addItem(new FilterItem(0L, "All Locations"));
        for (CampusLocation l : locationDao.findAllActive()) {
            locationCombo.addItem(new FilterItem(l.getId(), l.getName()));
        }

        applySearch();
    }

    public static class FilterItem {
        public final Long id;
        public final String name;

        public FilterItem(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
