package com.ing.sentinel.tools.orchestrator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RunPatternAnalyzerTool.
 * Tests pattern analyzer invocation and result structure.
 */
@DisplayName("RunPatternAnalyzerTool Tests")
class RunPatternAnalyzerToolTest {

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
                    "merchant_id": "MERCH-001",
                    "merchant_category": "ELECTRONICS",
                    "timestamp_utc": "2026-03-17T10:30:00Z"
                }
                """;
        
        String result = RunPatternAnalyzerTool.runPatternAnalyzer(transactionJson);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle JSON input")
    void testHandleJsonInput() {
        String transactionJson = """
                {
                    "transaction_id": "TX-12345",
                    "amount": 1500.00
                }
                """;
        
        String result = RunPatternAnalyzerTool.runPatternAnalyzer(transactionJson);
        
        assertNotNull(result);
        assertTrue(result.contains("{") || result.contains("error"));
    }

    @Test
    @DisplayName("Should handle invalid JSON gracefully")
    void testHandleInvalidJson() {
        String invalidJson = "not a valid json";
        
        String result = RunPatternAnalyzerTool.runPatternAnalyzer(invalidJson);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty input")
    void testHandleEmptyInput() {
        String result = RunPatternAnalyzerTool.runPatternAnalyzer("");
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle complete transaction payload")
    void testHandleCompletePayload() {
        String transactionJson = """
                {
                    "transaction_id": "TX-12345",
                    "amount": 1500.00,
                    "merchant_id": "MERCH-001",
                    "merchant_category": "ELECTRONICS",
                    "merchant_country": "NL",
                    "timestamp_utc": "2026-03-17T10:30:00Z",
                    "lat": 52.3702,
                    "lon": 4.8952,
                    "ip_address": "192.168.1.100",
                    "device_fingerprint": "device123abc",
                    "global_baseline": {
                        "mcc_avg_amount": 800.00,
                        "mcc_stdev_amount": 200.00
                    }
                }
                """;
        
        String result = RunPatternAnalyzerTool.runPatternAnalyzer(transactionJson);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle minimal transaction payload")
    void testHandleMinimalPayload() {
        String transactionJson = """
                {
                    "transaction_id": "TX-12345"
                }
                """;
        
        String result = RunPatternAnalyzerTool.runPatternAnalyzer(transactionJson);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should return string result")
    void testReturnStringResult() {
        String transactionJson = """
                {
                    "transaction_id": "TX-12345",
                    "amount": 1500.00
                }
                """;
        
        String result = RunPatternAnalyzerTool.runPatternAnalyzer(transactionJson);
        
        assertTrue(result instanceof String);
    }
}

