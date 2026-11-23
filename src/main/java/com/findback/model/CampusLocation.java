package com.findback.model;

import java.time.LocalDateTime;

/**
 * POJO representing a campus location where items can be lost or found.
 */
public class CampusLocation {

    private Long id;
    private String name;
    private String zone;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;

    public CampusLocation() {
    }

    public CampusLocation(Long id, String name, String zone, String description, boolean active,
                          LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.zone = zone;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
    }

    // ── Getters & Setters ───────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
