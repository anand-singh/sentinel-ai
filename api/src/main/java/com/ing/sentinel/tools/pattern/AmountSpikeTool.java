package com.ing.sentinel.tools.pattern;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Amount Spike Signal Tool
 * 
 * Analyzes transaction amounts against merchant category (MCC) baselines
 * using z-score calculation to detect unusual spending patterns.
 */
public class AmountSpikeTool {

    private static final Logger logger = Logger.getLogger(AmountSpikeTool.class.getName());
    
    // Threshold for flagging amount spike (z-score)
    private static final double AMOUNT_SPIKE_THRESHOLD = 2.0;

    /**
     * Analyzes if the transaction amount is unusually high compared to MCC baseline.
     * 
     * @param amount Transaction amount
     * @param globalAvgByMcc Global average transaction amount for this MCC
     * @param globalStdevByMcc Global standard deviation for this MCC
     * @return Analysis result with z-score, normalized signal, and flag
     */
    @Schema(name = "analyze_amount_spike", description = "Analyzes transaction amount against merchant category baseline to detect unusual spending spikes. Returns z-score, normalized signal (0-1), and whether AMOUNT_SPIKE flag should be raised.")
    public static Map<String, Object> analyzeAmountSpike(
            @Schema(name = "amount", description = "Transaction amount in the transaction currency") double amount,
            @Schema(name = "global_avg_by_mcc", description = "Global average transaction amount for this merchant category") double globalAvgByMcc,
            @Schema(name = "global_stdev_by_mcc", description = "Global standard deviation for this merchant category") double globalStdevByMcc) {
        
        logger.info("🔍 Analyzing amount spike: amount=" + amount + ", avg=" + globalAvgByMcc + ", stdev=" + globalStdevByMcc);
        
        Map<String, Object> result = new HashMap<>();
        
        // Prevent division by zero
        if (globalStdevByMcc <= 0) {
            globalStdevByMcc = 1.0;
        }
        
        // Calculate z-score: how many standard deviations from mean
        double zScore = (amount - globalAvgByMcc) / globalStdevByMcc;
        
        // Normalize to [0,1] using sigmoid function
        double normalizedSignal = sigmoid(zScore);
        
        // Determine if flag should be raised
        boolean flagRaised = zScore >= AMOUNT_SPIKE_THRESHOLD;
        
        result.put("z_score", Math.round(zScore * 100.0) / 100.0);
        result.put("normalized_signal", Math.round(normalizedSignal * 1000.0) / 1000.0);
        result.put("flag", flagRaised ? "AMOUNT_SPIKE" : null);
        result.put("flag_raised", flagRaised);
        result.put("threshold", AMOUNT_SPIKE_THRESHOLD);
        result.put("reasoning", generateReasoning(amount, globalAvgByMcc, zScore, flagRaised));
        
        logger.info("✅ Amount spike analysis: z=" + zScore + ", flag=" + flagRaised);
        
        return result;
    }
    
    /**
     * Sigmoid function to normalize z-score to [0,1] range
     */
    private static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
    
    /**
     * Generate human-readable reasoning
     */
    private static String generateReasoning(double amount, double avg, double zScore, boolean flagRaised) {
        if (flagRaised) {
            return String.format("Amount %.2f is %.1fx higher than category average (%.2f), z-score=%.2f exceeds threshold", 
                    amount, amount / avg, avg, zScore);
        } else {
            return String.format("Amount %.2f is within normal range for category (avg=%.2f, z=%.2f)", 
                    amount, avg, zScore);
        }
    }

    /**
     * Create FunctionTool wrapper for ADK
     */
    public static FunctionTool create() {
        return FunctionTool.create(AmountSpikeTool.class, "analyzeAmountSpike");
    }
}

