package com.findback.model;

import java.time.LocalDateTime;

/**
 * POJO representing an item category.
 */
public class Category {

    private Long id;
    private String name;
    private String description;
    private String iconName;
    private boolean active;
    private LocalDateTime createdAt;

    public Category() {
    }

    public Category(Long id, String name, String description, String iconName, boolean active,
                    LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconName = iconName;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
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
