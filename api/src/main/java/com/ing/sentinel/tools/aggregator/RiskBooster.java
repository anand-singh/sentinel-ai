package com.ing.sentinel.tools.aggregator;

import com.google.adk.tools.Annotations.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Risk Booster Tool
 * 
 * Applies non-linear risk boosts when certain high-risk flag combinations co-occur.
 * For example: AMOUNT_SPIKE + NEW_DEVICE + GEO_MISMATCH together is riskier than
 * the sum of individual signals.
 */
public class RiskBooster {

    private static final Logger logger = Logger.getLogger(RiskBooster.class.getName());
    
    // Boost caps to prevent runaway scores
    private static final double MAX_BOOST = 0.15; // Max +15% boost
    private static final double SINGLE_COMBO_BOOST = 0.05; // +5% per combo

    /**
     * Applies risk boost based on flag combinations.
     * 
     * @param baseScore The base blended score (0-1)
     * @param combinedFlags Comma-separated list of all flags from all agents
     * @return Boosted score with explanation
     */
    @Schema(name = "apply_risk_boost", description = "Applies non-linear risk boost when high-risk flag combinations co-occur (e.g., AMOUNT_SPIKE + NEW_DEVICE + GEO_MISMATCH). Returns boosted score.")
    public static Map<String, Object> applyRiskBoost(
            @Schema(name = "base_score", description = "The base blended score (0-1) before boost") double baseScore,
            @Schema(name = "combined_flags", description = "Comma-separated list of all flags from all agents (e.g., 'AMOUNT_SPIKE,NEW_DEVICE,GEO_MISMATCH')") String combinedFlags) {
        
        logger.info("🚀 Applying risk boost to base score: " + baseScore + " with flags: " + combinedFlags);
        
        Map<String, Object> result = new HashMap<>();
        
        if (combinedFlags == null || combinedFlags.trim().isEmpty()) {
            result.put("boosted_score", baseScore);
            result.put("boost_applied", 0.0);
            result.put("boost_reason", "No flags present");
            return result;
        }
        
        // Parse flags
        String[] flags = combinedFlags.toUpperCase().split(",");
        List<String> flagList = List.of(flags);
        
        double totalBoost = 0.0;
        StringBuilder boostReason = new StringBuilder();
        
        // Critical flags that warrant immediate boost
        if (containsAny(flagList, "PEP_MATCH_PENDING", "PEP_MATCH")) {
            totalBoost += SINGLE_COMBO_BOOST;
            boostReason.append("PEP match; ");
        }
        
        if (containsAny(flagList, "SANCTIONS_HIT", "SANCTIONS_LIST")) {
            totalBoost += SINGLE_COMBO_BOOST;
            boostReason.append("Sanctions hit; ");
        }
        
        // High-risk combo 1: Amount + Device + Geo
        if (containsAny(flagList, "AMOUNT_SPIKE") && 
            containsAny(flagList, "NEW_DEVICE") && 
            containsAny(flagList, "GEO_MISMATCH", "GEO_DEVIATION")) {
            totalBoost += SINGLE_COMBO_BOOST;
            boostReason.append("Amount+Device+Geo; ");
        }
        
        // High-risk combo 2: New Device + Unusual Time + Burst Activity
        if (containsAny(flagList, "NEW_DEVICE") && 
            containsAny(flagList, "UNUSUAL_TIME") && 
            containsAny(flagList, "BURST_ACTIVITY", "UNUSUAL_VELOCITY")) {
            totalBoost += SINGLE_COMBO_BOOST;
            boostReason.append("Device+Time+Velocity; ");
        }
        
        // High-risk combo 3: Amount Spike + Merchant Novelty + New IP
        if (containsAny(flagList, "AMOUNT_SPIKE") && 
            containsAny(flagList, "MERCHANT_NOVELTY", "RARE_MCC") && 
            containsAny(flagList, "NEW_IP_RANGE")) {
            totalBoost += SINGLE_COMBO_BOOST;
            boostReason.append("Amount+Merchant+IP; ");
        }
        
        // High-risk combo 4: Multiple new signals (takeover indicator)
        int newSignalsCount = 0;
        if (containsAny(flagList, "NEW_DEVICE")) newSignalsCount++;
        if (containsAny(flagList, "NEW_IP_RANGE")) newSignalsCount++;
        if (containsAny(flagList, "MERCHANT_NOVELTY")) newSignalsCount++;
        if (containsAny(flagList, "GEO_DEVIATION", "GEO_MISMATCH")) newSignalsCount++;
        
        if (newSignalsCount >= 3) {
            totalBoost += SINGLE_COMBO_BOOST;
            boostReason.append("Multiple-new-signals(").append(newSignalsCount).append("); ");
        }
        
        // Cap boost
        totalBoost = Math.min(totalBoost, MAX_BOOST);
        
        // Apply boost
        double boostedScore = Math.min(1.0, baseScore + totalBoost);
        
        result.put("boosted_score", Math.round(boostedScore * 10000.0) / 10000.0);
        result.put("boost_applied", Math.round(totalBoost * 10000.0) / 10000.0);
        result.put("boost_reason", boostReason.length() > 0 ? boostReason.toString().trim() : "No combos matched");
        result.put("base_score", baseScore);
        
        logger.info("✅ Boosted score: " + baseScore + " → " + boostedScore + 
                " (+" + totalBoost + ") | " + boostReason);
        
        return result;
    }
    
    private static boolean containsAny(List<String> list, String... targets) {
        for (String target : targets) {
            if (list.contains(target.toUpperCase().trim())) {
                return true;
            }
        }
        return false;
    }
}

