package com.findback.enums;

public enum ReportStatus {
    OPEN("Open / Active"),
    CLAIM_PENDING("Claim Pending"),
    RESOLVED("Resolved (Lost Item Claimed)"),
    RETURNED("Returned to Owner"),
    CLOSED("Closed / Cancelled");

    private final String displayName;

    ReportStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

