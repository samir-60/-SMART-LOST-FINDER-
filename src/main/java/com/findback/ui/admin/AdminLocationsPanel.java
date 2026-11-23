package com.findback.ui.admin;

import com.findback.dao.LocationDao;
import com.findback.model.CampusLocation;
import com.findback.service.AdminService;
import com.findback.ui.FindBackTheme;
import com.findback.ui.MainFrame;
import com.findback.ui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminLocationsPanel extends JPanel implements MainFrame.Refreshable {
    private final LocationDao locationDao = new LocationDao();
    private final AdminService adminService = new AdminService();

    private DefaultTableModel tableModel;
    private JTable locationTable;
    private List<CampusLocation> allLocations;

    public AdminLocationsPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(FindBackTheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Campus Location Management");
        title.setFont(FindBackTheme.getHeaderFont(26));
        title.setForeground(FindBackTheme.TEXT);
        JLabel subtitle = new JLabel("Metropolitan University Sylhet building, zone, and facility locations.");
        subtitle.setFont(FindBackTheme.getBodyFont(14));
        subtitle.setForeground(new Color(110, 120, 135));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Table Card
        tableModel = new DefaultTableModel(new String[]{"ID", "Location Name", "Campus Zone", "Description", "Active"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        locationTable = new JTable(tableModel);
        locationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        RoundedPanel tableCard = new RoundedPanel(12, FindBackTheme.CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        tableCard.add(new JScrollPane(locationTable), BorderLayout.CENTER);

        // Action Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnBar.setOpaque(false);

        JButton addBtn = FindBackTheme.createPrimaryButton("+ Add Campus Location");
        addBtn.addActionListener(e -> addLocation());
        btnBar.add(addBtn);

        JButton toggleBtn = FindBackTheme.createStyledButton("Toggle Active / Inactive", FindBackTheme.CARD, FindBackTheme.TEXT);
        toggleBtn.setBorder(BorderFactory.createLineBorder(FindBackTheme.BORDER));
        toggleBtn.addActionListener(e -> toggleActiveStatus());
        btnBar.add(toggleBtn);

        JButton removeBtn = FindBackTheme.createDangerButton("Remove Location");
        removeBtn.addActionListener(e -> removeLocation());
        btnBar.add(removeBtn);

        tableCard.add(btnBar, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);

        refresh();
    }

    private void removeLocation() {
        int row = locationTable.getSelectedRow();
        if (row < 0 || allLocations == null || row >= allLocations.size()) {
            JOptionPane.showMessageDialog(this, "Please select a location from the table to remove.", "Select Location", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CampusLocation l = allLocations.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove campus location '" + l.getName() + "'?\n" +
                "Note: If items are linked to this location, it may be safer to Toggle Inactive instead.",
                "Confirm Remove", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = adminService.deleteLocation(l.getId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Location '" + l.getName() + "' removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete location '" + l.getName() + "' because items are currently referencing it. Marking as Inactive instead.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                adminService.toggleLocation(l.getId());
            }
            refresh();
        }
    }

    private void addLocation() {
        String name = JOptionPane.showInputDialog(this, "Enter location name:", "New Location", JOptionPane.QUESTION_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;

        String zone = JOptionPane.showInputDialog(this, "Enter zone (e.g. Academic Zone A):", "Campus Zone", JOptionPane.QUESTION_MESSAGE);
        String desc = JOptionPane.showInputDialog(this, "Enter details:", "Location Description", JOptionPane.QUESTION_MESSAGE);

        adminService.createLocation(name.trim(), zone != null ? zone.trim() : "", desc != null ? desc.trim() : "");
        refresh();
    }

    private void toggleActiveStatus() {
        int row = locationTable.getSelectedRow();
        if (row < 0 || allLocations == null || row >= allLocations.size()) return;
        CampusLocation l = allLocations.get(row);
        adminService.toggleLocation(l.getId());
        refresh();
    }

    @Override
    public void refresh() {
        allLocations = locationDao.findAll();
        tableModel.setRowCount(0);
        for (CampusLocation l : allLocations) {
            tableModel.addRow(new Object[]{
                    l.getId(),
                    l.getName(),
                    l.getZone() != null ? l.getZone() : "-",
                    l.getDescription() != null ? l.getDescription() : "-",
                    l.isActive() ? "Active" : "INACTIVE"
            });
        }
    }
}
