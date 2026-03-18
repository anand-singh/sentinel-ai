package com.ing.sentinel.agent.tools.evidence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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

    @Test
    @DisplayName("Should exclude negative pattern score from agent scores")
    void negativePatternScoreIsExcluded() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                -1, 60, 45,
                "Test summary",
                flags,
                null,
                "Behavioral reasoning",
                "AML reasoning");
        
        assertFalse(result.agentScores.containsKey("pattern_score"));
        assertTrue(result.agentScores.containsKey("behavioral_score"));
        assertTrue(result.agentScores.containsKey("aml_score"));
    }

    @Test
    @DisplayName("Should exclude negative behavioral score from agent scores")
    void negativeBehavioralScoreIsExcluded() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, -1, 45,
                "Test summary",
                flags,
                "Pattern reasoning",
                null,
                "AML reasoning");
        
        assertTrue(result.agentScores.containsKey("pattern_score"));
        assertFalse(result.agentScores.containsKey("behavioral_score"));
        assertTrue(result.agentScores.containsKey("aml_score"));
    }

    @Test
    @DisplayName("Should exclude all negative scores from agent scores")
    void allNegativeScoresAreExcluded() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                -1, -1, -1,
                "Test summary",
                flags,
                null,
                null,
                null);
        
        assertThat(result.agentScores).isEmpty();
    }

    @Test
    @DisplayName("Should include zero scores in agent scores")
    void zeroScoresAreIncluded() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                0, 0, 0,
                "Test summary",
                flags,
                "Pattern reasoning",
                "Behavioral reasoning",
                "AML reasoning");
        
        assertThat(result.agentScores).hasSize(3);
        assertEquals(0, result.agentScores.get("pattern_score"));
        assertEquals(0, result.agentScores.get("behavioral_score"));
        assertEquals(0, result.agentScores.get("aml_score"));
    }

    @Test
    @DisplayName("Should handle maximum scores correctly")
    void maximumScoresAreHandled() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                100, 100, 100,
                "Test summary",
                flags,
                "Pattern reasoning",
                "Behavioral reasoning",
                "AML reasoning");
        
        assertEquals(100, result.agentScores.get("pattern_score"));
        assertEquals(100, result.agentScores.get("behavioral_score"));
        assertEquals(100, result.agentScores.get("aml_score"));
    }

    @Test
    @DisplayName("Should handle null combined flags")
    void nullCombinedFlagsBecomesEmptyList() {
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                null,
                "Pattern reasoning",
                "Behavioral reasoning",
                "AML reasoning");
        
        assertNotNull(result.combinedFlags);
        assertThat(result.combinedFlags).isEmpty();
    }

    @Test
    @DisplayName("Should exclude empty reasoning from explanation items")
    void emptyReasoningsAreExcluded() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                "",
                "",
                "");
        
        assertThat(result.explanationItems).isEmpty();
    }

    @Test
    @DisplayName("Should truncate long reasoning to 100 characters")
    void longReasoningIsTruncated() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        String longReasoning = "A".repeat(150);
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                longReasoning,
                null,
                null);
        
        assertThat(result.explanationItems).hasSize(1);
        String details = result.explanationItems.get(0).details;
        assertThat(details).hasSize(100);
        assertThat(details).endsWith("...");
    }

    @Test
    @DisplayName("Should truncate reasoning at first period if within 100 characters")
    void reasoningTruncatedAtFirstPeriod() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        String reasoning = "This is the first sentence that needs more text to exceed one hundred characters limit. This is the second sentence.";
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                reasoning,
                null,
                null);
        
        assertThat(result.explanationItems).hasSize(1);
        String details = result.explanationItems.get(0).details;
        assertThat(details).isEqualTo("This is the first sentence that needs more text to exceed one hundred characters limit.");
    }

    @Test
    @DisplayName("Should keep short reasoning unchanged")
    void shortReasoningIsKeptUnchanged() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        String reasoning = "Short reasoning";
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                reasoning,
                null,
                null);
        
        assertThat(result.explanationItems).hasSize(1);
        assertThat(result.explanationItems.get(0).details).isEqualTo(reasoning);
    }

    @Test
    @DisplayName("Should preserve explanation items order")
    void explanationItemsOrderIsPreserved() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                "Pattern reasoning",
                "Behavioral reasoning",
                "AML reasoning");
        
        assertThat(result.explanationItems).hasSize(3);
        assertThat(result.explanationItems.get(0).source).isEqualTo("pattern_agent");
        assertThat(result.explanationItems.get(1).source).isEqualTo("behavioral_agent");
        assertThat(result.explanationItems.get(2).source).isEqualTo("aml_agent");
    }

    @Test
    @DisplayName("Should set correct source for pattern agent")
    void patternAgentSourceIsCorrect() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                "Pattern reasoning",
                null,
                null);
        
        assertThat(result.explanationItems).hasSize(1);
        assertThat(result.explanationItems.get(0).source).isEqualTo("pattern_agent");
        assertThat(result.explanationItems.get(0).details).isEqualTo("Pattern reasoning");
    }

    @Test
    @DisplayName("Should set correct source for behavioral agent")
    void behavioralAgentSourceIsCorrect() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                null,
                "Behavioral reasoning",
                null);
        
        assertThat(result.explanationItems).hasSize(1);
        assertThat(result.explanationItems.get(0).source).isEqualTo("behavioral_agent");
        assertThat(result.explanationItems.get(0).details).isEqualTo("Behavioral reasoning");
    }

    @Test
    @DisplayName("Should set correct source for AML agent")
    void amlAgentSourceIsCorrect() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                null,
                null,
                "AML reasoning");
        
        assertThat(result.explanationItems).hasSize(1);
        assertThat(result.explanationItems.get(0).source).isEqualTo("aml_agent");
        assertThat(result.explanationItems.get(0).details).isEqualTo("AML reasoning");
    }

    @Test
    @DisplayName("Should preserve evidence summary exactly as provided")
    void evidenceSummaryIsPreservedExactly() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        String summary = "Complex summary with special characters: $100.50 @ 12:30PM";
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                summary,
                flags,
                "Pattern reasoning",
                "Behavioral reasoning",
                "AML reasoning");
        
        assertThat(result.evidenceSummary).isEqualTo(summary);
    }

    @Test
    @DisplayName("Should handle multiple flags in order")
    void multipleFlagsArePreservedInOrder() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE", "NEW_DEVICE", "GEO_MISMATCH", "BURST_ACTIVITY");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                "Pattern reasoning",
                "Behavioral reasoning",
                "AML reasoning");
        
        assertThat(result.combinedFlags).hasSize(4);
        assertThat(result.combinedFlags).containsExactly("AMOUNT_SPIKE", "NEW_DEVICE", "GEO_MISMATCH", "BURST_ACTIVITY");
    }

    @Test
    @DisplayName("Should maintain agent scores ordering")
    void agentScoresOrderingIsMaintained() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                "Pattern reasoning",
                "Behavioral reasoning",
                "AML reasoning");
        
        List<String> keys = new ArrayList<>(result.agentScores.keySet());
        assertThat(keys).containsExactly("pattern_score", "behavioral_score", "aml_score");
    }

    @Test
    @DisplayName("Should handle reasoning with period beyond 100 characters")
    void reasoningWithLatePeriodIsTruncatedAt100() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        String reasoning = "A".repeat(120) + ".";
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                reasoning,
                null,
                null);
        
        assertThat(result.explanationItems).hasSize(1);
        String details = result.explanationItems.get(0).details;
        assertThat(details).hasSize(100);
        assertThat(details).endsWith("...");
    }

    @Test
    @DisplayName("Should handle reasoning exactly 100 characters")
    void reasoningExactly100CharactersIsKept() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        String reasoning = "A".repeat(100);
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                reasoning,
                null,
                null);
        
        assertThat(result.explanationItems).hasSize(1);
        assertThat(result.explanationItems.get(0).details).isEqualTo(reasoning);
    }

    @Test
    @DisplayName("Should include only scores with non-null reasoning in explanations")
    void onlyNonNullReasoningsCreateExplanations() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                "Pattern reasoning",
                null,
                "AML reasoning");
        
        assertThat(result.explanationItems).hasSize(2);
        assertThat(result.agentScores).hasSize(3);
    }

    @Test
    @DisplayName("Should handle single flag correctly")
    void singleFlagIsHandledCorrectly() {
        List<String> flags = Arrays.asList("SINGLE_FLAG");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                "Pattern reasoning",
                "Behavioral reasoning",
                "AML reasoning");
        
        assertThat(result.combinedFlags).hasSize(1);
        assertThat(result.combinedFlags.get(0)).isEqualTo("SINGLE_FLAG");
    }

    @Test
    @DisplayName("Should handle whitespace-only reasoning as empty")
    void whitespaceOnlyReasoningIsExcluded() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
                75, 60, 45,
                "Test summary",
                flags,
                "   ",
                "  ",
                " ");
        
        assertThat(result.explanationItems).isEmpty();
    }
}

