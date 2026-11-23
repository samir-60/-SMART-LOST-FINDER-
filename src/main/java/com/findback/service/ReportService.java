package com.findback.service;

import com.findback.dao.ReportDao;
import com.findback.model.ItemReport;
import com.findback.enums.CustodyStatus;
import com.findback.enums.ReportStatus;
import com.findback.enums.ReportType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for creating, updating, and querying item reports (lost and found).
 */
public class ReportService {

    private final ReportDao reportDao;

    public ReportService() {
        this.reportDao = new ReportDao();
    }

    /**
     * Creates a new LOST item report.
     */
    public ItemReport createLostReport(Long userId, Long categoryId, Long locationId,
                                       String title, String description, String color,
                                       String specificLocation, LocalDate eventDate,
                                       String imagePath) {
        ItemReport report = new ItemReport();
        report.setUserId(userId);
        report.setCategoryId(categoryId);
        report.setLocationId(locationId);
        report.setReportType(ReportType.LOST);
        report.setTitle(title);
        report.setDescription(description);
        report.setColor(color);
        report.setSpecificLocation(specificLocation);
        report.setEventDate(eventDate);
        report.setImagePath(imagePath);
        report.setStatus(ReportStatus.OPEN);
        report.setCreatedAt(LocalDateTime.now());

        Long id = reportDao.save(report);
        if (id == null) {
            throw new IllegalStateException("Failed to save report: database returned null ID.");
        }
        report.setId(id);
        return report;
    }

    /**
     * Creates a new FOUND item report.
     */
    public ItemReport createFoundReport(Long userId, Long categoryId, Long locationId,
                                        String title, String description, String color,
                                        String specificLocation, LocalDate eventDate,
                                        String imagePath, CustodyStatus custodyStatus) {
        ItemReport report = new ItemReport();
        report.setUserId(userId);
        report.setCategoryId(categoryId);
        report.setLocationId(locationId);
        report.setReportType(ReportType.FOUND);
        report.setTitle(title);
        report.setDescription(description);
        report.setColor(color);
        report.setSpecificLocation(specificLocation);
        report.setEventDate(eventDate);
        report.setImagePath(imagePath);
        report.setStatus(ReportStatus.OPEN);
        report.setCustodyStatus(custodyStatus != null ? custodyStatus : CustodyStatus.WITH_FINDER);
        report.setCreatedAt(LocalDateTime.now());

        Long id = reportDao.save(report);
        if (id == null) {
            throw new IllegalStateException("Failed to save report: database returned null ID.");
        }
        report.setId(id);
        return report;
    }

    /**
     * Updates an existing report's editable fields.
     */
    public void updateReport(Long id, String title, String description, String color,
                             Long categoryId, Long locationId, String specificLocation) {
        ItemReport report = getById(id);
        report.setTitle(title);
        report.setDescription(description);
        report.setColor(color);
        report.setCategoryId(categoryId);
        report.setLocationId(locationId);
        report.setSpecificLocation(specificLocation);
        reportDao.update(report);
    }

    /**
     * Closes a report by setting its status to CLOSED.
     */
    public void closeReport(Long id) {
        reportDao.updateStatus(id, ReportStatus.CLOSED);
    }

    public boolean deleteReport(Long id) {
        return reportDao.delete(id);
    }

    /**
     * Retrieves a report by ID.
     *
     * @throws IllegalArgumentException if not found
     */
    public ItemReport getById(Long id) {
        return reportDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + id));
    }

    /**
     * Gets all reports submitted by a specific user.
     */
    public List<ItemReport> getByUser(Long userId) {
        return reportDao.findByUserId(userId);
    }

    /**
     * Gets the most recent LOST reports.
     */
    public List<ItemReport> getRecentLost(int limit) {
        return reportDao.findRecentByType(ReportType.LOST, limit);
    }

    /**
     * Gets the most recent FOUND reports.
     */
    public List<ItemReport> getRecentFound(int limit) {
        return reportDao.findRecentByType(ReportType.FOUND, limit);
    }

    /**
     * Searches reports by keyword, category, location, type, and color.
     */
    public List<ItemReport> search(String keyword, Long categoryId, Long locationId,
                                   ReportType type, String color) {
        return reportDao.search(keyword, categoryId, locationId, type, color);
    }

    public long countLost() {
        return reportDao.countByType(ReportType.LOST);
    }

    public long countFound() {
        return reportDao.countByType(ReportType.FOUND);
    }

    public long countOpen() {
        return reportDao.countByStatus(ReportStatus.OPEN);
    }
}
