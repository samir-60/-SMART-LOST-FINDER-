package com.findback.service;

import com.findback.model.ItemReport;
import com.findback.model.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Real-Time Semantic AI Matching Service powered by Groq Llama-3/GPT-OSS high-speed inference.
 * Compares campus lost & found belongings using deep contextual reasoning
 * and generates human-friendly, explainable assessments.
 */
public class GroqAiService {
    private static final Logger log = LoggerFactory.getLogger(GroqAiService.class);

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String PRIMARY_MODEL = "openai/gpt-oss-120b";
    private static final String FALLBACK_MODEL = "qwen/qwen3.8-27b";

    private final HttpClient httpClient;
    private final String apiKey;

    public GroqAiService() {
        String key = System.getenv("GROQ_API_KEY");
        if (key == null || key.trim().isEmpty()) {
            key = System.getProperty("groq.api.key");
        }
        if (key == null || key.trim().isEmpty()) {
            java.io.File keyFile = new java.io.File("config/groq.key");
            if (keyFile.exists()) {
                try {
                    key = java.nio.file.Files.readString(keyFile.toPath()).trim();
                } catch (Exception ignore) {}
            }
        }
        this.apiKey = key != null ? key : "";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .build();
    }

    public static class AiMatchAssessment {
        public double totalScore;
        public double categoryScore;
        public double locationScore;
        public double dateScore;
        public double colorScore;
        public double descriptionScore;
        public String confidence;
        public String explanation;
        public boolean isAiGenerated;

        public AiMatchAssessment(double totalScore, double cat, double loc, double date, double col, double desc, String conf, String exp, boolean isAi) {
            this.totalScore = totalScore;
            this.categoryScore = cat;
            this.locationScore = loc;
            this.dateScore = date;
            this.colorScore = col;
            this.descriptionScore = desc;
            this.confidence = conf;
            this.explanation = exp;
            this.isAiGenerated = isAi;
        }
    }

    /**
     * Evaluates a lost report and a found report using Groq real-time reasoning.
     * Falls back to offline calculation if offline or if API is unreachable.
     */
    public AiMatchAssessment evaluateMatch(ItemReport lost, ItemReport found) {
        try {
            String prompt = buildPrompt(lost, found);
            String responseJson = callGroqApi(prompt, PRIMARY_MODEL);
            if (responseJson == null) {
                responseJson = callGroqApi(prompt, FALLBACK_MODEL);
            }

            if (responseJson != null) {
                AiMatchAssessment assessment = parseAiResponse(responseJson);
                if (assessment != null) {
                    log.info("Groq AI successfully evaluated match between Lost #{} and Found #{}: Score={}",
                            lost.getId(), found.getId(), assessment.totalScore);
                    return assessment;
                }
            }
        } catch (Exception ex) {
            log.warn("Groq AI matching notice (will use offline rule engine): {}", ex.getMessage());
        }

        // Graceful offline fallback
        return null;
    }

    private String buildPrompt(ItemReport lost, ItemReport found) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an experienced campus lost & found coordinator at Metropolitan University.\\n");
        sb.append("Compare these two item reports and decide if they represent the same belongings.\\n\\n");
        sb.append("LOST ITEM:\\n");
        sb.append("- Title: ").append(escape(lost.getTitle())).append("\\n");
        sb.append("- Category: ").append(escape(lost.getCategoryName())).append("\\n");
        sb.append("- Campus Location: ").append(escape(lost.getLocationName()));
        if (lost.getSpecificLocation() != null) sb.append(" (").append(escape(lost.getSpecificLocation())).append(")");
        sb.append("\\n- Event Date: ").append(lost.getEventDate()).append("\\n");
        sb.append("- Color: ").append(escape(lost.getColor())).append("\\n");
        sb.append("- Details: ").append(escape(lost.getDescription())).append("\\n\\n");

        sb.append("FOUND ITEM:\\n");
        sb.append("- Title: ").append(escape(found.getTitle())).append("\\n");
        sb.append("- Category: ").append(escape(found.getCategoryName())).append("\\n");
        sb.append("- Campus Location: ").append(escape(found.getLocationName()));
        if (found.getSpecificLocation() != null) sb.append(" (").append(escape(found.getSpecificLocation())).append(")");
        sb.append("\\n- Event Date: ").append(found.getEventDate()).append("\\n");
        sb.append("- Color: ").append(escape(found.getColor())).append("\\n");
        sb.append("- Details: ").append(escape(found.getDescription())).append("\\n\\n");

