package com.ing.sentinel.agent.tools.aggregator;

import com.google.adk.tools.Annotations.Schema;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Score Normalizer Tool
 * 
 * Normalizes incoming risk scores from different agents to [0..1] scale
 * for consistent blending and aggregation.
 */
public class ScoreNormalizer {

    private static final Logger logger = Logger.getLogger(ScoreNormalizer.class.getName());

    /**
     * Normalizes a risk score to [0..1] scale.
     * 
     * @param score The input score (typically 0-100)
     * @param minValue Minimum expected value (default 0)
     * @param maxValue Maximum expected value (default 100)
     * @return Normalized score between 0 and 1
     */
    @Schema(name = "normalize_score", description = "Normalizes a risk score to [0..1] scale for consistent aggregation. Handles scores from different ranges.")
    public static Map<String, Object> normalizeScore(
            @Schema(name = "score", description = "The input score to normalize (e.g., 0-100)") double score,
            @Schema(name = "min_value", description = "Minimum expected value (default 0)") double minValue,
            @Schema(name = "max_value", description = "Maximum expected value (default 100)") double maxValue) {
        
        logger.info("🔧 Normalizing score: " + score + " from range [" + minValue + ", " + maxValue + "]");
        
        Map<String, Object> result = new HashMap<>();
        
        // Clamp input to valid range
        double clampedScore = Math.max(minValue, Math.min(maxValue, score));
        
        // Normalize to [0..1]
        double normalizedScore = (maxValue - minValue) > 0 
            ? (clampedScore - minValue) / (maxValue - minValue)
            : 0.0;
        
        // Ensure bounds
        normalizedScore = Math.max(0.0, Math.min(1.0, normalizedScore));
        
        result.put("normalized_score", Math.round(normalizedScore * 10000.0) / 10000.0);
        result.put("original_score", score);
        result.put("clamped_score", clampedScore);
        result.put("was_clamped", score != clampedScore);
        
        logger.info("✅ Normalized: " + score + " → " + normalizedScore);
        
        return result;
    }
}

