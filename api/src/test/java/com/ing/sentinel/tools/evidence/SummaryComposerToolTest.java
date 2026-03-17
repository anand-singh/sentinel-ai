package com.ing.sentinel.tools.evidence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SummaryComposerTool.
 * Tests summary composition and result structure.
 */
@DisplayName("SummaryComposerTool Tests")
class SummaryComposerToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should compose summary from all sources")
    void testComposeSummaryAllSources() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE", "NEW_DEVICE", "PEP_MATCH_PENDING");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Amount significantly above baseline",
                "New device detected during unusual hours",
                "Customer matched against PEP list",
                flags);
        
        assertNotNull(result);
        assertNotNull(result.evidenceSummary);
        assertTrue(result.evidenceSummary.length() > 0);
    }

    @Test
    @DisplayName("Should handle pattern reasoning only")
    void testPatternReasoningOnly() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE", "VELOCITY_HIGH");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "High amount and velocity detected",
                null,
                null,
                flags);
        
        assertNotNull(result);
        assertNotNull(result.evidenceSummary);
        assertTrue(result.phraseCount > 0);
    }

    @Test
    @DisplayName("Should handle behavioral reasoning only")
    void testBehavioralReasoningOnly() {
        List<String> flags = Arrays.asList("NEW_DEVICE", "UNUSUAL_TIME");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                null,
                "New device at unusual time",
                null,
                flags);
        
        assertNotNull(result);
        assertNotNull(result.evidenceSummary);
    }

    @Test
    @DisplayName("Should handle AML reasoning only")
    void testAmlReasoningOnly() {
        List<String> flags = Arrays.asList("PEP_MATCH_PENDING");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                null,
                null,
                "PEP match requires review",
                flags);
        
        assertNotNull(result);
        assertNotNull(result.evidenceSummary);
    }

    @Test
    @DisplayName("Should handle no reasoning provided")
    void testNoReasoning() {
        List<String> flags = Arrays.asList();
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                null,
                null,
                null,
                flags);
        
        assertNotNull(result);
        assertNotNull(result.evidenceSummary);
        assertTrue(result.evidenceSummary.contains("no significant risk"));
    }

    @Test
    @DisplayName("Should handle empty reasoning strings")
    void testEmptyReasoningStrings() {
        List<String> flags = Arrays.asList();
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "",
                "",
                "",
                flags);
        
        assertNotNull(result);
        assertNotNull(result.evidenceSummary);
    }

    @Test
    @DisplayName("Should include phrase count")
    void testIncludesPhraseCount() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE", "NEW_DEVICE");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                "Behavioral reasoning",
                null,
                flags);
        
        assertNotNull(result.phraseCount);
        assertTrue(result.phraseCount >= 0);
    }

    @Test
    @DisplayName("Should produce concise summary")
    void testConciseSummary() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE", "NEW_DEVICE", "VELOCITY_HIGH");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "This is a very long reasoning text that goes on and on explaining all the details about the transaction pattern analysis and what was discovered during the investigation of this particular transaction which shows multiple concerning indicators",
                "Another long behavioral reasoning text",
                "Yet another long AML reasoning text",
                flags);
        
        assertNotNull(result.evidenceSummary);
        // Summary should be reasonably concise
        assertThat(result.evidenceSummary.length()).isLessThan(500);
    }

    @Test
    @DisplayName("Should return result with required fields")
    void testRequiredFields() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                null,
                null,
                flags);
        
        assertNotNull(result.evidenceSummary);
        assertNotNull(result.phraseCount);
    }
}

