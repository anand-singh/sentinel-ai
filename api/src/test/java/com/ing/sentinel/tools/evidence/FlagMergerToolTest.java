package com.ing.sentinel.tools.evidence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

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
}

