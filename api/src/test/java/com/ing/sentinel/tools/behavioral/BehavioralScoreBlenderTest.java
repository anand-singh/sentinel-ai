package com.ing.sentinel.tools.behavioral;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BehavioralScoreBlender.
 * Tests behavioral score blending and result structure.
 */
@DisplayName("BehavioralScoreBlender Tests")
class BehavioralScoreBlenderTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should blend behavioral scores successfully")
    void testBlendBehavioralScores() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.5, 0.3, 0.4, 0.6, 0.2, 0.3, 0.4,
                null, null, null, "NEW_DEVICE", null, null, null,
                2.5, 100.0);
        
        assertNotNull(result);
        assertNotNull(result.get("behavioral_risk_score"));
        assertTrue(result.get("behavioral_risk_score") instanceof Integer);
    }

    @Test
    @DisplayName("Should include risk score in valid range")
    void testRiskScoreRange() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.8, 0.7, 0.9, 0.8, 0.6, 0.7, 0.8,
                "AMOUNT_DEVIATION", "UNUSUAL_TIME", "GEO_DEVIATION", 
                "NEW_DEVICE", "NEW_IP_RANGE", "RARE_MERCHANT", "BURST_ACTIVITY",
                3.0, 200.0);
        
        int score = (Integer) result.get("behavioral_risk_score");
        assertThat(score).isBetween(0, 100);
    }

    @Test
    @DisplayName("Should collect active flags")
    void testCollectActiveFlags() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.5, 0.3, 0.4, 0.6, 0.2, 0.3, 0.4,
                "AMOUNT_DEVIATION", null, "GEO_DEVIATION", "NEW_DEVICE", null, null, null,
                2.5, 100.0);
        
        assertNotNull(result.get("flags"));
        assertTrue(result.get("flags") instanceof java.util.List);
    }

    @Test
    @DisplayName("Should include reasoning")
    void testIncludesReasoning() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.5, 0.3, 0.4, 0.6, 0.2, 0.3, 0.4,
                null, null, null, null, null, null, null,
                1.5, 50.0);
        
        assertNotNull(result.get("reasoning"));
        assertTrue(result.get("reasoning") instanceof String);
    }

    @Test
    @DisplayName("Should include version info")
    void testIncludesVersionInfo() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.5, 0.3, 0.4, 0.6, 0.2, 0.3, 0.4,
                null, null, null, null, null, null, null,
                1.5, 50.0);
        
        assertTrue(result.containsKey("agent_version"));
        assertTrue(result.containsKey("config_version"));
    }

    @Test
    @DisplayName("Should include feature contributions")
    void testIncludesFeatureContributions() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.5, 0.3, 0.4, 0.6, 0.2, 0.3, 0.4,
                null, null, null, null, null, null, null,
                2.5, 100.0);
        
        assertTrue(result.containsKey("feature_contributions"));
        assertTrue(result.get("feature_contributions") instanceof Map);
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.5, 0.3, 0.4, 0.6, 0.2, 0.3, 0.4,
                null, null, null, null, null, null, null,
                1.5, 50.0);
        
        assertTrue(result.containsKey("behavioral_risk_score"));
        assertTrue(result.containsKey("flags"));
        assertTrue(result.containsKey("reasoning"));
        assertTrue(result.containsKey("agent_version"));
        assertTrue(result.containsKey("config_version"));
    }
}

