package com.ing.sentinel.agent.tools.aggregator;

import com.google.adk.tools.Annotations.Schema;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Score Calibrator Tool
 * 
 * Calibrates the blended/boosted score to [0..100] scale using piecewise linear
 * or other calibration functions. Ensures the final score is properly scaled
 * for downstream consumption.
 */
public class ScoreCalibrator {

    private static final Logger logger = Logger.getLogger(ScoreCalibrator.class.getName());

    /**
     * Calibrates a normalized score (0-1) to final risk score (0-100).
     * Uses piecewise linear calibration to ensure proper distribution.
     * 
     * @param normalizedScore The normalized score (0-1) to calibrate
     * @param calibrationMode Calibration mode: 'linear' or 'piecewise' (default)
     * @return Calibrated score (0-100) with metadata
     */
    @Schema(name = "calibrate_score", description = "Calibrates normalized score (0-1) to final risk score (0-100) using piecewise linear or other calibration. Ensures proper distribution across severity bands.")
    public static Map<String, Object> calibrateScore(
            @Schema(name = "normalized_score", description = "The normalized score (0-1) to calibrate") double normalizedScore,
            @Schema(name = "calibration_mode", description = "Calibration mode: 'linear' or 'piecewise' (default 'piecewise')") String calibrationMode) {
        
        logger.info("📊 Calibrating score: " + normalizedScore + " using mode: " + calibrationMode);
        
        Map<String, Object> result = new HashMap<>();
        
        // Clamp to [0..1]
        normalizedScore = Math.max(0.0, Math.min(1.0, normalizedScore));
        
        double calibratedScore;
        
        if ("linear".equalsIgnoreCase(calibrationMode)) {
            // Simple linear scaling
            calibratedScore = normalizedScore * 100.0;
        } else {
            // Piecewise linear calibration (default)
            // This ensures better distribution across severity bands
            // Segments: [0-0.34] → [0-34], [0.34-0.59] → [34-59], [0.59-0.79] → [59-79], [0.79-1.0] → [79-100]
            if (normalizedScore <= 0.34) {
                // LOW band: linear mapping to [0-34]
                calibratedScore = normalizedScore * (34.0 / 0.34);
            } else if (normalizedScore <= 0.59) {
                // MED band: linear mapping to [34-59]
                calibratedScore = 34.0 + ((normalizedScore - 0.34) * (25.0 / 0.25));
            } else if (normalizedScore <= 0.79) {
                // HIGH band: linear mapping to [59-79]
                calibratedScore = 59.0 + ((normalizedScore - 0.59) * (20.0 / 0.20));
            } else {
                // CRITICAL band: linear mapping to [79-100]
                calibratedScore = 79.0 + ((normalizedScore - 0.79) * (21.0 / 0.21));
            }
        }
        
        // Round and bound to [0-100]
        int finalScore = (int) Math.round(Math.max(0, Math.min(100, calibratedScore)));
        
        result.put("final_risk_score", finalScore);
        result.put("normalized_score", Math.round(normalizedScore * 10000.0) / 10000.0);
        result.put("calibration_mode", calibrationMode != null && !calibrationMode.isEmpty() ? calibrationMode : "piecewise");
        
        logger.info("✅ Calibrated: " + normalizedScore + " → " + finalScore);
        
        return result;
    }
}

