package com.ing.sentinel.agent.tools.evidence;

import com.google.adk.tools.Annotations.Schema;

import java.util.*;

/**
 * EvidenceBuilderTool
 *
 * Builds a comprehensive evidence bundle from multiple agent outputs.
 * Combines scores, flags, and explanations into a structured, auditable format.
 */
public class EvidenceBuilderTool {

    private static final String VERSION = "evidence-v1.0.0";
    private static final String CONFIG_VERSION = "evidence-policy-2026-03-16";

    @Schema(name = "build_evidence_bundle", description = "Builds a complete evidence bundle from all agent outputs with scores, flags, and explanations")
    public static EvidenceBundleResult buildEvidenceBundle(
        @Schema(name = "pattern_score", description = "Risk score from Pattern Analyzer agent (0-100)") int patternScore,
        @Schema(name = "behavioral_score", description = "Risk score from Behavioral Risk agent (0-100)") int behavioralScore,
        @Schema(name = "aml_score", description = "Risk score from AML agent (0-100)") int amlScore,
        @Schema(name = "evidence_summary", description = "Human-readable summary from compose_summary") String evidenceSummary,
        @Schema(name = "combined_flags", description = "Merged and sorted flags from merge_flags") List<String> combinedFlags,
        @Schema(name = "pattern_reasoning", description = "Original reasoning from Pattern Analyzer") String patternReasoning,
        @Schema(name = "behavioral_reasoning", description = "Original reasoning from Behavioral Risk agent") String behavioralReasoning,
        @Schema(name = "aml_reasoning", description = "Original reasoning from AML agent") String amlReasoning
    ) {
        // Build agent scores map
        Map<String, Integer> agentScores = new LinkedHashMap<>();
        if (patternScore >= 0) {
            agentScores.put("pattern_score", patternScore);
        }
        if (behavioralScore >= 0) {
            agentScores.put("behavioral_score", behavioralScore);
        }
        if (amlScore >= 0) {
            agentScores.put("aml_score", amlScore);
        }

        // Build explanation items
        List<ExplanationItem> explanationItems = new ArrayList<>();

        if (patternReasoning != null && !patternReasoning.trim().isEmpty()) {
            explanationItems.add(new ExplanationItem(
                "pattern_agent",
                summarizeReasoning(patternReasoning)
            ));
        }

        if (behavioralReasoning != null && !behavioralReasoning.trim().isEmpty()) {
            explanationItems.add(new ExplanationItem(
                "behavioral_agent",
                summarizeReasoning(behavioralReasoning)
            ));
        }

        if (amlReasoning != null && !amlReasoning.trim().isEmpty()) {
            explanationItems.add(new ExplanationItem(
                "aml_agent",
                summarizeReasoning(amlReasoning)
            ));
        }

        return new EvidenceBundleResult(
            evidenceSummary,
            combinedFlags != null ? combinedFlags : new ArrayList<>(),
            agentScores,
            explanationItems,
            VERSION,
            CONFIG_VERSION
        );
    }

    private static String summarizeReasoning(String reasoning) {
        // Keep reasoning concise - limit to first sentence or 100 chars
        if (reasoning.length() <= 100) {
            return reasoning;
        }

        int firstPeriod = reasoning.indexOf('.');
        if (firstPeriod > 0 && firstPeriod <= 100) {
            return reasoning.substring(0, firstPeriod + 1);
        }

        return reasoning.substring(0, 97) + "...";
    }

    public static class EvidenceBundleResult {
        public String evidenceSummary;
        public List<String> combinedFlags;
        public Map<String, Integer> agentScores;
        public List<ExplanationItem> explanationItems;
        public String version;
        public String configVersion;

        public EvidenceBundleResult(
            String evidenceSummary,
            List<String> combinedFlags,
            Map<String, Integer> agentScores,
            List<ExplanationItem> explanationItems,
            String version,
            String configVersion
        ) {
            this.evidenceSummary = evidenceSummary;
            this.combinedFlags = combinedFlags;
            this.agentScores = agentScores;
            this.explanationItems = explanationItems;
            this.version = version;
            this.configVersion = configVersion;
        }
    }

    public static class ExplanationItem {
        public String source;
        public String details;

        public ExplanationItem(String source, String details) {
            this.source = source;
            this.details = details;
        }
    }
}

