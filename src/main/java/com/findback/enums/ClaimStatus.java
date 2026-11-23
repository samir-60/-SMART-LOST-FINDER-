package com.findback.enums;

public enum ClaimStatus {
    PENDING("Pending Verification"),
    UNDER_REVIEW("Under Review by Staff"),
    APPROVED("Approved (Ready for Handover)"),
    REJECTED("Rejected"),
    COMPLETED("Completed (Handover Finished)");

    private final String displayName;

    ClaimStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

