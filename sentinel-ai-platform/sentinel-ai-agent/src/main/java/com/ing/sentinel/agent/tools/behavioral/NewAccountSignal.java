package com.ing.sentinel.agent.tools.behavioral;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * New Account Signal (Behavioral)
 * 
 * Detects if the customer account is newly created, which is a high-risk factor
 * for money mule and account takeover fraud patterns.
 */
public class NewAccountSignal {

    private static final Logger logger = Logger.getLogger(NewAccountSignal.class.getName());
    
    // Threshold for new account (in days)
    private static final int NEW_ACCOUNT_THRESHOLD_DAYS = 30;
    // Very new account threshold (extremely high risk)
    private static final int VERY_NEW_ACCOUNT_THRESHOLD_DAYS = 14;

    /**
     * Analyzes if the customer account is newly created.
     * 
     * @param accountAgeDays Age of the customer account in days
     * @param priorTransactionCount Number of prior transactions on this account
     * @return Analysis result with account age risk signal and flag
     */
    @Schema(name = "analyze_new_account", description = "Analyzes if the customer account is newly created (high risk for money mule fraud). Returns normalized signal (0-1) and whether NEW_ACCOUNT flag should be raised.")
    public static Map<String, Object> analyzeNewAccount(
            @Schema(name = "account_age_days", description = "Age of the customer account in days") int accountAgeDays,
            @Schema(name = "prior_transaction_count", description = "Number of prior transactions on this account") int priorTransactionCount) {
        
        logger.info("🔍 Analyzing new account: age=" + accountAgeDays + " days, priorTxns=" + priorTransactionCount);
        
        Map<String, Object> result = new HashMap<>();
        
        // Determine risk level based on account age
        boolean isNewAccount = accountAgeDays <= NEW_ACCOUNT_THRESHOLD_DAYS;
        boolean isVeryNewAccount = accountAgeDays <= VERY_NEW_ACCOUNT_THRESHOLD_DAYS;
        
        // Calculate normalized signal [0,1]
        // Newer accounts = higher signal
        double normalizedSignal;
        if (isVeryNewAccount) {
            // Very new: 0.8 - 1.0 based on how new
            normalizedSignal = 1.0 - (accountAgeDays / (double) VERY_NEW_ACCOUNT_THRESHOLD_DAYS) * 0.2;
        } else if (isNewAccount) {
            // New: 0.5 - 0.8
            normalizedSignal = 0.8 - ((accountAgeDays - VERY_NEW_ACCOUNT_THRESHOLD_DAYS) / 
                    (double) (NEW_ACCOUNT_THRESHOLD_DAYS - VERY_NEW_ACCOUNT_THRESHOLD_DAYS)) * 0.3;
        } else {
            // Established account: low signal based on age
            normalizedSignal = Math.max(0, 0.5 - (accountAgeDays - NEW_ACCOUNT_THRESHOLD_DAYS) / 100.0);
        }
        
        // Boost signal if very few prior transactions (indicates dormant or test account)
        if (priorTransactionCount <= 2 && isNewAccount) {
            normalizedSignal = Math.min(1.0, normalizedSignal + 0.15);
        }
        
        result.put("account_age_days", accountAgeDays);
        result.put("prior_transaction_count", priorTransactionCount);
        result.put("is_new_account", isNewAccount);
        result.put("is_very_new_account", isVeryNewAccount);
        result.put("normalized_signal", Math.round(normalizedSignal * 1000.0) / 1000.0);
        result.put("flag", isNewAccount ? "NEW_ACCOUNT" : null);
        result.put("flag_raised", isNewAccount);
        result.put("threshold_days", NEW_ACCOUNT_THRESHOLD_DAYS);
        result.put("reasoning", generateReasoning(accountAgeDays, priorTransactionCount, isNewAccount, isVeryNewAccount));
        
        logger.info("✅ New account analysis: isNew=" + isNewAccount + ", signal=" + normalizedSignal);
        
        return result;
    }
    
    private static String generateReasoning(int ageDays, int priorTxns, boolean isNew, boolean isVeryNew) {
        if (isVeryNew) {
            return String.format("Very new account (%d days old, only %d prior transactions) - HIGH RISK for money mule/fraud", 
                    ageDays, priorTxns);
        } else if (isNew) {
            return String.format("New account (%d days old, %d prior transactions) - elevated fraud risk", 
                    ageDays, priorTxns);
        } else {
            return String.format("Established account (%d days old, %d prior transactions) - normal risk", 
                    ageDays, priorTxns);
        }
    }

    public static FunctionTool create() {
        return FunctionTool.create(NewAccountSignal.class, "analyzeNewAccount");
    }
}

