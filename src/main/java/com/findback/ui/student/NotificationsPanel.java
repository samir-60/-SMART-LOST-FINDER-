package com.findback.ui.student;

import com.findback.model.Notification;
import com.findback.model.User;
import com.findback.service.NotificationService;
import com.findback.service.SessionManager;
import com.findback.ui.FindBackTheme;
import com.findback.ui.MainFrame;
import com.findback.ui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class NotificationsPanel extends JPanel implements MainFrame.Refreshable {
    private final NotificationService notificationService = new NotificationService();

    private DefaultTableModel tableModel;
    private JTable notifTable;
    private List<Notification> currentNotifs;

    public NotificationsPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(FindBackTheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Notifications");
        title.setFont(FindBackTheme.getHeaderFont(26));
        title.setForeground(FindBackTheme.TEXT);
        JLabel subtitle = new JLabel("Smart match alerts, claim updates, and campus proctor office notices.");
        subtitle.setFont(FindBackTheme.getBodyFont(14));
        subtitle.setForeground(new Color(110, 120, 135));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Table Card
        tableModel = new DefaultTableModel(new String[]{
                "Status", "Notification Title", "Message", "Type", "Timestamp"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        notifTable = new JTable(tableModel);
        notifTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        RoundedPanel tableCard = new RoundedPanel(12, FindBackTheme.CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        tableCard.add(new JScrollPane(notifTable), BorderLayout.CENTER);

        // Action Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnBar.setOpaque(false);

        JButton markReadBtn = FindBackTheme.createStyledButton("Mark Selected as Read", FindBackTheme.CARD, FindBackTheme.PRIMARY);
        markReadBtn.setBorder(BorderFactory.createLineBorder(FindBackTheme.BORDER));
        markReadBtn.addActionListener(e -> markSelectedRead());
        btnBar.add(markReadBtn);

        JButton markAllBtn = FindBackTheme.createPrimaryButton("Mark All as Read");
        markAllBtn.addActionListener(e -> markAllRead());
        btnBar.add(markAllBtn);

        tableCard.add(btnBar, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);

        refresh();
    }

    private void markSelectedRead() {
        int row = notifTable.getSelectedRow();
        if (row < 0 || currentNotifs == null || row >= currentNotifs.size()) return;
        Notification n = currentNotifs.get(row);
        notificationService.markRead(n.getId());
        refresh();
    }

    private void markAllRead() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;
        notificationService.markAllRead(user.getId());
        refresh();
    }

    @Override
    public void refresh() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        currentNotifs = notificationService.getForUser(user.getId());
        tableModel.setRowCount(0);
        for (Notification n : currentNotifs) {
            tableModel.addRow(new Object[]{
                    n.isRead() ? "Read" : "NEW",
                    n.getTitle(),
                    n.getMessage(),
                    n.getType() != null ? n.getType().name() : "GENERAL",
                    n.getCreatedAt() != null ? n.getCreatedAt().toString().replace("T", " ") : "-"
            });
        }
    }
}
