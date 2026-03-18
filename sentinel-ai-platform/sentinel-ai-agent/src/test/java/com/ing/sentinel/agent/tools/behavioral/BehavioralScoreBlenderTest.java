package com.ing.sentinel.agent.tools.behavioral;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                0.5, 0.3, 0.4, 0.6, 0.2, 0.3, 0.4, 0.5,
                null, null, null, "NEW_DEVICE", null, null, null, null,
                2.5, 100.0);
        
        assertNotNull(result);
        assertNotNull(result.get("behavioral_risk_score"));
        assertThat(result.get("behavioral_risk_score")).isInstanceOf(Integer.class);
    }

    @Test
    @DisplayName("Should include risk score in valid range")
    void testRiskScoreRange() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.8, 0.7, 0.9, 0.8, 0.6, 0.7, 0.8, 0.9,
                "AMOUNT_DEVIATION", "UNUSUAL_TIME", "GEO_DEVIATION", 
                "NEW_DEVICE", "NEW_IP_RANGE", "RARE_MERCHANT", "BURST_ACTIVITY", "NEW_ACCOUNT",
                3.0, 200.0);
        
        int score = (Integer) result.get("behavioral_risk_score");
        assertThat(score).isBetween(0, 100);
    }

    @Test
    @DisplayName("Should collect active flags")
    void testCollectActiveFlags() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.5, 0.3, 0.4, 0.6, 0.2, 0.3, 0.4, 0.5,
                "AMOUNT_DEVIATION", null, "GEO_DEVIATION", "NEW_DEVICE", null, null, null, null,
                2.5, 100.0);
        
        assertNotNull(result.get("flags"));
        assertThat(result.get("flags")).isInstanceOf(java.util.List.class);
    }

    @Test
    @DisplayName("Should include reasoning")
    void testIncludesReasoning() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.5, 0.3, 0.4, 0.6, 0.2, 0.3, 0.4, 0.5,
                null, null, null, null, null, null, null, null,
                1.5, 50.0);
        
        assertNotNull(result.get("reasoning"));
        assertThat(result.get("reasoning")).isInstanceOf(String.class);
    }

    @Test
    @DisplayName("Should include version info")
    void testIncludesVersionInfo() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.5, 0.3, 0.4, 0.6, 0.2, 0.3, 0.4, 0.5,
                null, null, null, null, null, null, null, null,
                1.5, 50.0);
        
        assertTrue(result.containsKey("agent_version"));
        assertTrue(result.containsKey("config_version"));
    }

    @Test
    @DisplayName("Should include feature contributions")
    void testIncludesFeatureContributions() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.5, 0.3, 0.4, 0.6, 0.2, 0.3, 0.4, 0.5,
                null, null, null, null, null, null, null, null,
                2.5, 100.0);
        
        assertTrue(result.containsKey("feature_contributions"));
        assertThat(result.get("feature_contributions")).isInstanceOf(Map.class);
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.5, 0.3, 0.4, 0.6, 0.2, 0.3, 0.4, 0.5,
                null, null, null, null, null, null, null, null,
                1.5, 50.0);
        
        assertTrue(result.containsKey("behavioral_risk_score"));
        assertTrue(result.containsKey("flags"));
        assertTrue(result.containsKey("reasoning"));
        assertTrue(result.containsKey("agent_version"));
        assertTrue(result.containsKey("config_version"));
    }

    @Test
    @DisplayName("Should return zero score when all signals are zero")
    void allSignalsZeroProducesMinimalScore() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                null, null, null, null, null, null, null, null,
                0.0, 0.0);
        
        int score = (Integer) result.get("behavioral_risk_score");
        assertThat(score).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return maximum score when all signals are at max")
    void allSignalsMaxProducesHighScore() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                "AMOUNT_DEVIATION", "UNUSUAL_TIME", "GEO_DEVIATION", 
                "NEW_DEVICE", "NEW_IP_RANGE", "RARE_MERCHANT", "BURST_ACTIVITY", "NEW_ACCOUNT",
                5.0, 500.0);
        
        int score = (Integer) result.get("behavioral_risk_score");
        assertThat(score).isGreaterThan(90);
    }

    @Test
    @DisplayName("Should apply high risk boost for new account with new device and high velocity")
    void highRiskCombinationAppliesBoost() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.3, 0.2, 0.3, 0.8, 0.8, 0.2, 0.9, 0.9,
                null, null, null, "NEW_DEVICE", "NEW_IP_RANGE", null, "BURST_ACTIVITY", "NEW_ACCOUNT",
                1.5, 100.0);
        
        int score = (Integer) result.get("behavioral_risk_score");
        assertThat(score).isGreaterThan(50);
    }

    @Test
    @DisplayName("Should prioritize device signal over IP signal when device is higher")
    void deviceSignalTakesPrecedenceWhenHigher() {
        Map<String, Object> result1 = BehavioralScoreBlender.blendBehavioralScores(
                0.0, 0.0, 0.0, 0.8, 0.2, 0.0, 0.0, 0.0,
                null, null, null, "NEW_DEVICE", null, null, null, null,
                0.0, 0.0);
        
        Map<String, Object> result2 = BehavioralScoreBlender.blendBehavioralScores(
                0.0, 0.0, 0.0, 0.2, 0.8, 0.0, 0.0, 0.0,
                null, null, null, null, "NEW_IP_RANGE", null, null, null,
                0.0, 0.0);
        
        int score1 = (Integer) result1.get("behavioral_risk_score");
        int score2 = (Integer) result2.get("behavioral_risk_score");
        assertThat(score1).isEqualTo(score2);
    }

    @Test
    @DisplayName("Should ignore null and empty flags")
    void nullAndEmptyFlagsAreExcluded() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.5, 0.3, 0.4, 0.6, 0.2, 0.3, 0.4, 0.5,
                null, "", "null", "NEW_DEVICE", null, null, null, null,
                2.0, 100.0);
        
        @SuppressWarnings("unchecked")
        java.util.List<String> flags = (java.util.List<String>) result.get("flags");
        assertThat(flags).containsOnly("NEW_DEVICE");
    }

    @Test
    @DisplayName("Should include timestamp in result")
    void resultContainsTimestamp() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.5, 0.3, 0.4, 0.6, 0.2, 0.3, 0.4, 0.5,
                null, null, null, null, null, null, null, null,
                1.5, 50.0);
        
        assertTrue(result.containsKey("analyzed_at"));
        assertNotNull(result.get("analyzed_at"));
    }

    @Test
    @DisplayName("Should include weights in result")
    void resultContainsWeights() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.5, 0.3, 0.4, 0.6, 0.2, 0.3, 0.4, 0.5,
                null, null, null, null, null, null, null, null,
                1.5, 50.0);
        
        assertTrue(result.containsKey("weights"));
        @SuppressWarnings("unchecked")
        Map<String, Double> weights = (Map<String, Double>) result.get("weights");
        assertThat(weights).containsKeys("amount", "geo", "device", "time", "merchant", "velocity", "new_account");
    }

    @Test
    @DisplayName("Should generate reasoning with no flags for normal behavior")
    void reasoningDescribesNormalBehavior() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1,
                null, null, null, null, null, null, null, null,
                0.5, 10.0);
        
        String reasoning = (String) result.get("reasoning");
        assertThat(reasoning).contains("consistent with customer's typical behavior");
    }

    @Test
    @DisplayName("Should generate reasoning with amount deviation details")
    void reasoningIncludesAmountDeviationDetails() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.8, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1,
                "AMOUNT_DEVIATION", null, null, null, null, null, null, null,
                3.5, 10.0);
        
        String reasoning = (String) result.get("reasoning");
        assertThat(reasoning).contains("3.5σ above customer's normal");
    }

    @Test
    @DisplayName("Should generate reasoning with geo deviation distance")
    void reasoningIncludesGeoDistance() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.1, 0.1, 0.9, 0.1, 0.1, 0.1, 0.1, 0.1,
                null, null, "GEO_DEVIATION", null, null, null, null, null,
                0.5, 250.5);
        
        String reasoning = (String) result.get("reasoning");
        assertThat(reasoning).contains("travel 251km from last txn");
    }

    @Test
    @DisplayName("Should generate reasoning for new account flag")
    void reasoningDescribesNewAccountRisk() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.9,
                null, null, null, null, null, null, null, "NEW_ACCOUNT",
                0.5, 10.0);
        
        String reasoning = (String) result.get("reasoning");
        assertThat(reasoning).contains("newly opened account");
        assertThat(reasoning).contains("money mule risk");
    }

    @Test
    @DisplayName("Should generate reasoning for multiple flags")
    void reasoningCombinesMultipleFlags() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.8, 0.8, 0.8, 0.8, 0.1, 0.1, 0.8, 0.8,
                "AMOUNT_DEVIATION", "UNUSUAL_TIME", "GEO_DEVIATION", 
                "NEW_DEVICE", null, null, "BURST_ACTIVITY", "NEW_ACCOUNT",
                4.2, 350.0);
        
        String reasoning = (String) result.get("reasoning");
        assertThat(reasoning).contains("4.2σ above customer's normal");
        assertThat(reasoning).contains("new device");
        assertThat(reasoning).contains("off-hours");
        assertThat(reasoning).contains("travel 350km");
        assertThat(reasoning).contains("high velocity");
        assertThat(reasoning).contains("newly opened account");
    }

    @Test
    @DisplayName("Should set feature contributions correctly for high signals")
    void featureContributionsReflectHighSignals() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.9, 0.8, 0.9, 0.9, 0.8, 0.7, 0.9, 0.9,
                "AMOUNT_DEVIATION", "UNUSUAL_TIME", "GEO_DEVIATION", 
                "NEW_DEVICE", "NEW_IP_RANGE", "RARE_MERCHANT", "BURST_ACTIVITY", "NEW_ACCOUNT",
                4.5, 300.0);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> contributions = (Map<String, Object>) result.get("feature_contributions");
        assertThat((Double) contributions.get("amount_zscore_customer")).isEqualTo(4.5);
        assertThat((Double) contributions.get("hour_deviation")).isEqualTo(0.8);
        assertThat((Double) contributions.get("geo_distance_km")).isEqualTo(300.0);
        assertThat((Integer) contributions.get("new_device")).isEqualTo(1);
        assertThat((Integer) contributions.get("new_ip_range")).isEqualTo(1);
        assertThat((Integer) contributions.get("burst_in_window")).isEqualTo(1);
        assertThat((Integer) contributions.get("new_account")).isEqualTo(1);
    }

    @Test
    @DisplayName("Should set feature contributions correctly for low signals")
    void featureContributionsReflectLowSignals() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.2, 0.1, 0.2, 0.3, 0.2, 0.1, 0.3, 0.2,
                null, null, null, null, null, null, null, null,
                0.8, 15.0);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> contributions = (Map<String, Object>) result.get("feature_contributions");
        assertThat((Integer) contributions.get("new_device")).isEqualTo(0);
        assertThat((Integer) contributions.get("new_ip_range")).isEqualTo(0);
        assertThat((Integer) contributions.get("burst_in_window")).isEqualTo(0);
        assertThat((Integer) contributions.get("new_account")).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle rare merchant flag in reasoning")
    void reasoningHandlesRareMerchantFlag() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.1, 0.1, 0.1, 0.1, 0.1, 0.8, 0.1, 0.1,
                null, null, null, null, null, "RARE_MERCHANT", null, null,
                0.5, 10.0);
        
        String reasoning = (String) result.get("reasoning");
        assertThat(reasoning).contains("unusual merchant/category");
    }

    @Test
    @DisplayName("Should handle rare MCC flag in reasoning")
    void reasoningHandlesRareMccFlag() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.1, 0.1, 0.1, 0.1, 0.1, 0.8, 0.1, 0.1,
                null, null, null, null, null, "RARE_MCC", null, null,
                0.5, 10.0);
        
        String reasoning = (String) result.get("reasoning");
        assertThat(reasoning).contains("unusual merchant/category");
    }

    @Test
    @DisplayName("Should handle unknown flag gracefully in reasoning")
    void reasoningHandlesUnknownFlagGracefully() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1,
                "UNKNOWN_FLAG", null, null, null, null, null, null, null,
                0.5, 10.0);
        
        String reasoning = (String) result.get("reasoning");
        assertThat(reasoning).contains("unknown flag");
    }

    @Test
    @DisplayName("Should cap score at 100 even with boost")
    void scoreNeverExceedsOneHundred() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                "AMOUNT_DEVIATION", "UNUSUAL_TIME", "GEO_DEVIATION", 
                "NEW_DEVICE", "NEW_IP_RANGE", "RARE_MERCHANT", "BURST_ACTIVITY", "NEW_ACCOUNT",
                10.0, 1000.0);
        
        int score = (Integer) result.get("behavioral_risk_score");
        assertThat(score).isLessThanOrEqualTo(100);
    }

    @Test
    @DisplayName("Should not apply boost when new account signal is below threshold")
    void noBoostWhenNewAccountSignalLow() {
        Map<String, Object> result1 = BehavioralScoreBlender.blendBehavioralScores(
                0.0, 0.0, 0.0, 0.6, 0.0, 0.0, 0.6, 0.6,
                null, null, null, "NEW_DEVICE", null, null, "BURST_ACTIVITY", null,
                0.0, 0.0);
        
        Map<String, Object> result2 = BehavioralScoreBlender.blendBehavioralScores(
                0.0, 0.0, 0.0, 0.6, 0.0, 0.0, 0.6, 0.8,
                null, null, null, "NEW_DEVICE", null, null, "BURST_ACTIVITY", "NEW_ACCOUNT",
                0.0, 0.0);
        
        int score1 = (Integer) result1.get("behavioral_risk_score");
        int score2 = (Integer) result2.get("behavioral_risk_score");
        assertThat(score2).isGreaterThan(score1);
    }

    @Test
    @DisplayName("Should round merchant novelty to three decimal places in contributions")
    void merchantNoveltyRoundedInContributions() {
        Map<String, Object> result = BehavioralScoreBlender.blendBehavioralScores(
                0.1, 0.1, 0.1, 0.1, 0.1, 0.123456789, 0.1, 0.1,
                null, null, null, null, null, null, null, null,
                0.5, 10.0);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> contributions = (Map<String, Object>) result.get("feature_contributions");
        double merchantNovelty = (Double) contributions.get("merchant_novelty");
        assertThat(merchantNovelty).isEqualTo(0.123);
    }

    @Test
    @DisplayName("Should emphasize new account weight in final score")
    void newAccountSignalHasHighImpact() {
        Map<String, Object> result1 = BehavioralScoreBlender.blendBehavioralScores(
                1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                "AMOUNT_DEVIATION", null, null, null, null, null, null, null,
                5.0, 0.0);
        
        Map<String, Object> result2 = BehavioralScoreBlender.blendBehavioralScores(
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0,
                null, null, null, null, null, null, null, "NEW_ACCOUNT",
                0.0, 0.0);
        
        int score1 = (Integer) result1.get("behavioral_risk_score");
        int score2 = (Integer) result2.get("behavioral_risk_score");
        assertThat(score2).isGreaterThan(score1);
    }
}

