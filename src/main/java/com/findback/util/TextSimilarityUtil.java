package com.findback.util;

import java.util.*;
import java.util.stream.Collectors;

public final class TextSimilarityUtil {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by",
            "could", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from", "further",
            "had", "has", "have", "having", "he", "her", "here", "hers", "herself", "him", "himself", "his",
            "how", "i", "if", "in", "into", "is", "it", "its", "itself", "just", "me", "more", "most", "my",
            "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "our", "ours",
            "ourselves", "out", "over", "own", "s", "same", "she", "should", "so", "some", "such", "t", "than",
            "that", "the", "their", "theirs", "them", "themselves", "then", "there", "these", "they", "this",
            "those", "through", "to", "too", "under", "until", "up", "very", "was", "we", "were", "what",
            "when", "where", "which", "while", "who", "whom", "why", "will", "with", "you", "your", "yours",
            "lost", "found", "item", "please", "help", "return", "contact", "belonging", "near", "around"
    );

    // Map of compatible/similar colors for campus items
    private static final Map<String, Set<String>> SIMILAR_COLORS = new HashMap<>();

    static {
        addSimilarGroup("black", "dark grey", "dark gray", "charcoal", "matte black", "slate");
        addSimilarGroup("grey", "gray", "silver", "ash", "metallic");
        addSimilarGroup("blue", "navy", "dark blue", "royal blue", "sky blue", "cyan");
        addSimilarGroup("brown", "tan", "beige", "khaki", "coffee", "camel");
        addSimilarGroup("white", "off-white", "cream", "ivory", "pearl");
        addSimilarGroup("red", "maroon", "burgundy", "crimson", "ruby");
        addSimilarGroup("green", "olive", "dark green", "forest green", "mint");
        addSimilarGroup("yellow", "gold", "amber", "mustard");
        addSimilarGroup("purple", "violet", "lavender", "indigo");
        addSimilarGroup("orange", "coral", "peach");
    }

    private static void addSimilarGroup(String... colors) {
        Set<String> set = Arrays.stream(colors).map(String::toLowerCase).collect(Collectors.toSet());
        for (String c : set) {
            SIMILAR_COLORS.put(c, set);
        }
    }

    private TextSimilarityUtil() {
    }

    /**
     * Tokenizes text, lowercases, removes non-alphanumeric chars and stop words.
     */
    public static Set<String> extractMeaningfulTokens(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptySet();
        }
        String[] rawTokens = text.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", " ").split("\\s+");
        Set<String> tokens = new HashSet<>();
        for (String token : rawTokens) {
            token = token.trim();
            if (token.length() > 2 && !STOP_WORDS.contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    /**
     * Calculates Jaccard similarity score between 0.0 and 100.0.
     * Formula: (|Intersection| / |Union|) * 100
     */
    public static double calculateJaccardSimilarity(String text1, String text2) {
        Set<String> tokens1 = extractMeaningfulTokens(text1);
        Set<String> tokens2 = extractMeaningfulTokens(text2);

        if (tokens1.isEmpty() && tokens2.isEmpty()) {
            return 100.0;
        }
        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);

        Set<String> union = new HashSet<>(tokens1);
        union.addAll(tokens2);

        double ratio = (double) intersection.size() / union.size();
        return Math.round(ratio * 100.0 * 10.0) / 10.0;
    }

    /**
     * Calculates color similarity score between 0.0 and 100.0.
     */
    public static double calculateColorSimilarity(String color1, String color2) {
        if (color1 == null || color2 == null) {
            return 0.0;
        }

        String c1 = color1.trim().toLowerCase();
        String c2 = color2.trim().toLowerCase();

        // Exact match
        if (c1.equals(c2)) {
            return 100.0;
        }

        // Substring match (e.g. "dark blue" vs "blue")
        if (c1.contains(c2) || c2.contains(c1)) {
            return 80.0;
        }

        // Check similar color groups
        Set<String> group = SIMILAR_COLORS.get(c1);
        if (group != null && group.contains(c2)) {
            return 65.0;
        }

        // Reverse check
        group = SIMILAR_COLORS.get(c2);
        if (group != null && group.contains(c1)) {
            return 65.0;
        }

        return 0.0;
    }
}

