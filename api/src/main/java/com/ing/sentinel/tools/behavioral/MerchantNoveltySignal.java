package com.ing.sentinel.tools.behavioral;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Merchant Novelty Signal (Behavioral)
 * 
 * Detects if the merchant or MCC is unusual for this customer based on
 * their historical transaction patterns.
 */
public class MerchantNoveltySignal {

    private static final Logger logger = Logger.getLogger(MerchantNoveltySignal.class.getName());
    
    // MCC weight threshold below which it's considered rare
    private static final double RARE_MCC_THRESHOLD = 0.05;

    /**
     * Analyzes merchant/MCC novelty for this customer.
     * 
     * @param merchantId Current merchant ID
     * @param merchantCategory Current merchant category
     * @param usualMerchantsTop Comma-separated list of customer's top merchants
     * @param mccDistribution JSON-like string of MCC distribution (e.g., "GROCERY:0.35,FUEL:0.15")
     * @return Analysis result with novelty scores and flags
     */
    @Schema(name = "analyze_merchant_novelty", description = "Analyzes if the merchant or category is unusual for this customer. Returns merchant novelty score and whether RARE_MERCHANT or RARE_MCC flags should be raised.")
    public static Map<String, Object> analyzeMerchantNovelty(
            @Schema(name = "merchant_id", description = "Current transaction merchant ID") String merchantId,
            @Schema(name = "merchant_category", description = "Current merchant category (MCC)") String merchantCategory,
            @Schema(name = "usual_merchants_top", description = "Comma-separated list of customer's frequently used merchant IDs") String usualMerchantsTop,
            @Schema(name = "mcc_distribution", description = "Customer's MCC distribution as key:value pairs (e.g., 'GROCERY:0.35,FUEL:0.15,ELECTRONICS:0.03')") String mccDistribution) {
        
        logger.info("🔍 Analyzing merchant novelty: merchant=" + merchantId + ", mcc=" + merchantCategory);
        
        Map<String, Object> result = new HashMap<>();
        
        // Parse usual merchants
        List<String> topMerchants = usualMerchantsTop != null && !usualMerchantsTop.isEmpty()
                ? Arrays.asList(usualMerchantsTop.split(","))
                : List.of();
        
        // Check if merchant is in top merchants
        boolean isTopMerchant = topMerchants.stream()
                .map(String::trim)
                .anyMatch(m -> m.equals(merchantId));
        
        // Parse MCC distribution and find weight for current MCC
        double mccWeight = parseMccWeight(mccDistribution, merchantCategory);
        boolean isRareMcc = mccWeight < RARE_MCC_THRESHOLD;
        
        // Determine flags
        boolean isRareMerchant = !isTopMerchant;
        boolean flagRaised = isRareMerchant || isRareMcc;
        
        // Calculate normalized signal (higher = more novel)
        double normalizedSignal = calculateNoveltySignal(isRareMerchant, mccWeight);
        
        result.put("merchant_id", merchantId);
        result.put("merchant_category", merchantCategory);
        result.put("is_top_merchant", isTopMerchant);
        result.put("mcc_weight_in_history", Math.round(mccWeight * 1000.0) / 1000.0);
        result.put("is_rare_mcc", isRareMcc);
        result.put("merchant_novelty", Math.round(normalizedSignal * 1000.0) / 1000.0);
        result.put("normalized_signal", Math.round(normalizedSignal * 1000.0) / 1000.0);
        
        // Set appropriate flag
        String flag = null;
        if (isRareMcc && isRareMerchant) {
            flag = "RARE_MERCHANT";
        } else if (isRareMcc) {
            flag = "RARE_MCC";
        } else if (isRareMerchant) {
            flag = "RARE_MERCHANT";
        }
        
        result.put("flag", flag);
        result.put("flag_raised", flagRaised);
        result.put("reasoning", generateReasoning(merchantCategory, mccWeight, isTopMerchant, isRareMcc));
        
        logger.info("✅ Merchant novelty: rareMerchant=" + isRareMerchant + ", rareMcc=" + isRareMcc);
        
        return result;
    }
    
    private static double parseMccWeight(String mccDistribution, String mcc) {
        if (mccDistribution == null || mccDistribution.isEmpty()) {
            return 0.0;
        }
        
        String mccUpper = mcc.toUpperCase();
        String[] pairs = mccDistribution.split(",");
        
        for (String pair : pairs) {
            String[] kv = pair.trim().split(":");
            if (kv.length == 2 && kv[0].trim().toUpperCase().equals(mccUpper)) {
                try {
                    return Double.parseDouble(kv[1].trim());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        }
        return 0.0; // MCC not found in distribution
    }
    
    private static double calculateNoveltySignal(boolean rareMerchant, double mccWeight) {
        double signal = 0.0;
        
        // Merchant novelty contributes 50%
        if (rareMerchant) {
            signal += 0.5;
        }
        
        // MCC rarity contributes based on inverse of weight
        signal += (1.0 - Math.min(1.0, mccWeight * 10)) * 0.5;
        
        return Math.min(1.0, signal);
    }
    
    private static String generateReasoning(String mcc, double weight, boolean isTop, boolean isRare) {
        if (isRare && !isTop) {
            return String.format("First-time merchant in rare category %s (%.1f%% of customer's history)", 
                    mcc, weight * 100);
        } else if (isRare) {
            return String.format("Unusual category %s (%.1f%% of customer's history)", mcc, weight * 100);
        } else if (!isTop) {
            return String.format("New merchant but familiar category %s (%.1f%%)", mcc, weight * 100);
        } else {
            return String.format("Familiar merchant and category %s (%.1f%%)", mcc, weight * 100);
        }
    }

    public static FunctionTool create() {
        return FunctionTool.create(MerchantNoveltySignal.class, "analyzeMerchantNovelty");
    }
}

