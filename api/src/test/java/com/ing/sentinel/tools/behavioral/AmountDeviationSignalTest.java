package com.ing.sentinel.tools.behavioral;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AmountDeviationSignal.
 * Tests amount deviation analysis and result structure.
 */
@DisplayName("AmountDeviationSignal Tests")
class AmountDeviationSignalTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should analyze normal amount deviation")
    void testNormalAmountDeviation() {
        Map<String, Object> result = AmountDeviationSignal.analyzeAmountDeviation(
                100.0, 100.0, 20.0);
        
        assertNotNull(result);
        assertNotNull(result.get("normalized_signal"));
        assertEquals(false, result.get("flag_raised"));
        assertNull(result.get("flag"));
    }

    @Test
    @DisplayName("Should detect high amount deviation")
    void testHighAmountDeviation() {
        Map<String, Object> result = AmountDeviationSignal.analyzeAmountDeviation(
                200.0, 100.0, 20.0);
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("flag_raised"));
        assertEquals("AMOUNT_DEVIATION", result.get("flag"));
    }

    @Test
    @DisplayName("Should include z-score calculation")
    void testZScoreCalculation() {
        Map<String, Object> result = AmountDeviationSignal.analyzeAmountDeviation(
                150.0, 100.0, 20.0);
        
        assertNotNull(result.get("z_score"));
        double zScore = ((Number) result.get("z_score")).doubleValue();
        assertThat(zScore).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should handle zero standard deviation")
    void testZeroStandardDeviation() {
        Map<String, Object> result = AmountDeviationSignal.analyzeAmountDeviation(
                150.0, 100.0, 0.0);
        
        assertNotNull(result);
        assertNotNull(result.get("normalized_signal"));
    }

    @Test
    @DisplayName("Should include reasoning")
    void testIncludesReasoning() {
        Map<String, Object> result = AmountDeviationSignal.analyzeAmountDeviation(
                100.0, 100.0, 20.0);
        
        assertNotNull(result.get("reasoning"));
        assertTrue(result.get("reasoning") instanceof String);
    }

    @Test
    @DisplayName("Should normalize signal to range 0-1")
    void testNormalizedSignalRange() {
        Map<String, Object> result = AmountDeviationSignal.analyzeAmountDeviation(
                150.0, 100.0, 20.0);
        
        double signal = ((Number) result.get("normalized_signal")).doubleValue();
        assertThat(signal).isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = AmountDeviationSignal.analyzeAmountDeviation(
                100.0, 100.0, 20.0);
        
        assertTrue(result.containsKey("normalized_signal"));
        assertTrue(result.containsKey("flag"));
        assertTrue(result.containsKey("flag_raised"));
        assertTrue(result.containsKey("reasoning"));
    }
}

