package com.findback.model;

import com.findback.enums.NotificationType;

import java.time.LocalDateTime;

/**
 * POJO representing a user notification.
 */
public class Notification {

    private Long id;
    private Long userId;
    private String title;
    private String message;
    private String targetUrl;
    private NotificationType type;
    private boolean read;
    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(Long id, Long userId, String title, String message, String targetUrl,
                        NotificationType type, boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.targetUrl = targetUrl;
        this.type = type;
        this.read = read;
        this.createdAt = createdAt;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
