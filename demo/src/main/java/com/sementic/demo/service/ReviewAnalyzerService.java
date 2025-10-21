package com.sementic.demo.service;

import com.sementic.demo.model.AspectRating;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ReviewAnalyzerService {
    private static final Map<String, String[]> ASPECT_KEYWORDS = Map.of(
            "display", new String[]{
                    "screen", "display", "oled", "amoled", "brightness", "contrast",
                    "colors", "blacks", "refresh", "hz", "sunlight", "outdoors", "indoor"
            },
            "camera", new String[]{
                    "camera", "photo", "picture", "selfie", "lens", "zoom", "low-light",
                    "grainy", "bokeh", "ultra-wide", "macro", "night", "portrait", "dynamic range"
            },
            "battery", new String[]{
                    "battery", "charging", "power", "backup", "drain", "drains", "endurance",
                    "mah", "fast charging", "overnight", "standby", "swelled", "longevity"
            },
            "build", new String[]{
                    "build", "material", "design", "durability", "feel", "premium", "plastic",
                    "glass", "aluminum", "frame", "matte", "glossy", "fingerprint", "scratches", "flex"
            },
            "value", new String[]{"price", "cost", "value", "expensive", "cheap", "worth"}
    );

    // --- SENTIMENT LEXICON ---
    private static final Map<String, Double> SENTIMENT_WORDS = Map.ofEntries(
            // Positive
            Map.entry("awesome", 0.9),
            Map.entry("excellent", 0.95),
            Map.entry("great", 0.85),
            Map.entry("good", 0.6),
            Map.entry("nice", 0.55),
            Map.entry("ok", 0.3),
            Map.entry("average", 0.4),
            Map.entry("decent", 0.5),
            Map.entry("beautiful", 0.85),
            Map.entry("fine", 0.4),
            Map.entry("outdoors", 0.3),
            Map.entry("daylight", 0.2),
            // Negative
            Map.entry("bad", -0.6),
            Map.entry("worse", -0.8),
            Map.entry("worst", -0.95),
            Map.entry("terrible", -0.9),
            Map.entry("poor", -0.7),
            Map.entry("awful", -0.9),
            Map.entry("struggles", -0.75),
            Map.entry("struggle", -0.75),
            Map.entry("drains", -0.8),
            Map.entry("drained", -0.75),
            Map.entry("indoors", -0.3),
            Map.entry("stunning", 0.9),
            Map.entry("vibrant", 0.7),
            Map.entry("deep", 0.5),
            Map.entry("letdown", -0.8),
            Map.entry("grainy", -0.6),
            Map.entry("disappointing", -0.75),
            Map.entry("premium", 0.7),
            Map.entry("cheap", -0.5),
            Map.entry("buttery", 0.6),
            Map.entry("unacceptable", -0.9),
            Map.entry("polished", 0.8)
    );

    // --- PRECOMPUTED: keyword -> aspect ---
    private static final Map<String, String> KEYWORD_TO_ASPECT = new HashMap<>();

    static {
        for (Map.Entry<String, String[]> entry : ASPECT_KEYWORDS.entrySet()) {
            String aspect = entry.getKey();
            for (String keyword : entry.getValue()) {
                KEYWORD_TO_ASPECT.put(keyword, aspect);
            }
        }
    }

    // --- DATA CLASSES (keep as static nested or top-level) ---
    private record AspectMention(String aspect, int position)  {}
    private record SentimentWord(double sentiment, int position)  {}

    // --- MAIN ANALYSIS METHOD ---
    public List<AspectRating> analyze(String reviewText) {
        if (reviewText == null || reviewText.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Split into sentences (handles missing terminal punctuation by treating whole as one)
        String[] sentences = reviewText.split("(?<=[.!?])\\s*");
        Map<String, List<Double>> aspectSentiments = new HashMap<>();

        for (String sentence : sentences) {
            String cleanSent = sentence.toLowerCase().trim();
            if (cleanSent.isEmpty()) continue;

            // Tokenize: split and remove non-letter chars
            List<String> tokens = Arrays.stream(cleanSent.split("\\s+"))
                    .map(token -> token.replaceAll("[^a-z]", ""))
                    .filter(token -> !token.isEmpty())
                    .collect(Collectors.toList());

            if (tokens.isEmpty()) continue;

            // Build position -> aspect map for O(1) lookup
            Map<Integer, String> aspectAtPosition = new HashMap<>();
            for (int i = 0; i < tokens.size(); i++) {
                String aspect = KEYWORD_TO_ASPECT.get(tokens.get(i));
                if (aspect != null) {
                    aspectAtPosition.put(i, aspect);
                }
            }

            // Find sentiment words with positions
            List<SentimentWord> sentimentWords = IntStream.range(0, tokens.size())
                    .filter(i -> SENTIMENT_WORDS.containsKey(tokens.get(i)))
                    .mapToObj(i -> new SentimentWord(SENTIMENT_WORDS.get(tokens.get(i)), i))
                    .collect(Collectors.toList());

            // Assign each sentiment word to the nearest aspect **to the left** (within 6 words)
            for (SentimentWord sw : sentimentWords) {
                for (int pos = sw.position - 1; pos >= Math.max(0, sw.position - 6); pos--) {
                    String aspect = aspectAtPosition.get(pos);
                    if (aspect != null) {
                        aspectSentiments.computeIfAbsent(aspect, k -> new ArrayList<>())
                                .add(sw.sentiment);
                        break; // assign to first (closest left) aspect only
                    }
                }
            }
        }

        // Build final result
        return aspectSentiments.entrySet().stream()
                .map(entry -> {
                    String aspect = entry.getKey();
                    List<Double> sentiments = entry.getValue();
                    double avgSent = sentiments.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    double rating = Math.max(1.0, Math.min(5.0, 3.0 + avgSent * 2.0));
                    return new AspectRating(aspect, avgSent, rating);
                })
                .collect(Collectors.toList());
    }
}