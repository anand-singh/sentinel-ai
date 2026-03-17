package com.ing.sentinel.tools.pattern;

import com.google.adk.tools.FunctionTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AmountSpikeTool.
 * Tests z-score calculation, normalization, and flag detection.
 */
@DisplayName("AmountSpikeTool Tests")
class AmountSpikeToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should detect amount spike above threshold")
    void testDetectAmountSpike() {
        Map<String, Object> result = AmountSpikeTool.analyzeAmountSpike(
                5000.0, 1000.0, 500.0);
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("flag_raised"));
        assertEquals("AMOUNT_SPIKE", result.get("flag"));
    }

    @Test
    @DisplayName("Should not flag normal amounts")
    void testNormalAmount() {
        Map<String, Object> result = AmountSpikeTool.analyzeAmountSpike(
                1100.0, 1000.0, 500.0);
        
        assertFalse((Boolean) result.get("flag_raised"));
        assertNull(result.get("flag"));
    }

    @Test
    @DisplayName("Should calculate z-score correctly")
    void testZScoreCalculation() {
        Map<String, Object> result = AmountSpikeTool.analyzeAmountSpike(
                1500.0, 1000.0, 250.0); // z = (1500-1000)/250 = 2.0
        
        double zScore = (Double) result.get("z_score");
        assertEquals(2.0, zScore, 0.01);
    }

    @Test
    @DisplayName("Should normalize signal to 0-1 range")
    void testNormalizedSignal() {
        Map<String, Object> result = AmountSpikeTool.analyzeAmountSpike(
                2000.0, 1000.0, 500.0);
        
        double normalizedSignal = (Double) result.get("normalized_signal");
        assertTrue(normalizedSignal >= 0.0 && normalizedSignal <= 1.0);
    }

    @Test
    @DisplayName("Should include threshold in result")
    void testIncludesThreshold() {
        Map<String, Object> result = AmountSpikeTool.analyzeAmountSpike(
                1000.0, 1000.0, 500.0);
        
        assertEquals(2.0, result.get("threshold"));
    }

    @Test
    @DisplayName("Should include reasoning")
    void testIncludesReasoning() {
        Map<String, Object> result = AmountSpikeTool.analyzeAmountSpike(
                3000.0, 1000.0, 500.0);
        
        assertNotNull(result.get("reasoning"));
        String reasoning = (String) result.get("reasoning");
        assertThat(reasoning).containsAnyOf("higher", "exceeds", "threshold");
    }

    @Test
    @DisplayName("Should handle zero standard deviation")
    void testZeroStdDev() {
        Map<String, Object> result = AmountSpikeTool.analyzeAmountSpike(
                1500.0, 1000.0, 0.0);
        
        assertNotNull(result);
        assertEquals("SUCCESS", result.getOrDefault("status", "SUCCESS"));
    }

    @Test
    @DisplayName("Should handle negative z-score")
    void testNegativeZScore() {
        Map<String, Object> result = AmountSpikeTool.analyzeAmountSpike(
                500.0, 1000.0, 200.0); // z = -2.5
        
        double zScore = (Double) result.get("z_score");
        assertTrue(zScore < 0);
        assertFalse((Boolean) result.get("flag_raised"));
    }

    @Test
    @DisplayName("Should round z-score to 2 decimals")
    void testZScoreRounding() {
        Map<String, Object> result = AmountSpikeTool.analyzeAmountSpike(
                1234.56, 1000.0, 100.0);
        
        double zScore = (Double) result.get("z_score");
        String zScoreStr = String.valueOf(zScore);
        int decimalPlaces = zScoreStr.indexOf('.') >= 0 
                ? zScoreStr.length() - zScoreStr.indexOf('.') - 1 : 0;
        assertTrue(decimalPlaces <= 2);
    }

    @Test
    @DisplayName("Should round normalized signal to 3 decimals")
    void testNormalizedSignalRounding() {
        Map<String, Object> result = AmountSpikeTool.analyzeAmountSpike(
                1500.0, 1000.0, 200.0);
        
        double signal = (Double) result.get("normalized_signal");
        String signalStr = String.valueOf(signal);
        int decimalPlaces = signalStr.indexOf('.') >= 0 
                ? signalStr.length() - signalStr.indexOf('.') - 1 : 0;
        assertTrue(decimalPlaces <= 3);
    }

    @Test
    @DisplayName("Should create FunctionTool wrapper")
    void testCreateFunctionTool() {
        FunctionTool tool = AmountSpikeTool.create();
        assertNotNull(tool);
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = AmountSpikeTool.analyzeAmountSpike(
                1500.0, 1000.0, 500.0);
        
        assertTrue(result.containsKey("z_score"));
        assertTrue(result.containsKey("normalized_signal"));
        assertTrue(result.containsKey("flag"));
        assertTrue(result.containsKey("flag_raised"));
        assertTrue(result.containsKey("threshold"));
        assertTrue(result.containsKey("reasoning"));
    }
}

