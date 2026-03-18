package com.ing.sentinel.agent.tools.evidence;

import java.util.*;

/**
 * Manual Tool Verification
 *
 * Simple standalone program to verify Evidence Builder tools work correctly.
 * Run with: mvn exec:java -Dexec.mainClass="com.ing.sentinel.agent.tools.evidence.ManualToolVerification"
 */
public class ManualToolVerification {

    public static void main(String[] args) {
        System.out.println("🧪 Evidence Builder Tools - Manual Verification\n");

        // Test 1: Flag Merger
        System.out.println("═══════════════════════════════════════");
        System.out.println("Test 1: FlagMergerTool");
        System.out.println("═══════════════════════════════════════");
        testFlagMerger();

        // Test 2: Summary Composer
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("Test 2: SummaryComposerTool");
        System.out.println("═══════════════════════════════════════");
        testSummaryComposer();

        // Test 3: Evidence Builder
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("Test 3: EvidenceBuilderTool");
        System.out.println("═══════════════════════════════════════");
        testEvidenceBuilder();

        // Test 4: Edge Cases
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("Test 4: Edge Cases");
        System.out.println("═══════════════════════════════════════");
        testEdgeCases();

        System.out.println("\n✅ All manual tests completed!");
    }

    private static void testFlagMerger() {
        List<String> patternFlags = Arrays.asList("AMOUNT_SPIKE", "GEO_MISMATCH", "VELOCITY_HIGH");
        List<String> behavioralFlags = Arrays.asList("NEW_DEVICE", "UNUSUAL_TIME", "AMOUNT_SPIKE");
        List<String> amlFlags = Arrays.asList("PEP_MATCH_PENDING");

        FlagMergerTool.MergedFlagsResult result = FlagMergerTool.mergeFlags(
            patternFlags, behavioralFlags, amlFlags
        );

        System.out.println("Input:");
        System.out.println("  Pattern: " + patternFlags);
        System.out.println("  Behavioral: " + behavioralFlags);
        System.out.println("  AML: " + amlFlags);
        System.out.println("\nOutput:");
        System.out.println("  Total Unique Flags: " + result.totalUniqueFlags);
        System.out.println("  Combined Flags (sorted by priority): " + result.combinedFlags);
        System.out.println("  Message: " + result.message);

        // Verify PEP_MATCH_PENDING is first (highest priority)
        if (!result.combinedFlags.isEmpty() && "PEP_MATCH_PENDING".equals(result.combinedFlags.get(0))) {
            System.out.println("  ✅ Priority sorting correct (PEP_MATCH_PENDING first)");
        } else {
            System.out.println("  ❌ Priority sorting incorrect");
        }

        // Verify deduplication
        if (result.totalUniqueFlags == 6) {
            System.out.println("  ✅ Deduplication correct (6 unique flags)");
        } else {
            System.out.println("  ❌ Deduplication incorrect (expected 6, got " + result.totalUniqueFlags + ")");
        }
    }

    private static void testSummaryComposer() {
        String patternReasoning = "Transaction amount 4.2σ above MCC baseline; 5 transactions in 10 minutes.";
        String behavioralReasoning = "Device fingerprint never seen; transaction at 3:47 AM (customer usually inactive).";
        String amlReasoning = "Customer matched against PEP list; pending analyst verification.";
        List<String> flags = Arrays.asList("PEP_MATCH_PENDING", "AMOUNT_SPIKE", "NEW_DEVICE",
                                           "UNUSUAL_TIME", "VELOCITY_HIGH");

        SummaryComposerTool.SummaryResult result = SummaryComposerTool.composeSummary(
            patternReasoning, behavioralReasoning, amlReasoning, flags
        );

        System.out.println("Input Flags: " + flags);
        System.out.println("\nOutput:");
        System.out.println("  Evidence Summary: " + result.evidenceSummary);
        System.out.println("  Phrase Count: " + result.phraseCount);

        if (result.evidenceSummary != null && result.evidenceSummary.length() > 0) {
            System.out.println("  ✅ Summary generated");
        } else {
            System.out.println("  ❌ Summary generation failed");
        }
    }

