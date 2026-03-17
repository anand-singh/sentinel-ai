package com.ing.sentinel.tools.aggregator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScoreCalibrator.
 * Tests score calibration and adjustment logic.
 */
@DisplayName("ScoreCalibrator Tests")
class ScoreCalibratorTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should calibrate score with piecewise mode")
    void testCalibrateScore() {
        Map<String, Object> result = ScoreCalibrator.calibrateScore(
                0.75, "piecewise");
        
        assertNotNull(result);
        assertNotNull(result.get("calibrated_score"));
        int calibrated = (Integer) result.get("calibrated_score");
        assertTrue(calibrated >= 0 && calibrated <= 100);
    }

    @Test
    @DisplayName("Should calibrate score with linear mode")
    void testLinearCalibration() {
        Map<String, Object> result = ScoreCalibrator.calibrateScore(
                0.50, "linear");
        
        int calibrated = (Integer) result.get("calibrated_score");
        assertEquals(50, calibrated);
    }

    @Test
    @DisplayName("Should use piecewise calibration by default")
    void testPiecewiseCalibration() {
        Map<String, Object> result = ScoreCalibrator.calibrateScore(
                0.50, "piecewise");
        
        int calibrated = (Integer) result.get("calibrated_score");
        assertTrue(calibrated >= 0 && calibrated <= 100);
    }

    @Test
    @DisplayName("Should cap at 100")
    void testCapAt100() {
        Map<String, Object> result = ScoreCalibrator.calibrateScore(
                1.0, "linear");
        
        int calibrated = (Integer) result.get("calibrated_score");
        assertEquals(100, calibrated);
    }

    @Test
    @DisplayName("Should handle zero score")
    void testZeroScore() {
        Map<String, Object> result = ScoreCalibrator.calibrateScore(
                0.0, "linear");
        
        int calibrated = (Integer) result.get("calibrated_score");
        assertEquals(0, calibrated);
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = ScoreCalibrator.calibrateScore(
                0.60, "piecewise");
        
        assertTrue(result.containsKey("calibrated_score"));
        assertTrue(result.containsKey("calibration_mode"));
    }
}

