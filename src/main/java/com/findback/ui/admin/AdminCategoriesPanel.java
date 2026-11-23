package com.findback.ui.admin;

import com.findback.dao.CategoryDao;
import com.findback.model.Category;
import com.findback.service.AdminService;
import com.findback.ui.FindBackTheme;
import com.findback.ui.MainFrame;
import com.findback.ui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminCategoriesPanel extends JPanel implements MainFrame.Refreshable {
    private final CategoryDao categoryDao = new CategoryDao();
    private final AdminService adminService = new AdminService();

    private DefaultTableModel tableModel;
    private JTable categoryTable;
    private List<Category> allCategories;

    public AdminCategoriesPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(FindBackTheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Category Management");
        title.setFont(FindBackTheme.getHeaderFont(26));
        title.setForeground(FindBackTheme.TEXT);
        JLabel subtitle = new JLabel("Manage item categories used for classification and smart matching weights.");
        subtitle.setFont(FindBackTheme.getBodyFont(14));
        subtitle.setForeground(new Color(110, 120, 135));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Table Card
        tableModel = new DefaultTableModel(new String[]{"ID", "Category Name", "Description", "Icon", "Active"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        categoryTable = new JTable(tableModel);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        RoundedPanel tableCard = new RoundedPanel(12, FindBackTheme.CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        tableCard.add(new JScrollPane(categoryTable), BorderLayout.CENTER);

        // Action Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnBar.setOpaque(false);

        JButton addBtn = FindBackTheme.createPrimaryButton("+ Add New Category");
        addBtn.addActionListener(e -> addCategory());
        btnBar.add(addBtn);

        JButton toggleBtn = FindBackTheme.createStyledButton("Toggle Active / Inactive", FindBackTheme.CARD, FindBackTheme.TEXT);
        toggleBtn.setBorder(BorderFactory.createLineBorder(FindBackTheme.BORDER));
        toggleBtn.addActionListener(e -> toggleActiveStatus());
        btnBar.add(toggleBtn);

        JButton removeBtn = FindBackTheme.createDangerButton("Remove Category");
        removeBtn.addActionListener(e -> removeCategory());
        btnBar.add(removeBtn);

        tableCard.add(btnBar, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);

        refresh();
    }

    private void removeCategory() {
        int row = categoryTable.getSelectedRow();
        if (row < 0 || allCategories == null || row >= allCategories.size()) {
            JOptionPane.showMessageDialog(this, "Please select a category from the table to remove.", "Select Category", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Category c = allCategories.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove category '" + c.getName() + "'?\n" +
                "Note: If items are linked to this category, it may be safer to Toggle Inactive instead.",
                "Confirm Remove", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = adminService.deleteCategory(c.getId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Category '" + c.getName() + "' removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete category '" + c.getName() + "' because items are currently referencing it. Marking as Inactive instead.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                adminService.toggleCategory(c.getId());
            }
            refresh();
        }
    }

    private void addCategory() {
        String name = JOptionPane.showInputDialog(this, "Enter category name:", "New Category", JOptionPane.QUESTION_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;

        String desc = JOptionPane.showInputDialog(this, "Enter brief description:", "Category Description", JOptionPane.QUESTION_MESSAGE);
        adminService.createCategory(name.trim(), desc != null ? desc.trim() : "", "tag");
        refresh();
    }

    private void toggleActiveStatus() {
        int row = categoryTable.getSelectedRow();
        if (row < 0 || allCategories == null || row >= allCategories.size()) return;
        Category c = allCategories.get(row);
        adminService.toggleCategory(c.getId());
        refresh();
    }

    @Override
    public void refresh() {
        allCategories = categoryDao.findAll();
        tableModel.setRowCount(0);
        for (Category c : allCategories) {
            tableModel.addRow(new Object[]{
                    c.getId(),
                    c.getName(),
                    c.getDescription() != null ? c.getDescription() : "-",
                    c.getIconName(),
                    c.isActive() ? "Active" : "INACTIVE"
            });
        }
    }
}
