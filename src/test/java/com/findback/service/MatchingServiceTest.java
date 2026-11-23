package com.findback.service;

import com.findback.model.ItemReport;
import com.findback.model.Match;
import com.findback.util.DateComparisonUtil;
import com.findback.util.TextSimilarityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MatchingServiceTest {

    private MatchingService matchingService;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        matchingService = new MatchingService();
        today = LocalDate.now();
    }

    @Test
    @DisplayName("Identical features produce maximum weighted score of 100")
    void testExactMatchScore() {
        ItemReport lost = new ItemReport();
        lost.setCategoryId(1L);
        lost.setLocationId(2L);
        lost.setEventDate(today);
        lost.setColor("Black");
        lost.setDescription("Black leather bi-fold wallet with university student ID card");

        ItemReport found = new ItemReport();
        found.setCategoryId(1L);
        found.setLocationId(2L);
        found.setEventDate(today);
        found.setColor("Black");
        found.setDescription("Black leather bi-fold wallet with university student ID card");

        Match match = matchingService.calculateMatch(lost, found);

        assertNotNull(match);
        assertEquals(25.0, match.getCategoryScore(), 0.1);
        assertEquals(25.0, match.getLocationScore(), 0.1);
        assertEquals(20.0, match.getDateScore(), 0.1);
        assertEquals(15.0, match.getColorScore(), 0.1);
        assertEquals(15.0, match.getDescriptionScore(), 0.1);
        assertEquals(100.0, match.getTotalScore(), 0.1);
        assertTrue(match.getExplanation().contains("Category"));
    }

    @Test
    @DisplayName("Different category gives 0 category score")
    void testDifferentCategory() {
        ItemReport lost = new ItemReport();
        lost.setCategoryId(1L);
        lost.setLocationId(2L);
        lost.setEventDate(today);
        lost.setColor("Blue");
        lost.setDescription("Casio scientific calculator");

        ItemReport found = new ItemReport();
        found.setCategoryId(5L); // Different category
        found.setLocationId(2L);
        found.setEventDate(today);
        found.setColor("Blue");
        found.setDescription("Casio scientific calculator");

        Match match = matchingService.calculateMatch(lost, found);

        assertEquals(0.0, match.getCategoryScore());
        assertEquals(25.0, match.getLocationScore(), 0.1);
        assertTrue(match.getTotalScore() < 80.0);
    }

    @Test
    @DisplayName("Similar colors receive partial color credit")
    void testSimilarColors() {
        double score = TextSimilarityUtil.calculateColorSimilarity("Navy", "Blue");
        assertTrue(score >= 65.0, "Navy and Blue should have similarity >= 65");

        double exactScore = TextSimilarityUtil.calculateColorSimilarity("Black", "Black");
        assertEquals(100.0, exactScore);
    }

    @Test
    @DisplayName("Date proximity diminishes gracefully with days elapsed")
    void testDateProximity() {
        assertEquals(100.0, DateComparisonUtil.calculateDateSimilarity(today, today));
        assertEquals(90.0, DateComparisonUtil.calculateDateSimilarity(today.minusDays(1), today));
        assertEquals(70.0, DateComparisonUtil.calculateDateSimilarity(today.minusDays(3), today));
        assertEquals(45.0, DateComparisonUtil.calculateDateSimilarity(today.minusDays(7), today));
        assertEquals(0.0, DateComparisonUtil.calculateDateSimilarity(today.minusDays(40), today));
    }

    @Test
    @DisplayName("Jaccard text similarity calculates accurate token overlap")
    void testJaccardSimilarity() {
        String t1 = "Casio fx-991EX scientific calculator with solar panel";
        String t2 = "Casio scientific calculator found in library";

        double sim = TextSimilarityUtil.calculateJaccardSimilarity(t1, t2);
        assertTrue(sim > 30.0, "Should have significant token overlap");

        double zeroSim = TextSimilarityUtil.calculateJaccardSimilarity("umbrella black", "laptop dell charger");
        assertEquals(0.0, zeroSim);
    }
}
