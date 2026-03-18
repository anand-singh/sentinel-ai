package com.ing.sentinel.agent.tools.aggregator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WeightedScoreBlender.
 * Tests weighted blending of multiple agent scores.
 */
@DisplayName("WeightedScoreBlender Tests")
class WeightedScoreBlenderTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should blend multiple scores with weights")
    void testBlendScores() {
        Map<String, Object> result = WeightedScoreBlender.blendWeightedScores(
                0.6, 0.8, 0.4, 0.4, 0.4, 0.2);
        
        assertNotNull(result);
        assertNotNull(result.get("blended_score"));
    }

    @Test
    @DisplayName("Should return final score in 0-1 range")
    void testFinalScoreRange() {
        Map<String, Object> result = WeightedScoreBlender.blendWeightedScores(
                0.5, 0.5, -1.0, 0.5, 0.5, 0.0);
        
        double blendedScore = (Double) result.get("blended_score");
        assertTrue(blendedScore >= 0.0 && blendedScore <= 1.0);
    }

    @Test
    @DisplayName("Should include contributions breakdown")
    void testContributionsBreakdown() {
        Map<String, Object> result = WeightedScoreBlender.blendWeightedScores(
                0.8, 0.6, -1.0, 0.5, 0.5, 0.0);
        
        assertNotNull(result.get("contributions"));
    }

    @Test
    @DisplayName("Should handle single score")
    void testSingleScore() {
        Map<String, Object> result = WeightedScoreBlender.blendWeightedScores(
                0.75, 0.0, -1.0, 1.0, 0.0, 0.0);
        
        double blendedScore = (Double) result.get("blended_score");
        assertEquals(0.75, blendedScore, 0.01);
    }

    @Test
    @DisplayName("Should normalize weights if not sum to 1")
    void testWeightNormalization() {
        Map<String, Object> result = WeightedScoreBlender.blendWeightedScores(
                0.5, 0.5, -1.0, 2.0, 2.0, 0.0);
        
        assertNotNull(result.get("blended_score"));
        double blendedScore = (Double) result.get("blended_score");
        assertTrue(blendedScore >= 0.0 && blendedScore <= 1.0);
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = WeightedScoreBlender.blendWeightedScores(
                0.5, 0.5, -1.0, 0.5, 0.5, 0.0);
        
        assertTrue(result.containsKey("blended_score"));
        assertTrue(result.containsKey("contributions"));
    }
}

