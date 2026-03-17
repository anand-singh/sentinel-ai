package com.ing.sentinel.tools.aggregator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScoreNormalizer.
 * Tests score normalization to [0..1] scale.
 */
@DisplayName("ScoreNormalizer Tests")
class ScoreNormalizerTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should normalize score to 0-1 range")
    void testNormalizeScore() {
        Map<String, Object> result = ScoreNormalizer.normalizeScore(
                50.0, 0.0, 100.0);
        
        assertNotNull(result);
        double normalized = (Double) result.get("normalized_score");
        assertEquals(0.5, normalized, 0.01);
    }

    @Test
    @DisplayName("Should clamp score above max")
    void testClampAboveMax() {
        Map<String, Object> result = ScoreNormalizer.normalizeScore(
                150.0, 0.0, 100.0);
        
        double clamped = (Double) result.get("clamped_score");
        assertEquals(100.0, clamped);
        assertTrue((Boolean) result.get("was_clamped"));
    }

    @Test
    @DisplayName("Should clamp score below min")
    void testClampBelowMin() {
        Map<String, Object> result = ScoreNormalizer.normalizeScore(
                -10.0, 0.0, 100.0);
        
        double clamped = (Double) result.get("clamped_score");
        assertEquals(0.0, clamped);
        assertTrue((Boolean) result.get("was_clamped"));
    }

    @Test
    @DisplayName("Should not clamp score in range")
    void testNoClampInRange() {
        Map<String, Object> result = ScoreNormalizer.normalizeScore(
                50.0, 0.0, 100.0);
        
        assertFalse((Boolean) result.get("was_clamped"));
    }

    @Test
    @DisplayName("Should include original score")
    void testIncludesOriginalScore() {
        Map<String, Object> result = ScoreNormalizer.normalizeScore(
                75.0, 0.0, 100.0);
        
        assertEquals(75.0, result.get("original_score"));
    }

    @Test
    @DisplayName("Should handle custom min-max range")
    void testCustomRange() {
        Map<String, Object> result = ScoreNormalizer.normalizeScore(
                5.0, 0.0, 10.0);
        
        double normalized = (Double) result.get("normalized_score");
        assertEquals(0.5, normalized, 0.01);
    }

    @Test
    @DisplayName("Should handle zero range gracefully")
    void testZeroRange() {
        Map<String, Object> result = ScoreNormalizer.normalizeScore(
                50.0, 50.0, 50.0);
        
        double normalized = (Double) result.get("normalized_score");
        assertEquals(0.0, normalized);
    }

    @Test
    @DisplayName("Should round to 4 decimal places")
    void testRounding() {
        Map<String, Object> result = ScoreNormalizer.normalizeScore(
                33.333, 0.0, 100.0);
        
        double normalized = (Double) result.get("normalized_score");
        String normalizedStr = String.valueOf(normalized);
        int decimalPlaces = normalizedStr.indexOf('.') >= 0 
                ? normalizedStr.length() - normalizedStr.indexOf('.') - 1 : 0;
        assertTrue(decimalPlaces <= 4);
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = ScoreNormalizer.normalizeScore(
                50.0, 0.0, 100.0);
        
        assertTrue(result.containsKey("normalized_score"));
        assertTrue(result.containsKey("original_score"));
        assertTrue(result.containsKey("clamped_score"));
        assertTrue(result.containsKey("was_clamped"));
    }
}

