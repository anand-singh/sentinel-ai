package com.ing.sentinel.agent.tools.pattern;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Score Blender Tool
 * 
 * Combines all signal analyses into a single weighted risk score (0-100).
 * Uses configurable weights for each signal type.
 */
public class ScoreBlenderTool {

    private static final Logger logger = Logger.getLogger(ScoreBlenderTool.class.getName());
    
    // Default signal weights (must sum to 1.0)
    private static final double WEIGHT_AMOUNT = 0.35;
    private static final double WEIGHT_GEO = 0.25;
    private static final double WEIGHT_VELOCITY = 0.20;
    private static final double WEIGHT_MCC = 0.10;
    private static final double WEIGHT_TIME = 0.10;
    
    // Version info
    private static final String AGENT_VERSION = "pattern-v1.0.0";
    private static final String CONFIG_VERSION = "weights-2026-03-16";

    /**
     * Blends individual signal scores into a final risk score.
     * 
     * @param amountSignal Normalized amount spike signal (0-1)
     * @param geoSignal Normalized geo distance signal (0-1)
     * @param velocitySignal Normalized velocity signal (0-1)
     * @param mccSignal Normalized MCC rarity signal (0-1)
     * @param timeSignal Normalized time window signal (0-1)
     * @param amountFlag Flag from amount analysis (or null)
     * @param geoFlag Flag from geo analysis (or null)
     * @param velocityFlag Flag from velocity analysis (or null)
     * @param mccFlag Flag from MCC analysis (or null)
     * @param timeFlag Flag from time analysis (or null)
     * @return Blended risk score with all metadata
     */
    @Schema(name = "blend_risk_scores", description = "Combines all signal analyses into a final weighted risk score (0-100). Returns risk_score, flags, reasoning, feature_contributions, and version info.")
    public static Map<String, Object> blendRiskScores(
            @Schema(name = "amount_signal", description = "Normalized amount spike signal (0-1)") double amountSignal,
            @Schema(name = "geo_signal", description = "Normalized geo distance signal (0-1)") double geoSignal,
            @Schema(name = "velocity_signal", description = "Normalized velocity signal (0-1)") double velocitySignal,
            @Schema(name = "mcc_signal", description = "Normalized MCC rarity signal (0-1)") double mccSignal,
            @Schema(name = "time_signal", description = "Normalized time window signal (0-1)") double timeSignal,
            @Schema(name = "amount_flag", description = "Flag from amount analysis (AMOUNT_SPIKE or null)") String amountFlag,
            @Schema(name = "geo_flag", description = "Flag from geo analysis (GEO_MISMATCH or null)") String geoFlag,
            @Schema(name = "velocity_flag", description = "Flag from velocity analysis (UNUSUAL_VELOCITY or null)") String velocityFlag,
            @Schema(name = "mcc_flag", description = "Flag from MCC analysis (RARE_MCC or null)") String mccFlag,
            @Schema(name = "time_flag", description = "Flag from time analysis (UNUSUAL_TIME or null)") String timeFlag) {
        
        logger.info("🔍 Blending scores: amount=" + amountSignal + ", geo=" + geoSignal + 
                ", velocity=" + velocitySignal + ", mcc=" + mccSignal + ", time=" + timeSignal);
        
        Map<String, Object> result = new HashMap<>();
        
        // Calculate weighted sum
        double weightedSum = 
                (amountSignal * WEIGHT_AMOUNT) +
                (geoSignal * WEIGHT_GEO) +
                (velocitySignal * WEIGHT_VELOCITY) +
                (mccSignal * WEIGHT_MCC) +
                (timeSignal * WEIGHT_TIME);
        
        // Convert to 0-100 scale
        int riskScore = (int) Math.round(Math.min(100, Math.max(0, weightedSum * 100)));
        
        // Collect active flags
        List<String> flags = new ArrayList<>();
        if (amountFlag != null && !amountFlag.isEmpty() && !"null".equals(amountFlag)) flags.add(amountFlag);
        if (geoFlag != null && !geoFlag.isEmpty() && !"null".equals(geoFlag)) flags.add(geoFlag);
        if (velocityFlag != null && !velocityFlag.isEmpty() && !"null".equals(velocityFlag)) flags.add(velocityFlag);
        if (mccFlag != null && !mccFlag.isEmpty() && !"null".equals(mccFlag)) flags.add(mccFlag);
        if (timeFlag != null && !timeFlag.isEmpty() && !"null".equals(timeFlag)) flags.add(timeFlag);
        
        // Determine severity
        String severity = determineSeverity(riskScore);
        
        // Determine recommended action
        String recommendation = determineRecommendation(riskScore, flags);
        
        // Build feature contributions
        Map<String, Object> contributions = new HashMap<>();
        contributions.put("amount_contribution", Math.round(amountSignal * WEIGHT_AMOUNT * 100 * 10.0) / 10.0);
        contributions.put("geo_contribution", Math.round(geoSignal * WEIGHT_GEO * 100 * 10.0) / 10.0);
        contributions.put("velocity_contribution", Math.round(velocitySignal * WEIGHT_VELOCITY * 100 * 10.0) / 10.0);
        contributions.put("mcc_contribution", Math.round(mccSignal * WEIGHT_MCC * 100 * 10.0) / 10.0);
        contributions.put("time_contribution", Math.round(timeSignal * WEIGHT_TIME * 100 * 10.0) / 10.0);
        
        // Generate reasoning
        String reasoning = generateReasoning(flags, contributions, riskScore);
        
        // Build response
        result.put("risk_score", riskScore);
        result.put("severity", severity);
        result.put("flags", flags);
        result.put("reasoning", reasoning);
        result.put("recommendation", recommendation);
        result.put("feature_contributions", contributions);
        result.put("weights", Map.of(
                "amount", WEIGHT_AMOUNT,
                "geo", WEIGHT_GEO,
                "velocity", WEIGHT_VELOCITY,
                "mcc", WEIGHT_MCC,
                "time", WEIGHT_TIME
        ));
        result.put("agent_version", AGENT_VERSION);
        result.put("config_version", CONFIG_VERSION);
        result.put("analyzed_at", Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
        
        logger.info("✅ Blended score: " + riskScore + " (" + severity + "), flags=" + flags);
        
        return result;
    }
    
    /**
     * Determine severity level based on risk score
     */
    private static String determineSeverity(int riskScore) {
        if (riskScore >= 80) return "CRITICAL";
        if (riskScore >= 60) return "HIGH";
        if (riskScore >= 40) return "MEDIUM";
        if (riskScore >= 20) return "LOW";
        return "MINIMAL";
    }
    
    /**
     * Determine recommended action based on risk score and flags
     */
    private static String determineRecommendation(int riskScore, List<String> flags) {
        // Money mule patterns are always critical
        if (flags.contains("VELOCITY_CHECK_FAILED") || flags.contains("ROUND_TRIP_TRANSFER")) {
            return "BLOCK_AND_NOTIFY";
        }
        if (riskScore >= 80 || flags.contains("GEO_MISMATCH") || flags.contains("HIGH_RISK_DESTINATION")) {
            return "BLOCK_AND_NOTIFY";
        } else if (riskScore >= 60) {
            return "STEP_UP_AUTH";
        } else if (riskScore >= 40) {
            return "FLAG_FOR_REVIEW";
        } else if (riskScore >= 20) {
            return "MONITOR";
        }
        return "ALLOW";
    }
    
    /**
     * Generate human-readable reasoning from flags and contributions
     */
    private static String generateReasoning(List<String> flags, Map<String, Object> contributions, int score) {
        if (flags.isEmpty()) {
            return String.format("No anomalies detected. Risk score: %d", score);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Anomalies detected: ");
        
        List<String> reasons = new ArrayList<>();
        
        if (flags.contains("AMOUNT_SPIKE")) {
            reasons.add(String.format("unusual amount (+%.1f pts)", (Double) contributions.get("amount_contribution")));
        }
        if (flags.contains("GEO_MISMATCH")) {
            reasons.add(String.format("impossible travel (+%.1f pts)", (Double) contributions.get("geo_contribution")));
        }
        if (flags.contains("UNUSUAL_VELOCITY") || flags.contains("VELOCITY_CHECK_FAILED")) {
            reasons.add(String.format("high velocity (+%.1f pts)", (Double) contributions.get("velocity_contribution")));
        }
        if (flags.contains("RARE_MCC")) {
            reasons.add(String.format("unusual merchant (+%.1f pts)", (Double) contributions.get("mcc_contribution")));
        }
        if (flags.contains("UNUSUAL_TIME")) {
            reasons.add(String.format("unusual time (+%.1f pts)", (Double) contributions.get("time_contribution")));
        }
        if (flags.contains("ROUND_TRIP_TRANSFER")) {
            reasons.add("money mule pattern (round-trip transfer)");
        }
        if (flags.contains("HIGH_RISK_DESTINATION")) {
            reasons.add("high-risk destination country");
        }
        
        sb.append(String.join(", ", reasons));
        sb.append(String.format(". Total risk: %d/100", score));
        
        return sb.toString();
    }

    /**
     * Create FunctionTool wrapper for ADK
     */
    public static FunctionTool create() {
        return FunctionTool.create(ScoreBlenderTool.class, "blendRiskScores");
    }
}

