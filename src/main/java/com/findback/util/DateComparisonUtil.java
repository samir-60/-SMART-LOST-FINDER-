package com.findback.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class DateComparisonUtil {

    private DateComparisonUtil() {
    }

    /**
     * Calculates date similarity score between 0.0 and 100.0.
     * Evaluates chronological proximity between lost date and found date.
     */
    public static double calculateDateSimilarity(LocalDate lostDate, LocalDate foundDate) {
        if (lostDate == null || foundDate == null) {
            return 0.0;
        }

        long daysBetween = ChronoUnit.DAYS.between(lostDate, foundDate);

        // Found on the exact same day
        if (daysBetween == 0) {
            return 100.0;
        }

        // Found 1 day after (or estimated 1 day before due to uncertainty)
        if (daysBetween == 1) {
            return 90.0;
        }
        if (daysBetween == -1) {
            return 75.0; // Student may have slightly misremembered the exact lost day
        }

        // Within 3 days
        if (daysBetween >= 2 && daysBetween <= 3) {
            return 70.0;
        }
        if (daysBetween == -2 || daysBetween == -3) {
            return 50.0;
        }

        // Within 7 days
        if (daysBetween > 3 && daysBetween <= 7) {
            return 45.0;
        }

        // Within 14 days
        if (daysBetween > 7 && daysBetween <= 14) {
            return 25.0;
        }

        // Within 30 days
        if (daysBetween > 14 && daysBetween <= 30) {
            return 10.0;
        }

        return 0.0;
    }
}

