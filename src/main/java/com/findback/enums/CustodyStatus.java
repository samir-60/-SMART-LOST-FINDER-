package com.findback.enums;

public enum CustodyStatus {
    WITH_FINDER("In Finder's Personal Possession"),
    SECURITY_DESK("Submitted to Campus Security Desk"),
    DEPARTMENT_OFFICE("Submitted to Department Office"),
    CENTRAL_LOST_AND_FOUND("At University Central Lost & Found Vault");

    private final String description;

    CustodyStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

