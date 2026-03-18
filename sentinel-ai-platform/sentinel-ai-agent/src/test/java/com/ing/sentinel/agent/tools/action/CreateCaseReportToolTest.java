package com.ing.sentinel.agent.tools.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CreateCaseReportTool.
 * Tests case report creation and result structure.
 */
@DisplayName("CreateCaseReportTool Tests")
class CreateCaseReportToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should create case report successfully")
    void testCreateCaseReportSuccess() {
        Map<String, Object> result = CreateCaseReportTool.createCaseReport(
                "TX-12345", "CUST-123", 85.0, "HIGH", "AMOUNT_SPIKE,GEO_MISMATCH", "Critical risk indicators detected");
        
        assertNotNull(result);
        assertEquals("create_case_report", result.get("action"));
        assertEquals("SUCCESS", result.get("status"));
    }

    @Test
    @DisplayName("Should generate case ID")
    void testGenerateCaseId() {
        Map<String, Object> result = CreateCaseReportTool.createCaseReport(
                "TX-12345", "CUST-123", 75.0, "MED", "AMOUNT_SPIKE", "Test summary");
        
        assertNotNull(result.get("case_id"));
        String caseId = (String) result.get("case_id");
        assertThat(caseId).startsWith("case_");
    }

    @Test
    @DisplayName("Should include transaction ID")
    void testIncludesTransactionId() {
        Map<String, Object> result = CreateCaseReportTool.createCaseReport(
                "TX-12345", "CUST-123", 75.0, "MED", "", "Test");
        
        assertEquals("TX-12345", result.get("transaction_id"));
    }

    @Test
    @DisplayName("Should include risk score")
    void testIncludesRiskScore() {
        Map<String, Object> result = CreateCaseReportTool.createCaseReport(
                "TX-12345", "CUST-123", 85.0, "HIGH", "", "Test");
        
        assertEquals(85.0, ((Number) result.get("final_risk_score")).doubleValue());
    }

    @Test
    @DisplayName("Should include severity")
    void testIncludesSeverity() {
        String severity = "HIGH";
        Map<String, Object> result = CreateCaseReportTool.createCaseReport(
                "TX-12345", "CUST-123", 75.0, severity, "", "Test");
        
        assertEquals(severity, result.get("severity"));
    }

    @Test
    @DisplayName("Should include timestamp")
    void testIncludesTimestamp() {
        Map<String, Object> result = CreateCaseReportTool.createCaseReport(
                "TX-12345", "CUST-123", 75.0, "MED", "", "Test");
        
        assertNotNull(result.get("timestamp_utc"));
    }

    @Test
    @DisplayName("Should handle various risk scores")
    void testVariousRiskScores() {
        double[] scores = {0.0, 25.0, 50.0, 75.0, 100.0};
        
        for (double score : scores) {
            Map<String, Object> result = CreateCaseReportTool.createCaseReport(
                    "TX-" + (int)score, "CUST-123", score, "LOW", "", "Test");
            assertEquals("SUCCESS", result.get("status"));
        }
    }

    @Test
    @DisplayName("Should generate unique case IDs")
    void testUniqueCaseIds() throws InterruptedException {
        Map<String, Object> result1 = CreateCaseReportTool.createCaseReport(
                "TX-001", "CUST-123", 75.0, "MED", "", "Test 1");
        Thread.sleep(2); // Ensure different timestamps
        Map<String, Object> result2 = CreateCaseReportTool.createCaseReport(
                "TX-002", "CUST-123", 75.0, "MED", "", "Test 2");
        
        assertNotEquals(result1.get("case_id"), result2.get("case_id"));
    }

    @Test
    @DisplayName("Should return map with required fields")
    void testRequiredFields() {
        Map<String, Object> result = CreateCaseReportTool.createCaseReport(
                "TX-12345", "CUST-123", 75.0, "MED", "", "Test");
        
        assertTrue(result.containsKey("action"));
        assertTrue(result.containsKey("transaction_id"));
        assertTrue(result.containsKey("case_id"));
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("timestamp_utc"));
    }
}

