package com.ing.sentinel.agent.tools.evidence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FlagMergerTool.
 * Tests flag merging and deduplication.
 */
@DisplayName("FlagMergerTool Tests")
class FlagMergerToolTest {

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should merge flags from multiple sources")
    void testMergeFlags() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE", "GEO_MISMATCH");
        List<String> behavioralFlags = Arrays.asList("NEW_DEVICE", "UNUSUAL_TIME");
        List<String> amlFlags = Arrays.asList("PEP_MATCH_PENDING");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, behavioralFlags, amlFlags);
        
        assertNotNull(result);
        assertNotNull(result.combinedFlags);
        assertEquals(5, result.totalUniqueFlags);
    }

    @Test
    @DisplayName("Should deduplicate flags")
    void testDeduplicateFlags() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE", "GEO_MISMATCH");
        List<String> behavioralFlags = Arrays.asList("AMOUNT_SPIKE", "NEW_DEVICE");
        List<String> amlFlags = Arrays.asList("GEO_MISMATCH");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, behavioralFlags, amlFlags);
        
        assertNotNull(result);
        assertEquals(3, result.totalUniqueFlags);
        assertTrue(result.combinedFlags.contains("AMOUNT_SPIKE"));
    }

    @Test
    @DisplayName("Should sort flags by priority")
    void testSortByPriority() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE", "RARE_MCC");
        List<String> behavioralFlags = Arrays.asList("NEW_DEVICE");
        List<String> amlFlags = Arrays.asList("PEP_MATCH_PENDING");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, behavioralFlags, amlFlags);
        
        assertNotNull(result.combinedFlags);
        // PEP_MATCH_PENDING should be first (highest priority)
        assertEquals("PEP_MATCH_PENDING", result.combinedFlags.get(0));
    }

    @Test
    @DisplayName("Should handle null flag lists")
    void testNullFlagLists() {
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                null, null, null);
        
        assertNotNull(result);
        assertEquals(0, result.totalUniqueFlags);
        assertTrue(result.combinedFlags.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty flag lists")
    void testEmptyFlagLists() {
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                Arrays.asList(), Arrays.asList(), Arrays.asList());
        
        assertNotNull(result);
        assertEquals(0, result.totalUniqueFlags);
        assertTrue(result.combinedFlags.isEmpty());
    }

    @Test
    @DisplayName("Should handle single source")
    void testSingleSource() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE", "GEO_MISMATCH", "VELOCITY_HIGH");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, null, null);
        
        assertNotNull(result);
        assertEquals(3, result.totalUniqueFlags);
    }

    @Test
    @DisplayName("Should include message")
    void testIncludesMessage() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE");
        List<String> behavioralFlags = Arrays.asList("NEW_DEVICE");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, behavioralFlags, null);
        
        assertNotNull(result.message);
        assertTrue(result.message.contains("flags"));
    }

    @Test
    @DisplayName("Should return result with required fields")
    void testRequiredFields() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, null, null);
        
        assertNotNull(result.combinedFlags);
        assertNotNull(result.totalUniqueFlags);
        assertNotNull(result.message);
    }

    @Test
    @DisplayName("Should preserve all unique flags across sources")
    void allUniqueFlagsArePreserved() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE");
        List<String> behavioralFlags = Arrays.asList("NEW_DEVICE");
        List<String> amlFlags = Arrays.asList("PEP_MATCH_PENDING");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, behavioralFlags, amlFlags);
        
        assertThat(result.combinedFlags).contains("AMOUNT_SPIKE", "NEW_DEVICE", "PEP_MATCH_PENDING");
    }

    @Test
    @DisplayName("Should handle duplicate flags across all three sources")
    void duplicateFlagsAcrossAllSourcesAreDeduplicated() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE", "GEO_MISMATCH");
        List<String> behavioralFlags = Arrays.asList("AMOUNT_SPIKE", "GEO_MISMATCH");
        List<String> amlFlags = Arrays.asList("AMOUNT_SPIKE", "GEO_MISMATCH");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, behavioralFlags, amlFlags);
        
        assertEquals(2, result.totalUniqueFlags);
        assertThat(result.combinedFlags).hasSize(2);
    }

    @Test
    @DisplayName("Should sort critical flags before high priority flags")
    void criticalFlagsComeBeforeHighPriorityFlags() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE", "GEO_MISMATCH");
        List<String> behavioralFlags = Arrays.asList("NEW_DEVICE");
        List<String> amlFlags = Arrays.asList("SANCTIONS_HIT", "PEP_MATCH_PENDING");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, behavioralFlags, amlFlags);
        
        List<String> criticalFlags = Arrays.asList("PEP_MATCH_PENDING", "SANCTIONS_HIT");
        int lastCriticalIndex = -1;
        int firstHighIndex = result.combinedFlags.size();
        
        for (int i = 0; i < result.combinedFlags.size(); i++) {
            if (criticalFlags.contains(result.combinedFlags.get(i))) {
                lastCriticalIndex = i;
            } else {
                firstHighIndex = Math.min(firstHighIndex, i);
            }
        }
        
        assertThat(lastCriticalIndex).isLessThan(firstHighIndex);
    }

    @Test
    @DisplayName("Should sort high priority flags before medium priority flags")
    void highPriorityFlagsComeBeforeMediumPriorityFlags() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE", "RARE_MCC");
        List<String> behavioralFlags = Arrays.asList("NEW_IP_RANGE", "TIME_ANOMALY");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, behavioralFlags, null);
        
        int amountSpikeIndex = result.combinedFlags.indexOf("AMOUNT_SPIKE");
        int rareMccIndex = result.combinedFlags.indexOf("RARE_MCC");
        int newIpIndex = result.combinedFlags.indexOf("NEW_IP_RANGE");
        
        assertThat(amountSpikeIndex).isLessThan(rareMccIndex);
        assertThat(amountSpikeIndex).isLessThan(newIpIndex);
    }

    @Test
    @DisplayName("Should sort flags alphabetically when same priority")
    void flagsWithSamePriorityAreSortedAlphabetically() {
        List<String> patternFlags = Arrays.asList("GEO_MISMATCH", "AMOUNT_SPIKE", "NEW_DEVICE");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, null, null);
        
        int amountIndex = result.combinedFlags.indexOf("AMOUNT_SPIKE");
        int geoIndex = result.combinedFlags.indexOf("GEO_MISMATCH");
        int deviceIndex = result.combinedFlags.indexOf("NEW_DEVICE");
        
        assertThat(amountIndex).isLessThan(geoIndex);
        assertThat(amountIndex).isLessThan(deviceIndex);
    }

    @Test
    @DisplayName("Should handle unknown flags with lowest priority")
    void unknownFlagsHaveLowestPriority() {
        List<String> patternFlags = Arrays.asList("UNKNOWN_FLAG", "AMOUNT_SPIKE");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, null, null);
        
        int amountIndex = result.combinedFlags.indexOf("AMOUNT_SPIKE");
        int unknownIndex = result.combinedFlags.indexOf("UNKNOWN_FLAG");
        
        assertThat(amountIndex).isLessThan(unknownIndex);
    }

    @Test
    @DisplayName("Should sort multiple unknown flags alphabetically")
    void multipleUnknownFlagsAreSortedAlphabetically() {
        List<String> patternFlags = Arrays.asList("ZEBRA_FLAG", "ALPHA_FLAG", "BETA_FLAG");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, null, null);
        
        assertThat(result.combinedFlags).containsExactly("ALPHA_FLAG", "BETA_FLAG", "ZEBRA_FLAG");
    }

    @Test
    @DisplayName("Should handle mix of known and unknown flags")
    void mixOfKnownAndUnknownFlagsIsSortedCorrectly() {
        List<String> patternFlags = Arrays.asList("UNKNOWN_A", "AMOUNT_SPIKE", "UNKNOWN_B", "GEO_MISMATCH");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, null, null);
        
        List<String> knownFlags = Arrays.asList("AMOUNT_SPIKE", "GEO_MISMATCH");
        int lastKnownIndex = -1;
        int firstUnknownIndex = result.combinedFlags.size();
        
        for (int i = 0; i < result.combinedFlags.size(); i++) {
            if (knownFlags.contains(result.combinedFlags.get(i))) {
                lastKnownIndex = i;
            } else {
                firstUnknownIndex = Math.min(firstUnknownIndex, i);
            }
        }
        
        assertThat(lastKnownIndex).isLessThan(firstUnknownIndex);
    }

    @Test
    @DisplayName("Should handle large number of flags")
    void largeNumberOfFlagsIsHandled() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE", "GEO_MISMATCH", "VELOCITY_HIGH", "RARE_MCC");
        List<String> behavioralFlags = Arrays.asList("NEW_DEVICE", "UNUSUAL_TIME", "NEW_IP_RANGE", "BURST_ACTIVITY");
        List<String> amlFlags = Arrays.asList("PEP_MATCH_PENDING", "SANCTIONS_HIT");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, behavioralFlags, amlFlags);
        
        assertEquals(10, result.totalUniqueFlags);
        assertThat(result.combinedFlags).hasSize(10);
    }

    @Test
    @DisplayName("Should generate message with correct source count for one source")
    void messageContainsCorrectSourceCountForOneSource() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, null, null);
        
        assertThat(result.message).contains("1 source");
    }

    @Test
    @DisplayName("Should generate message with correct source count for two sources")
    void messageContainsCorrectSourceCountForTwoSources() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE");
        List<String> behavioralFlags = Arrays.asList("NEW_DEVICE");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, behavioralFlags, null);
        
        assertThat(result.message).contains("2 sources");
    }

    @Test
    @DisplayName("Should generate message with correct source count for three sources")
    void messageContainsCorrectSourceCountForThreeSources() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE");
        List<String> behavioralFlags = Arrays.asList("NEW_DEVICE");
        List<String> amlFlags = Arrays.asList("PEP_MATCH_PENDING");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, behavioralFlags, amlFlags);
        
        assertThat(result.message).contains("3 sources");
    }

    @Test
    @DisplayName("Should generate message with correct flag count")
    void messageContainsCorrectFlagCount() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE", "GEO_MISMATCH");
        List<String> behavioralFlags = Arrays.asList("NEW_DEVICE");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, behavioralFlags, null);
        
        assertThat(result.message).contains("3 unique flags");
    }

    @Test
    @DisplayName("Should not count empty list as source")
    void emptyListIsNotCountedAsSource() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE");
        List<String> behavioralFlags = Arrays.asList();
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, behavioralFlags, null);
        
        assertThat(result.message).contains("1 source");
    }

    @Test
    @DisplayName("Should handle duplicate within single source")
    void duplicateWithinSingleSourceIsDeduplicated() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE", "AMOUNT_SPIKE", "GEO_MISMATCH");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, null, null);
        
        assertEquals(2, result.totalUniqueFlags);
        assertThat(result.combinedFlags).containsExactlyInAnyOrder("AMOUNT_SPIKE", "GEO_MISMATCH");
    }

    @Test
    @DisplayName("Should handle all flags from behavioral source only")
    void behavioralSourceOnlyIsHandled() {
        List<String> behavioralFlags = Arrays.asList("NEW_DEVICE", "UNUSUAL_TIME", "BURST_ACTIVITY");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                null, behavioralFlags, null);
        
        assertEquals(3, result.totalUniqueFlags);
        assertThat(result.combinedFlags).hasSize(3);
    }

    @Test
    @DisplayName("Should handle all flags from AML source only")
    void amlSourceOnlyIsHandled() {
        List<String> amlFlags = Arrays.asList("PEP_MATCH_PENDING", "SANCTIONS_HIT");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                null, null, amlFlags);
        
        assertEquals(2, result.totalUniqueFlags);
        assertThat(result.combinedFlags).containsExactly("PEP_MATCH_PENDING", "SANCTIONS_HIT");
    }

    @Test
    @DisplayName("Should maintain correct order with multiple priority levels")
    void complexPriorityOrderingIsCorrect() {
        List<String> patternFlags = Arrays.asList("RARE_MCC", "AMOUNT_SPIKE", "TIME_ANOMALY");
        List<String> behavioralFlags = Arrays.asList("GEO_DEVIATION", "NEW_IP_RANGE");
        List<String> amlFlags = Arrays.asList("SANCTIONS_HIT");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, behavioralFlags, amlFlags);
        
        assertThat(result.combinedFlags.get(0)).isEqualTo("SANCTIONS_HIT");
        
        int sanctionsIndex = result.combinedFlags.indexOf("SANCTIONS_HIT");
        int amountIndex = result.combinedFlags.indexOf("AMOUNT_SPIKE");
        int rareMccIndex = result.combinedFlags.indexOf("RARE_MCC");
        
        assertThat(sanctionsIndex).isLessThan(amountIndex);
        assertThat(amountIndex).isLessThan(rareMccIndex);
    }

    @Test
    @DisplayName("Should handle single flag from each source")
    void singleFlagFromEachSourceIsHandled() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE");
        List<String> behavioralFlags = Arrays.asList("NEW_DEVICE");
        List<String> amlFlags = Arrays.asList("PEP_MATCH_PENDING");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, behavioralFlags, amlFlags);
        
        assertEquals(3, result.totalUniqueFlags);
        assertThat(result.message).contains("3 sources");
    }

    @Test
    @DisplayName("Should handle case sensitive flags")
    void caseSensitiveFlagsAreTreatedAsDistinct() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE", "amount_spike");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                patternFlags, null, null);
        
        assertEquals(2, result.totalUniqueFlags);
    }

    @Test
    @DisplayName("Should sort SANCTIONS_HIT and PEP_MATCH_PENDING alphabetically")
    void criticalFlagsWithSamePriorityAreSortedAlphabetically() {
        List<String> amlFlags = Arrays.asList("SANCTIONS_HIT", "PEP_MATCH_PENDING");
        
        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
                null, null, amlFlags);
        
        assertThat(result.combinedFlags).containsExactly("PEP_MATCH_PENDING", "SANCTIONS_HIT");
    }
}

