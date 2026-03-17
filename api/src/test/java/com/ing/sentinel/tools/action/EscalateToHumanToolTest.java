package com.ing.sentinel.tools.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EscalateToHumanTool.
 * Tests escalation to human review and result structure.
 */
@DisplayName("EscalateToHumanTool Tests")
class EscalateToHumanToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should escalate to human successfully")
    void testEscalateSuccess() {
        Map<String, Object> result = EscalateToHumanTool.escalateToHuman(
                "TX-12345", "CUST-123", "URGENT", "Complex case requiring expert review", "FRAUD_REVIEW");
        
        assertNotNull(result);
        assertEquals("escalate_to_human", result.get("action"));
        assertEquals("SUCCESS", result.get("status"));
    }

    @Test
    @DisplayName("Should include transaction ID")
    void testIncludesTransactionId() {
        Map<String, Object> result = EscalateToHumanTool.escalateToHuman(
                "TX-12345", "CUST-123", "URGENT", "Test", "FRAUD_REVIEW");
        
        assertEquals("TX-12345", result.get("transaction_id"));
    }

    @Test
    @DisplayName("Should include priority")
    void testIncludesPriority() {
        Map<String, Object> result = EscalateToHumanTool.escalateToHuman(
                "TX-12345", "CUST-123", "MED", "Test", "FRAUD_REVIEW");
        
        assertEquals("MED", result.get("priority"));
    }

    @Test
    @DisplayName("Should include timestamp")
    void testIncludesTimestamp() {
        Map<String, Object> result = EscalateToHumanTool.escalateToHuman(
                "TX-12345", "CUST-123", "URGENT", "Test", "FRAUD_REVIEW");
        
        assertNotNull(result.get("timestamp_utc"));
    }

    @Test
    @DisplayName("Should handle all priority levels")
    void testAllPriorityLevels() {
        String[] priorities = {"URGENT", "HIGH", "MED", "LOW"};
        
        for (String priority : priorities) {
            Map<String, Object> result = EscalateToHumanTool.escalateToHuman(
                    "TX-" + priority, "CUST-123", priority, "Test", "FRAUD_REVIEW");
            assertEquals("SUCCESS", result.get("status"));
            assertEquals(priority, result.get("priority"));
        }
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = EscalateToHumanTool.escalateToHuman(
                "TX-12345", "CUST-123", "URGENT", "Test", "FRAUD_REVIEW");
        
        assertTrue(result.containsKey("action"));
        assertTrue(result.containsKey("transaction_id"));
        assertTrue(result.containsKey("priority"));
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("timestamp_utc"));
    }
}

