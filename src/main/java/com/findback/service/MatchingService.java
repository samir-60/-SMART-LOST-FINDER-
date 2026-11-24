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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Core matching engine — compares lost and found reports using weighted scoring
 * across category (25%), location (25%), date (20%), color (15%), and description (15%).
 */
public class MatchingService {

    private static final double CATEGORY_WEIGHT = 25.0;
    private static final double LOCATION_WEIGHT = 25.0;
    private static final double DATE_WEIGHT = 20.0;
    private static final double COLOR_WEIGHT = 15.0;
    private static final double DESCRIPTION_WEIGHT = 15.0;

    private static final double MATCH_THRESHOLD = 70.0;
    private static final double HIGH_CONFIDENCE_THRESHOLD = 85.0;

    private final ReportDao reportDao = new ReportDao();
    private final MatchDao matchDao = new MatchDao();
    private final NotificationDao notificationDao = new NotificationDao();
    private final GroqAiService groqAiService = new GroqAiService();

    /**
     * Processes potential matches for a newly created or updated report.
     */
    public void processReportMatches(Long reportId) {
        ItemReport report = reportDao.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + reportId));

        List<ItemReport> candidates = reportDao.findByTypeAndStatus(
                report.isLost() ? ReportType.FOUND : ReportType.LOST, ReportStatus.OPEN);

        boolean matchFound = false;
        for (ItemReport candidate : candidates) {
            ItemReport lost = report.isLost() ? report : candidate;
            ItemReport found = report.isFound() ? report : candidate;

            if (matchDao.exists(lost.getId(), found.getId())) continue;

            Match match = calculateMatch(lost, found);
            if (match.getTotalScore() >= MATCH_THRESHOLD) {
                match.setStatus(MatchStatus.POTENTIAL);
                matchDao.save(match);
                matchFound = true;
            }
        }

        if (matchFound) {
            notifyUser(report.getUserId(), "Potential Match Found!",
                    "We found potential matches for your " + (report.isLost() ? "lost" : "found") +
                            " item: \"" + report.getTitle() + "\". Check your matches.");
        }
    }

    /**
     * Calculates the match score between a lost and a found report using the 100-point rubric.
     */
    public Match calculateMatch(ItemReport lost, ItemReport found) {
        Match match = new Match();
        match.setLostReportId(lost.getId());
        match.setFoundReportId(found.getId());

        // 1. Category (25%)
        boolean catMatch = lost.getCategoryId() != null && Objects.equals(lost.getCategoryId(), found.getCategoryId());
        double categoryScore = catMatch ? CATEGORY_WEIGHT : 0.0;

        // 2. Location (25%)
        boolean locMatch = lost.getLocationId() != null && Objects.equals(lost.getLocationId(), found.getLocationId());
        double locationScore = locMatch ? LOCATION_WEIGHT : 0.0;

        // 3. Date (20%)
        double dateScore = (lost.getEventDate() != null && found.getEventDate() != null)
                ? DateComparisonUtil.calculateDateSimilarity(lost.getEventDate(), found.getEventDate()) * DATE_WEIGHT / 100.0
                : 0.0;

        // 4. Color (15%)
        double colorScore = (hasText(lost.getColor()) && hasText(found.getColor()))
                ? TextSimilarityUtil.calculateColorSimilarity(lost.getColor(), found.getColor()) * COLOR_WEIGHT / 100.0
                : 0.0;

        // 5. Description (15%)
        double descriptionScore = (hasText(lost.getDescription()) && hasText(found.getDescription()))
                ? TextSimilarityUtil.calculateJaccardSimilarity(lost.getDescription(), found.getDescription()) * DESCRIPTION_WEIGHT / 100.0
                : 0.0;

        double totalScore = categoryScore + locationScore + dateScore + colorScore + descriptionScore;

        match.setCategoryScore(categoryScore);
        match.setLocationScore(locationScore);
        match.setDateScore(dateScore);
        match.setColorScore(colorScore);
        match.setDescriptionScore(descriptionScore);
        match.setTotalScore(totalScore);

        match.setExplanation(String.format(
                "Category: %.1f/%.0f (%s) | Location: %.1f/%.0f (%s) | Date: %.1f/%.0f | Color: %.1f/%.0f | Description: %.1f/%.0f | TOTAL: %.1f/100",
                categoryScore, CATEGORY_WEIGHT, catMatch ? "exact match" : "different",
                locationScore, LOCATION_WEIGHT, locMatch ? "exact match" : "different",
                dateScore, DATE_WEIGHT, colorScore, COLOR_WEIGHT, descriptionScore, DESCRIPTION_WEIGHT, totalScore));

        return match;
    }

    /**
     * Enhanced AI matching leveraging Groq real-time semantic analysis.
     */
    public Match calculateAiMatch(ItemReport lost, ItemReport found) {
        Match match = calculateMatch(lost, found);
        try {
            GroqAiService.AiMatchAssessment ai = groqAiService.evaluateMatch(lost, found);
            if (ai != null) {
                match.setTotalScore(ai.totalScore);
                if (ai.categoryScore > 0) match.setCategoryScore(ai.categoryScore);
                if (ai.locationScore > 0) match.setLocationScore(ai.locationScore);
                if (ai.dateScore > 0) match.setDateScore(ai.dateScore);
                if (ai.colorScore > 0) match.setColorScore(ai.colorScore);
                if (ai.descriptionScore > 0) match.setDescriptionScore(ai.descriptionScore);
                match.setExplanation(ai.explanation);
            }
        } catch (Exception ignored) {}
        return match;
    }

    /**
     * Searches and ranks all active open found items against an ad-hoc lost item criteria
     * (used by the Instant AI Smart Search tool).
     */
    public List<Match> searchFoundMatches(ItemReport queryLost) {
        List<ItemReport> openFound = reportDao.findByTypeAndStatus(ReportType.FOUND, ReportStatus.OPEN);
        List<Match> results = new ArrayList<>();
        for (ItemReport found : openFound) {
            Match m = calculateAiMatch(queryLost, found);
            m.setFoundTitle(found.getTitle());
            m.setFoundCategory(found.getCategoryName());
            m.setFoundColor(found.getColor());
            m.setFoundLocation(found.getLocationName());
            m.setFoundImage(found.getImagePath());
            m.setFinderPhone(found.getContactPhone());
            m.setFinderName(found.getUserName());
            if (m.getTotalScore() >= 45.0) {
                results.add(m);
            }
        }
        results.sort((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()));
        return results;
    }

    public List<Match> getMatchesForUser(Long userId) {
        return matchDao.findMatchesForUser(userId);
    }

    public List<Match> getAllMatches() {
        return matchDao.findAll();
    }

    public List<Match> getHighConfidenceMatches() {
        return matchDao.findByMinScore(HIGH_CONFIDENCE_THRESHOLD);
    }

    /**
     * Runs the matching engine across all open lost and found items.
     */
    public int runMatchingEngine() {
        List<ItemReport> lostReports = reportDao.findByTypeAndStatus(ReportType.LOST, ReportStatus.OPEN);
        List<ItemReport> foundReports = reportDao.findByTypeAndStatus(ReportType.FOUND, ReportStatus.OPEN);
        int newMatches = 0;

        for (ItemReport lost : lostReports) {
            for (ItemReport found : foundReports) {
                if (matchDao.exists(lost.getId(), found.getId())) continue;

                Match match = calculateAiMatch(lost, found);
                if (match.getTotalScore() >= MATCH_THRESHOLD) {
                    match.setStatus(MatchStatus.POTENTIAL);
                    matchDao.save(match);
                    newMatches++;

                    String score = String.format("%.0f%%", match.getTotalScore());
                    notifyUser(lost.getUserId(), "AI Match Discovered!",
                            "Potential match (" + score + ") found for your lost item: '" + lost.getTitle() + "' with found item '" + found.getTitle() + "'.");
                    notifyUser(found.getUserId(), "AI Match Discovered!",
                            "Your found item '" + found.getTitle() + "' matches reported lost item '" + lost.getTitle() + "' (" + score + " match).");
                }
            }
        }
        return newMatches;
    }

    private void notifyUser(Long userId, String title, String message) {
        if (userId == null) return;
        try {
            Notification n = new Notification();
            n.setUserId(userId);
            n.setTitle(title);
            n.setMessage(message);
            n.setTargetUrl("/matches");
            n.setType(NotificationType.NEW_MATCH);
            n.setRead(false);
            n.setCreatedAt(LocalDateTime.now());
            notificationDao.save(n);
        } catch (Exception ignored) {}
    }

    private static boolean hasText(String str) {
        return str != null && !str.isBlank();
    }
}
