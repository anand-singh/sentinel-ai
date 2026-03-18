package com.ing.sentinel.agent.tools.behavioral;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for GeoDeviationSignal.
 * Tests geographic deviation detection and result structure.
 */
@DisplayName("GeoDeviationSignal Tests")
class GeoDeviationSignalTest {

    private String currentTime;
    private String pastTime;

    @BeforeEach
    void setUp() {
        currentTime = Instant.now().toString();
        pastTime = Instant.now().minusSeconds(3600).toString();
    }

    @Test
    @DisplayName("Should analyze normal geo deviation")
    void testNormalGeoDeviation() {
        Map<String, Object> result = GeoDeviationSignal.analyzeGeoDeviation(
                52.3702, 4.8952,  // Amsterdam
                52.3676, 4.9041,  // Nearby in Amsterdam
                currentTime, pastTime,
                "NL", "NL");
        
        assertNotNull(result);
        assertNotNull(result.get("geo_distance_km"));
    }

    @Test
    @DisplayName("Should detect large distance deviation")
    void testLargeDistanceDeviation() {
        Map<String, Object> result = GeoDeviationSignal.analyzeGeoDeviation(
                52.3702, 4.8952,  // Amsterdam
                40.7128, -74.0060, // New York
                currentTime, pastTime,
                "NL,BE", "US");
        
        assertNotNull(result);
        double distance = ((Number) result.get("geo_distance_km")).doubleValue();
        assertThat(distance).isGreaterThan(1000);
    }

    @Test
    @DisplayName("Should detect impossible travel")
    void testImpossibleTravel() {
        String veryRecentTime = Instant.now().minusSeconds(60).toString();
        
        Map<String, Object> result = GeoDeviationSignal.analyzeGeoDeviation(
                52.3702, 4.8952,  // Amsterdam
                40.7128, -74.0060, // New York
                currentTime, veryRecentTime,
                "NL", "US");
        
        assertNotNull(result.get("impossible_travel"));
        assertNotNull(result.get("required_speed_kmh"));
    }

    @Test
    @DisplayName("Should check home country status")
    void testHomeCountryCheck() {
        Map<String, Object> result = GeoDeviationSignal.analyzeGeoDeviation(
                52.3702, 4.8952,
                52.5200, 13.4050, // Berlin
                currentTime, pastTime,
                "NL,DE", "DE");
        
        assertNotNull(result.get("is_home_country"));
        assertTrue((Boolean) result.get("is_home_country"));
    }

    @Test
    @DisplayName("Should include reasoning")
    void testIncludesReasoning() {
        Map<String, Object> result = GeoDeviationSignal.analyzeGeoDeviation(
                52.3702, 4.8952,
                52.3676, 4.9041,
                currentTime, pastTime,
                "NL", "NL");
        
        assertNotNull(result.get("reasoning"));
        assertTrue(result.get("reasoning") instanceof String);
    }

    @Test
    @DisplayName("Should normalize signal to range 0-1")
    void testNormalizedSignalRange() {
        Map<String, Object> result = GeoDeviationSignal.analyzeGeoDeviation(
                52.3702, 4.8952,
                52.5200, 13.4050,
                currentTime, pastTime,
                "NL", "DE");
        
        double signal = ((Number) result.get("normalized_signal")).doubleValue();
        assertThat(signal).isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = GeoDeviationSignal.analyzeGeoDeviation(
                52.3702, 4.8952,
                52.3676, 4.9041,
                currentTime, pastTime,
                "NL", "NL");
        
        assertTrue(result.containsKey("geo_distance_km"));
        assertTrue(result.containsKey("required_speed_kmh"));
        assertTrue(result.containsKey("impossible_travel"));
        assertTrue(result.containsKey("is_home_country"));
        assertTrue(result.containsKey("normalized_signal"));
        assertTrue(result.containsKey("flag"));
        assertTrue(result.containsKey("flag_raised"));
        assertTrue(result.containsKey("reasoning"));
    }
}

