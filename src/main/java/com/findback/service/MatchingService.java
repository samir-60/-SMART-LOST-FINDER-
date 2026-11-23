package com.findback.service;

import com.findback.dao.MatchDao;
import com.findback.dao.NotificationDao;
import com.findback.dao.ReportDao;
import com.findback.enums.MatchStatus;
import com.findback.enums.NotificationType;
import com.findback.enums.ReportStatus;
import com.findback.enums.ReportType;
import com.findback.model.ItemReport;
import com.findback.model.Match;
import com.findback.model.Notification;
import com.findback.util.DateComparisonUtil;
import com.findback.util.TextSimilarityUtil;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Core matching engine — compares lost and found reports using weighted scoring
 * across category, location, date, color, and description similarity.
 */
public class MatchingService {

    private static final double CATEGORY_WEIGHT = 25.0;
    private static final double LOCATION_WEIGHT = 25.0;
    private static final double DATE_WEIGHT = 20.0;
    private static final double COLOR_WEIGHT = 15.0;
    private static final double DESCRIPTION_WEIGHT = 15.0;

    private static final double MATCH_THRESHOLD = 70.0;
    private static final double HIGH_CONFIDENCE_THRESHOLD = 85.0;

    private final ReportDao reportDao;
    private final MatchDao matchDao;
    private final NotificationDao notificationDao;

    public MatchingService() {
        this.reportDao = new ReportDao();
        this.matchDao = new MatchDao();
        this.notificationDao = new NotificationDao();
    }

