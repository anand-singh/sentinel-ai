package com.ing.sentinel.agent.tools.pattern;

import com.google.adk.tools.FunctionTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TimeWindowTool.
 * Tests unusual transaction time detection.
 */
@DisplayName("TimeWindowTool Tests")
class TimeWindowToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should detect unusual time")
    void testDetectUnusualTime() {
        Map<String, Object> result = TimeWindowTool.analyzeTimeWindow(
                "2026-03-17T03:15:00Z", // 3 AM
                "9,10,11,12,13,14,15,16,17,18", // Customer usually active 9-6pm
                "8,9,10,11,12,13,14,15,16,17,18,19,20"); // MCC typical hours
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("flag_raised"));
        assertEquals("UNUSUAL_TIME", result.get("flag"));
    }

    @Test
    @DisplayName("Should not flag normal time")
    void testNormalTime() {
        Map<String, Object> result = TimeWindowTool.analyzeTimeWindow(
                "2026-03-17T14:30:00Z", // 2:30 PM
                "9,10,11,12,13,14,15,16,17,18", // Customer typical hours
                "8,9,10,11,12,13,14,15,16,17,18,19,20"); // MCC typical hours
        
        assertFalse((Boolean) result.get("flag_raised"));
    }

    @Test
    @DisplayName("Should include hour in result")
    void testIncludesHour() {
        Map<String, Object> result = TimeWindowTool.analyzeTimeWindow(
                "2026-03-17T15:00:00Z",
                "9,10,11,12,13,14,15,16,17,18",
                "8,9,10,11,12,13,14,15,16,17,18,19,20");
        
        assertEquals(15, result.get("transaction_hour"));
    }

    @Test
    @DisplayName("Should calculate anomaly score")
    void testAnomalyScore() {
        Map<String, Object> result = TimeWindowTool.analyzeTimeWindow(
                "2026-03-17T02:00:00Z", // 2 AM - unusual
                "9,10,11,12,13,14,15,16,17,18",
                "8,9,10,11,12,13,14,15,16,17,18,19,20");
        
        assertNotNull(result.get("hour_deviation"));
        double deviation = ((Number) result.get("hour_deviation")).doubleValue();
        assertTrue(deviation >= 0.0 && deviation <= 1.0);
    }

    @Test
    @DisplayName("Should normalize signal to 0-1 range")
    void testNormalizedSignal() {
        Map<String, Object> result = TimeWindowTool.analyzeTimeWindow(
                "2026-03-17T03:00:00Z",
                "9,10,11,12,13,14,15,16,17,18",
                "8,9,10,11,12,13,14,15,16,17,18,19,20");
        
        double signal = (Double) result.get("normalized_signal");
        assertTrue(signal >= 0.0 && signal <= 1.0);
    }

    @Test
    @DisplayName("Should include reasoning")
    void testIncludesReasoning() {
        Map<String, Object> result = TimeWindowTool.analyzeTimeWindow(
                "2026-03-17T03:00:00Z",
                "9,10,11,12,13,14,15,16,17,18",
                "8,9,10,11,12,13,14,15,16,17,18,19,20");
        
        assertNotNull(result.get("reasoning"));
    }

    @Test
    @DisplayName("Should create FunctionTool wrapper")
    void testCreateFunctionTool() {
        FunctionTool tool = TimeWindowTool.create();
        assertNotNull(tool);
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = TimeWindowTool.analyzeTimeWindow(
                "2026-03-17T10:00:00Z",
                "9,10,11,12,13,14,15,16,17,18",
                "8,9,10,11,12,13,14,15,16,17,18,19,20");
        
        assertTrue(result.containsKey("transaction_hour"));
        assertTrue(result.containsKey("normalized_signal"));
        assertTrue(result.containsKey("flag_raised"));
    }
}

