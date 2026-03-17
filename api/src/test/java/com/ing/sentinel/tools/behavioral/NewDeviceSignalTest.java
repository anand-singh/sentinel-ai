package com.ing.sentinel.tools.behavioral;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NewDeviceSignal.
 * Tests device novelty detection and result structure.
 */
@DisplayName("NewDeviceSignal Tests")
class NewDeviceSignalTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should recognize known device")
    void testKnownDevice() {
        Map<String, Object> result = NewDeviceSignal.analyzeNewDevice(
                "device123abc",
                "device123abc,device456def,device789ghi");
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("is_known_device"));
        assertEquals(false, result.get("flag_raised"));
        assertNull(result.get("flag"));
    }

    @Test
    @DisplayName("Should detect new device")
    void testNewDevice() {
        Map<String, Object> result = NewDeviceSignal.analyzeNewDevice(
                "deviceXYZ999",
                "device123abc,device456def");
        
        assertNotNull(result);
        assertFalse((Boolean) result.get("is_known_device"));
        assertTrue((Boolean) result.get("flag_raised"));
        assertEquals("NEW_DEVICE", result.get("flag"));
    }

    @Test
    @DisplayName("Should mask device fingerprint")
    void testMaskDeviceFingerprint() {
        Map<String, Object> result = NewDeviceSignal.analyzeNewDevice(
                "device123456789abc",
                "device999");
        
        assertNotNull(result.get("device_fingerprint_masked"));
        String masked = (String) result.get("device_fingerprint_masked");
        assertTrue(masked.contains("****"));
    }

    @Test
    @DisplayName("Should handle empty device list")
    void testEmptyDeviceList() {
        Map<String, Object> result = NewDeviceSignal.analyzeNewDevice(
                "device123abc",
                "");
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("flag_raised"));
        assertEquals(0, result.get("known_devices_count"));
    }

    @Test
    @DisplayName("Should include reasoning")
    void testIncludesReasoning() {
        Map<String, Object> result = NewDeviceSignal.analyzeNewDevice(
                "device123abc",
                "device456def");
        
        assertNotNull(result.get("reasoning"));
        assertTrue(result.get("reasoning") instanceof String);
    }

    @Test
    @DisplayName("Should normalize signal to 0 or 1")
    void testNormalizedSignalValue() {
        Map<String, Object> result = NewDeviceSignal.analyzeNewDevice(
                "device123abc",
                "device456def");
        
        double signal = ((Number) result.get("normalized_signal")).doubleValue();
        assertTrue(signal == 0.0 || signal == 1.0);
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = NewDeviceSignal.analyzeNewDevice(
                "device123abc",
                "device456def");
        
        assertTrue(result.containsKey("device_fingerprint_masked"));
        assertTrue(result.containsKey("is_known_device"));
        assertTrue(result.containsKey("known_devices_count"));
        assertTrue(result.containsKey("normalized_signal"));
        assertTrue(result.containsKey("flag"));
        assertTrue(result.containsKey("flag_raised"));
        assertTrue(result.containsKey("reasoning"));
    }
}

