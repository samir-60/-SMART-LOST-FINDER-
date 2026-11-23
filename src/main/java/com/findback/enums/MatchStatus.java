package com.findback.enums;

public enum MatchStatus {
    POTENTIAL("Potential Match", "info"),
    CONFIRMED("Confirmed Match", "success"),
    STRONG_MATCH("Strong Potential Match", "success"),
    POSSIBLE_MATCH("Possible Match", "warning"),
    LOW_CONFIDENCE("Low Confidence", "secondary"),
    CLAIMED("Claim Initiated", "info"),
    RESOLVED("Resolved", "primary"),
    DISMISSED("Dismissed by User", "dark");

    private final String label;
    private final String badgeColor;

    MatchStatus(String label, String badgeColor) {
        this.label = label;
        this.badgeColor = badgeColor;
    }

    public String getLabel() {
        return label;
    }

    public String getBadgeColor() {
        return badgeColor;
    }
}
