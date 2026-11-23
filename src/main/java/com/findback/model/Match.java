package com.findback.model;

import com.findback.enums.MatchStatus;

import java.time.LocalDateTime;

/**
 * POJO representing a computed match between a lost report and a found report.
 */
public class Match {

    private Long id;
    private Long lostReportId;
    private Long foundReportId;
    private double totalScore;
    private double categoryScore;
    private double locationScore;
    private double dateScore;
    private double colorScore;
    private double descriptionScore;
    private String explanation;
    private MatchStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Transient display fields ────────────────────────────────────────
    private String lostTitle;
    private String foundTitle;
    private String lostColor;
    private String foundColor;
    private String lostCategory;
    private String foundCategory;

    public Match() {
    }

    public Match(Long id, Long lostReportId, Long foundReportId, double totalScore,
                 double categoryScore, double locationScore, double dateScore, double colorScore,
                 double descriptionScore, String explanation, MatchStatus status,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.lostReportId = lostReportId;
        this.foundReportId = foundReportId;
        this.totalScore = totalScore;
        this.categoryScore = categoryScore;
        this.locationScore = locationScore;
        this.dateScore = dateScore;
        this.colorScore = colorScore;
        this.descriptionScore = descriptionScore;
        this.explanation = explanation;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ── Getters & Setters ───────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLostReportId() {
        return lostReportId;
    }

    public void setLostReportId(Long lostReportId) {
        this.lostReportId = lostReportId;
    }

    public Long getFoundReportId() {
        return foundReportId;
    }

    public void setFoundReportId(Long foundReportId) {
        this.foundReportId = foundReportId;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public double getCategoryScore() {
        return categoryScore;
    }

    public void setCategoryScore(double categoryScore) {
        this.categoryScore = categoryScore;
    }

    public double getLocationScore() {
        return locationScore;
    }

    public void setLocationScore(double locationScore) {
        this.locationScore = locationScore;
    }

    public double getDateScore() {
        return dateScore;
    }

    public void setDateScore(double dateScore) {
        this.dateScore = dateScore;
    }

    public double getColorScore() {
        return colorScore;
    }

    public void setColorScore(double colorScore) {
        this.colorScore = colorScore;
    }

    public double getDescriptionScore() {
        return descriptionScore;
    }

    public void setDescriptionScore(double descriptionScore) {
        this.descriptionScore = descriptionScore;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
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

    public String getLostTitle() {
        return lostTitle;
    }

    public void setLostTitle(String lostTitle) {
        this.lostTitle = lostTitle;
    }

    public String getFoundTitle() {
        return foundTitle;
    }

    public void setFoundTitle(String foundTitle) {
        this.foundTitle = foundTitle;
    }

    public String getLostColor() {
        return lostColor;
    }

    public void setLostColor(String lostColor) {
        this.lostColor = lostColor;
    }

    public String getFoundColor() {
        return foundColor;
    }

    public void setFoundColor(String foundColor) {
        this.foundColor = foundColor;
    }

    public String getLostCategory() {
        return lostCategory;
    }

    public void setLostCategory(String lostCategory) {
        this.lostCategory = lostCategory;
    }

    public String getFoundCategory() {
        return foundCategory;
    }

    public void setFoundCategory(String foundCategory) {
        this.foundCategory = foundCategory;
    }
}
