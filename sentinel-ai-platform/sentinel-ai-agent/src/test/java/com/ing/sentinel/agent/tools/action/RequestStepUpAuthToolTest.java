package com.ing.sentinel.agent.tools.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RequestStepUpAuthTool.
 * Tests step-up authentication request and result structure.
 */
@DisplayName("RequestStepUpAuthTool Tests")
class RequestStepUpAuthToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should request step-up auth successfully")
    void testRequestSuccess() {
        Map<String, Object> result = RequestStepUpAuthTool.requestStepUpAuth(
                "CUST-123", "TX-12345", "OTP", "High risk transaction");
        
        assertNotNull(result);
        assertEquals("request_step_up_auth", result.get("action"));
        assertEquals("SUCCESS", result.get("status"));
    }

    @Test
    @DisplayName("Should include transaction ID")
    void testIncludesTransactionId() {
        Map<String, Object> result = RequestStepUpAuthTool.requestStepUpAuth(
                "CUST-123", "TX-12345", "OTP", "Test");
        
        assertEquals("TX-12345", result.get("transaction_id"));
    }

    @Test
    @DisplayName("Should include auth method")
    void testIncludesAuthMethod() {
        Map<String, Object> result = RequestStepUpAuthTool.requestStepUpAuth(
                "CUST-123", "TX-12345", "PUSH", "Test");
        
        assertEquals("PUSH", result.get("auth_method"));
    }

    @Test
    @DisplayName("Should include timestamp")
    void testIncludesTimestamp() {
        Map<String, Object> result = RequestStepUpAuthTool.requestStepUpAuth(
                "CUST-123", "TX-12345", "OTP", "Test");
        
        assertNotNull(result.get("timestamp_utc"));
    }

    @Test
    @DisplayName("Should handle all auth methods")
    void testAllAuthMethods() {
        String[] methods = {"OTP", "PUSH", "BIOMETRIC"};
        
        for (String method : methods) {
            Map<String, Object> result = RequestStepUpAuthTool.requestStepUpAuth(
                    "CUST-123", "TX-" + method, method, "Test");
            assertEquals("SUCCESS", result.get("status"));
            assertEquals(method, result.get("auth_method"));
        }
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = RequestStepUpAuthTool.requestStepUpAuth(
                "CUST-123", "TX-12345", "OTP", "Test");
        
        assertTrue(result.containsKey("action"));
        assertTrue(result.containsKey("transaction_id"));
        assertTrue(result.containsKey("auth_method"));
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("timestamp_utc"));
    }
}

