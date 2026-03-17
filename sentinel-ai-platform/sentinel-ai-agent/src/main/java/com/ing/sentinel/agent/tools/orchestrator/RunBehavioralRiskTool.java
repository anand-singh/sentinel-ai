package com.ing.sentinel.agent.tools.orchestrator;

import com.google.adk.agents.BaseAgent;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.tools.Annotations.Schema;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.ing.sentinel.agent.agents.BehavioralRiskDetector;

import java.util.logging.Logger;

/**
 * Orchestrator Tool — Step 2: Run Behavioral Risk Detector
 *
 * Invokes the BehavioralRiskDetector LlmAgent in-process.
 * Returns JSON with behavioral_risk_score, flags, and feature contributions.
 */
public class RunBehavioralRiskTool {

    private static final Logger logger = Logger.getLogger(RunBehavioralRiskTool.class.getName());
    private static final String USER_ID = "orchestrator";

    /**
     * Runs the Behavioral Risk Detector agent on the given transaction + customer profile.
     *
     * @param transactionWithProfileJson Transaction JSON enriched with customer_profile_snapshot
     * @return Behavioral risk result JSON (behavioral_risk_score, flags, reasoning, feature_contributions, version)
     */
    @Schema(
        name = "run_behavioral_risk",
        description = "Runs the Behavioral Risk Detector (Agent #2) on the transaction and customer profile. " +
                      "Returns JSON with behavioral_risk_score (0-100), flags (e.g. NEW_DEVICE, UNUSUAL_TIME, GEO_DEVIATION), " +
                      "customer-specific reasoning, and feature_contributions. Call after Pattern Analyzer."
    )
    public static String runBehavioralRisk(
            @Schema(name = "transaction_with_profile_json",
                    description = "Transaction JSON enriched with customer_profile_snapshot field containing " +
                                  "the customer's historical behavior data (avg amounts, devices, hours, geo, merchants).")
            String transactionWithProfileJson) {

        logger.info("🧩 [Orchestrator] Running Behavioral Risk Detector...");

        try {
            BaseAgent agent = BehavioralRiskDetector.initAgent();
            if (agent == null) {
                return "{\"error\": \"BehavioralRiskDetector agent failed to initialize\"}";
            }

            InMemoryRunner runner = new InMemoryRunner(agent);
            Session session = runner.sessionService()
                    .createSession(agent.name(), USER_ID)
                    .blockingGet();

            Content userMsg = Content.fromParts(Part.fromText(transactionWithProfileJson));
            var events = runner.runAsync(USER_ID, session.id(), userMsg);

            StringBuilder result = new StringBuilder();
            events.blockingForEach(event -> {
                String content = event.stringifyContent();
                if (content != null && !content.isBlank()) {
                    result.append(content);
                }
            });

            String output = result.toString().trim();
            logger.info("✅ [Orchestrator] Behavioral Risk Detector complete. Output length: " + output.length());
            return output.isEmpty() ? "{\"error\": \"No output from BehavioralRiskDetector\"}" : output;

        } catch (Exception e) {
            logger.severe("❌ [Orchestrator] Behavioral Risk Detector failed: " + e.getMessage());
            return "{\"error\": \"BehavioralRiskDetector exception: " + e.getMessage() + "\"}";
        }
    }
}
