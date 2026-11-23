package com.findback.model;

import com.findback.enums.ClaimStatus;

import java.time.LocalDateTime;

/**
 * POJO representing an ownership claim on a found item.
 */
public class Claim {

    private Long id;
    private Long claimantId;
    private Long lostReportId;
    private Long foundReportId;
    private Long matchId;
    private String proofDescription;
    private String proofImagePath;
    private ClaimStatus status;
    private String adminRemarks;
    private Long reviewedById;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Transient display fields ────────────────────────────────────────
    private String claimantName;
    private String claimantPhone;
    private String lostTitle;
    private String foundTitle;
    private String finderName;
    private String finderPhone;

    public Claim() {
    }

    public Claim(Long id, Long claimantId, Long lostReportId, Long foundReportId, Long matchId,
                 String proofDescription, String proofImagePath, ClaimStatus status,
                 String adminRemarks, Long reviewedById, LocalDateTime reviewedAt,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.claimantId = claimantId;
        this.lostReportId = lostReportId;
        this.foundReportId = foundReportId;
        this.matchId = matchId;
        this.proofDescription = proofDescription;
        this.proofImagePath = proofImagePath;
        this.status = status;
        this.adminRemarks = adminRemarks;
        this.reviewedById = reviewedById;
        this.reviewedAt = reviewedAt;
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

    public Long getClaimantId() {
        return claimantId;
    }

    public void setClaimantId(Long claimantId) {
        this.claimantId = claimantId;
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

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public String getProofDescription() {
        return proofDescription;
    }

    public void setProofDescription(String proofDescription) {
        this.proofDescription = proofDescription;
    }

    public String getProofImagePath() {
        return proofImagePath;
    }

    public void setProofImagePath(String proofImagePath) {
        this.proofImagePath = proofImagePath;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public String getAdminRemarks() {
        return adminRemarks;
    }

    public void setAdminRemarks(String adminRemarks) {
        this.adminRemarks = adminRemarks;
    }

    public Long getReviewedById() {
        return reviewedById;
    }

    public void setReviewedById(Long reviewedById) {
        this.reviewedById = reviewedById;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
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

    public String getClaimantName() {
        return claimantName;
    }

    public void setClaimantName(String claimantName) {
        this.claimantName = claimantName;
    }

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

    public String getClaimantPhone() {
        return claimantPhone != null ? claimantPhone : "";
    }

    public void setClaimantPhone(String claimantPhone) {
        this.claimantPhone = claimantPhone;
    }

    public String getFinderName() {
        return finderName != null ? finderName : "Campus Community Member";
    }

    public void setFinderName(String finderName) {
        this.finderName = finderName;
    }

    public String getFinderPhone() {
        return finderPhone != null ? finderPhone : "";
    }

    public void setFinderPhone(String finderPhone) {
        this.finderPhone = finderPhone;
    }
}
