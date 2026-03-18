package com.ing.sentinel.tools.aggregator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RiskBooster.
 * Tests risk score boosting based on critical flags.
 */
@DisplayName("RiskBooster Tests")
class RiskBoosterTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should boost score for PEP match")
    void testBoostForPepMatch() {
        Map<String, Object> result = RiskBooster.applyRiskBoost(
                0.6, "PEP_MATCH_PENDING");
        
        assertNotNull(result);
        double boostedScore = (Double) result.get("boosted_score");
        assertTrue(boostedScore > 0.6);
    }

    @Test
    @DisplayName("Should boost score for sanctions hit")
    void testBoostForSanctions() {
        Map<String, Object> result = RiskBooster.applyRiskBoost(
                0.6, "SANCTIONS_HIT");
        
        double boostedScore = (Double) result.get("boosted_score");
        assertTrue(boostedScore > 0.6);
    }

    @Test
    @DisplayName("Should not boost for no critical flags")
    void testNoBoostForNormalFlags() {
        Map<String, Object> result = RiskBooster.applyRiskBoost(
                0.6, "AMOUNT_SPIKE");
        
        double boostedScore = (Double) result.get("boosted_score");
        assertEquals(0.6, boostedScore, 0.01);
    }

    @Test
    @DisplayName("Should include boost amount")
    void testIncludesBoostAmount() {
        Map<String, Object> result = RiskBooster.applyRiskBoost(
                0.6, "PEP_MATCH_PENDING");
        
        assertNotNull(result.get("boost_applied"));
    }

    @Test
    @DisplayName("Should list applied boosts")
    void testAppliedBoosts() {
        Map<String, Object> result = RiskBooster.applyRiskBoost(
                0.6, "PEP_MATCH_PENDING,SANCTIONS_HIT");
        
        assertNotNull(result.get("boost_reason"));
    }

    @Test
    @DisplayName("Should cap score at 1.0")
    void testScoreCapping() {
        Map<String, Object> result = RiskBooster.applyRiskBoost(
                0.95, "PEP_MATCH_PENDING,SANCTIONS_HIT");
        
        double boostedScore = (Double) result.get("boosted_score");
        assertTrue(boostedScore <= 1.0);
    }

    @Test
    @DisplayName("Should handle empty flags list")
    void testEmptyFlags() {
        Map<String, Object> result = RiskBooster.applyRiskBoost(
                0.5, "");
        
        double boostedScore = (Double) result.get("boosted_score");
        assertEquals(0.5, boostedScore);
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = RiskBooster.applyRiskBoost(
                0.6, "PEP_MATCH_PENDING");
        
        assertTrue(result.containsKey("boosted_score"));
        assertTrue(result.containsKey("boost_applied"));
        assertTrue(result.containsKey("boost_reason"));
    }
}

