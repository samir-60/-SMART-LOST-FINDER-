package com.findback.ui.student;

import com.findback.dao.CategoryDao;
import com.findback.dao.LocationDao;
import com.findback.model.CampusLocation;
import com.findback.model.Category;
import com.findback.model.ItemReport;
import com.findback.model.User;
import com.findback.service.FileStorageService;
import com.findback.service.MatchingService;
import com.findback.service.ReportService;
import com.findback.service.SessionManager;
import com.findback.ui.FindBackTheme;
import com.findback.ui.MainFrame;
import com.findback.ui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

public class LostReportPanel extends JPanel implements MainFrame.Refreshable {
    private final ReportService reportService = new ReportService();
    private final MatchingService matchingService = new MatchingService();
    private final FileStorageService fileStorageService = new FileStorageService();
    private final CategoryDao categoryDao = new CategoryDao();
    private final LocationDao locationDao = new LocationDao();

    private JTextField titleField;
    private JComboBox<CategoryItem> categoryCombo;
    private JComboBox<LocationItem> locationCombo;
    private JTextField specificLocField;
    private JTextField colorField;
    private JTextField dateField;
    private JTextArea descArea;
    private JLabel imagePreviewLabel;
    private File selectedImageFile;

    public LostReportPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(FindBackTheme.BG);
        setBorder(new EmptyBorder(24, 32, 24, 32));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Report a Lost Item");
        title.setFont(FindBackTheme.getHeaderFont(26));
        title.setForeground(FindBackTheme.TEXT);
        JLabel subtitle = new JLabel("Submit detailed lost item information so our Smart Matching Engine can search for matches.");
        subtitle.setFont(FindBackTheme.getBodyFont(14));
        subtitle.setForeground(new Color(110, 120, 135));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Form Card
        RoundedPanel formCard = new RoundedPanel(14, FindBackTheme.CARD);
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(new EmptyBorder(24, 28, 24, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        formCard.add(createLabel("Item Title *"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        titleField = new JTextField();
        titleField.setPreferredSize(new Dimension(0, 36));
        formCard.add(titleField, gbc);

        // Category & Location
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0;
        formCard.add(createLabel("Category *"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        categoryCombo = new JComboBox<>();
        categoryCombo.setPreferredSize(new Dimension(0, 36));
        formCard.add(categoryCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formCard.add(createLabel("Campus Location *"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        locationCombo = new JComboBox<>();
        locationCombo.setPreferredSize(new Dimension(0, 36));
        formCard.add(locationCombo, gbc);

        // Specific Location
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        formCard.add(createLabel("Specific Location"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2;
        specificLocField = new JTextField();
        specificLocField.setPreferredSize(new Dimension(0, 36));
        specificLocField.setToolTipText("e.g. 3rd Floor reading desk #24, Room 402");
        formCard.add(specificLocField, gbc);

        // Color & Date
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        formCard.add(createLabel("Color *"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 1; gbc.weightx = 0.5;
        colorField = new JTextField();
        colorField.setPreferredSize(new Dimension(0, 36));
        formCard.add(colorField, gbc);

        gbc.gridx = 2; gbc.gridy = 4; gbc.weightx = 0.5;
        JPanel datePanel = new JPanel(new BorderLayout(6, 0));
        datePanel.setOpaque(false);
        datePanel.add(new JLabel("Lost Date (YYYY-MM-DD):"), BorderLayout.WEST);
        dateField = new JTextField(LocalDate.now().toString());
        dateField.setPreferredSize(new Dimension(140, 36));
        datePanel.add(dateField, BorderLayout.CENTER);
        formCard.add(datePanel, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1; gbc.weightx = 0;
        formCard.add(createLabel("Detailed Description *"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 2;
        descArea = new JTextArea(4, 30);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        formCard.add(new JScrollPane(descArea), gbc);

        // Image Upload
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        formCard.add(createLabel("Item Photograph"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; gbc.gridwidth = 2;
        JPanel imgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        imgPanel.setOpaque(false);
        JButton uploadBtn = FindBackTheme.createStyledButton("Upload Photo...", FindBackTheme.CARD, FindBackTheme.PRIMARY);
        uploadBtn.setBorder(BorderFactory.createLineBorder(FindBackTheme.BORDER));
        uploadBtn.addActionListener(e -> chooseImage());
        imagePreviewLabel = new JLabel("No image selected");
        imagePreviewLabel.setFont(FindBackTheme.getBodyFont(12));
        imagePreviewLabel.setForeground(Color.GRAY);
        imgPanel.add(uploadBtn);
        imgPanel.add(imagePreviewLabel);
        formCard.add(imgPanel, gbc);

        // Buttons
        gbc.gridx = 1; gbc.gridy = 7; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnPanel.setOpaque(false);

        JButton clearBtn = FindBackTheme.createStyledButton("Clear Form", FindBackTheme.CARD, FindBackTheme.TEXT);
        clearBtn.setBorder(BorderFactory.createLineBorder(FindBackTheme.BORDER));
        clearBtn.addActionListener(e -> clearForm());

        JButton submitBtn = FindBackTheme.createPrimaryButton("Submit Lost Report");
        submitBtn.addActionListener(e -> submitReport());

        btnPanel.add(clearBtn);
        btnPanel.add(submitBtn);
        formCard.add(btnPanel, gbc);

        JScrollPane scroll = new JScrollPane(formCard);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FindBackTheme.getHeaderFont(13));
        l.setForeground(FindBackTheme.TEXT);
        return l;
    }

    private void chooseImage() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "webp", "svg"));
        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fc.getSelectedFile();
            imagePreviewLabel.setText(selectedImageFile.getName() + " (" + (selectedImageFile.length() / 1024) + " KB)");
        }
    }

    private void clearForm() {
        titleField.setText("");
        specificLocField.setText("");
        colorField.setText("");
        descArea.setText("");
        dateField.setText(LocalDate.now().toString());
        selectedImageFile = null;
        imagePreviewLabel.setText("No image selected");
    }

    private void submitReport() {
        String title = titleField.getText().trim();
        String color = colorField.getText().trim();
        String desc = descArea.getText().trim();
        String dateStr = dateField.getText().trim();

        if (title.isEmpty() || color.isEmpty() || desc.isEmpty() || dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields marked with *", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate eventDate;
        try {
            eventDate = LocalDate.parse(dateStr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CategoryItem cat = (CategoryItem) categoryCombo.getSelectedItem();
        LocationItem loc = (LocationItem) locationCombo.getSelectedItem();
        if (cat == null || loc == null) {
            JOptionPane.showMessageDialog(this, "Please select Category and Location", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Session expired. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String imagePath = null;
        if (selectedImageFile != null && selectedImageFile.exists()) {
            try {
                byte[] bytes = Files.readAllBytes(selectedImageFile.toPath());
                imagePath = fileStorageService.storeFile(bytes, selectedImageFile.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to upload image: " + ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }

        try {
            ItemReport report = reportService.createLostReport(
                    user.getId(),
                    cat.id,
                    loc.id,
                    title,
                    desc,
                    color,
                    specificLocField.getText().trim(),
                    eventDate,
                    imagePath
            );

            // Trigger Smart Matching Engine
            matchingService.processReportMatches(report.getId());

            JOptionPane.showMessageDialog(this,
                    "Lost Item Report #" + report.getId() + " submitted successfully!\n" +
                    "Smart Matching Engine is actively scanning for matches.",
                    "Report Submitted", JOptionPane.INFORMATION_MESSAGE);

            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refresh() {
        categoryCombo.removeAllItems();
        List<Category> categories = categoryDao.findAllActive();
        for (Category c : categories) {
            categoryCombo.addItem(new CategoryItem(c.getId(), c.getName()));
        }

        locationCombo.removeAllItems();
        List<CampusLocation> locations = locationDao.findAllActive();
        for (CampusLocation l : locations) {
            locationCombo.addItem(new LocationItem(l.getId(), l.getName()));
        }
    }

    public static class CategoryItem {
        public final Long id;
        public final String name;
        public CategoryItem(Long id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    public static class LocationItem {
        public final Long id;
        public final String name;
        public LocationItem(Long id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}
