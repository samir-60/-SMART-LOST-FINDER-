package com.findback.model;

import java.time.LocalDateTime;

/**
 * POJO representing a physical item handover event.
 */
public class Handover {

    private Long id;
    private Long claimId;
    private Long lostReportId;
    private Long foundReportId;
    private Long handedById;
    private Long receivedById;
    private Long staffWitnessId;
    private LocalDateTime handoverDate;
    private String venue;
    private String remarks;
    private String receiptCode;
    private boolean completed;
    private LocalDateTime createdAt;

    // ── Transient display fields ────────────────────────────────────────
    private String receiverName;
    private String handedByName;
    private String witnessName;
    private String lostTitle;
    private String foundTitle;

    public Handover() {
    }

    public Handover(Long id, Long claimId, Long lostReportId, Long foundReportId, Long handedById,
                    Long receivedById, Long staffWitnessId, LocalDateTime handoverDate,
                    String venue, String remarks, String receiptCode, boolean completed,
                    LocalDateTime createdAt) {
        this.id = id;
        this.claimId = claimId;
        this.lostReportId = lostReportId;
        this.foundReportId = foundReportId;
        this.handedById = handedById;
        this.receivedById = receivedById;
        this.staffWitnessId = staffWitnessId;
        this.handoverDate = handoverDate;
        this.venue = venue;
        this.remarks = remarks;
        this.receiptCode = receiptCode;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    // ── Getters & Setters ───────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
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

    public Long getHandedById() {
        return handedById;
    }

    public void setHandedById(Long handedById) {
        this.handedById = handedById;
    }

    public Long getReceivedById() {
        return receivedById;
    }

    public void setReceivedById(Long receivedById) {
        this.receivedById = receivedById;
    }

    public Long getStaffWitnessId() {
        return staffWitnessId;
    }

    public void setStaffWitnessId(Long staffWitnessId) {
        this.staffWitnessId = staffWitnessId;
    }

    public LocalDateTime getHandoverDate() {
        return handoverDate;
    }

    public void setHandoverDate(LocalDateTime handoverDate) {
        this.handoverDate = handoverDate;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getReceiptCode() {
        return receiptCode;
    }

    public void setReceiptCode(String receiptCode) {
        this.receiptCode = receiptCode;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ── Transient display field getters/setters ─────────────────────────

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getHandedByName() {
        return handedByName;
    }

    public void setHandedByName(String handedByName) {
        this.handedByName = handedByName;
    }

    public String getWitnessName() {
        return witnessName;
    }

    public void setWitnessName(String witnessName) {
        this.witnessName = witnessName;
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
}
