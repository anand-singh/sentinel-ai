package com.ing.sentinel.agent.tools.orchestrator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RunAggregatedScorerTool.
 * Tests aggregated scorer invocation and result structure.
 */
@DisplayName("RunAggregatedScorerTool Tests")
class RunAggregatedScorerToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should return non-null result")
    void testReturnsNonNullResult() {
        String aggregatorInputJson = """
                {
                    "transaction_id": "TX-12345",
                    "customer_id": "CUST-67890",
                    "pattern_agent": {"risk_score": 75, "flags": ["AMOUNT_SPIKE"]},
                    "behavioral_agent": {"behavioral_risk_score": 60, "flags": ["NEW_DEVICE"]},
                    "evidence_agent": {"evidence_summary": "Test summary"}
                }
                """;
        
        String result = RunAggregatedScorerTool.runAggregatedScorer(aggregatorInputJson);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle JSON input")
    void testHandleJsonInput() {
        String aggregatorInputJson = """
                {
                    "transaction_id": "TX-12345",
                    "customer_id": "CUST-67890",
                    "pattern_agent": {"risk_score": 75},
                    "behavioral_agent": {"behavioral_risk_score": 60},
                    "evidence_agent": {"evidence_summary": "Test"}
                }
                """;
        
        String result = RunAggregatedScorerTool.runAggregatedScorer(aggregatorInputJson);
        
        assertNotNull(result);
        assertTrue(result.contains("{") || result.contains("error"));
    }

    @Test
    @DisplayName("Should handle invalid JSON gracefully")
    void testHandleInvalidJson() {
        String invalidJson = "not a valid json";
        
        String result = RunAggregatedScorerTool.runAggregatedScorer(invalidJson);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty input")
    void testHandleEmptyInput() {
        String result = RunAggregatedScorerTool.runAggregatedScorer("");
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle input with all agent outputs")
    void testHandleAllAgentOutputs() {
        String fullInputJson = """
                {
                    "transaction_id": "TX-12345",
                    "customer_id": "CUST-67890",
                    "pattern_agent": {"risk_score": 75, "flags": ["AMOUNT_SPIKE", "VELOCITY_HIGH"]},
                    "behavioral_agent": {"behavioral_risk_score": 60, "flags": ["NEW_DEVICE"]},
                    "evidence_agent": {"evidence_summary": "Multiple risk indicators"},
                    "aml_agent": {"aml_score": 45, "flags": ["PEP_MATCH_PENDING"]}
                }
                """;
        
        String result = RunAggregatedScorerTool.runAggregatedScorer(fullInputJson);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should return string result")
    void testReturnStringResult() {
        String aggregatorInputJson = """
                {
                    "transaction_id": "TX-12345",
                    "customer_id": "CUST-67890",
                    "pattern_agent": {"risk_score": 75},
                    "behavioral_agent": {"behavioral_risk_score": 60},
                    "evidence_agent": {"evidence_summary": "Test"}
                }
                """;
        
        String result = RunAggregatedScorerTool.runAggregatedScorer(aggregatorInputJson);
        
        assertTrue(result instanceof String);
    }
}

