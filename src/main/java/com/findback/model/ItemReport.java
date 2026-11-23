package com.findback.model;

import com.findback.enums.CustodyStatus;
import com.findback.enums.ReportStatus;
import com.findback.enums.ReportType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * POJO representing a lost-or-found item report.
 */
public class ItemReport {

    private Long id;
    private Long userId;
    private Long categoryId;
    private Long locationId;
    private ReportType reportType;
    private String title;
    private String description;
    private String color;
    private String specificLocation;
    private LocalDate eventDate;
    private String imagePath;
    private ReportStatus status;
    private CustodyStatus custodyStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Transient display fields (populated via JOINs) ──────────────────
    private String userName;
    private String categoryName;
    private String locationName;

    public ItemReport() {
    }

    public ItemReport(Long id, Long userId, Long categoryId, Long locationId, ReportType reportType,
                      String title, String description, String color, String specificLocation,
                      LocalDate eventDate, String imagePath, ReportStatus status,
                      CustodyStatus custodyStatus, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.locationId = locationId;
        this.reportType = reportType;
        this.title = title;
        this.description = description;
        this.color = color;
        this.specificLocation = specificLocation;
        this.eventDate = eventDate;
        this.imagePath = imagePath;
        this.status = status;
        this.custodyStatus = custodyStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ── Helper methods ──────────────────────────────────────────────────

    public boolean isLost() {
        return reportType == ReportType.LOST;
    }

    public boolean isFound() {
        return reportType == ReportType.FOUND;
    }

    public boolean isOpen() {
        return status == ReportStatus.OPEN;
    }

    // ── Getters & Setters ───────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSpecificLocation() {
        return specificLocation;
    }

    public void setSpecificLocation(String specificLocation) {
        this.specificLocation = specificLocation;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public CustodyStatus getCustodyStatus() {
        return custodyStatus;
    }

    public void setCustodyStatus(CustodyStatus custodyStatus) {
        this.custodyStatus = custodyStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ── Transient display field getters/setters ─────────────────────────

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}
