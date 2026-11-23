package com.findback.ui.admin;

import com.findback.dao.UserDao;
import com.findback.enums.Role;
import com.findback.model.User;
import com.findback.service.AdminService;
import com.findback.service.SessionManager;
import com.findback.ui.FindBackTheme;
import com.findback.ui.MainFrame;
import com.findback.ui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminUsersPanel extends JPanel implements MainFrame.Refreshable {
    private final UserDao userDao = new UserDao();
    private final AdminService adminService = new AdminService();

    private DefaultTableModel tableModel;
    private JTable usersTable;
    private List<User> allUsers;

    public AdminUsersPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(FindBackTheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("User Management & Roles");
        title.setFont(FindBackTheme.getHeaderFont(26));
        title.setForeground(FindBackTheme.TEXT);
        JLabel subtitle = new JLabel("Manage student, staff moderator, and proctor administrator accounts.");
        subtitle.setFont(FindBackTheme.getBodyFont(14));
        subtitle.setForeground(new Color(110, 120, 135));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Table Card
        tableModel = new DefaultTableModel(new String[]{
                "ID", "Full Name", "Email", "Student / Staff ID", "Department", "Phone", "Role", "Active"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        usersTable = new JTable(tableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        RoundedPanel tableCard = new RoundedPanel(12, FindBackTheme.CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        tableCard.add(new JScrollPane(usersTable), BorderLayout.CENTER);

        // Action Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnBar.setOpaque(false);

        JButton toggleBtn = FindBackTheme.createStyledButton("Toggle Active / Suspend", FindBackTheme.CARD, FindBackTheme.TEXT);
        toggleBtn.setBorder(BorderFactory.createLineBorder(FindBackTheme.BORDER));
        toggleBtn.addActionListener(e -> toggleActiveStatus());
        btnBar.add(toggleBtn);

        JButton roleBtn = FindBackTheme.createPrimaryButton("Change Role");
        roleBtn.addActionListener(e -> changeRole());
        btnBar.add(roleBtn);

        JButton removeBtn = FindBackTheme.createDangerButton("Remove User");
        removeBtn.addActionListener(e -> removeUser());
        btnBar.add(removeBtn);

        tableCard.add(btnBar, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);

        refresh();
    }

    private void removeUser() {
        int row = usersTable.getSelectedRow();
        if (row < 0 || allUsers == null || row >= allUsers.size()) {
            JOptionPane.showMessageDialog(this, "Please select a user to remove.", "Select User", JOptionPane.WARNING_MESSAGE);
            return;
        }
        User u = allUsers.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove user '" + u.getFullName() + "' (" + u.getEmail() + ")?\n" +
                "Note: If user has associated reports or claims, suspend account instead.",
                "Confirm Remove User", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = adminService.deleteUser(u.getId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "User removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete user with active reports/claims. User account suspended instead.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                adminService.toggleUserActive(u.getId());
            }
            refresh();
        }
    }

    private void toggleActiveStatus() {
        int row = usersTable.getSelectedRow();
        if (row < 0 || allUsers == null || row >= allUsers.size()) return;
        User u = allUsers.get(row);
        adminService.toggleUserActive(u.getId());
        refresh();
    }

    private void changeRole() {
        int row = usersTable.getSelectedRow();
        if (row < 0 || allUsers == null || row >= allUsers.size()) return;
        User u = allUsers.get(row);

        Role selected = (Role) JOptionPane.showInputDialog(this,
                "Select new role for " + u.getFullName() + ":",
                "Change Role", JOptionPane.QUESTION_MESSAGE, null, Role.values(), u.getRole());

        if (selected != null && selected != u.getRole()) {
            adminService.changeUserRole(u.getId(), selected);
            refresh();
        }
    }

    @Override
    public void refresh() {
        allUsers = userDao.findAll();
        tableModel.setRowCount(0);
        for (User u : allUsers) {
            tableModel.addRow(new Object[]{
                    u.getId(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getStudentId() != null ? u.getStudentId() : "-",
                    u.getDepartment() != null ? u.getDepartment() : "-",
                    u.getPhoneNumber() != null ? u.getPhoneNumber() : "-",
                    u.getRole() != null ? u.getRole().getDisplayName() : "Student",
                    u.isActive() ? "Active" : "SUSPENDED"
            });
        }
    }
}
