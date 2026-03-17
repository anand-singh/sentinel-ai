package com.ing.sentinel.tools.pattern;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Money Mule Pattern Tool
 * 
 * Detects classic money mule indicators:
 * - Large inbound transfer followed by rapid outbound transfers
 * - Round-trip transfers (in → out quickly)
 * - New account with high-value wire transfers
 * - Transfers to high-risk destinations
 */
public class MoneyMulePatternTool {

    private static final Logger logger = Logger.getLogger(MoneyMulePatternTool.class.getName());
    
    // Thresholds for money mule detection
    private static final double ROUND_TRIP_RATIO_THRESHOLD = 0.8;  // If outbound >= 80% of inbound
    private static final double LARGE_TRANSFER_THRESHOLD = 5000.0; // USD
    private static final int NEW_ACCOUNT_DAYS_THRESHOLD = 30;
    
    // High-risk destination countries (sample list)
    private static final String[] HIGH_RISK_COUNTRIES = {"UA", "NG", "RU", "BY", "KZ", "VN", "PH"};

    /**
     * Analyzes transaction for money mule patterns.
     * 
     * @param largeInboundTransferToday Total large inbound transfers today
     * @param outboundToday Total outbound transfers today
     * @param accountAgeDays Age of the account in days
     * @param transactionAmount Current transaction amount
     * @param destinationCountry Destination country code for wire transfers
     * @param merchantCategory Merchant category (WIRE_TRANSFER is high risk)
     * @return Analysis result with money mule risk signal and flags
     */
    @Schema(name = "analyze_money_mule_pattern", description = "Analyzes transaction for money mule patterns including round-trip transfers, new account high-value wire transfers, and high-risk destinations. Returns risk signal and relevant flags.")
    public static Map<String, Object> analyzeMoneyMulePattern(
            @Schema(name = "large_inbound_transfer_today", description = "Total large inbound transfers received today (USD)") double largeInboundTransferToday,
            @Schema(name = "outbound_today", description = "Total outbound transfers sent today (USD)") double outboundToday,
            @Schema(name = "account_age_days", description = "Age of the customer account in days") int accountAgeDays,
            @Schema(name = "transaction_amount", description = "Current transaction amount (USD)") double transactionAmount,
            @Schema(name = "destination_country", description = "Destination country code for wire transfers (e.g., 'UA', 'NG')") String destinationCountry,
            @Schema(name = "merchant_category", description = "Merchant category code (e.g., 'WIRE_TRANSFER')") String merchantCategory) {
        
        logger.info("🔍 Analyzing money mule pattern: inbound=" + largeInboundTransferToday + 
                ", outbound=" + outboundToday + ", accountAge=" + accountAgeDays);
        
        Map<String, Object> result = new HashMap<>();
        
        // Calculate round-trip ratio
        double roundTripRatio = largeInboundTransferToday > 0 ? 
                (outboundToday + transactionAmount) / largeInboundTransferToday : 0;
        
        // Detect flags
        boolean isRoundTrip = largeInboundTransferToday >= LARGE_TRANSFER_THRESHOLD && 
                              roundTripRatio >= ROUND_TRIP_RATIO_THRESHOLD;
        boolean isNewAccount = accountAgeDays <= NEW_ACCOUNT_DAYS_THRESHOLD;
        boolean isHighRiskDestination = isHighRiskCountry(destinationCountry);
        boolean isWireTransfer = "WIRE_TRANSFER".equalsIgnoreCase(merchantCategory);
        boolean isLargeAmount = transactionAmount >= LARGE_TRANSFER_THRESHOLD || 
                                largeInboundTransferToday >= LARGE_TRANSFER_THRESHOLD;
        
        // Calculate normalized signal [0,1]
        double normalizedSignal = calculateRiskSignal(isRoundTrip, isNewAccount, isHighRiskDestination, 
                                                       isWireTransfer, isLargeAmount, roundTripRatio);
        
        // Determine if this is a velocity failure (rapid in→out)
        boolean velocityCheckFailed = isRoundTrip && roundTripRatio >= 0.9;
        
        // Determine amount spike (large transfer from new/dormant account)
        boolean amountSpike = isLargeAmount && isNewAccount;
        
        // Build flags list
        java.util.List<String> flags = new java.util.ArrayList<>();
        if (velocityCheckFailed) flags.add("VELOCITY_CHECK_FAILED");
        if (amountSpike) flags.add("AMOUNT_SPIKE");
        if (isRoundTrip) flags.add("ROUND_TRIP_TRANSFER");
        if (isHighRiskDestination) flags.add("HIGH_RISK_DESTINATION");
        
        result.put("large_inbound_transfer_today", largeInboundTransferToday);
        result.put("outbound_today", outboundToday);
        result.put("transaction_amount", transactionAmount);
        result.put("round_trip_ratio", Math.round(roundTripRatio * 100.0) / 100.0);
        result.put("account_age_days", accountAgeDays);
        result.put("destination_country", destinationCountry);
        result.put("is_round_trip", isRoundTrip);
        result.put("is_new_account", isNewAccount);
        result.put("is_high_risk_destination", isHighRiskDestination);
        result.put("is_wire_transfer", isWireTransfer);
        result.put("velocity_check_failed", velocityCheckFailed);
        result.put("amount_spike", amountSpike);
        result.put("normalized_signal", Math.round(normalizedSignal * 1000.0) / 1000.0);
        result.put("flags", flags);
        result.put("flag_raised", !flags.isEmpty());
        result.put("reasoning", generateReasoning(largeInboundTransferToday, outboundToday, transactionAmount, 
                                                   roundTripRatio, accountAgeDays, destinationCountry, flags));
        
        logger.info("✅ Money mule analysis: roundTripRatio=" + roundTripRatio + ", flags=" + flags);
        
        return result;
    }
    
