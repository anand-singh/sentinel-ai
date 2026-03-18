package com.ing.sentinel.agent.tools.pattern;

import com.google.adk.tools.FunctionTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for VelocityTool.
 * Tests transaction velocity calculation and anomaly detection.
 */
@DisplayName("VelocityTool Tests")
class VelocityToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should detect high velocity")
    void testDetectHighVelocity() {
        Map<String, Object> result = VelocityTool.analyzeVelocity(
                15, 30, 3.5, 5.0);
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("flag_raised"));
        assertEquals("UNUSUAL_VELOCITY", result.get("flag"));
    }

    @Test
    @DisplayName("Should not flag normal velocity")
    void testNormalVelocity() {
        Map<String, Object> result = VelocityTool.analyzeVelocity(
                2, 5, 3.5, 10.0);
        
        assertFalse((Boolean) result.get("flag_raised"));
    }

    @Test
    @DisplayName("Should calculate velocity ratio")
    void testVelocityRatio() {
        Map<String, Object> result = VelocityTool.analyzeVelocity(
                5, 15, 2.5, 10.0);
        
        assertNotNull(result.get("ratio_1h"));
        assertNotNull(result.get("ratio_24h"));
    }

    @Test
    @DisplayName("Should normalize signal to 0-1 range")
    void testNormalizedSignal() {
        Map<String, Object> result = VelocityTool.analyzeVelocity(
                5, 15, 3.0, 12.0);
        
        double signal = (Double) result.get("normalized_signal");
        assertTrue(signal >= 0.0 && signal <= 1.0);
    }

    @Test
    @DisplayName("Should include reasoning")
    void testIncludesReasoning() {
        Map<String, Object> result = VelocityTool.analyzeVelocity(
                5, 15, 3.0, 12.0);
        
        assertNotNull(result.get("reasoning"));
    }

    @Test
    @DisplayName("Should create FunctionTool wrapper")
    void testCreateFunctionTool() {
        FunctionTool tool = VelocityTool.create();
        assertNotNull(tool);
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = VelocityTool.analyzeVelocity(
                3, 10, 2.0, 8.0);
        
        assertTrue(result.containsKey("velocity_1h"));
        assertTrue(result.containsKey("velocity_24h"));
        assertTrue(result.containsKey("ratio_1h"));
        assertTrue(result.containsKey("normalized_signal"));
        assertTrue(result.containsKey("flag_raised"));
    }
}

