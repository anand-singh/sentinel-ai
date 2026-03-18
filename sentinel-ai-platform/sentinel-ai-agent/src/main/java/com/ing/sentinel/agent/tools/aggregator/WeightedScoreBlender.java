package com.ing.sentinel.agent.tools.aggregator;

import com.google.adk.tools.Annotations.Schema;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Weighted Score Blender Tool
 * 
 * Combines normalized scores from multiple agents (Pattern, Behavioral, AML)
 * into a single weighted risk score using configurable weights.
 */
public class WeightedScoreBlender {

    private static final Logger logger = Logger.getLogger(WeightedScoreBlender.class.getName());
    
    // Default weights (configurable, must sum to 1.0)
    private static final double DEFAULT_WEIGHT_PATTERN = 0.40;
    private static final double DEFAULT_WEIGHT_BEHAVIORAL = 0.40;
    private static final double DEFAULT_WEIGHT_AML = 0.20;

    /**
     * Blends normalized scores from multiple agents into a weighted aggregate.
     * 
     * @param patternScore Normalized pattern agent score (0-1)
     * @param behavioralScore Normalized behavioral agent score (0-1)
     * @param amlScore Normalized AML agent score (0-1), optional
     * @param weightPattern Weight for pattern score (default 0.40)
     * @param weightBehavioral Weight for behavioral score (default 0.40)
     * @param weightAml Weight for AML score (default 0.20)
     * @return Weighted blended score with contributions
     */
    @Schema(name = "blend_weighted_scores", description = "Combines normalized scores from Pattern, Behavioral, and AML agents into a weighted aggregate risk score. Returns blended score and per-source contributions.")
    public static Map<String, Object> blendWeightedScores(
            @Schema(name = "pattern_score", description = "Normalized pattern agent score (0-1)") double patternScore,
            @Schema(name = "behavioral_score", description = "Normalized behavioral agent score (0-1)") double behavioralScore,
            @Schema(name = "aml_score", description = "Normalized AML agent score (0-1), use -1 if not available") double amlScore,
            @Schema(name = "weight_pattern", description = "Weight for pattern score (0-1), default 0.40") double weightPattern,
            @Schema(name = "weight_behavioral", description = "Weight for behavioral score (0-1), default 0.40") double weightBehavioral,
            @Schema(name = "weight_aml", description = "Weight for AML score (0-1), default 0.20") double weightAml) {
        
        logger.info("🎛️ Blending scores: pattern=" + patternScore + ", behavioral=" + behavioralScore + 
                ", aml=" + amlScore);
        
        Map<String, Object> result = new HashMap<>();
        
        // Handle missing AML score - redistribute weights proportionally
        boolean hasAml = amlScore >= 0;
        double actualWeightPattern = weightPattern > 0 ? weightPattern : DEFAULT_WEIGHT_PATTERN;
        double actualWeightBehavioral = weightBehavioral > 0 ? weightBehavioral : DEFAULT_WEIGHT_BEHAVIORAL;
        double actualWeightAml = weightAml > 0 ? weightAml : DEFAULT_WEIGHT_AML;
        
        if (!hasAml) {
            // Redistribute AML weight proportionally between pattern and behavioral
            double totalNonAml = actualWeightPattern + actualWeightBehavioral;
            if (totalNonAml > 0) {
                // Only redistribute if weights don't already sum to 1
                double weightSum = actualWeightPattern + actualWeightBehavioral;
                if (weightSum < 1.0) {
                    double scaleFactor = (actualWeightPattern + actualWeightBehavioral + actualWeightAml) / totalNonAml;
                    actualWeightPattern *= scaleFactor;
                    actualWeightBehavioral *= scaleFactor;
                }
                actualWeightAml = 0;
            }
            logger.info("⚠️ AML score not available, redistributed weights: pattern=" + 
                    actualWeightPattern + ", behavioral=" + actualWeightBehavioral);
        }
        
        // Calculate weighted blend
        double blendedScore = 
                (patternScore * actualWeightPattern) +
                (behavioralScore * actualWeightBehavioral) +
                (hasAml ? amlScore * actualWeightAml : 0.0);
        
        // Ensure bounds [0..1]
        blendedScore = Math.max(0.0, Math.min(1.0, blendedScore));
        
        // Calculate individual contributions (as percentages of final score)
        Map<String, Double> contributions = new HashMap<>();
        contributions.put("pattern_agent", Math.round(patternScore * actualWeightPattern * 100 * 10.0) / 10.0);
        contributions.put("behavioral_agent", Math.round(behavioralScore * actualWeightBehavioral * 100 * 10.0) / 10.0);
        if (hasAml) {
            contributions.put("aml_agent", Math.round(amlScore * actualWeightAml * 100 * 10.0) / 10.0);
        }
        
        result.put("blended_score", Math.round(blendedScore * 10000.0) / 10000.0);
        result.put("contributions", contributions);
        result.put("weights_used", Map.of(
            "pattern", actualWeightPattern,
            "behavioral", actualWeightBehavioral,
            "aml", actualWeightAml
        ));
        result.put("aml_included", hasAml);
        
        logger.info("✅ Blended score: " + blendedScore + " (contributions: " + contributions + ")");
        
        return result;
    }
}

