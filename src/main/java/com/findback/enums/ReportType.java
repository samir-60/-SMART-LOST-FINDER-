package com.findback.enums;

public enum ReportType {
    LOST("Lost Item"),
    FOUND("Found Item");

    private final String displayName;

    ReportType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

