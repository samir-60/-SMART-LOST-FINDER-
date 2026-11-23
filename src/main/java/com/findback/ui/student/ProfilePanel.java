package com.findback.ui.student;

import com.findback.model.User;
import com.findback.service.SessionManager;
import com.findback.ui.FindBackTheme;
import com.findback.ui.MainFrame;
import com.findback.ui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProfilePanel extends JPanel implements MainFrame.Refreshable {
    private JLabel nameLabel;
    private JLabel emailLabel;
    private JLabel roleLabel;
    private JLabel studentIdLabel;
    private JLabel deptLabel;
    private JLabel phoneLabel;

    public ProfilePanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(FindBackTheme.BG);
        setBorder(new EmptyBorder(32, 40, 32, 40));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("User Profile & Student Identity");
        title.setFont(FindBackTheme.getHeaderFont(26));
        title.setForeground(FindBackTheme.TEXT);
        JLabel subtitle = new JLabel("Official campus credentials and contact information registered at Metropolitan University.");
        subtitle.setFont(FindBackTheme.getBodyFont(14));
        subtitle.setForeground(new Color(110, 120, 135));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Center Profile Card
        RoundedPanel card = new RoundedPanel(14, FindBackTheme.CARD);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(32, 36, 32, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 12, 10, 12);
        gbc.weightx = 1.0;

        nameLabel = createValueLabel();
        emailLabel = createValueLabel();
        roleLabel = createValueLabel();
        studentIdLabel = createValueLabel();
        deptLabel = createValueLabel();
        phoneLabel = createValueLabel();

        addProfileRow(card, gbc, 0, "Full Name:", nameLabel);
        addProfileRow(card, gbc, 1, "University Email:", emailLabel);
        addProfileRow(card, gbc, 2, "Portal Role:", roleLabel);
        addProfileRow(card, gbc, 3, "Student / Employee ID:", studentIdLabel);
        addProfileRow(card, gbc, 4, "Academic Department:", deptLabel);
        addProfileRow(card, gbc, 5, "Contact Phone:", phoneLabel);

        JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        centerWrap.setOpaque(false);
        centerWrap.add(card);
        add(centerWrap, BorderLayout.CENTER);

        refresh();
    }

    private void addProfileRow(JPanel p, GridBagConstraints gbc, int row, String label, JLabel val) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel l = new JLabel(label);
        l.setFont(FindBackTheme.getHeaderFont(14));
        l.setForeground(FindBackTheme.TEXT);
        p.add(l, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0.7;
        p.add(val, gbc);
    }

    private JLabel createValueLabel() {
        JLabel l = new JLabel("-");
        l.setFont(FindBackTheme.getBodyFont(14));
        l.setForeground(new Color(60, 75, 95));
        return l;
    }

    @Override
    public void refresh() {
        User u = SessionManager.getInstance().getCurrentUser();
        if (u != null) {
            nameLabel.setText(u.getFullName());
            emailLabel.setText(u.getEmail());
            roleLabel.setText(u.getRole() != null ? u.getRole().getDisplayName() : "Student");
            studentIdLabel.setText(u.getStudentId() != null ? u.getStudentId() : "-");
            deptLabel.setText(u.getDepartment() != null ? u.getDepartment() : "-");
            phoneLabel.setText(u.getPhoneNumber() != null ? u.getPhoneNumber() : "-");
        }
    }
}
