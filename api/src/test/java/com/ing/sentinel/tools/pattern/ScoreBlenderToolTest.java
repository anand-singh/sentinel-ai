package com.ing.sentinel.tools.pattern;

import com.google.adk.tools.FunctionTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScoreBlenderTool.
 * Tests weighted score blending and risk level calculation.
 */
@DisplayName("ScoreBlenderTool Tests")
class ScoreBlenderToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should blend scores with weights")
    void testBlendScores() {
        Map<String, Object> result = ScoreBlenderTool.blendRiskScores(
                0.8, 0.6, 0.7, 0.5, 0.4,
                "AMOUNT_SPIKE", "GEO_MISMATCH", "VELOCITY_HIGH", null, null);
        
        assertNotNull(result);
        assertNotNull(result.get("risk_score"));
    }

    @Test
    @DisplayName("Should return score in 0-100 range")
    void testScoreRange() {
        Map<String, Object> result = ScoreBlenderTool.blendRiskScores(
                0.5, 0.5, 0.5, 0.5, 0.5,
                null, null, null, null, null);
        
        int score = (Integer) result.get("risk_score");
        assertTrue(score >= 0 && score <= 100);
    }

    @Test
    @DisplayName("Should calculate weighted average correctly")
    void testWeightedAverage() {
        Map<String, Object> result = ScoreBlenderTool.blendRiskScores(
                0.5, 0.5, 0.5, 0.5, 0.5,
                null, null, null, null, null);
        
        int score = (Integer) result.get("risk_score");
        assertTrue(score >= 40 && score <= 60); // Should be around 50
    }

    @Test
    @DisplayName("Should collect active flags")
    void testCollectFlags() {
        Map<String, Object> result = ScoreBlenderTool.blendRiskScores(
                0.9, 0.8, 0.7, 0.6, 0.5,
                "AMOUNT_SPIKE", "GEO_MISMATCH", null, null, null);
        
        assertNotNull(result.get("flags"));
        assertInstanceOf(java.util.List.class, result.get("flags"));
    }

    @Test
    @DisplayName("Should include reasoning")
    void testIncludesReasoning() {
        Map<String, Object> result = ScoreBlenderTool.blendRiskScores(
                0.8, 0.6, 0.5, 0.4, 0.3,
                "AMOUNT_SPIKE", null, null, null, null);
        
        assertNotNull(result.get("reasoning"));
    }

    @Test
    @DisplayName("Should create FunctionTool wrapper")
    void testCreateFunctionTool() {
        FunctionTool tool = ScoreBlenderTool.create();
        assertNotNull(tool);
    }

    @Test
    @DisplayName("Should handle all zero signals")
    void testZeroSignals() {
        Map<String, Object> result = ScoreBlenderTool.blendRiskScores(
                0.0, 0.0, 0.0, 0.0, 0.0,
                null, null, null, null, null);
        
        assertNotNull(result);
        int score = (Integer) result.get("risk_score");
        assertEquals(0, score);
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = ScoreBlenderTool.blendRiskScores(
                0.5, 0.5, 0.5, 0.5, 0.5,
                null, null, null, null, null);
        
        assertTrue(result.containsKey("risk_score"));
        assertTrue(result.containsKey("flags"));
        assertTrue(result.containsKey("reasoning"));
        assertTrue(result.containsKey("agent_version"));
        assertTrue(result.containsKey("config_version"));
    }
}