    private static void testEvidenceBuilder() {
        String summary = "High amount vs pattern, high velocity; New device, off-hours; AML check pending.";
        List<String> flags = Arrays.asList("PEP_MATCH_PENDING", "AMOUNT_SPIKE", "NEW_DEVICE",
                                           "UNUSUAL_TIME", "VELOCITY_HIGH");
        String patternReasoning = "Transaction amount 4.2σ above MCC baseline; 5 transactions in 10 minutes.";
        String behavioralReasoning = "Device fingerprint never seen; off-hours transaction.";
        String amlReasoning = "PEP match pending verification.";

        EvidenceBuilderTool.EvidenceBundleResult result = EvidenceBuilderTool.buildEvidenceBundle(
            85, 78, 55, summary, flags, patternReasoning, behavioralReasoning, amlReasoning
        );

        System.out.println("Input Scores: Pattern=85, Behavioral=78, AML=55");
        System.out.println("\nOutput:");
        System.out.println("  Evidence Summary: " + result.evidenceSummary);
        System.out.println("  Combined Flags: " + result.combinedFlags);
        System.out.println("  Agent Scores: " + result.agentScores);
        System.out.println("  Explanation Items Count: " + result.explanationItems.size());
        System.out.println("  Version: " + result.version);
        System.out.println("  Config Version: " + result.configVersion);

        boolean allChecks = true;

        // Verify all scores present
        if (result.agentScores.containsKey("pattern_score") &&
            result.agentScores.containsKey("behavioral_score") &&
            result.agentScores.containsKey("aml_score")) {
            System.out.println("  ✅ All agent scores present");
        } else {
            System.out.println("  ❌ Missing agent scores");
            allChecks = false;
        }

        // Verify explanation items
        if (result.explanationItems.size() == 3) {
            System.out.println("  ✅ All explanation items present");
        } else {
            System.out.println("  ❌ Incorrect explanation items count");
            allChecks = false;
        }

        // Verify version info
        if ("evidence-v1.0.0".equals(result.version) &&
            "evidence-policy-2026-03-16".equals(result.configVersion)) {
            System.out.println("  ✅ Version info correct");
        } else {
            System.out.println("  ❌ Version info incorrect");
            allChecks = false;
        }

        if (allChecks) {
            System.out.println("\n  ✅ All evidence bundle checks passed");
        }
    }

    private static void testEdgeCases() {
        System.out.println("Testing edge cases...\n");

        // Test 1: Null inputs
        System.out.println("1. Null inputs to FlagMerger:");
        FlagMergerTool.MergedFlagsResult result1 = FlagMergerTool.mergeFlags(null, null, null);
        System.out.println("   Total Flags: " + result1.totalUniqueFlags + " (expected: 0)");
        System.out.println("   " + (result1.totalUniqueFlags == 0 ? "✅ PASS" : "❌ FAIL"));

        // Test 2: Partial inputs
        System.out.println("\n2. Pattern-only input to SummaryComposer:");
        List<String> flags = Arrays.asList("AMOUNT_SPIKE");
        SummaryComposerTool.SummaryResult result2 = SummaryComposerTool.composeSummary(
            "Amount spike detected", null, null, flags
        );
        System.out.println("   Summary: " + result2.evidenceSummary);
        System.out.println("   " + (result2.evidenceSummary != null ? "✅ PASS" : "❌ FAIL"));

        // Test 3: Empty flags
        System.out.println("\n3. Empty flags:");
        FlagMergerTool.MergedFlagsResult result3 = FlagMergerTool.mergeFlags(
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        System.out.println("   Total Flags: " + result3.totalUniqueFlags + " (expected: 0)");
        System.out.println("   " + (result3.totalUniqueFlags == 0 ? "✅ PASS" : "❌ FAIL"));

        // Test 4: Long reasoning (should be truncated)
        System.out.println("\n4. Very long reasoning text:");
        String longReasoning = "This is a very long reasoning text that exceeds one hundred characters and should be truncated by the evidence builder tool to maintain concise outputs.";
        EvidenceBuilderTool.EvidenceBundleResult result4 = EvidenceBuilderTool.buildEvidenceBundle(
            50, -1, -1, "Summary", Arrays.asList("TEST_FLAG"), longReasoning, null, null
        );
        boolean truncated = result4.explanationItems.get(0).details.length() <= 100;
        System.out.println("   Original length: " + longReasoning.length());
        System.out.println("   Stored length: " + result4.explanationItems.get(0).details.length());
        System.out.println("   " + (truncated ? "✅ PASS (truncated)" : "❌ FAIL (not truncated)"));
    }
}

