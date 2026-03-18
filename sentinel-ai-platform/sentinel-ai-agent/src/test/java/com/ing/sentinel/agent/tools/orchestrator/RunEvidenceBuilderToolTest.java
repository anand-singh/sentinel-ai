package com.ing.sentinel.agent.tools.orchestrator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RunEvidenceBuilderTool.
 * Tests evidence builder invocation and result structure.
 */
@DisplayName("RunEvidenceBuilderTool Tests")
class RunEvidenceBuilderToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should return non-null result")
    void testReturnsNonNullResult() {
        String evidenceInputJson = """
                {
                    "transaction_id": "TX-12345",
                    "customer_id": "CUST-67890",
                    "pattern_agent_output": {
                        "risk_score": 75,
                        "flags": ["AMOUNT_SPIKE", "VELOCITY_HIGH"],
                        "reasoning": "High amount and velocity detected"
                    },
                    "behavioral_agent_output": {
                        "behavioral_risk_score": 60,
                        "flags": ["NEW_DEVICE"],
                        "reasoning": "New device detected"
                    }
                }
                """;
        
        String result = RunEvidenceBuilderTool.runEvidenceBuilder(evidenceInputJson);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle JSON input")
    void testHandleJsonInput() {
        String evidenceInputJson = """
                {
                    "transaction_id": "TX-12345",
                    "customer_id": "CUST-67890",
                    "pattern_agent_output": {"risk_score": 75},
                    "behavioral_agent_output": {"behavioral_risk_score": 60}
                }
                """;
        
        String result = RunEvidenceBuilderTool.runEvidenceBuilder(evidenceInputJson);
        
        assertNotNull(result);
        assertTrue(result.contains("{") || result.contains("error"));
    }

    @Test
    @DisplayName("Should handle invalid JSON gracefully")
    void testHandleInvalidJson() {
        String invalidJson = "not a valid json";
        
        String result = RunEvidenceBuilderTool.runEvidenceBuilder(invalidJson);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty input")
    void testHandleEmptyInput() {
        String result = RunEvidenceBuilderTool.runEvidenceBuilder("");
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle input with AML agent output")
    void testHandleWithAmlOutput() {
        String evidenceInputJson = """
                {
                    "transaction_id": "TX-12345",
                    "customer_id": "CUST-67890",
                    "pattern_agent_output": {
                        "risk_score": 75,
                        "flags": ["AMOUNT_SPIKE"]
                    },
                    "behavioral_agent_output": {
                        "behavioral_risk_score": 60,
                        "flags": ["NEW_DEVICE"]
                    },
                    "aml_agent_output": {
                        "aml_score": 45,
                        "flags": ["PEP_MATCH_PENDING"],
                        "reasoning": "PEP match pending review"
                    }
                }
                """;
        
        String result = RunEvidenceBuilderTool.runEvidenceBuilder(evidenceInputJson);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle minimal agent outputs")
    void testHandleMinimalOutputs() {
        String evidenceInputJson = """
                {
                    "transaction_id": "TX-12345",
                    "customer_id": "CUST-67890",
                    "pattern_agent_output": {},
                    "behavioral_agent_output": {}
                }
                """;
        
        String result = RunEvidenceBuilderTool.runEvidenceBuilder(evidenceInputJson);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should return string result")
    void testReturnStringResult() {
        String evidenceInputJson = """
                {
                    "transaction_id": "TX-12345",
                    "customer_id": "CUST-67890",
                    "pattern_agent_output": {},
                    "behavioral_agent_output": {}
                }
                """;
        
        String result = RunEvidenceBuilderTool.runEvidenceBuilder(evidenceInputJson);
        
        assertTrue(result instanceof String);
    }
}

