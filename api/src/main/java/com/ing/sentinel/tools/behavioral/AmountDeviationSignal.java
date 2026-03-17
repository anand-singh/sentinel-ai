package com.ing.sentinel.tools.behavioral;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Amount Deviation Signal (Behavioral)
 * 
 * Compares transaction amount against the CUSTOMER's personal baseline,
 * not global averages. Uses customer-specific avg and stdev.
 */
public class AmountDeviationSignal {

    private static final Logger logger = Logger.getLogger(AmountDeviationSignal.class.getName());
    
    // Threshold for flagging amount deviation (z-score)
    private static final double AMOUNT_DEVIATION_THRESHOLD = 2.5;

    /**
     * Analyzes if the transaction amount deviates from customer's personal baseline.
     * 
     * @param amount Transaction amount
     * @param customerAvgAmount Customer's average transaction amount
     * @param customerStdevAmount Customer's standard deviation for amounts
     * @return Analysis result with z-score, normalized signal, and flag
     */
    @Schema(name = "analyze_amount_deviation", description = "Analyzes transaction amount against the customer's personal spending baseline. Returns z-score relative to customer's history, normalized signal (0-1), and whether AMOUNT_DEVIATION flag should be raised.")
    public static Map<String, Object> analyzeAmountDeviation(
            @Schema(name = "amount", description = "Transaction amount") double amount,
            @Schema(name = "customer_avg_amount", description = "Customer's historical average transaction amount") double customerAvgAmount,
            @Schema(name = "customer_stdev_amount", description = "Customer's historical standard deviation for amounts") double customerStdevAmount) {
        
        logger.info("🔍 Analyzing amount deviation: amount=" + amount + ", customerAvg=" + customerAvgAmount);
        
        Map<String, Object> result = new HashMap<>();
        
        // Prevent division by zero
        if (customerStdevAmount <= 0) {
            customerStdevAmount = customerAvgAmount * 0.3; // Assume 30% CV if no stdev
        }
        
        // Calculate z-score relative to customer's baseline
        double zScore = (amount - customerAvgAmount) / customerStdevAmount;
        
        // Normalize to [0,1] using sigmoid function
        double normalizedSignal = sigmoid(zScore);
        
        // Flag if significantly above customer's normal
        boolean flagRaised = Math.abs(zScore) >= AMOUNT_DEVIATION_THRESHOLD;
        
        result.put("z_score", Math.round(zScore * 100.0) / 100.0);
        result.put("amount_zscore_customer", Math.round(zScore * 100.0) / 100.0);
        result.put("normalized_signal", Math.round(normalizedSignal * 1000.0) / 1000.0);
        result.put("flag", flagRaised ? "AMOUNT_DEVIATION" : null);
        result.put("flag_raised", flagRaised);
        result.put("threshold", AMOUNT_DEVIATION_THRESHOLD);
        result.put("customer_avg", customerAvgAmount);
        result.put("deviation_factor", Math.round((amount / customerAvgAmount) * 100.0) / 100.0);
        result.put("reasoning", generateReasoning(amount, customerAvgAmount, zScore, flagRaised));
        
        logger.info("✅ Amount deviation: z=" + zScore + ", flag=" + flagRaised);
        
        return result;
    }
    
    private static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
    
    private static String generateReasoning(double amount, double avg, double zScore, boolean flagRaised) {
        double factor = amount / avg;
        if (flagRaised) {
            return String.format("Amount %.2f is %.1fσ from customer's average (%.2f), %.1fx their typical spending", 
                    amount, zScore, avg, factor);
        } else {
            return String.format("Amount %.2f is within customer's normal range (avg=%.2f, z=%.2f)", 
                    amount, avg, zScore);
        }
    }

    public static FunctionTool create() {
        return FunctionTool.create(AmountDeviationSignal.class, "analyzeAmountDeviation");
    }
}

