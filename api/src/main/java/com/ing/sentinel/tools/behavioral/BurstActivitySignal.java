package com.ing.sentinel.tools.behavioral;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Burst Activity Signal (Behavioral)
 * 
 * Detects if the transaction velocity exceeds the customer's personal baseline.
 */
public class BurstActivitySignal {

    private static final Logger logger = Logger.getLogger(BurstActivitySignal.class.getName());
    
    // Multiplier threshold for burst detection
    private static final double BURST_MULTIPLIER_THRESHOLD = 3.0;

    /**
     * Analyzes if transaction velocity exceeds customer's personal baseline.
     * 
     * @param transactionsInLastHour Number of transactions in the last hour
     * @param transactionsInLast24h Number of transactions in the last 24 hours
     * @param customerVelocityBaselinePerHour Customer's typical transactions per hour
     * @return Analysis result with velocity comparison and flag
     */
    @Schema(name = "analyze_burst_activity", description = "Analyzes if transaction velocity exceeds customer's personal baseline. Returns velocity ratio and whether BURST_ACTIVITY flag should be raised.")
    public static Map<String, Object> analyzeBurstActivity(
            @Schema(name = "transactions_in_last_hour", description = "Number of transactions by this customer in the last hour") int transactionsInLastHour,
            @Schema(name = "transactions_in_last_24h", description = "Number of transactions by this customer in the last 24 hours") int transactionsInLast24h,
            @Schema(name = "customer_velocity_baseline_per_hour", description = "Customer's typical transactions per hour (baseline)") double customerVelocityBaselinePerHour) {
        
        logger.info("🔍 Analyzing burst activity: txns1h=" + transactionsInLastHour + ", baseline=" + customerVelocityBaselinePerHour);
        
        Map<String, Object> result = new HashMap<>();
        
        // Use default baseline if not provided
        if (customerVelocityBaselinePerHour <= 0) {
            customerVelocityBaselinePerHour = 1.0;
        }
        
        // Calculate velocity ratios
        double hourlyRatio = transactionsInLastHour / customerVelocityBaselinePerHour;
        double dailyBaseline = customerVelocityBaselinePerHour * 24;
        double dailyRatio = dailyBaseline > 0 ? transactionsInLast24h / dailyBaseline : 1.0;
        
        // Detect burst
        boolean isBurst = hourlyRatio >= BURST_MULTIPLIER_THRESHOLD;
        
        // Normalize to [0,1]
        double normalizedSignal = Math.min(1.0, hourlyRatio / BURST_MULTIPLIER_THRESHOLD);
        
        result.put("transactions_in_last_hour", transactionsInLastHour);
        result.put("transactions_in_last_24h", transactionsInLast24h);
        result.put("customer_baseline_per_hour", customerVelocityBaselinePerHour);
        result.put("hourly_velocity_ratio", Math.round(hourlyRatio * 100.0) / 100.0);
        result.put("daily_velocity_ratio", Math.round(dailyRatio * 100.0) / 100.0);
        result.put("burst_in_window", isBurst ? 1 : 0);
        result.put("normalized_signal", Math.round(normalizedSignal * 1000.0) / 1000.0);
        result.put("flag", isBurst ? "BURST_ACTIVITY" : null);
        result.put("flag_raised", isBurst);
        result.put("reasoning", generateReasoning(transactionsInLastHour, customerVelocityBaselinePerHour, hourlyRatio, isBurst));
        
        logger.info("✅ Burst analysis: ratio=" + hourlyRatio + ", isBurst=" + isBurst);
        
        return result;
    }
    
    private static String generateReasoning(int txns, double baseline, double ratio, boolean isBurst) {
        if (isBurst) {
            return String.format("Unusual burst: %d transactions in last hour (%.1fx customer's baseline of %.1f/hr)", 
                    txns, ratio, baseline);
        } else if (ratio > 1.5) {
            return String.format("Elevated activity: %d transactions (%.1fx baseline)", txns, ratio);
        } else {
            return String.format("Normal activity: %d transactions within customer's baseline", txns);
        }
    }

    public static FunctionTool create() {
        return FunctionTool.create(BurstActivitySignal.class, "analyzeBurstActivity");
    }
}

