package com.ing.sentinel.tools.orchestrator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RunBehavioralRiskTool.
 * Tests behavioral risk detector invocation and result structure.
 */
@DisplayName("RunBehavioralRiskTool Tests")
class RunBehavioralRiskToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should return non-null result")
    void testReturnsNonNullResult() {
        String transactionJson = """
                {
                    "transaction_id": "TX-12345",
                    "amount": 1500.00,
                    "customer_profile_snapshot": {
                        "avg_amount": 500.00,
                        "usual_devices": ["device123"],
                        "usual_hours": "9-17"
                    }
                }
                """;
        
        String result = RunBehavioralRiskTool.runBehavioralRisk(transactionJson);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle JSON input")
    void testHandleJsonInput() {
        String transactionJson = """
                {
                    "transaction_id": "TX-12345",
                    "amount": 1500.00,
                    "customer_profile_snapshot": {}
                }
                """;
        
        String result = RunBehavioralRiskTool.runBehavioralRisk(transactionJson);
        
        assertNotNull(result);
        assertTrue(result.contains("{") || result.contains("error"));
    }

    @Test
    @DisplayName("Should handle invalid JSON gracefully")
    void testHandleInvalidJson() {
        String invalidJson = "not a valid json";
        
        String result = RunBehavioralRiskTool.runBehavioralRisk(invalidJson);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty input")
    void testHandleEmptyInput() {
        String result = RunBehavioralRiskTool.runBehavioralRisk("");
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle complete customer profile")
    void testHandleCompleteProfile() {
        String transactionJson = """
                {
                    "transaction_id": "TX-12345",
                    "amount": 1500.00,
                    "merchant_id": "MERCH-999",
                    "device_fingerprint": "device-new",
                    "customer_profile_snapshot": {
                        "avg_amount": 500.00,
                        "stdev_amount": 100.00,
                        "usual_devices": ["device123", "device456"],
                        "usual_merchants": ["MERCH-001", "MERCH-002"],
                        "sleep_hours_start": 23,
                        "sleep_hours_end": 6,
                        "active_hours_start": 8,
                        "active_hours_end": 18
                    }
                }
                """;
        
        String result = RunBehavioralRiskTool.runBehavioralRisk(transactionJson);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should return string result")
    void testReturnStringResult() {
        String transactionJson = """
                {
                    "transaction_id": "TX-12345",
                    "customer_profile_snapshot": {}
                }
                """;
        
        String result = RunBehavioralRiskTool.runBehavioralRisk(transactionJson);
        
        assertTrue(result instanceof String);
    }
}

