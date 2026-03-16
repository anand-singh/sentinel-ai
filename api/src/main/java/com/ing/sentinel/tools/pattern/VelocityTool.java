package com.ing.sentinel.tools.pattern;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Velocity Signal Tool
 * 
 * Analyzes transaction velocity (frequency) patterns to detect
 * unusual bursts of activity that may indicate fraud.
 */
public class VelocityTool {

    private static final Logger logger = Logger.getLogger(VelocityTool.class.getName());
    
    // Thresholds for velocity flags
    private static final int VELOCITY_1H_THRESHOLD = 3;   // Max normal transactions per hour
    private static final int VELOCITY_24H_THRESHOLD = 10; // Max normal transactions per day
    
    // Baseline multipliers for MCC-specific thresholds
    private static final double HIGH_VELOCITY_MULTIPLIER = 2.0;

    /**
     * Analyzes transaction velocity patterns.
     * 
     * @param velocityLast1h Number of transactions in the last hour
     * @param velocityLast24h Number of transactions in the last 24 hours
     * @param mccBaselineVelocity1h Expected hourly velocity for this MCC
     * @param mccBaselineVelocity24h Expected daily velocity for this MCC
     * @return Analysis result with velocity metrics and flag
     */
    @Schema(name = "analyze_velocity", description = "Analyzes transaction frequency patterns to detect unusual velocity bursts. Returns velocity metrics, comparison to baselines, and whether UNUSUAL_VELOCITY flag should be raised.")
    public static Map<String, Object> analyzeVelocity(
            @Schema(name = "velocity_last_1h", description = "Number of transactions by this customer in the last hour") int velocityLast1h,
            @Schema(name = "velocity_last_24h", description = "Number of transactions by this customer in the last 24 hours") int velocityLast24h,
            @Schema(name = "mcc_baseline_velocity_1h", description = "Expected hourly transaction count for this merchant category (default: 1)") double mccBaselineVelocity1h,
            @Schema(name = "mcc_baseline_velocity_24h", description = "Expected daily transaction count for this merchant category (default: 3)") double mccBaselineVelocity24h) {
        
        logger.info("🔍 Analyzing velocity: 1h=" + velocityLast1h + ", 24h=" + velocityLast24h);
        
        Map<String, Object> result = new HashMap<>();
        
        // Use defaults if baselines not provided
        if (mccBaselineVelocity1h <= 0) mccBaselineVelocity1h = 1.0;
        if (mccBaselineVelocity24h <= 0) mccBaselineVelocity24h = 3.0;
        
        // Calculate velocity ratios
        double ratio1h = velocityLast1h / mccBaselineVelocity1h;
        double ratio24h = velocityLast24h / mccBaselineVelocity24h;
        
        // Determine if velocity is unusual
        boolean unusual1h = velocityLast1h >= VELOCITY_1H_THRESHOLD || ratio1h >= HIGH_VELOCITY_MULTIPLIER;
        boolean unusual24h = velocityLast24h >= VELOCITY_24H_THRESHOLD || ratio24h >= HIGH_VELOCITY_MULTIPLIER;
        boolean flagRaised = unusual1h || unusual24h;
        
        // Calculate normalized signal [0,1]
        double normalizedSignal = calculateNormalizedSignal(ratio1h, ratio24h);
        
        result.put("velocity_1h", velocityLast1h);
        result.put("velocity_24h", velocityLast24h);
        result.put("ratio_1h", Math.round(ratio1h * 100.0) / 100.0);
        result.put("ratio_24h", Math.round(ratio24h * 100.0) / 100.0);
        result.put("normalized_signal", Math.round(normalizedSignal * 1000.0) / 1000.0);
        result.put("flag", flagRaised ? "UNUSUAL_VELOCITY" : null);
        result.put("flag_raised", flagRaised);
        result.put("unusual_1h", unusual1h);
        result.put("unusual_24h", unusual24h);
        result.put("reasoning", generateReasoning(velocityLast1h, velocityLast24h, ratio1h, ratio24h, flagRaised));
        
        logger.info("✅ Velocity analysis: ratio_1h=" + ratio1h + ", ratio_24h=" + ratio24h + ", flag=" + flagRaised);
        
        return result;
    }
    
    /**
     * Calculate normalized signal [0,1]
     */
    private static double calculateNormalizedSignal(double ratio1h, double ratio24h) {
        // Weight 1h velocity more heavily (more indicative of burst attacks)
        double weightedRatio = (ratio1h * 0.7) + (ratio24h * 0.3);
        // Sigmoid-like normalization
        return Math.min(1.0, weightedRatio / 3.0);
    }
    
    /**
     * Generate human-readable reasoning
     */
    private static String generateReasoning(int v1h, int v24h, double r1h, double r24h, boolean flagRaised) {
        if (flagRaised) {
            if (r1h >= HIGH_VELOCITY_MULTIPLIER) {
                return String.format("High velocity burst: %d transactions in last hour (%.1fx baseline)", v1h, r1h);
            } else {
                return String.format("Elevated transaction frequency: %d in 24h (%.1fx baseline)", v24h, r24h);
            }
        } else {
            return String.format("Normal velocity: %d/hour, %d/day within expected range", v1h, v24h);
        }
    }

    /**
     * Create FunctionTool wrapper for ADK
     */
    public static FunctionTool create() {
        return FunctionTool.create(VelocityTool.class, "analyzeVelocity");
    }
}

