package com.ing.sentinel.agent.tools.evidence;

import com.google.adk.tools.Annotations.Schema;

import java.util.*;

/**
 * SummaryComposerTool
 *
 * Composes a human-readable evidence summary from structured inputs from multiple agents.
 * Uses template-based sentence building to maintain factual accuracy and determinism.
 */
public class SummaryComposerTool {

    @Schema(name = "compose_summary", description = "Composes a human-readable evidence summary from agent outputs")
    public static SummaryResult composeSummary(
        @Schema(name = "pattern_reasoning", description = "Reasoning text from Pattern Analyzer agent") String patternReasoning,
        @Schema(name = "behavioral_reasoning", description = "Reasoning text from Behavioral Risk agent") String behavioralReasoning,
        @Schema(name = "aml_reasoning", description = "Reasoning text from AML agent") String amlReasoning,
        @Schema(name = "combined_flags", description = "List of all combined flags from merge_flags") List<String> combinedFlags
    ) {
        List<String> summaryParts = new ArrayList<>();

        // Pattern analysis summary
        if (patternReasoning != null && !patternReasoning.isEmpty()) {
            String patternPhrase = extractKeyPhrase(patternReasoning, combinedFlags, "pattern");
            if (patternPhrase != null) {
                summaryParts.add(patternPhrase);
            }
        }

        // Behavioral analysis summary
        if (behavioralReasoning != null && !behavioralReasoning.isEmpty()) {
            String behavioralPhrase = extractKeyPhrase(behavioralReasoning, combinedFlags, "behavioral");
            if (behavioralPhrase != null) {
                summaryParts.add(behavioralPhrase);
            }
        }

        // AML summary
        if (amlReasoning != null && !amlReasoning.isEmpty()) {
            String amlPhrase = extractKeyPhrase(amlReasoning, combinedFlags, "aml");
            if (amlPhrase != null) {
                summaryParts.add(amlPhrase);
            }
        }

        // Build final summary
        String summary;
        if (summaryParts.isEmpty()) {
            summary = "Transaction analysis completed with no significant risk indicators.";
        } else {
            summary = String.join("; ", summaryParts) + ".";
        }

        return new SummaryResult(summary, summaryParts.size());
    }

    private static String extractKeyPhrase(String reasoning, List<String> flags, String source) {
        // Extract concise key phrases based on the reasoning text and flags
        StringBuilder phrase = new StringBuilder();

        if ("pattern".equals(source)) {
            if (containsFlag(flags, "AMOUNT_SPIKE")) {
                phrase.append("High amount vs pattern");
            }
            if (containsFlag(flags, "GEO_MISMATCH")) {
                if (phrase.length() > 0) phrase.append(", ");
                phrase.append("abnormal location");
            }
            if (containsFlag(flags, "VELOCITY_HIGH")) {
                if (phrase.length() > 0) phrase.append(", ");
                phrase.append("high velocity");
            }
            if (containsFlag(flags, "RARE_MCC")) {
                if (phrase.length() > 0) phrase.append(", ");
                phrase.append("unusual merchant category");
            }
            if (containsFlag(flags, "TIME_ANOMALY")) {
                if (phrase.length() > 0) phrase.append(", ");
                phrase.append("unusual time");
            }
        } else if ("behavioral".equals(source)) {
            if (containsFlag(flags, "NEW_DEVICE")) {
                phrase.append("New device");
            }
            if (containsFlag(flags, "UNUSUAL_TIME")) {
                if (phrase.length() > 0) phrase.append(", ");
                phrase.append("off-hours");
            }
            if (containsFlag(flags, "GEO_DEVIATION")) {
                if (phrase.length() > 0) phrase.append(", ");
                phrase.append("abnormal location distance");
            }
            if (containsFlag(flags, "NEW_IP_RANGE")) {
                if (phrase.length() > 0) phrase.append(", ");
                phrase.append("new IP range");
            }
            if (containsFlag(flags, "MERCHANT_NOVELTY")) {
                if (phrase.length() > 0) phrase.append(", ");
                phrase.append("unusual merchant for customer");
            }
            if (containsFlag(flags, "BURST_ACTIVITY")) {
                if (phrase.length() > 0) phrase.append(", ");
                phrase.append("burst activity");
            }
        } else if ("aml".equals(source)) {
            if (containsFlag(flags, "PEP_MATCH_PENDING")) {
                phrase.append("AML check pending");
            }
            if (containsFlag(flags, "SANCTIONS_HIT")) {
                phrase.append("Sanctions screening hit");
            }
        }

        return phrase.length() > 0 ? phrase.toString() : null;
    }

    private static boolean containsFlag(List<String> flags, String flag) {
        return flags != null && flags.contains(flag);
    }

    public static class SummaryResult {
        public String evidenceSummary;
        public int phraseCount;

        public SummaryResult(String evidenceSummary, int phraseCount) {
            this.evidenceSummary = evidenceSummary;
            this.phraseCount = phraseCount;
        }
    }
}

