package com.ing.sentinel.agent.tools.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FreezeTransactionTool.
 * Tests tool execution, result structure, and idempotency.
 */
@DisplayName("FreezeTransactionTool Tests")
class FreezeTransactionToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should freeze transaction successfully")
    void testFreezeTransactionSuccess() {
        Map<String, Object> result = FreezeTransactionTool.freezeTransaction(
                "TX-12345", "Suspicious activity detected");
        
        assertNotNull(result);
        assertEquals("freeze_transaction", result.get("action"));
        assertEquals("TX-12345", result.get("transaction_id"));
        assertEquals("SUCCESS", result.get("status"));
    }

    @Test
    @DisplayName("Should include timestamp in result")
    void testResultIncludesTimestamp() {
        Map<String, Object> result = FreezeTransactionTool.freezeTransaction(
                "TX-12345", "Test reason");
        
        assertNotNull(result.get("timestamp_utc"));
        String timestamp = (String) result.get("timestamp_utc");
        assertThat(timestamp).contains("T");
    }

    @Test
    @DisplayName("Should include success message")
    void testSuccessMessage() {
        Map<String, Object> result = FreezeTransactionTool.freezeTransaction(
                "TX-12345", "Test reason");
        
        assertEquals("Transaction frozen successfully", result.get("message"));
    }

    @Test
    @DisplayName("Should generate idempotency key")
    void testIdempotencyKey() {
        Map<String, Object> result = FreezeTransactionTool.freezeTransaction(
                "TX-12345", "Test reason");
        
        assertNotNull(result.get("idempotency_key"));
        String key = (String) result.get("idempotency_key");
        assertThat(key).startsWith("TX-12345-freeze-");
    }

    @Test
    @DisplayName("Should handle different transaction IDs")
    void testDifferentTransactionIds() {
        Map<String, Object> result1 = FreezeTransactionTool.freezeTransaction(
                "TX-001", "Reason 1");
        Map<String, Object> result2 = FreezeTransactionTool.freezeTransaction(
                "TX-002", "Reason 2");
        
        assertEquals("TX-001", result1.get("transaction_id"));
        assertEquals("TX-002", result2.get("transaction_id"));
    }

    @Test
    @DisplayName("Should handle different reasons")
    void testDifferentReasons() {
        Map<String, Object> result = FreezeTransactionTool.freezeTransaction(
                "TX-12345", "High risk score detected");
        
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
    }

    @Test
    @DisplayName("Should be idempotent with same transaction ID")
    void testIdempotency() {
        Map<String, Object> result1 = FreezeTransactionTool.freezeTransaction(
                "TX-SAME", "First call");
        Map<String, Object> result2 = FreezeTransactionTool.freezeTransaction(
                "TX-SAME", "Second call");
        
        assertEquals("SUCCESS", result1.get("status"));
        assertEquals("SUCCESS", result2.get("status"));
    }

    @Test
    @DisplayName("Should return map with all required fields")
    void testRequiredFields() {
        Map<String, Object> result = FreezeTransactionTool.freezeTransaction(
                "TX-12345", "Test reason");
        
        assertTrue(result.containsKey("action"));
        assertTrue(result.containsKey("transaction_id"));
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("message"));
        assertTrue(result.containsKey("timestamp_utc"));
        assertTrue(result.containsKey("idempotency_key"));
    }

    @Test
    @DisplayName("Should handle empty reason")
    void testEmptyReason() {
        Map<String, Object> result = FreezeTransactionTool.freezeTransaction(
                "TX-12345", "");
        
        assertEquals("SUCCESS", result.get("status"));
    }

    @Test
    @DisplayName("Should handle null reason gracefully")
    void testNullReason() {
        Map<String, Object> result = FreezeTransactionTool.freezeTransaction(
                "TX-12345", null);
        
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
    }

    @Test
    @DisplayName("Should log transaction freeze")
    void testLogging() {
        assertDoesNotThrow(() -> {
            FreezeTransactionTool.freezeTransaction("TX-LOG", "Test");
        });
    }
}

