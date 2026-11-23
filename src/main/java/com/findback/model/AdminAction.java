package com.findback.model;

import java.time.LocalDateTime;

/**
 * POJO representing an administrative action log entry.
 */
public class AdminAction {

    private Long id;
    private Long adminId;
    private String actionType;
    private String targetEntity;
    private Long targetId;
    private String details;
    private LocalDateTime createdAt;

    // ── Transient display field ─────────────────────────────────────────
    private String adminName;

    public AdminAction() {
    }

    public AdminAction(Long id, Long adminId, String actionType, String targetEntity, Long targetId,
                       String details, LocalDateTime createdAt) {
        this.id = id;
        this.adminId = adminId;
        this.actionType = actionType;
        this.targetEntity = targetEntity;
        this.targetId = targetId;
        this.details = details;
        this.createdAt = createdAt;
    }

    // ── Getters & Setters ───────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ── Transient display field getter/setter ───────────────────────────

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }
}
