package com.ing.sentinel.tools.pattern;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Rare MCC Signal Tool
 * 
 * Analyzes merchant category code (MCC) rarity for a customer
 * to detect transactions at unusual merchant types.
 */
public class RareMccTool {

    private static final Logger logger = Logger.getLogger(RareMccTool.class.getName());
    
    // High-risk MCCs commonly associated with fraud
    private static final Set<String> HIGH_RISK_MCCS = new HashSet<>();
    static {
        HIGH_RISK_MCCS.add("GAMBLING");
        HIGH_RISK_MCCS.add("CRYPTO");
        HIGH_RISK_MCCS.add("WIRE_TRANSFER");
        HIGH_RISK_MCCS.add("MONEY_ORDER");
        HIGH_RISK_MCCS.add("PAWN_SHOP");
        HIGH_RISK_MCCS.add("ADULT");
    }
    
    // MCC rarity threshold (if customer has used this MCC less than X times)
    private static final int RARE_MCC_THRESHOLD = 2;

    /**
     * Analyzes merchant category rarity for fraud detection.
     * 
     * @param merchantCategory The merchant category code/name
     * @param customerMccHistory Number of times customer has used this MCC
     * @param customerTotalTransactions Total transactions by customer
     * @param globalMccFrequency Global frequency of this MCC (0-1)
     * @return Analysis result with rarity score and flag
     */
    @Schema(name = "analyze_rare_mcc", description = "Analyzes merchant category rarity to detect transactions at unusual merchant types for the customer. Returns rarity score, risk level, and whether RARE_MCC flag should be raised.")
    public static Map<String, Object> analyzeRareMcc(
            @Schema(name = "merchant_category", description = "Merchant category code or name (e.g., ELECTRONICS, GROCERY, GAMBLING)") String merchantCategory,
            @Schema(name = "customer_mcc_history", description = "Number of times this customer has transacted at this MCC") int customerMccHistory,
            @Schema(name = "customer_total_transactions", description = "Total number of transactions by this customer") int customerTotalTransactions,
            @Schema(name = "global_mcc_frequency", description = "Global frequency of this MCC among all transactions (0-1)") double globalMccFrequency) {
        
        logger.info("🔍 Analyzing MCC rarity: mcc=" + merchantCategory + ", history=" + customerMccHistory);
        
        Map<String, Object> result = new HashMap<>();
        
        String mccUpper = merchantCategory.toUpperCase();
        
        // Check if high-risk MCC
        boolean isHighRiskMcc = HIGH_RISK_MCCS.contains(mccUpper);
        
        // Calculate customer-specific rarity
        double customerMccRatio = customerTotalTransactions > 0 
                ? (double) customerMccHistory / customerTotalTransactions 
                : 0.0;
        
        // Is this MCC rare for this customer?
        boolean isRareForCustomer = customerMccHistory < RARE_MCC_THRESHOLD;
        
        // Is this MCC globally rare?
        boolean isGloballyRare = globalMccFrequency < 0.05; // Less than 5% of transactions
        
        // Determine if flag should be raised
        boolean flagRaised = isHighRiskMcc || (isRareForCustomer && isGloballyRare);
        
        // Calculate normalized signal [0,1]
        double normalizedSignal = calculateNormalizedSignal(isHighRiskMcc, customerMccRatio, globalMccFrequency, isRareForCustomer);
        
        // Calculate rarity score (inverse of frequency)
        double rarityScore = 1.0 - Math.min(1.0, customerMccRatio * 10.0);
        
        result.put("merchant_category", merchantCategory);
        result.put("is_high_risk_mcc", isHighRiskMcc);
        result.put("customer_mcc_history", customerMccHistory);
        result.put("customer_mcc_ratio", Math.round(customerMccRatio * 1000.0) / 1000.0);
        result.put("global_mcc_frequency", globalMccFrequency);
        result.put("is_rare_for_customer", isRareForCustomer);
        result.put("is_globally_rare", isGloballyRare);
        result.put("rarity_score", Math.round(rarityScore * 1000.0) / 1000.0);
        result.put("normalized_signal", Math.round(normalizedSignal * 1000.0) / 1000.0);
        result.put("flag", flagRaised ? "RARE_MCC" : null);
        result.put("flag_raised", flagRaised);
        result.put("reasoning", generateReasoning(merchantCategory, isHighRiskMcc, isRareForCustomer, customerMccHistory, flagRaised));
        
        logger.info("✅ MCC analysis: highRisk=" + isHighRiskMcc + ", rare=" + isRareForCustomer + ", flag=" + flagRaised);
        
        return result;
    }
    
    /**
     * Calculate normalized signal [0,1]
     */
    private static double calculateNormalizedSignal(boolean isHighRisk, double customerRatio, double globalFreq, boolean isRare) {
        double signal = 0.0;
        
        if (isHighRisk) {
            signal += 0.5; // High-risk MCC adds significant signal
        }
        
        if (isRare) {
            signal += 0.3; // First-time MCC for customer
        }
        
        // Inverse of customer ratio (less common = higher signal)
        signal += (1.0 - customerRatio) * 0.2;
        
        return Math.min(1.0, signal);
    }
    
    /**
     * Generate human-readable reasoning
     */
    private static String generateReasoning(String mcc, boolean highRisk, boolean rare, int history, boolean flagRaised) {
        if (highRisk) {
            return String.format("High-risk merchant category: %s", mcc);
        } else if (rare && flagRaised) {
            return String.format("First-time transaction at %s category (history: %d)", mcc, history);
        } else if (rare) {
            return String.format("Uncommon category for customer: %s (used %d times)", mcc, history);
        } else {
            return String.format("Familiar merchant category: %s (used %d times)", mcc, history);
        }
    }

    /**
     * Create FunctionTool wrapper for ADK
     */
    public static FunctionTool create() {
        return FunctionTool.create(RareMccTool.class, "analyzeRareMcc");
    }
}

