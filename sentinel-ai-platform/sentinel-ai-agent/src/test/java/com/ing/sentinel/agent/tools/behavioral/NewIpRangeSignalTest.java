package com.ing.sentinel.agent.tools.behavioral;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NewIpRangeSignal.
 * Tests IP range novelty detection and result structure.
 */
@DisplayName("NewIpRangeSignal Tests")
class NewIpRangeSignalTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should recognize known IP range")
    void testKnownIpRange() {
        Map<String, Object> result = NewIpRangeSignal.analyzeNewIpRange(
                "192.168.1.100",
                "192.168.1.0/24,10.0.0.0/8");
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("is_in_known_range"));
        assertEquals(false, result.get("flag_raised"));
        assertNull(result.get("flag"));
    }

    @Test
    @DisplayName("Should detect new IP range")
    void testNewIpRange() {
        Map<String, Object> result = NewIpRangeSignal.analyzeNewIpRange(
                "203.0.113.45",
                "192.168.1.0/24,10.0.0.0/8");
        
        assertNotNull(result);
        assertFalse((Boolean) result.get("is_in_known_range"));
        assertTrue((Boolean) result.get("flag_raised"));
        assertEquals("NEW_IP_RANGE", result.get("flag"));
    }

    @Test
    @DisplayName("Should mask IP address")
    void testMaskIpAddress() {
        Map<String, Object> result = NewIpRangeSignal.analyzeNewIpRange(
                "192.168.1.100",
                "10.0.0.0/8");
        
        assertNotNull(result.get("ip_masked"));
        String masked = (String) result.get("ip_masked");
        assertTrue(masked.contains("*"));
    }

    @Test
    @DisplayName("Should handle empty IP range list")
    void testEmptyIpRangeList() {
        Map<String, Object> result = NewIpRangeSignal.analyzeNewIpRange(
                "192.168.1.100",
                "");
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("flag_raised"));
        assertEquals(0, result.get("known_ranges_count"));
    }

    @Test
    @DisplayName("Should include reasoning")
    void testIncludesReasoning() {
        Map<String, Object> result = NewIpRangeSignal.analyzeNewIpRange(
                "192.168.1.100",
                "192.168.1.0/24");
        
        assertNotNull(result.get("reasoning"));
        assertTrue(result.get("reasoning") instanceof String);
    }

    @Test
    @DisplayName("Should normalize signal to 0 or 1")
    void testNormalizedSignalValue() {
        Map<String, Object> result = NewIpRangeSignal.analyzeNewIpRange(
                "192.168.1.100",
                "10.0.0.0/8");
        
        double signal = ((Number) result.get("normalized_signal")).doubleValue();
        assertTrue(signal == 0.0 || signal == 1.0);
    }

    @Test
    @DisplayName("Should handle CIDR notation correctly")
    void testCidrNotation() {
        Map<String, Object> result = NewIpRangeSignal.analyzeNewIpRange(
                "192.168.1.50",
                "192.168.1.0/24");
        
        assertTrue((Boolean) result.get("is_in_known_range"));
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = NewIpRangeSignal.analyzeNewIpRange(
                "192.168.1.100",
                "192.168.1.0/24");
        
        assertTrue(result.containsKey("ip_masked"));
        assertTrue(result.containsKey("is_in_known_range"));
        assertTrue(result.containsKey("known_ranges_count"));
        assertTrue(result.containsKey("normalized_signal"));
        assertTrue(result.containsKey("flag"));
        assertTrue(result.containsKey("flag_raised"));
        assertTrue(result.containsKey("reasoning"));
    }
}

