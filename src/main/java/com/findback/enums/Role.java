package com.findback.enums;

public enum Role {
    ROLE_STUDENT("Student"),
    ROLE_MODERATOR("Moderator"),
    ROLE_ADMIN("Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

