package com.ing.sentinel.agent.tools.behavioral;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TimeDeviationSignal.
 * Tests time deviation detection and result structure.
 */
@DisplayName("TimeDeviationSignal Tests")
class TimeDeviationSignalTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should detect normal activity during active hours")
    void testNormalActiveHours() {
        // Create timestamp at 10 AM UTC
        String timestamp = ZonedDateTime.now().withHour(10).withMinute(0).toString();
        
        Map<String, Object> result = TimeDeviationSignal.analyzeTimeDeviation(
                timestamp, 23, 6, 8, 18);
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("during_active_hours"));
        assertEquals(false, result.get("flag_raised"));
        assertNull(result.get("flag"));
    }

    @Test
    @DisplayName("Should detect activity during sleep hours")
    void testSleepHours() {
        // Create timestamp at 2 AM UTC
        String timestamp = ZonedDateTime.now().withHour(2).withMinute(0).toString();
        
        Map<String, Object> result = TimeDeviationSignal.analyzeTimeDeviation(
                timestamp, 23, 6, 8, 18);
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("during_sleep_hours"));
        assertTrue((Boolean) result.get("flag_raised"));
        assertEquals("UNUSUAL_TIME", result.get("flag"));
    }

    @Test
    @DisplayName("Should detect activity outside active hours but not during sleep")
    void testOutsideActiveNotSleep() {
        // Create timestamp at 7 AM UTC
        String timestamp = ZonedDateTime.now().withHour(7).withMinute(0).toString();
        
        Map<String, Object> result = TimeDeviationSignal.analyzeTimeDeviation(
                timestamp, 23, 6, 8, 18);
        
        assertNotNull(result);
        assertFalse((Boolean) result.get("during_active_hours"));
        assertFalse((Boolean) result.get("during_sleep_hours"));
    }

    @Test
    @DisplayName("Should include transaction hour")
    void testIncludesTransactionHour() {
        String timestamp = ZonedDateTime.now().withHour(14).withMinute(30).toString();
        
        Map<String, Object> result = TimeDeviationSignal.analyzeTimeDeviation(
                timestamp, 23, 6, 8, 18);
        
        assertNotNull(result.get("transaction_hour"));
        assertEquals(14, result.get("transaction_hour"));
    }

    @Test
    @DisplayName("Should include day of week")
    void testIncludesDayOfWeek() {
        String timestamp = ZonedDateTime.now().toString();
        
        Map<String, Object> result = TimeDeviationSignal.analyzeTimeDeviation(
                timestamp, 23, 6, 8, 18);
        
        assertNotNull(result.get("day_of_week"));
        assertTrue(result.get("day_of_week") instanceof String);
    }

    @Test
    @DisplayName("Should include reasoning")
    void testIncludesReasoning() {
        String timestamp = ZonedDateTime.now().withHour(10).toString();
        
        Map<String, Object> result = TimeDeviationSignal.analyzeTimeDeviation(
                timestamp, 23, 6, 8, 18);
        
        assertNotNull(result.get("reasoning"));
        assertTrue(result.get("reasoning") instanceof String);
    }

    @Test
    @DisplayName("Should normalize signal to range 0-1")
    void testNormalizedSignalRange() {
        String timestamp = ZonedDateTime.now().withHour(10).toString();
        
        Map<String, Object> result = TimeDeviationSignal.analyzeTimeDeviation(
                timestamp, 23, 6, 8, 18);
        
        double signal = ((Number) result.get("normalized_signal")).doubleValue();
        assertThat(signal).isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("Should handle overnight sleep range")
    void testOvernightSleepRange() {
        // Test that 23-6 range works correctly (crosses midnight)
        String timestamp1 = ZonedDateTime.now().withHour(1).toString();
        String timestamp2 = ZonedDateTime.now().withHour(23).toString();
        
        Map<String, Object> result1 = TimeDeviationSignal.analyzeTimeDeviation(
                timestamp1, 23, 6, 8, 18);
        Map<String, Object> result2 = TimeDeviationSignal.analyzeTimeDeviation(
                timestamp2, 23, 6, 8, 18);
        
        assertTrue((Boolean) result1.get("during_sleep_hours"));
        assertTrue((Boolean) result2.get("during_sleep_hours"));
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        String timestamp = ZonedDateTime.now().toString();
        
        Map<String, Object> result = TimeDeviationSignal.analyzeTimeDeviation(
                timestamp, 23, 6, 8, 18);
        
        assertTrue(result.containsKey("transaction_hour"));
        assertTrue(result.containsKey("day_of_week"));
        assertTrue(result.containsKey("during_sleep_hours"));
        assertTrue(result.containsKey("during_active_hours"));
        assertTrue(result.containsKey("normalized_signal"));
        assertTrue(result.containsKey("flag"));
        assertTrue(result.containsKey("flag_raised"));
        assertTrue(result.containsKey("reasoning"));
    }
}