    /**
     * Processes potential matches for a newly created or updated report.
     * If the report is LOST, it is compared against all OPEN FOUND reports.
     * If the report is FOUND, it is compared against all OPEN LOST reports.
     */
    public void processReportMatches(Long reportId) {
        ItemReport report = reportDao.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + reportId));

        List<ItemReport> candidates;
        if (report.isLost()) {
            candidates = reportDao.findByTypeAndStatus(ReportType.FOUND, ReportStatus.OPEN);
        } else {
            candidates = reportDao.findByTypeAndStatus(ReportType.LOST, ReportStatus.OPEN);
        }

        boolean matchFound = false;

        for (ItemReport candidate : candidates) {
            ItemReport lost = report.isLost() ? report : candidate;
            ItemReport found = report.isFound() ? report : candidate;

            // Skip if match already exists
            if (matchDao.exists(lost.getId(), found.getId())) {
                continue;
            }

            Match match = calculateMatch(lost, found);

            if (match.getTotalScore() >= MATCH_THRESHOLD) {
                match.setStatus(MatchStatus.POTENTIAL);
                matchDao.save(match);
                matchFound = true;
            }
        }

        if (matchFound) {
            Notification notification = new Notification();
            notification.setUserId(report.getUserId());
            notification.setTitle("Potential Match Found!");
            notification.setMessage("We found potential matches for your " +
                    (report.isLost() ? "lost" : "found") + " item: \"" + report.getTitle() + "\". Check your matches.");
            notification.setTargetUrl("/matches/" + reportId);
            notification.setType(NotificationType.NEW_MATCH);
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notificationDao.save(notification);
        }
    }

    /**
     * Calculates the match score between a lost and a found report.
     * Returns a Match object with individual component scores and an explanation.
     */
    public Match calculateMatch(ItemReport lost, ItemReport found) {
        Match match = new Match();
        match.setLostReportId(lost.getId());
        match.setFoundReportId(found.getId());

        // Category score: exact match = full weight, else 0
        double categoryScore = 0.0;
        if (lost.getCategoryId() != null && found.getCategoryId() != null
                && lost.getCategoryId().equals(found.getCategoryId())) {
            categoryScore = CATEGORY_WEIGHT;
        }

        // Location score: exact match = full weight, else 0
        double locationScore = 0.0;
        if (lost.getLocationId() != null && found.getLocationId() != null
                && lost.getLocationId().equals(found.getLocationId())) {
            locationScore = LOCATION_WEIGHT;
        }

        // Date score: similarity scaled by weight
        double dateScore = 0.0;
        if (lost.getEventDate() != null && found.getEventDate() != null) {
            double dateSimilarity = DateComparisonUtil.calculateDateSimilarity(
                    lost.getEventDate(), found.getEventDate());
            dateScore = dateSimilarity * DATE_WEIGHT / 100.0;
        }

        // Color score: similarity scaled by weight
        double colorScore = 0.0;
        if (lost.getColor() != null && found.getColor() != null
                && !lost.getColor().isEmpty() && !found.getColor().isEmpty()) {
            double colorSimilarity = TextSimilarityUtil.calculateColorSimilarity(
                    lost.getColor(), found.getColor());
            colorScore = colorSimilarity * COLOR_WEIGHT / 100.0;
        }

        // Description score: Jaccard similarity scaled by weight
        double descriptionScore = 0.0;
        if (lost.getDescription() != null && found.getDescription() != null
                && !lost.getDescription().isEmpty() && !found.getDescription().isEmpty()) {
            double descSimilarity = TextSimilarityUtil.calculateJaccardSimilarity(
                    lost.getDescription(), found.getDescription());
            descriptionScore = descSimilarity * DESCRIPTION_WEIGHT / 100.0;
        }

        double totalScore = categoryScore + locationScore + dateScore + colorScore + descriptionScore;

        match.setCategoryScore(categoryScore);
        match.setLocationScore(locationScore);
        match.setDateScore(dateScore);
        match.setColorScore(colorScore);
        match.setDescriptionScore(descriptionScore);
        match.setTotalScore(totalScore);

        // Build explanation
        StringBuilder explanation = new StringBuilder();
        explanation.append(String.format("Category: %.1f/%.0f", categoryScore, CATEGORY_WEIGHT));
        explanation.append(categoryScore > 0 ? " (exact match)" : " (different)");
        explanation.append(String.format(" | Location: %.1f/%.0f", locationScore, LOCATION_WEIGHT));
        explanation.append(locationScore > 0 ? " (exact match)" : " (different)");
        explanation.append(String.format(" | Date: %.1f/%.0f", dateScore, DATE_WEIGHT));
        explanation.append(String.format(" | Color: %.1f/%.0f", colorScore, COLOR_WEIGHT));
        explanation.append(String.format(" | Description: %.1f/%.0f", descriptionScore, DESCRIPTION_WEIGHT));
        explanation.append(String.format(" | TOTAL: %.1f/100", totalScore));
        match.setExplanation(explanation.toString());

        return match;
    }

    /**
     * Gets all matches relevant to a user (matches involving their reports).
     */
    public List<Match> getMatchesForUser(Long userId) {
        return matchDao.findMatchesForUser(userId);
    }

    /**
     * Gets all high-confidence matches (score >= 85).
     */
    public List<Match> getHighConfidenceMatches() {
        return matchDao.findByMinScore(HIGH_CONFIDENCE_THRESHOLD);
    }

    /**
     * Executes the AI rule-based matching engine across all open lost and found items.
     * Evaluates Category (25%), Location (25%), Date Chronology (20%), Color (15%),
     * and Title/Description similarity (15%). 100% free, runs completely offline on device.
     *
     * @return count of newly discovered potential matches
     */
    public int runMatchingEngine() {
        List<ItemReport> lostReports = reportDao.findByTypeAndStatus(ReportType.LOST, ReportStatus.OPEN);
        List<ItemReport> foundReports = reportDao.findByTypeAndStatus(ReportType.FOUND, ReportStatus.OPEN);
        int newMatches = 0;

        for (ItemReport lost : lostReports) {
            for (ItemReport found : foundReports) {
                if (matchDao.exists(lost.getId(), found.getId())) {
                    continue;
                }
                Match match = calculateMatch(lost, found);
                if (match.getTotalScore() >= MATCH_THRESHOLD) {
                    match.setStatus(MatchStatus.POTENTIAL);
                    matchDao.save(match);
                    newMatches++;

                    // Send notification to both reporting users
                    try {
                        Notification nLost = new Notification();
                        nLost.setUserId(lost.getUserId());
                        nLost.setTitle("AI Match Discovered!");
                        nLost.setMessage("Potential match (" + String.format("%.0f%%", match.getTotalScore()) +
                                ") found for your lost item: '" + lost.getTitle() + "' with found item '" + found.getTitle() + "'.");
                        nLost.setTargetUrl("/matches");
                        nLost.setType(NotificationType.NEW_MATCH);
                        nLost.setRead(false);
                        nLost.setCreatedAt(LocalDateTime.now());
                        notificationDao.save(nLost);

                        Notification nFound = new Notification();
                        nFound.setUserId(found.getUserId());
                        nFound.setTitle("AI Match Discovered!");
                        nFound.setMessage("Your found item '" + found.getTitle() + "' matches reported lost item '" +
                                lost.getTitle() + "' (" + String.format("%.0f%%", match.getTotalScore()) + " match).");
                        nFound.setTargetUrl("/matches");
                        nFound.setType(NotificationType.NEW_MATCH);
                        nFound.setRead(false);
                        nFound.setCreatedAt(LocalDateTime.now());
                        notificationDao.save(nFound);
                    } catch (Exception ignored) {}
                }
            }
        }
        return newMatches;
    }
}
