package com.ing.sentinel.agent.tools.pattern;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Time Window Signal Tool
 * 
 * Analyzes transaction timing to detect unusual hour-of-day patterns
 * that may indicate fraud (e.g., transactions at 3 AM when customer
 * normally transacts during business hours).
 */
public class TimeWindowTool {

    private static final Logger logger = Logger.getLogger(TimeWindowTool.class.getName());
    
    // Define unusual hours (late night / early morning)
    private static final int UNUSUAL_HOUR_START = 23; // 11 PM
    private static final int UNUSUAL_HOUR_END = 5;    // 5 AM
    
    // Weekend factor (some MCCs have different weekend patterns)
    private static final double WEEKEND_ANOMALY_BOOST = 0.2;

    /**
     * Analyzes transaction timing for anomalies.
     * 
     * @param timestampUtc Transaction timestamp in ISO 8601 format
     * @param customerTypicalHours Comma-separated typical hours for customer (e.g., "9,10,11,12,13,14,15,16,17,18")
     * @param mccTypicalHours Comma-separated typical hours for this MCC
     * @return Analysis result with time anomaly score and flag
     */
    @Schema(name = "analyze_time_window", description = "Analyzes transaction timing to detect unusual hour-of-day patterns. Returns time anomaly score, hour analysis, and whether UNUSUAL_TIME flag should be raised.")
    public static Map<String, Object> analyzeTimeWindow(
            @Schema(name = "timestamp_utc", description = "Transaction timestamp in ISO 8601 format (e.g., 2026-03-16T03:15:00Z)") String timestampUtc,
            @Schema(name = "customer_typical_hours", description = "Comma-separated typical transaction hours for this customer in UTC (e.g., '9,10,11,12,13,14,15,16,17,18')") String customerTypicalHours,
            @Schema(name = "mcc_typical_hours", description = "Comma-separated typical transaction hours for this MCC (e.g., '8,9,10,11,12,13,14,15,16,17,18,19,20')") String mccTypicalHours) {
        
        logger.info("🔍 Analyzing time window: timestamp=" + timestampUtc);
        
        Map<String, Object> result = new HashMap<>();
        
        // Parse timestamp
        ZonedDateTime txTime = ZonedDateTime.parse(timestampUtc, DateTimeFormatter.ISO_DATE_TIME);
        int hour = txTime.getHour();
        int dayOfWeek = txTime.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        boolean isWeekend = dayOfWeek >= 6;
        
        // Check if hour is in unusual range
        boolean isUnusualHour = isInUnusualHours(hour);
        
        // Check if hour is typical for customer
        boolean isTypicalForCustomer = isHourInList(hour, customerTypicalHours);
        
        // Check if hour is typical for MCC
        boolean isTypicalForMcc = isHourInList(hour, mccTypicalHours);
        
        // Determine if flag should be raised
        boolean flagRaised = isUnusualHour || (!isTypicalForCustomer && !isTypicalForMcc);
        
        // Calculate normalized signal [0,1]
        double normalizedSignal = calculateNormalizedSignal(isUnusualHour, isTypicalForCustomer, isTypicalForMcc, isWeekend);
        
        // Calculate hour deviation (how far from typical hours)
        double hourDeviation = calculateHourDeviation(hour, customerTypicalHours, isTypicalForCustomer);
        
        result.put("transaction_hour", hour);
        result.put("day_of_week", txTime.getDayOfWeek().toString());
        result.put("is_weekend", isWeekend);
        result.put("is_unusual_hour", isUnusualHour);
        result.put("is_typical_for_customer", isTypicalForCustomer);
        result.put("is_typical_for_mcc", isTypicalForMcc);
        result.put("hour_deviation", Math.round(hourDeviation * 1000.0) / 1000.0);
        result.put("normalized_signal", Math.round(normalizedSignal * 1000.0) / 1000.0);
        result.put("flag", flagRaised ? "UNUSUAL_TIME" : null);
        result.put("flag_raised", flagRaised);
        result.put("reasoning", generateReasoning(hour, isUnusualHour, isTypicalForCustomer, isTypicalForMcc, isWeekend, flagRaised));
        
        logger.info("✅ Time analysis: hour=" + hour + ", unusual=" + isUnusualHour + ", flag=" + flagRaised);
        
        return result;
    }
    
