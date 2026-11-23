package com.findback.ui;

import com.findback.model.User;
import com.findback.service.AuthService;
import com.findback.service.SessionManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterDialog extends JDialog {
    private JTextField fullNameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField studentIdField;
    private JTextField departmentField;
    private JTextField phoneField;
    private final JFrame parent;

    public RegisterDialog(JFrame parent) {
        super(parent, "Register — FindBack", true);
        this.parent = parent;
        setSize(480, 640);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(FindBackTheme.BG);
        mainPanel.setBorder(new EmptyBorder(24, 40, 24, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Header
        JLabel logo = new JLabel("Create Account", SwingConstants.CENTER);
        logo.setFont(FindBackTheme.getHeaderFont(26));
        logo.setForeground(FindBackTheme.PRIMARY);
        gbc.insets = new Insets(0, 0, 4, 0);
        mainPanel.add(logo, gbc);

        JLabel subtitle = new JLabel("Join FindBack to start recovering your items", SwingConstants.CENTER);
        subtitle.setFont(FindBackTheme.getBodyFont(13));
        subtitle.setForeground(new Color(120, 130, 150));
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(subtitle, gbc);

        // Form fields
        fullNameField = addFormField(mainPanel, gbc, "Full Name", "Enter your full name");
        emailField = addFormField(mainPanel, gbc, "Email Address", "Enter your email");
        passwordField = addPasswordField(mainPanel, gbc, "Password", "Create a password");
        confirmPasswordField = addPasswordField(mainPanel, gbc, "Confirm Password", "Confirm your password");
        studentIdField = addFormField(mainPanel, gbc, "Student ID", "Enter your student ID");
        departmentField = addFormField(mainPanel, gbc, "Department", "Enter your department");
        phoneField = addFormField(mainPanel, gbc, "Phone Number", "Enter your phone number");

        // Register button
        JButton registerBtn = FindBackTheme.createPrimaryButton("Create Account");
        registerBtn.setPreferredSize(new Dimension(0, 44));
        registerBtn.addActionListener(e -> doRegister());
        gbc.insets = new Insets(8, 0, 10, 0);
        mainPanel.add(registerBtn, gbc);

        // Cancel button
        JButton cancelBtn = FindBackTheme.createOutlineButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(0, 40));
        cancelBtn.addActionListener(e -> dispose());
        gbc.insets = new Insets(0, 0, 10, 0);
        mainPanel.add(cancelBtn, gbc);

        // Login link
        JLabel loginLink = new JLabel("<html><u>Already have an account? Sign in</u></html>", SwingConstants.CENTER);
        loginLink.setFont(FindBackTheme.getBodyFont(13));
        loginLink.setForeground(FindBackTheme.SECONDARY);
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
                new LoginDialog(RegisterDialog.this.parent).setVisible(true);
            }
        });
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(loginLink, gbc);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        setContentPane(scrollPane);
    }

    private JTextField addFormField(JPanel panel, GridBagConstraints gbc, String labelText, String placeholder) {
        JLabel label = new JLabel(labelText);
        label.setFont(FindBackTheme.getBodyFont(12));
        label.setForeground(FindBackTheme.TEXT);
        gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(label, gbc);

        JTextField field = new JTextField();
        field.setFont(FindBackTheme.getBodyFont(14));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setPreferredSize(new Dimension(0, 36));
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(field, gbc);
        return field;
    }

    private JPasswordField addPasswordField(JPanel panel, GridBagConstraints gbc, String labelText, String placeholder) {
        JLabel label = new JLabel(labelText);
        label.setFont(FindBackTheme.getBodyFont(12));
        label.setForeground(FindBackTheme.TEXT);
        gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(label, gbc);

        JPasswordField field = new JPasswordField();
        field.setFont(FindBackTheme.getBodyFont(14));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setPreferredSize(new Dimension(0, 36));
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(field, gbc);
        return field;
    }

    private void doRegister() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String studentId = studentIdField.getText().trim();
        String department = departmentField.getText().trim();
        String phone = phoneField.getText().trim();

        // Validation
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your full name, email, and password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                AuthService authService = new AuthService();
                return authService.register(fullName, email, password, studentId, department, phone);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    JOptionPane.showMessageDialog(RegisterDialog.this,
                            "Registration successful! Welcome to FindBack, " + user.getFullName() + "!", "Welcome", JOptionPane.INFORMATION_MESSAGE);
                    SessionManager.getInstance().login(user);
                    MainFrame mainFrame = new MainFrame();
                    dispose();
                    if (RegisterDialog.this.parent != null) {
                        RegisterDialog.this.parent.setVisible(false);
                        RegisterDialog.this.parent.dispose();
                    }
                    mainFrame.setVisible(true);
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(RegisterDialog.this,
                            "Registration failed: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
