package com.findback.enums;

public enum NotificationType {
    NEW_MATCH("New Potential Match Found", "bi-bullseye", "text-primary"),
    CLAIM_SUBMITTED("New Ownership Claim Submitted", "bi-shield-exclamation", "text-warning"),
    CLAIM_APPROVED("Ownership Claim Approved", "bi-check-circle-fill", "text-success"),
    CLAIM_REJECTED("Ownership Claim Rejected", "bi-x-circle-fill", "text-danger"),
    REPORT_UPDATED("Report Status Updated", "bi-arrow-repeat", "text-info"),
    ITEM_RECOVERED("Item Successfully Recovered & Handed Over", "bi-gift-fill", "text-success"),
    ADMIN_MESSAGE("Campus Security / Admin Notice", "bi-megaphone-fill", "text-primary"),
    GENERAL("Notification", "bi-bell", "text-secondary");

    private final String title;
    private final String icon;
    private final String colorClass;

    NotificationType(String title, String icon, String colorClass) {
        this.title = title;
        this.icon = icon;
        this.colorClass = colorClass;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public String getColorClass() {
        return colorClass;
    }
}

