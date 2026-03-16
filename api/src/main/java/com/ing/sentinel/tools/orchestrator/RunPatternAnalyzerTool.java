package com.ing.sentinel.tools.orchestrator;

import com.google.adk.agents.BaseAgent;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.tools.Annotations.Schema;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.ing.sentinel.agents.TransactionPatternAnalyzer;

import java.util.logging.Logger;

/**
 * Orchestrator Tool — Step 1: Run Pattern Analyzer
 *
 * Invokes the TransactionPatternAnalyzer LlmAgent in-process.
 * Returns the JSON result containing risk_score, flags, and reasoning.
 */
public class RunPatternAnalyzerTool {

    private static final Logger logger = Logger.getLogger(RunPatternAnalyzerTool.class.getName());
    private static final String USER_ID = "orchestrator";

    /**
     * Runs the Transaction Pattern Analyzer agent on the given transaction payload.
     *
     * @param transactionJson Full transaction JSON as a string
     * @return Pattern analysis result JSON (risk_score, flags, reasoning, feature_contributions, version)
     */
    @Schema(
        name = "run_pattern_analyzer",
        description = "Runs the Transaction Pattern Analyzer (Agent #1) on the given transaction payload. " +
                      "Returns JSON containing risk_score (0-100), flags (e.g. AMOUNT_SPIKE, GEO_MISMATCH), " +
                      "reasoning, feature_contributions, and version. Call this first in the pipeline."
    )
    public static String runPatternAnalyzer(
            @Schema(name = "transaction_json",
                    description = "Full transaction payload as a JSON string, including amount, merchant, geo, timestamp, etc.")
            String transactionJson) {

        logger.info("🔬 [Orchestrator] Running Pattern Analyzer on transaction...");

        try {
            BaseAgent agent = TransactionPatternAnalyzer.initAgent();
            if (agent == null) {
                return "{\"error\": \"PatternAnalyzer agent failed to initialize\"}";
            }

            InMemoryRunner runner = new InMemoryRunner(agent);
            Session session = runner.sessionService()
                    .createSession(agent.name(), USER_ID)
                    .blockingGet();

            Content userMsg = Content.fromParts(Part.fromText(transactionJson));
            var events = runner.runAsync(USER_ID, session.id(), userMsg);

            StringBuilder result = new StringBuilder();
            events.blockingForEach(event -> {
                String content = event.stringifyContent();
                if (content != null && !content.isBlank()) {
                    result.append(content);
                }
            });

            String output = result.toString().trim();
            logger.info("✅ [Orchestrator] Pattern Analyzer complete. Output length: " + output.length());
            return output.isEmpty() ? "{\"error\": \"No output from PatternAnalyzer\"}" : output;

        } catch (Exception e) {
            logger.severe("❌ [Orchestrator] Pattern Analyzer failed: " + e.getMessage());
            return "{\"error\": \"PatternAnalyzer exception: " + e.getMessage() + "\"}";
        }
    }
}
