package com.ing.sentinel.tools.evidence;

import com.google.adk.tools.Annotations.Schema;

import java.util.*;
import java.util.stream.Collectors;

/**
 * FlagMergerTool
 *
 * Merges and deduplicates flags from multiple upstream agents (Pattern Analyzer,
 * Behavioral Risk, AML, etc.) and sorts them by priority.
 */
public class FlagMergerTool {

    // Priority mapping for flags (higher = more critical)
    private static final Map<String, Integer> FLAG_PRIORITY = new HashMap<>();

    static {
        // Critical flags
        FLAG_PRIORITY.put("PEP_MATCH_PENDING", 100);
        FLAG_PRIORITY.put("SANCTIONS_HIT", 100);

        // High priority flags
        FLAG_PRIORITY.put("AMOUNT_SPIKE", 80);
        FLAG_PRIORITY.put("GEO_MISMATCH", 80);
        FLAG_PRIORITY.put("NEW_DEVICE", 80);
        FLAG_PRIORITY.put("UNUSUAL_TIME", 70);
        FLAG_PRIORITY.put("GEO_DEVIATION", 75);
        FLAG_PRIORITY.put("BURST_ACTIVITY", 75);

        // Medium priority flags
        FLAG_PRIORITY.put("NEW_IP_RANGE", 60);
        FLAG_PRIORITY.put("MERCHANT_NOVELTY", 60);
        FLAG_PRIORITY.put("VELOCITY_HIGH", 65);
        FLAG_PRIORITY.put("RARE_MCC", 55);
        FLAG_PRIORITY.put("TIME_ANOMALY", 50);
    }

    @Schema(name = "merge_flags", description = "Merges flags from multiple agents, deduplicates, and sorts by priority")
    public static MergedFlagsResult mergeFlags(
        @Schema(name = "pattern_flags", description = "List of flags from Pattern Analyzer agent") List<String> patternFlags,
        @Schema(name = "behavioral_flags", description = "List of flags from Behavioral Risk agent") List<String> behavioralFlags,
        @Schema(name = "aml_flags", description = "List of flags from AML agent") List<String> amlFlags
    ) {
        // Combine all flags
        Set<String> allFlags = new HashSet<>();
        if (patternFlags != null) allFlags.addAll(patternFlags);
        if (behavioralFlags != null) allFlags.addAll(behavioralFlags);
        if (amlFlags != null) allFlags.addAll(amlFlags);

        // Sort by priority (highest first), then alphabetically
        List<String> sortedFlags = allFlags.stream()
            .sorted((a, b) -> {
                int priorityA = FLAG_PRIORITY.getOrDefault(a, 0);
                int priorityB = FLAG_PRIORITY.getOrDefault(b, 0);
                if (priorityA != priorityB) {
                    return Integer.compare(priorityB, priorityA); // descending
                }
                return a.compareTo(b); // alphabetical
            })
            .collect(Collectors.toList());

        return new MergedFlagsResult(
            sortedFlags,
            allFlags.size(),
            "Merged and deduplicated " + allFlags.size() + " unique flags from " +
            countSources(patternFlags, behavioralFlags, amlFlags) + " sources"
        );
    }

    private static int countSources(List<String> pattern, List<String> behavioral, List<String> aml) {
        int count = 0;
        if (pattern != null && !pattern.isEmpty()) count++;
        if (behavioral != null && !behavioral.isEmpty()) count++;
        if (aml != null && !aml.isEmpty()) count++;
        return count;
    }

    public static class MergedFlagsResult {
        public List<String> combinedFlags;
        public int totalUniqueFlags;
        public String message;

        public MergedFlagsResult(List<String> combinedFlags, int totalUniqueFlags, String message) {
            this.combinedFlags = combinedFlags;
            this.totalUniqueFlags = totalUniqueFlags;
            this.message = message;
        }
    }
}

