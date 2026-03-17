package com.ing.sentinel.tools.behavioral;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BurstActivitySignal.
 * Tests burst activity detection and result structure.
 */
@DisplayName("BurstActivitySignal Tests")
class BurstActivitySignalTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should analyze normal activity")
    void testNormalActivity() {
        Map<String, Object> result = BurstActivitySignal.analyzeBurstActivity(
                2, 10, 2.0);
        
        assertNotNull(result);
        assertEquals(false, result.get("flag_raised"));
        assertNull(result.get("flag"));
    }

    @Test
    @DisplayName("Should detect burst activity")
    void testBurstActivityDetection() {
        Map<String, Object> result = BurstActivitySignal.analyzeBurstActivity(
                10, 20, 2.0);
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("flag_raised"));
        assertEquals("BURST_ACTIVITY", result.get("flag"));
    }

    @Test
    @DisplayName("Should calculate velocity ratios")
    void testVelocityRatios() {
        Map<String, Object> result = BurstActivitySignal.analyzeBurstActivity(
                5, 15, 2.0);
        
        assertNotNull(result.get("hourly_velocity_ratio"));
        assertNotNull(result.get("daily_velocity_ratio"));
    }

    @Test
    @DisplayName("Should handle zero baseline")
    void testZeroBaseline() {
        Map<String, Object> result = BurstActivitySignal.analyzeBurstActivity(
                5, 15, 0.0);
        
        assertNotNull(result);
        assertNotNull(result.get("normalized_signal"));
    }

    @Test
    @DisplayName("Should include reasoning")
    void testIncludesReasoning() {
        Map<String, Object> result = BurstActivitySignal.analyzeBurstActivity(
                2, 10, 2.0);
        
        assertNotNull(result.get("reasoning"));
        assertTrue(result.get("reasoning") instanceof String);
    }

    @Test
    @DisplayName("Should normalize signal to range 0-1")
    void testNormalizedSignalRange() {
        Map<String, Object> result = BurstActivitySignal.analyzeBurstActivity(
                5, 15, 2.0);
        
        double signal = ((Number) result.get("normalized_signal")).doubleValue();
        assertThat(signal).isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = BurstActivitySignal.analyzeBurstActivity(
                2, 10, 2.0);
        
        assertTrue(result.containsKey("transactions_in_last_hour"));
        assertTrue(result.containsKey("hourly_velocity_ratio"));
        assertTrue(result.containsKey("normalized_signal"));
        assertTrue(result.containsKey("flag"));
        assertTrue(result.containsKey("flag_raised"));
        assertTrue(result.containsKey("reasoning"));
    }
}

