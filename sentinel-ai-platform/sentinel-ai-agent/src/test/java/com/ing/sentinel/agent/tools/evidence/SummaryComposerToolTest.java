package com.ing.sentinel.agent.tools.evidence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
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

    @Test
    @DisplayName("Should handle null flags list")
    void nullFlagsListIsHandledGracefully() {
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                "Behavioral reasoning",
                "AML reasoning",
                null);
        
        assertNotNull(result.evidenceSummary);
        assertThat(result.evidenceSummary).contains("no significant risk");
    }

    @Test
    @DisplayName("Should handle whitespace only reasoning")
    void whitespaceOnlyReasoningIsIgnored() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "   ",
                "  ",
                " ",
                flags);
        
        assertNotNull(result.evidenceSummary);
        assertEquals(0, result.phraseCount);
    }

    @Test
    @DisplayName("Should generate phrase for AMOUNT_SPIKE flag")
    void amountSpikeFlagGeneratesPhrase() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                null,
                null,
                flags);
        
        assertThat(result.evidenceSummary).contains("High amount vs pattern");
    }

    @Test
    @DisplayName("Should generate phrase for GEO_MISMATCH flag")
    void geoMismatchFlagGeneratesPhrase() {
        List<String> flags = Arrays.asList("GEO_MISMATCH");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                null,
                null,
                flags);
        
        assertThat(result.evidenceSummary).contains("abnormal location");
    }

    @Test
    @DisplayName("Should generate phrase for VELOCITY_HIGH flag")
    void velocityHighFlagGeneratesPhrase() {
        List<String> flags = Arrays.asList("VELOCITY_HIGH");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                null,
                null,
                flags);
        
        assertThat(result.evidenceSummary).contains("high velocity");
    }

    @Test
    @DisplayName("Should generate phrase for RARE_MCC flag")
    void rareMccFlagGeneratesPhrase() {
        List<String> flags = Arrays.asList("RARE_MCC");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                null,
                null,
                flags);
        
        assertThat(result.evidenceSummary).contains("unusual merchant category");
    }

    @Test
    @DisplayName("Should generate phrase for TIME_ANOMALY flag")
    void timeAnomalyFlagGeneratesPhrase() {
        List<String> flags = Arrays.asList("TIME_ANOMALY");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                null,
                null,
                flags);
        
        assertThat(result.evidenceSummary).contains("unusual time");
    }

    @Test
    @DisplayName("Should generate phrase for NEW_DEVICE flag")
    void newDeviceFlagGeneratesPhrase() {
        List<String> flags = Arrays.asList("NEW_DEVICE");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                null,
                "Behavioral reasoning",
                null,
                flags);
        
        assertThat(result.evidenceSummary).contains("New device");
    }

    @Test
    @DisplayName("Should generate phrase for UNUSUAL_TIME flag")
    void unusualTimeFlagGeneratesPhrase() {
        List<String> flags = Arrays.asList("UNUSUAL_TIME");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                null,
                "Behavioral reasoning",
                null,
                flags);
        
        assertThat(result.evidenceSummary).contains("off-hours");
    }

    @Test
    @DisplayName("Should generate phrase for GEO_DEVIATION flag")
    void geoDeviationFlagGeneratesPhrase() {
        List<String> flags = Arrays.asList("GEO_DEVIATION");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                null,
                "Behavioral reasoning",
                null,
                flags);
        
        assertThat(result.evidenceSummary).contains("abnormal location distance");
    }

    @Test
    @DisplayName("Should generate phrase for NEW_IP_RANGE flag")
    void newIpRangeFlagGeneratesPhrase() {
        List<String> flags = Arrays.asList("NEW_IP_RANGE");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                null,
                "Behavioral reasoning",
                null,
                flags);
        
        assertThat(result.evidenceSummary).contains("new IP range");
    }

    @Test
    @DisplayName("Should generate phrase for MERCHANT_NOVELTY flag")
    void merchantNoveltyFlagGeneratesPhrase() {
        List<String> flags = Arrays.asList("MERCHANT_NOVELTY");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                null,
                "Behavioral reasoning",
                null,
                flags);
        
        assertThat(result.evidenceSummary).contains("unusual merchant for customer");
    }

    @Test
    @DisplayName("Should generate phrase for BURST_ACTIVITY flag")
    void burstActivityFlagGeneratesPhrase() {
        List<String> flags = Arrays.asList("BURST_ACTIVITY");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                null,
                "Behavioral reasoning",
                null,
                flags);
        
        assertThat(result.evidenceSummary).contains("burst activity");
    }

    @Test
    @DisplayName("Should generate phrase for PEP_MATCH_PENDING flag")
    void pepMatchPendingFlagGeneratesPhrase() {
        List<String> flags = Arrays.asList("PEP_MATCH_PENDING");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                null,
                null,
                "AML reasoning",
                flags);
        
        assertThat(result.evidenceSummary).contains("AML check pending");
    }

    @Test
    @DisplayName("Should generate phrase for SANCTIONS_HIT flag")
    void sanctionsHitFlagGeneratesPhrase() {
        List<String> flags = Arrays.asList("SANCTIONS_HIT");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                null,
                null,
                "AML reasoning",
                flags);
        
        assertThat(result.evidenceSummary).contains("Sanctions screening hit");
    }

    @Test
    @DisplayName("Should combine multiple pattern flags with commas")
    void multiplePatternFlagsAreCombined() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE", "GEO_MISMATCH", "VELOCITY_HIGH");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                null,
                null,
                flags);
        
        assertThat(result.evidenceSummary).contains("High amount vs pattern");
        assertThat(result.evidenceSummary).contains("abnormal location");
        assertThat(result.evidenceSummary).contains("high velocity");
        assertThat(result.evidenceSummary).contains(",");
    }

    @Test
    @DisplayName("Should combine multiple behavioral flags with commas")
    void multipleBehavioralFlagsAreCombined() {
        List<String> flags = Arrays.asList("NEW_DEVICE", "UNUSUAL_TIME", "GEO_DEVIATION");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                null,
                "Behavioral reasoning",
                null,
                flags);
        
        assertThat(result.evidenceSummary).contains("New device");
        assertThat(result.evidenceSummary).contains("off-hours");
        assertThat(result.evidenceSummary).contains("abnormal location distance");
        assertThat(result.evidenceSummary).contains(",");
    }

    @Test
    @DisplayName("Should separate phrases from different sources with semicolon")
    void phrasesFromDifferentSourcesAreSeparatedBySemicolon() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE", "NEW_DEVICE", "PEP_MATCH_PENDING");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                "Behavioral reasoning",
                "AML reasoning",
                flags);
        
        assertThat(result.evidenceSummary).contains(";");
    }

    @Test
    @DisplayName("Should end summary with period")
    void summaryEndsWithPeriod() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                null,
                null,
                flags);
        
        assertThat(result.evidenceSummary).endsWith(".");
    }

    @Test
    @DisplayName("Should count phrases correctly for single source")
    void phraseCountIsCorrectForSingleSource() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                null,
                null,
                flags);
        
        assertEquals(1, result.phraseCount);
    }

    @Test
    @DisplayName("Should count phrases correctly for two sources")
    void phraseCountIsCorrectForTwoSources() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE", "NEW_DEVICE");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                "Behavioral reasoning",
                null,
                flags);
        
        assertEquals(2, result.phraseCount);
    }

    @Test
    @DisplayName("Should count phrases correctly for three sources")
    void phraseCountIsCorrectForThreeSources() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE", "NEW_DEVICE", "PEP_MATCH_PENDING");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                "Behavioral reasoning",
                "AML reasoning",
                flags);
        
        assertEquals(3, result.phraseCount);
    }

    @Test
    @DisplayName("Should have zero phrase count when no reasoning provided")
    void phraseCountIsZeroWithNoReasoning() {
        List<String> flags = Arrays.asList();
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                null,
                null,
                null,
                flags);
        
        assertEquals(0, result.phraseCount);
    }

    @Test
    @DisplayName("Should ignore unknown flags")
    void unknownFlagsAreIgnored() {
        List<String> flags = Arrays.asList("UNKNOWN_FLAG", "ANOTHER_UNKNOWN");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                null,
                null,
                flags);
        
        assertEquals(0, result.phraseCount);
        assertThat(result.evidenceSummary).contains("no significant risk");
    }

    @Test
    @DisplayName("Should handle mix of known and unknown flags")
    void mixOfKnownAndUnknownFlagsIsHandled() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE", "UNKNOWN_FLAG", "NEW_DEVICE");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                "Behavioral reasoning",
                null,
                flags);
        
        assertThat(result.evidenceSummary).contains("High amount vs pattern");
        assertThat(result.evidenceSummary).contains("New device");
        assertThat(result.evidenceSummary).doesNotContain("UNKNOWN_FLAG");
    }

    @Test
    @DisplayName("Should not generate phrase when no flags match")
    void noPhrasesWhenNoFlagsMatch() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                null,
                "Behavioral reasoning",
                null,
                flags);
        
        assertEquals(0, result.phraseCount);
        assertThat(result.evidenceSummary).contains("no significant risk");
    }

    @Test
    @DisplayName("Should handle all pattern flags")
    void allPatternFlagsAreHandled() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE", "GEO_MISMATCH", "VELOCITY_HIGH", "RARE_MCC", "TIME_ANOMALY");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                null,
                null,
                flags);
        
        assertEquals(1, result.phraseCount);
        assertThat(result.evidenceSummary).contains("High amount vs pattern");
        assertThat(result.evidenceSummary).contains("abnormal location");
        assertThat(result.evidenceSummary).contains("high velocity");
        assertThat(result.evidenceSummary).contains("unusual merchant category");
        assertThat(result.evidenceSummary).contains("unusual time");
    }

    @Test
    @DisplayName("Should handle all behavioral flags")
    void allBehavioralFlagsAreHandled() {
        List<String> flags = Arrays.asList("NEW_DEVICE", "UNUSUAL_TIME", "GEO_DEVIATION", "NEW_IP_RANGE", "MERCHANT_NOVELTY", "BURST_ACTIVITY");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                null,
                "Behavioral reasoning",
                null,
                flags);
        
        assertEquals(1, result.phraseCount);
        assertThat(result.evidenceSummary).contains("New device");
        assertThat(result.evidenceSummary).contains("off-hours");
        assertThat(result.evidenceSummary).contains("abnormal location distance");
        assertThat(result.evidenceSummary).contains("new IP range");
        assertThat(result.evidenceSummary).contains("unusual merchant for customer");
        assertThat(result.evidenceSummary).contains("burst activity");
    }

    @Test
    @DisplayName("Should handle both AML flags")
    void bothAmlFlagsAreHandled() {
        List<String> flags = Arrays.asList("PEP_MATCH_PENDING", "SANCTIONS_HIT");
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                null,
                null,
                "AML reasoning",
                flags);
        
        assertEquals(1, result.phraseCount);
        assertThat(result.evidenceSummary).contains("AML check pending");
    }

    @Test
    @DisplayName("Should handle empty flags list")
    void emptyFlagsListIsHandled() {
        List<String> flags = Collections.emptyList();
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                null,
                null,
                flags);
        
        assertEquals(0, result.phraseCount);
        assertThat(result.evidenceSummary).contains("no significant risk");
    }

    @Test
    @DisplayName("Should handle reasoning without matching flags")
    void reasoningWithoutMatchingFlagsProducesDefaultMessage() {
        List<String> flags = Collections.emptyList();
        
        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
                "Some pattern reasoning",
                "Some behavioral reasoning",
                "Some AML reasoning",
                flags);
        
        assertEquals(0, result.phraseCount);
        assertThat(result.evidenceSummary).contains("no significant risk");
    }

    @Test
    @DisplayName("Should generate consistent output for same inputs")
    void outputIsConsistentForSameInputs() {
        List<String> flags = Arrays.asList("AMOUNT_SPIKE", "NEW_DEVICE");
        
        SummaryComposerTool.SummaryResult result1 = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                "Behavioral reasoning",
                null,
                flags);
        
        SummaryComposerTool.SummaryResult result2 = SummaryComposerTool.composeSummary(
                "Pattern reasoning",
                "Behavioral reasoning",
                null,
                flags);
        
        assertEquals(result1.evidenceSummary, result2.evidenceSummary);
        assertEquals(result1.phraseCount, result2.phraseCount);
    }
}

