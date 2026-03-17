package com.ing.sentinel.tools.behavioral;

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
 * Behavioral Score Blender
 * 
 * Combines all behavioral signal analyses into a single weighted risk score (0-100).
 * Uses configurable weights for each signal type, focused on customer-specific behavior.
 */
public class BehavioralScoreBlender {

    private static final Logger logger = Logger.getLogger(BehavioralScoreBlender.class.getName());
    
    // Default signal weights (must sum to 1.0)
    private static final double WEIGHT_AMOUNT = 0.20;
    private static final double WEIGHT_GEO = 0.15;
    private static final double WEIGHT_DEVICE = 0.15;
    private static final double WEIGHT_TIME = 0.05;
    private static final double WEIGHT_MERCHANT = 0.05;
    private static final double WEIGHT_VELOCITY = 0.15;
    private static final double WEIGHT_NEW_ACCOUNT = 0.25;  // High weight for new accounts (money mule risk)
    
    // Version info
    private static final String AGENT_VERSION = "behavior-v1.0.0";
    private static final String CONFIG_VERSION = "behavior-weights-2026-03-16";

    /**
     * Blends individual behavioral signal scores into a final risk score.
     */
    @Schema(name = "blend_behavioral_scores", description = "Combines all behavioral signal analyses into a final weighted behavioral risk score (0-100). Returns behavioral_risk_score, flags, reasoning, feature_contributions, and version info.")
    public static Map<String, Object> blendBehavioralScores(
            @Schema(name = "amount_signal", description = "Normalized amount deviation signal (0-1)") double amountSignal,
            @Schema(name = "time_signal", description = "Normalized time deviation signal (0-1)") double timeSignal,
            @Schema(name = "geo_signal", description = "Normalized geo deviation signal (0-1)") double geoSignal,
            @Schema(name = "device_signal", description = "Normalized new device signal (0-1)") double deviceSignal,
            @Schema(name = "ip_signal", description = "Normalized new IP range signal (0-1)") double ipSignal,
            @Schema(name = "merchant_signal", description = "Normalized merchant novelty signal (0-1)") double merchantSignal,
            @Schema(name = "velocity_signal", description = "Normalized burst activity signal (0-1)") double velocitySignal,
            @Schema(name = "new_account_signal", description = "Normalized new account signal (0-1), high for accounts < 30 days old") double newAccountSignal,
            @Schema(name = "amount_flag", description = "Flag from amount analysis (AMOUNT_DEVIATION or null)") String amountFlag,
            @Schema(name = "time_flag", description = "Flag from time analysis (UNUSUAL_TIME or null)") String timeFlag,
            @Schema(name = "geo_flag", description = "Flag from geo analysis (GEO_DEVIATION or null)") String geoFlag,
            @Schema(name = "device_flag", description = "Flag from device analysis (NEW_DEVICE or null)") String deviceFlag,
            @Schema(name = "ip_flag", description = "Flag from IP analysis (NEW_IP_RANGE or null)") String ipFlag,
            @Schema(name = "merchant_flag", description = "Flag from merchant analysis (RARE_MERCHANT/RARE_MCC or null)") String merchantFlag,
            @Schema(name = "velocity_flag", description = "Flag from velocity analysis (BURST_ACTIVITY or null)") String velocityFlag,
            @Schema(name = "new_account_flag", description = "Flag from new account analysis (NEW_ACCOUNT or null)") String newAccountFlag,
            @Schema(name = "amount_zscore_customer", description = "Customer-specific amount z-score for feature contributions") double amountZscoreCustomer,
            @Schema(name = "geo_distance_km", description = "Geographic distance in km for feature contributions") double geoDistanceKm) {
        
        logger.info("🔍 Blending behavioral scores...");
        
        Map<String, Object> result = new HashMap<>();
        
        // Combine device and IP signals (they're related)
        double combinedDeviceIpSignal = Math.max(deviceSignal, ipSignal);
        
        // Calculate weighted sum
        double weightedSum = 
                (amountSignal * WEIGHT_AMOUNT) +
                (geoSignal * WEIGHT_GEO) +
                (combinedDeviceIpSignal * WEIGHT_DEVICE) +
                (timeSignal * WEIGHT_TIME) +
                (merchantSignal * WEIGHT_MERCHANT) +
                (velocitySignal * WEIGHT_VELOCITY) +
                (newAccountSignal * WEIGHT_NEW_ACCOUNT);
        
        // Apply boost for high-risk combinations (new account + new device + high velocity)
        if (newAccountSignal > 0.7 && combinedDeviceIpSignal > 0.5 && velocitySignal > 0.5) {
            weightedSum = Math.min(1.0, weightedSum + 0.15);
            logger.info("⚠️ Applying high-risk combination boost");
        }
        
        // Convert to 0-100 scale
        int behavioralRiskScore = (int) Math.round(Math.min(100, Math.max(0, weightedSum * 100)));
        
        // Collect active flags
        List<String> flags = new ArrayList<>();
        addFlagIfPresent(flags, amountFlag);
        addFlagIfPresent(flags, timeFlag);
        addFlagIfPresent(flags, geoFlag);
        addFlagIfPresent(flags, deviceFlag);
        addFlagIfPresent(flags, ipFlag);
        addFlagIfPresent(flags, merchantFlag);
        addFlagIfPresent(flags, velocityFlag);
        addFlagIfPresent(flags, newAccountFlag);
        
        // Build feature contributions
        Map<String, Object> contributions = new HashMap<>();
        contributions.put("amount_zscore_customer", amountZscoreCustomer);
        contributions.put("hour_deviation", timeSignal);
        contributions.put("geo_distance_km", geoDistanceKm);
        contributions.put("new_device", deviceSignal > 0.5 ? 1 : 0);
        contributions.put("new_ip_range", ipSignal > 0.5 ? 1 : 0);
        contributions.put("merchant_novelty", Math.round(merchantSignal * 1000.0) / 1000.0);
        contributions.put("burst_in_window", velocitySignal > 0.5 ? 1 : 0);
        contributions.put("new_account", newAccountSignal > 0.5 ? 1 : 0);
        
        // Generate reasoning
        String reasoning = generateReasoning(flags, amountZscoreCustomer, geoDistanceKm, behavioralRiskScore);
        
        // Build response
        result.put("behavioral_risk_score", behavioralRiskScore);
        result.put("flags", flags);
        result.put("reasoning", reasoning);
        result.put("feature_contributions", contributions);
        result.put("weights", Map.of(
                "amount", WEIGHT_AMOUNT,
                "geo", WEIGHT_GEO,
                "device", WEIGHT_DEVICE,
                "time", WEIGHT_TIME,
                "merchant", WEIGHT_MERCHANT,
                "velocity", WEIGHT_VELOCITY,
                "new_account", WEIGHT_NEW_ACCOUNT
        ));
        result.put("version", AGENT_VERSION);
        result.put("config_version", CONFIG_VERSION);
        result.put("analyzed_at", Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
        
        logger.info("✅ Behavioral score: " + behavioralRiskScore + ", flags=" + flags);
        
        return result;
    }
    
    private static void addFlagIfPresent(List<String> flags, String flag) {
        if (flag != null && !flag.isEmpty() && !"null".equals(flag)) {
            flags.add(flag);
        }
    }
    
    private static String generateReasoning(List<String> flags, double amountZscore, double geoKm, int score) {
        if (flags.isEmpty()) {
            return String.format("Transaction is consistent with customer's typical behavior. Risk score: %d", score);
        }
        
        List<String> reasons = new ArrayList<>();
        
        for (String flag : flags) {
            switch (flag) {
                case "AMOUNT_DEVIATION":
                    reasons.add(String.format("Amount %.1fσ above customer's normal", amountZscore));
                    break;
                case "NEW_DEVICE":
                    reasons.add("new device");
                    break;
                case "NEW_IP_RANGE":
                    reasons.add("new IP range");
                    break;
                case "UNUSUAL_TIME":
                    reasons.add("off-hours");
                    break;
                case "GEO_DEVIATION":
                    reasons.add(String.format("travel %.0fkm from last txn", geoKm));
                    break;
                case "RARE_MERCHANT":
                case "RARE_MCC":
                    reasons.add("unusual merchant/category");
                    break;
                case "BURST_ACTIVITY":
                    reasons.add("high velocity");
                    break;
                case "NEW_ACCOUNT":
                    reasons.add("newly opened account (high money mule risk)");
                    break;
                default:
                    reasons.add(flag.toLowerCase().replace("_", " "));
            }
        }
        
        return String.join(", ", reasons) + ".";
    }

    public static FunctionTool create() {
        return FunctionTool.create(BehavioralScoreBlender.class, "blendBehavioralScores");
    }
}

