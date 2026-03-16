package com.ing.sentinel.tools.behavioral;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Time Deviation Signal (Behavioral)
 * 
 * Checks if the transaction time falls within the customer's sleep hours
 * or outside their typical active hours.
 */
public class TimeDeviationSignal {

    private static final Logger logger = Logger.getLogger(TimeDeviationSignal.class.getName());

    /**
     * Analyzes if the transaction time is unusual for this specific customer.
     * 
     * @param timestampUtc Transaction timestamp in ISO 8601 format
     * @param sleepHoursStart Customer's typical sleep start hour (UTC, 0-23)
     * @param sleepHoursEnd Customer's typical sleep end hour (UTC, 0-23)
     * @param activeHoursStart Customer's typical active start hour (UTC, 0-23)
     * @param activeHoursEnd Customer's typical active end hour (UTC, 0-23)
     * @return Analysis result with time deviation score and flag
     */
    @Schema(name = "analyze_time_deviation", description = "Analyzes if the transaction time is unusual for this customer based on their sleep and active hours. Returns deviation score and whether UNUSUAL_TIME flag should be raised.")
    public static Map<String, Object> analyzeTimeDeviation(
            @Schema(name = "timestamp_utc", description = "Transaction timestamp in ISO 8601 format") String timestampUtc,
            @Schema(name = "sleep_hours_start", description = "Customer's typical sleep start hour in UTC (0-23)") int sleepHoursStart,
            @Schema(name = "sleep_hours_end", description = "Customer's typical sleep end hour in UTC (0-23)") int sleepHoursEnd,
            @Schema(name = "active_hours_start", description = "Customer's typical active start hour in UTC (0-23)") int activeHoursStart,
            @Schema(name = "active_hours_end", description = "Customer's typical active end hour in UTC (0-23)") int activeHoursEnd) {
        
        logger.info("🔍 Analyzing time deviation: timestamp=" + timestampUtc);
        
        Map<String, Object> result = new HashMap<>();
        
        // Parse timestamp
        ZonedDateTime txTime = ZonedDateTime.parse(timestampUtc, DateTimeFormatter.ISO_DATE_TIME);
        int hour = txTime.getHour();
        
        // Check if during sleep hours
        boolean duringSleep = isInHourRange(hour, sleepHoursStart, sleepHoursEnd);
        
        // Check if during active hours
        boolean duringActive = isInHourRange(hour, activeHoursStart, activeHoursEnd);
        
        // Calculate deviation score
        double normalizedSignal;
        if (duringSleep) {
            normalizedSignal = 1.0; // Maximum deviation - during sleep
        } else if (!duringActive) {
            normalizedSignal = 0.5; // Moderate deviation - outside active but not sleep
        } else {
            normalizedSignal = 0.0; // Normal - during active hours
        }
        
        boolean flagRaised = duringSleep;
        
        result.put("transaction_hour", hour);
        result.put("day_of_week", txTime.getDayOfWeek().toString());
        result.put("during_sleep_hours", duringSleep);
        result.put("during_active_hours", duringActive);
        result.put("normalized_signal", normalizedSignal);
        result.put("hour_deviation", normalizedSignal);
        result.put("flag", flagRaised ? "UNUSUAL_TIME" : null);
        result.put("flag_raised", flagRaised);
        result.put("reasoning", generateReasoning(hour, duringSleep, duringActive, sleepHoursStart, sleepHoursEnd));
        
        logger.info("✅ Time deviation: hour=" + hour + ", duringSleep=" + duringSleep + ", flag=" + flagRaised);
        
        return result;
    }
    
    /**
     * Check if hour falls within a range (handles overnight ranges like 23-6)
     */
    private static boolean isInHourRange(int hour, int start, int end) {
        if (start <= end) {
            return hour >= start && hour <= end;
        } else {
            // Overnight range (e.g., 23 to 6)
            return hour >= start || hour <= end;
        }
    }
    
    private static String generateReasoning(int hour, boolean duringSleep, boolean duringActive, int sleepStart, int sleepEnd) {
        String timeDesc = String.format("%02d:00 UTC", hour);
        
        if (duringSleep) {
            return String.format("Transaction at %s falls within customer's sleep hours (%02d:00-%02d:00)", 
                    timeDesc, sleepStart, sleepEnd);
        } else if (!duringActive) {
            return String.format("Transaction at %s is outside customer's typical active hours", timeDesc);
        } else {
            return String.format("Transaction at %s is within customer's normal active hours", timeDesc);
        }
    }

    public static FunctionTool create() {
        return FunctionTool.create(TimeDeviationSignal.class, "analyzeTimeDeviation");
    }
}