    private static boolean isHighRiskCountry(String countryCode) {
        if (countryCode == null) return false;
        for (String highRisk : HIGH_RISK_COUNTRIES) {
            if (highRisk.equalsIgnoreCase(countryCode)) return true;
        }
        return false;
    }
    
    private static double calculateRiskSignal(boolean isRoundTrip, boolean isNewAccount, 
                                               boolean isHighRiskDestination, boolean isWireTransfer,
                                               boolean isLargeAmount, double roundTripRatio) {
        double signal = 0.0;
        
        // Round-trip is the strongest indicator
        if (isRoundTrip) signal += 0.4;
        
        // New account + large transfer is very suspicious
        if (isNewAccount && isLargeAmount) signal += 0.25;
        else if (isNewAccount) signal += 0.1;
        else if (isLargeAmount) signal += 0.1;
        
        // High-risk destination adds significant risk
        if (isHighRiskDestination) signal += 0.2;
        
        // Wire transfer is the typical mule channel
        if (isWireTransfer) signal += 0.1;
        
        // Boost based on round-trip ratio
        if (roundTripRatio >= 0.95) signal += 0.1;
        else if (roundTripRatio >= 0.9) signal += 0.05;
        
        return Math.min(1.0, signal);
    }
    
    private static String generateReasoning(double inbound, double outbound, double txnAmount, 
                                             double ratio, int ageDays, String destCountry,
                                             java.util.List<String> flags) {
        if (flags.isEmpty()) {
            return "No money mule indicators detected";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("MONEY MULE ALERT: ");
        
        if (flags.contains("ROUND_TRIP_TRANSFER")) {
            sb.append(String.format("$%.2f received today, $%.2f being sent out (%.0f%% round-trip). ", 
                    inbound, outbound + txnAmount, ratio * 100));
        }
        
        if (flags.contains("VELOCITY_CHECK_FAILED")) {
            sb.append("Rapid in→out transfer pattern detected. ");
        }
        
        if (flags.contains("AMOUNT_SPIKE")) {
            sb.append(String.format("Large transfer from %d-day-old account with minimal history. ", ageDays));
        }
        
        if (flags.contains("HIGH_RISK_DESTINATION")) {
            sb.append(String.format("Transfer to high-risk destination: %s. ", destCountry));
        }
        
        return sb.toString().trim();
    }

    public static FunctionTool create() {
        return FunctionTool.create(MoneyMulePatternTool.class, "analyzeMoneyMulePattern");
    }
}

