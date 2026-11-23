package com.findback.ui;

import com.findback.model.User;
import com.findback.service.AuthService;
import com.findback.service.SessionManager;
import com.findback.ui.components.RoundedPanel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginDialog extends JDialog {
    private JTextField emailField;
    private JPasswordField passwordField;
    private final JFrame parent;

    public LoginDialog(JFrame parent) {
        this(parent, "");
    }

    public LoginDialog(JFrame parent, String prefillEmail) {
        super(parent, "Login — FindBack", true);
        this.parent = parent;
        setSize(440, 480);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(FindBackTheme.BG);
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);

        // Logo
        JLabel logo = new JLabel("FindBack", SwingConstants.CENTER);
        logo.setFont(FindBackTheme.getHeaderFont(32));
        logo.setForeground(FindBackTheme.PRIMARY);
        gbc.insets = new Insets(0, 0, 4, 0);
        mainPanel.add(logo, gbc);

        JLabel subtitle = new JLabel("Sign in to your account", SwingConstants.CENTER);
        subtitle.setFont(FindBackTheme.getBodyFont(14));
        subtitle.setForeground(new Color(120, 130, 150));
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(subtitle, gbc);

        // Email
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(FindBackTheme.getBodyFont(13));
        emailLabel.setForeground(FindBackTheme.TEXT);
        gbc.insets = new Insets(0, 0, 6, 0);
        mainPanel.add(emailLabel, gbc);

        emailField = new JTextField();
        if (prefillEmail != null && !prefillEmail.isEmpty()) {
            emailField.setText(prefillEmail);
        }
        emailField.setFont(FindBackTheme.getBodyFont(14));
        emailField.putClientProperty("JTextField.placeholderText", "Enter your email");
        emailField.setPreferredSize(new Dimension(0, 40));
        gbc.insets = new Insets(0, 0, 16, 0);
        mainPanel.add(emailField, gbc);

        // Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(FindBackTheme.getBodyFont(13));
        passLabel.setForeground(FindBackTheme.TEXT);
        gbc.insets = new Insets(0, 0, 6, 0);
        mainPanel.add(passLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(FindBackTheme.getBodyFont(14));
        passwordField.putClientProperty("JTextField.placeholderText", "Enter your password");
        passwordField.setPreferredSize(new Dimension(0, 40));
        gbc.insets = new Insets(0, 0, 24, 0);
        mainPanel.add(passwordField, gbc);

        if (prefillEmail != null && !prefillEmail.isEmpty()) {
            SwingUtilities.invokeLater(() -> passwordField.requestFocusInWindow());
        }

        // Login button
        JButton loginBtn = FindBackTheme.createPrimaryButton("Sign In");
        loginBtn.setPreferredSize(new Dimension(0, 44));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.addActionListener(e -> doLogin());
        gbc.insets = new Insets(0, 0, 8, 0);
        mainPanel.add(loginBtn, gbc);

        // Demo shortcuts
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        demoPanel.setOpaque(false);
        JLabel demoLabel = new JLabel("Quick Fill:");
        demoLabel.setFont(FindBackTheme.getBodyFont(11));
        demoLabel.setForeground(new Color(110, 120, 135));

        JButton fillAdminBtn = new JButton("Admin");
        fillAdminBtn.setFont(FindBackTheme.getBodyFont(11));
        fillAdminBtn.setFocusPainted(false);
        fillAdminBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fillAdminBtn.addActionListener(e -> {
            emailField.setText("admin@findback.edu");
            passwordField.setText("Admin@123");
        });

        JButton fillStudentBtn = new JButton("Student");
        fillStudentBtn.setFont(FindBackTheme.getBodyFont(11));
        fillStudentBtn.setFocusPainted(false);
        fillStudentBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fillStudentBtn.addActionListener(e -> {
            emailField.setText("student@findback.edu");
            passwordField.setText("Student@123");
        });

        demoPanel.add(demoLabel);
        demoPanel.add(fillAdminBtn);
        demoPanel.add(fillStudentBtn);
        gbc.insets = new Insets(0, 0, 10, 0);
        mainPanel.add(demoPanel, gbc);

        // Cancel button
        JButton cancelBtn = FindBackTheme.createOutlineButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(0, 40));
        cancelBtn.addActionListener(e -> dispose());
        gbc.insets = new Insets(0, 0, 16, 0);
        mainPanel.add(cancelBtn, gbc);

        // Register link
        JLabel registerLink = new JLabel("<html><u>Don't have an account? Register instead</u></html>", SwingConstants.CENTER);
        registerLink.setFont(FindBackTheme.getBodyFont(13));
        registerLink.setForeground(FindBackTheme.SECONDARY);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
                new RegisterDialog(LoginDialog.this.parent).setVisible(true);
            }
        });
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(registerLink, gbc);

        // Enter key triggers login
        getRootPane().setDefaultButton(loginBtn);

        setContentPane(mainPanel);
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                AuthService authService = new AuthService();
                return authService.login(email, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        SessionManager.getInstance().login(user);
                        MainFrame mainFrame = new MainFrame();
                        dispose();
                        if (parent != null) {
                            parent.setVisible(false);
                            parent.dispose();
                        }
                        mainFrame.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(LoginDialog.this,
                                "Invalid email or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "Login failed: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
