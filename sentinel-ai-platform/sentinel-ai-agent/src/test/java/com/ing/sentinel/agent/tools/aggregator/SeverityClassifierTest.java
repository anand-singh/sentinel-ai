package com.ing.sentinel.agent.tools.aggregator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SeverityClassifier.
 * Tests severity classification based on risk scores.
 */
@DisplayName("SeverityClassifier Tests")
class SeverityClassifierTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should classify as CRITICAL")
    void testClassifyCritical() {
        Map<String, Object> result = SeverityClassifier.classifySeverity(95);
        
        assertNotNull(result);
        assertEquals("CRITICAL", result.get("severity"));
    }

    @Test
    @DisplayName("Should classify as HIGH")
    void testClassifyHigh() {
        Map<String, Object> result = SeverityClassifier.classifySeverity(75);
        
        assertEquals("HIGH", result.get("severity"));
    }

    @Test
    @DisplayName("Should classify as MED")
    void testClassifyMedium() {
        Map<String, Object> result = SeverityClassifier.classifySeverity(55);
        
        assertEquals("MED", result.get("severity"));
    }

    @Test
    @DisplayName("Should classify as LOW")
    void testClassifyLow() {
        Map<String, Object> result = SeverityClassifier.classifySeverity(25);
        
        assertEquals("LOW", result.get("severity"));
    }

    @Test
    @DisplayName("Should include risk score in result")
    void testIncludesRiskScore() {
        Map<String, Object> result = SeverityClassifier.classifySeverity(80);
        
        assertEquals(80, result.get("final_risk_score"));
    }

    @Test
    @DisplayName("Should include recommended action")
    void testIncludesRecommendedAction() {
        Map<String, Object> result = SeverityClassifier.classifySeverity(50);
        
        assertNotNull(result.get("recommended_action"));
    }

    @Test
    @DisplayName("Should handle boundary values")
    void testBoundaryValues() {
        Map<String, Object> r1 = SeverityClassifier.classifySeverity(80);
        Map<String, Object> r2 = SeverityClassifier.classifySeverity(60);
        Map<String, Object> r3 = SeverityClassifier.classifySeverity(35);
        
        assertNotNull(r1.get("severity"));
        assertNotNull(r2.get("severity"));
        assertNotNull(r3.get("severity"));
    }

    @Test
    @DisplayName("Should handle edge cases")
    void testEdgeCases() {
        Map<String, Object> r1 = SeverityClassifier.classifySeverity(0);
        Map<String, Object> r2 = SeverityClassifier.classifySeverity(100);
        
        assertEquals("LOW", r1.get("severity"));
        assertEquals("CRITICAL", r2.get("severity"));
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = SeverityClassifier.classifySeverity(70);
        
        assertTrue(result.containsKey("severity"));
        assertTrue(result.containsKey("final_risk_score"));
        assertTrue(result.containsKey("recommended_action"));
    }
}