        sb.append("Output STRICT valid JSON with these fields:\\n");
        sb.append("{\\n");
        sb.append("  \\\"score\\\": (number 0 to 100),\\n");
        sb.append("  \\\"category_score\\\": (number 0 to 25),\\n");
        sb.append("  \\\"location_score\\\": (number 0 to 25),\\n");
        sb.append("  \\\"date_score\\\": (number 0 to 20),\\n");
        sb.append("  \\\"color_score\\\": (number 0 to 15),\\n");
        sb.append("  \\\"description_score\\\": (number 0 to 15),\\n");
        sb.append("  \\\"confidence\\\": \\\"Strong Potential Match\\\" or \\\"Possible Match\\\" or \\\"Low Probability\\\",\\n");
        sb.append("  \\\"explanation\\\": \\\"A warm, conversational, human explanation (2-3 sentences) detailing why these items match or differ. Do NOT say 'As an AI' or 'Based on the JSON'. Speak like a helpful university coordinator.\\\"\\n");
        sb.append("}");
        return sb.toString();
    }

    private String callGroqApi(String userContent, String model) {
        try {
            String requestBody = "{"
                    + "\"model\": \"" + model + "\","
                    + "\"messages\": ["
                    + "  {\"role\": \"system\", \"content\": \"You are a helpful campus lost and found coordinator at Metropolitan University. Always output strict JSON.\"},"
                    + "  {\"role\": \"user\", \"content\": \"" + userContent + "\"}"
                    + "],"
                    + "\"temperature\": 0.2,"
                    + "\"response_format\": {\"type\": \"json_object\"}"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                log.warn("Groq API returned status {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("Failed to contact Groq API with model {}: {}", model, e.getMessage());
        }
        return null;
    }

    private AiMatchAssessment parseAiResponse(String responseJson) {
        try {
            // Find message content in OpenAI format
            int contentIdx = responseJson.indexOf("\"content\":");
            if (contentIdx == -1) return null;

            int startQuote = responseJson.indexOf("\"", contentIdx + 10);
            if (startQuote == -1) return null;

            // Unescape JSON string content or extract embedded JSON object
            String rawContent;
            int closingBrace = responseJson.lastIndexOf("}");
            String contentSubstring = responseJson.substring(startQuote + 1);
            int endQuote = findMatchingEndQuote(contentSubstring);
            if (endQuote != -1) {
                rawContent = unescape(contentSubstring.substring(0, endQuote));
            } else {
                rawContent = responseJson;
            }

            double score = extractDouble(rawContent, "score", 0.0);
            double cat = extractDouble(rawContent, "category_score", 0.0);
            double loc = extractDouble(rawContent, "location_score", 0.0);
            double date = extractDouble(rawContent, "date_score", 0.0);
            double col = extractDouble(rawContent, "color_score", 0.0);
            double desc = extractDouble(rawContent, "description_score", 0.0);

            if (score <= 0 && (cat + loc + date + col + desc) > 0) {
                score = cat + loc + date + col + desc;
            }

            String confidence = extractString(rawContent, "confidence",
                    score >= 85 ? "Strong Potential Match" : (score >= 70 ? "Possible Match" : "Low Probability"));
            String explanation = extractString(rawContent, "explanation",
                    "Real-time campus item comparison completed with " + String.format("%.0f%%", score) + " similarity.");

            return new AiMatchAssessment(score, cat, loc, date, col, desc, confidence, explanation, true);
        } catch (Exception e) {
            log.warn("Error parsing Groq AI output: {}", e.getMessage());
            return null;
        }
    }

    private int findMatchingEndQuote(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '"' && (i == 0 || s.charAt(i - 1) != '\\')) {
                return i;
            }
        }
        return -1;
    }

    private double extractDouble(String json, String key, double defaultVal) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
        Matcher m = p.matcher(json);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (Exception ignore) {}
        }
        return defaultVal;
    }

    private String extractString(String json, String key, String defaultVal) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1).replace("\\n", " ").replace("\\\"", "\"");
        }
        return defaultVal;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }

    private String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\");
    }
}
