package com.ing.sentinel.agent.tools.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotifySecurityTeamTool.
 * Tests tool execution and notification result structure.
 */
@DisplayName("NotifySecurityTeamTool Tests")
class NotifySecurityTeamToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should notify security team successfully")
    void testNotifySuccess() {
        Map<String, Object> result = NotifySecurityTeamTool.notifySecurityTeam(
                "TX-12345", "CRITICAL", "High risk transaction detected", "CORR-123");
        
        assertNotNull(result);
        assertEquals("notify_security_team", result.get("action"));
        assertEquals("SUCCESS", result.get("status"));
    }

    @Test
    @DisplayName("Should include transaction ID in result")
    void testIncludesTransactionId() {
        Map<String, Object> result = NotifySecurityTeamTool.notifySecurityTeam(
                "TX-12345", "HIGH", "Test message", "CORR-123");
        
        assertEquals("TX-12345", result.get("transaction_id"));
    }

    @Test
    @DisplayName("Should include severity in result")
    void testIncludesSeverity() {
        Map<String, Object> result = NotifySecurityTeamTool.notifySecurityTeam(
                "TX-12345", "CRITICAL", "Test message", "CORR-123");
        
        assertEquals("CRITICAL", result.get("severity"));
    }

    @Test
    @DisplayName("Should include timestamp")
    void testIncludesTimestamp() {
        Map<String, Object> result = NotifySecurityTeamTool.notifySecurityTeam(
                "TX-12345", "HIGH", "Test message", "CORR-123");
        
        assertNotNull(result.get("timestamp_utc"));
    }

    @Test
    @DisplayName("Should include success message")
    void testSuccessMessage() {
        Map<String, Object> result = NotifySecurityTeamTool.notifySecurityTeam(
                "TX-12345", "HIGH", "Test", "CORR-123");
        
        assertThat((String) result.get("message"))
                .contains("Security team notified");
    }

    @Test
    @DisplayName("Should handle all severity levels")
    void testAllSeverityLevels() {
        String[] severities = {"CRITICAL", "HIGH", "MED", "LOW"};
        
        for (String severity : severities) {
            Map<String, Object> result = NotifySecurityTeamTool.notifySecurityTeam(
                    "TX-" + severity, severity, "Test", "CORR-123");
            assertEquals("SUCCESS", result.get("status"));
        }
    }

    @Test
    @DisplayName("Should handle long notification message")
    void testLongMessage() {
        String longMessage = "A".repeat(500);
        Map<String, Object> result = NotifySecurityTeamTool.notifySecurityTeam(
                "TX-12345", "HIGH", longMessage, "CORR-123");
        
        assertEquals("SUCCESS", result.get("status"));
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = NotifySecurityTeamTool.notifySecurityTeam(
                "TX-12345", "HIGH", "Test", "CORR-123");
        
        assertTrue(result.containsKey("action"));
        assertTrue(result.containsKey("transaction_id"));
        assertTrue(result.containsKey("severity"));
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("timestamp_utc"));
    }
}

