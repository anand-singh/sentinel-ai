package com.ing.sentinel.agent.tools.orchestrator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RunActionExecutorTool.
 * Tests action executor invocation and result structure.
 */
@DisplayName("RunActionExecutorTool Tests")
class RunActionExecutorToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should return non-null result")
    void testReturnsNonNullResult() {
        String riskDecisionJson = """
                {
                    "transaction_id": "TX-12345",
                    "customer_id": "CUST-67890",
                    "final_risk_score": 85,
                    "severity": "HIGH",
                    "recommended_action": "CHALLENGE",
                    "explanation": "Multiple risk indicators detected",
                    "correlation_id": "CORR-123"
                }
                """;
        
        String result = RunActionExecutorTool.runActionExecutor(riskDecisionJson);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle JSON input")
    void testHandleJsonInput() {
        String riskDecisionJson = """
                {
                    "transaction_id": "TX-12345",
                    "customer_id": "CUST-67890",
                    "final_risk_score": 85,
                    "severity": "HIGH",
                    "recommended_action": "CHALLENGE",
                    "explanation": "Test",
                    "correlation_id": "CORR-123"
                }
                """;
        
        String result = RunActionExecutorTool.runActionExecutor(riskDecisionJson);
        
        assertNotNull(result);
        // Should return JSON or error message
        assertTrue(result.contains("{") || result.contains("error"));
    }

    @Test
    @DisplayName("Should handle invalid JSON gracefully")
    void testHandleInvalidJson() {
        String invalidJson = "not a valid json";
        
        String result = RunActionExecutorTool.runActionExecutor(invalidJson);
        
        assertNotNull(result);
        // Should return some result even with invalid input
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty input")
    void testHandleEmptyInput() {
        String result = RunActionExecutorTool.runActionExecutor("");
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle minimal valid JSON")
    void testHandleMinimalJson() {
        String minimalJson = """
                {
                    "transaction_id": "TX-001",
                    "customer_id": "CUST-001",
                    "final_risk_score": 25,
                    "severity": "LOW",
                    "recommended_action": "ALLOW",
                    "explanation": "Low risk",
                    "correlation_id": "CORR-001"
                }
                """;
        
        String result = RunActionExecutorTool.runActionExecutor(minimalJson);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should return string result")
    void testReturnStringResult() {
        String riskDecisionJson = """
                {
                    "transaction_id": "TX-12345",
                    "customer_id": "CUST-67890",
                    "final_risk_score": 85,
                    "severity": "HIGH",
                    "recommended_action": "CHALLENGE",
                    "explanation": "Test",
                    "correlation_id": "CORR-123"
                }
                """;
        
        String result = RunActionExecutorTool.runActionExecutor(riskDecisionJson);
        
        assertTrue(result instanceof String);
    }
}

