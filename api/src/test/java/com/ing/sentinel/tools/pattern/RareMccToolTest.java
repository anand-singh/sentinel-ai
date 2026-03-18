package com.ing.sentinel.tools.pattern;

import com.google.adk.tools.FunctionTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RareMccTool.
 * Tests merchant category rarity detection.
 */
@DisplayName("RareMccTool Tests")
class RareMccToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should detect rare MCC")
    void testDetectRareMcc() {
        Map<String, Object> result = RareMccTool.analyzeRareMcc(
                "GAMBLING", 1, 100, 0.02);
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("flag_raised"));
        assertEquals("RARE_MCC", result.get("flag"));
    }

    @Test
    @DisplayName("Should not flag common MCC")
    void testCommonMcc() {
        Map<String, Object> result = RareMccTool.analyzeRareMcc(
                "GROCERY", 50, 100, 0.15);
        
        assertFalse((Boolean) result.get("flag_raised"));
    }

    @Test
    @DisplayName("Should include rarity score")
    void testRarityScore() {
        Map<String, Object> result = RareMccTool.analyzeRareMcc(
                "ELECTRONICS", 1, 100, 0.02);
        
        assertNotNull(result.get("rarity_score"));
        double rarity = ((Number) result.get("rarity_score")).doubleValue();
        assertTrue(rarity >= 0.0 && rarity <= 1.0);
    }

    @Test
    @DisplayName("Should normalize signal to 0-1 range")
    void testNormalizedSignal() {
        Map<String, Object> result = RareMccTool.analyzeRareMcc(
                "JEWELRY", 1, 100, 0.05);
        
        double signal = (Double) result.get("normalized_signal");
        assertTrue(signal >= 0.0 && signal <= 1.0);
    }

    @Test
    @DisplayName("Should include MCC code in result")
    void testIncludesMccCode() {
        Map<String, Object> result = RareMccTool.analyzeRareMcc(
                "GAMBLING", 1, 100, 0.02);
        
        assertEquals("GAMBLING", result.get("merchant_category"));
    }

    @Test
    @DisplayName("Should include reasoning")
    void testIncludesReasoning() {
        Map<String, Object> result = RareMccTool.analyzeRareMcc(
                "GAMBLING", 1, 100, 0.02);
        
        assertNotNull(result.get("reasoning"));
    }

    @Test
    @DisplayName("Should create FunctionTool wrapper")
    void testCreateFunctionTool() {
        FunctionTool tool = RareMccTool.create();
        assertNotNull(tool);
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = RareMccTool.analyzeRareMcc(
                "ELECTRONICS", 2, 100, 0.05);
        
        assertTrue(result.containsKey("merchant_category"));
        assertTrue(result.containsKey("normalized_signal"));
        assertTrue(result.containsKey("flag_raised"));
    }
}

