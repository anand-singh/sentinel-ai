package com.ing.sentinel.tools.pattern;

import com.google.adk.tools.FunctionTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GeoDistanceTool.
 * Tests geographic distance calculation and anomaly detection.
 */
@DisplayName("GeoDistanceTool Tests")
class GeoDistanceToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should detect geo mismatch")
    void testDetectGeoMismatch() {
        Map<String, Object> result = GeoDistanceTool.analyzeGeoDistance(
                40.7128, -74.0060, // New York
                51.5074, -0.1278,  // London
                "2026-03-17T10:00:00Z",
                "2026-03-17T09:00:00Z");

        assertNotNull(result);
        assertTrue((Boolean) result.get("flag_raised"));
        assertEquals("GEO_MISMATCH", result.get("flag"));
    }

    @Test
    @DisplayName("Should not flag close distances")
    void testNormalDistance() {
        Map<String, Object> result = GeoDistanceTool.analyzeGeoDistance(
                40.7128, -74.0060, // New York
                40.7589, -73.9851, // Also NYC
                "2026-03-17T10:00:00Z",
                "2026-03-17T09:00:00Z");

        assertFalse((Boolean) result.get("flag_raised"));
    }

    @Test
    @DisplayName("Should calculate distance in kilometers")
    void testDistanceCalculation() {
        Map<String, Object> result = GeoDistanceTool.analyzeGeoDistance(
                40.7128, -74.0060,
                51.5074, -0.1278,
                "2026-03-17T10:00:00Z",
                "2026-03-17T09:00:00Z");

        double distance = (Double) result.get("distance_km");
        assertTrue(distance > 5000, "NY to London should be > 5000 km");
    }

    @Test
    @DisplayName("Should calculate speed in km/h")
    void testSpeedCalculation() {
        Map<String, Object> result = GeoDistanceTool.analyzeGeoDistance(
                40.7128, -74.0060,
                51.5074, -0.1278,
                "2026-03-17T10:00:00Z",
                "2026-03-17T09:00:00Z");

        assertNotNull(result.get("required_speed_kmh"));
    }

    @Test
    @DisplayName("Should include normalized signal")
    void testNormalizedSignal() {
        Map<String, Object> result = GeoDistanceTool.analyzeGeoDistance(
                40.7128, -74.0060,
                51.5074, -0.1278,
                "2026-03-17T10:00:00Z",
                "2026-03-17T09:00:00Z");

        double signal = (Double) result.get("normalized_signal");
        assertTrue(signal >= 0.0 && signal <= 1.0);
    }

    @Test
    @DisplayName("Should include reasoning")
    void testIncludesReasoning() {
        Map<String, Object> result = GeoDistanceTool.analyzeGeoDistance(
                40.7128, -74.0060,
                51.5074, -0.1278,
                "2026-03-17T10:00:00Z",
                "2026-03-17T09:00:00Z");

        assertNotNull(result.get("reasoning"));
    }

    @Test
    @DisplayName("Should create FunctionTool wrapper")
    void testCreateFunctionTool() {
        FunctionTool tool = GeoDistanceTool.create();
        assertNotNull(tool);
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = GeoDistanceTool.analyzeGeoDistance(
                40.0, -74.0, 41.0, -73.0,
                "2026-03-17T10:00:00Z",
                "2026-03-17T09:00:00Z");

        assertTrue(result.containsKey("distance_km"));
        assertTrue(result.containsKey("normalized_signal"));
        assertTrue(result.containsKey("flag_raised"));
    }
}

