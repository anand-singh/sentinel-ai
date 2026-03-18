package com.ing.sentinel.tools.evidence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EvidenceBuilderTool.
 * Tests evidence bundle building and result structure.
 */
@DisplayName("EvidenceBuilderTool Tests")
class EvidenceBuilderToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should build evidence bundle successfully")
    void testBuildEvidenceBundle() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE", "NEW_DEVICE", "GEO_MISMATCH");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "High risk transaction with multiple indicators",
                flags,
                "Amount significantly above pattern",
                "New device detected during unusual hours",
                "No AML concerns");
        
        assertNotNull(result);
        assertNotNull(result.evidenceSummary);
        assertNotNull(result.combinedFlags);
    }

    @Test
    @DisplayName("Should include agent scores")
    void testIncludesAgentScores() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                "Pattern reasoning",
                "Behavioral reasoning",
                "AML reasoning");
        
        assertNotNull(result.agentScores);
        assertTrue(result.agentScores.containsKey("pattern_score"));
        assertTrue(result.agentScores.containsKey("behavioral_score"));
        assertTrue(result.agentScores.containsKey("aml_score"));
        assertEquals(75, result.agentScores.get("pattern_score"));
    }

    @Test
    @DisplayName("Should include explanation items")
    void testIncludesExplanationItems() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                "Pattern reasoning",
                "Behavioral reasoning",
                "AML reasoning");
        
        assertNotNull(result.explanationItems);
        assertFalse(result.explanationItems.isEmpty());
    }

    @Test
    @DisplayName("Should include version info")
    void testIncludesVersionInfo() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                "Pattern reasoning",
                "Behavioral reasoning",
                "AML reasoning");
        
        assertNotNull(result.version);
        assertNotNull(result.configVersion);
        assertTrue(result.version.contains("evidence"));
    }

    @Test
    @DisplayName("Should handle null reasoning gracefully")
    void testNullReasoning() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, -1,
                "Test summary",
                flags,
                "Pattern reasoning",
                null,
                null);
        
        assertNotNull(result);
        assertNotNull(result.agentScores);
        assertFalse(result.agentScores.containsKey("aml_score"));
    }

    @Test
    @DisplayName("Should handle empty flags list")
    void testEmptyFlagsList() {
        List<String> flags = Arrays.asList();
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                "Pattern reasoning",
                "Behavioral reasoning",
                "AML reasoning");
        
        assertNotNull(result);
        assertNotNull(result.combinedFlags);
        assertTrue(result.combinedFlags.isEmpty());
    }

    @Test
    @DisplayName("Should preserve combined flags")
    void testPreserveCombinedFlags() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE", "NEW_DEVICE", "GEO_MISMATCH");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                "Pattern reasoning",
                "Behavioral reasoning",
                "AML reasoning");
        
        assertEquals(3, result.combinedFlags.size());
        assertTrue(result.combinedFlags.contains("AMOUNT_SPIKE"));
    }

    @Test
    @DisplayName("Should have required fields")
    void testRequiredFields() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                "Pattern reasoning",
                "Behavioral reasoning",
                "AML reasoning");
        
        assertNotNull(result.evidenceSummary);
        assertNotNull(result.combinedFlags);
        assertNotNull(result.agentScores);
        assertNotNull(result.explanationItems);
        assertNotNull(result.version);
        assertNotNull(result.configVersion);
    }
}