    /**
     * Check if hour falls in unusual time range
     */
    private static boolean isInUnusualHours(int hour) {
        return hour >= UNUSUAL_HOUR_START || hour <= UNUSUAL_HOUR_END;
    }
    
    /**
     * Check if hour is in comma-separated list of typical hours
     */
    private static boolean isHourInList(int hour, String hourList) {
        if (hourList == null || hourList.isEmpty()) {
            return true; // No data means we assume it's typical
        }
        
        String[] hours = hourList.split(",");
        for (String h : hours) {
            try {
                if (Integer.parseInt(h.trim()) == hour) {
                    return true;
                }
            } catch (NumberFormatException e) {
                // Skip invalid entries
            }
        }
        return false;
    }
    
    /**
     * Calculate hour deviation from customer's typical hours [0,1]
     */
    private static double calculateHourDeviation(int hour, String customerTypicalHours, boolean isTypical) {
        if (isTypical) {
            return 0.0; // No deviation if it's a typical hour
        }
        
        if (customerTypicalHours == null || customerTypicalHours.isEmpty()) {
            return 0.5; // Unknown baseline
        }
        
        // Parse customer typical hours
        String[] hours = customerTypicalHours.split(",");
        if (hours.length == 0) {
            return 0.5;
        }
        
        // Find minimum distance to any typical hour
        int minDistance = 24;
        for (String h : hours) {
            try {
                int typicalHour = Integer.parseInt(h.trim());
                int distance = Math.min(
                    Math.abs(hour - typicalHour),
                    24 - Math.abs(hour - typicalHour) // Circular distance
                );
                minDistance = Math.min(minDistance, distance);
            } catch (NumberFormatException e) {
                // Skip invalid entries
            }
        }
        
        // Normalize to [0,1] where 12 hours away = 1.0
        return Math.min(1.0, minDistance / 12.0);
    }
    
    /**
     * Calculate normalized signal [0,1]
     */
    private static double calculateNormalizedSignal(boolean unusualHour, boolean typicalCustomer, boolean typicalMcc, boolean weekend) {
        double signal = 0.0;
        
        if (unusualHour) {
            signal += 0.5;
        }
        
        if (!typicalCustomer) {
            signal += 0.3;
        }
        
        if (!typicalMcc) {
            signal += 0.2;
        }
        
        // Boost signal slightly for weekend anomalies in business MCCs
        if (weekend && !typicalMcc) {
            signal += WEEKEND_ANOMALY_BOOST;
        }
        
        return Math.min(1.0, signal);
    }
    
    /**
     * Generate human-readable reasoning
     */
    private static String generateReasoning(int hour, boolean unusual, boolean typicalCust, boolean typicalMcc, boolean weekend, boolean flagRaised) {
        String timeDesc = String.format("%02d:00 UTC", hour);
        
        if (unusual && flagRaised) {
            return String.format("Transaction at unusual hour (%s) - outside normal activity window", timeDesc);
        } else if (!typicalCust && flagRaised) {
            return String.format("Transaction at %s is atypical for this customer's pattern", timeDesc);
        } else if (weekend && !typicalMcc) {
            return String.format("Weekend transaction at %s unusual for this merchant category", timeDesc);
        } else {
            return String.format("Transaction time (%s) is within normal patterns", timeDesc);
        }
    }

    /**
     * Create FunctionTool wrapper for ADK
     */
    public static FunctionTool create() {
        return FunctionTool.create(TimeWindowTool.class, "analyzeTimeWindow");
    }
}

