package com.ing.sentinel.tools.aggregator;

import com.google.adk.tools.Annotations.Schema;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Severity Classifier Tool
 * 
 * Maps a final risk score (0-100) to severity bands (LOW/MED/HIGH/CRITICAL)
 * and recommended actions (ALLOW/REVIEW/CHALLENGE/BLOCK) based on configurable thresholds.
 */
public class SeverityClassifier {

    private static final Logger logger = Logger.getLogger(SeverityClassifier.class.getName());
    
    // Severity thresholds (configurable)
    private static final int THRESHOLD_MED = 35;
    private static final int THRESHOLD_HIGH = 60;
    private static final int THRESHOLD_CRITICAL = 80;
    
    // Policy version
    private static final String POLICY_VERSION = "policy-2026-03-16";

    /**
     * Classifies risk score into severity band and recommends action.
     * 
     * @param finalRiskScore The final risk score (0-100)
     * @return Severity, recommended action, and thresholds used
     */
    @Schema(name = "classify_severity", description = "Maps final risk score (0-100) to severity band (LOW/MED/HIGH/CRITICAL) and recommended action (ALLOW/REVIEW/CHALLENGE/BLOCK). Returns severity, action, and policy version.")
    public static Map<String, Object> classifySeverity(
            @Schema(name = "final_risk_score", description = "The final risk score (0-100) to classify") int finalRiskScore) {
        
        logger.info("🏷️ Classifying severity for score: " + finalRiskScore);
        
        Map<String, Object> result = new HashMap<>();
        
        String severity;
        String recommendedAction;
        String explanation;
        
        if (finalRiskScore < THRESHOLD_MED) {
            // 0-34: LOW
            severity = "LOW";
            recommendedAction = "ALLOW";
            explanation = "Risk score within normal range, no intervention needed";
        } else if (finalRiskScore < THRESHOLD_HIGH) {
            // 35-59: MED
            severity = "MED";
            recommendedAction = "REVIEW";
            explanation = "Moderate risk detected, manual review recommended";
        } else if (finalRiskScore < THRESHOLD_CRITICAL) {
            // 60-79: HIGH
            severity = "HIGH";
            recommendedAction = "CHALLENGE";
            explanation = "High risk detected, additional authentication required";
        } else {
            // 80-100: CRITICAL
            severity = "CRITICAL";
            recommendedAction = "BLOCK";
            explanation = "Critical risk detected, transaction should be blocked";
        }
        
        result.put("severity", severity);
        result.put("recommended_action", recommendedAction);
        result.put("explanation", explanation);
        result.put("final_risk_score", finalRiskScore);
        result.put("thresholds_used", Map.of(
            "MED", THRESHOLD_MED,
            "HIGH", THRESHOLD_HIGH,
            "CRITICAL", THRESHOLD_CRITICAL
        ));
        result.put("policy_version", POLICY_VERSION);
        
        logger.info("✅ Classified: score=" + finalRiskScore + " → severity=" + severity + 
                ", action=" + recommendedAction);
        
        return result;
    }
}

