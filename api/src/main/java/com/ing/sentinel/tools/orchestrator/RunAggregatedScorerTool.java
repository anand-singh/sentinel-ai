package com.ing.sentinel.tools.orchestrator;

import com.google.adk.agents.BaseAgent;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.tools.Annotations.Schema;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.ing.sentinel.agents.AggregatedRiskScorer;

import java.util.logging.Logger;

/**
 * Orchestrator Tool — Step 4: Run Aggregated Risk Scorer
 *
 * Invokes the AggregatedRiskScorer LlmAgent in-process with outputs
 * from agents #1, #2, #3 (and optional AML).
 * Returns the final calibrated risk score, severity, and recommended action.
 */
public class RunAggregatedScorerTool {

    private static final Logger logger = Logger.getLogger(RunAggregatedScorerTool.class.getName());
    private static final String USER_ID = "orchestrator";

    /**
     * Runs the Aggregated Risk Scorer on combined upstream agent outputs.
     *
     * @param aggregatorInputJson JSON with fields: transaction_id, customer_id, pattern_agent, behavioral_agent,
     *                            evidence_agent, and optionally aml_agent
     * @return Aggregated result JSON (final_risk_score, severity, recommended_action, source_contributions, explanation, version)
     */
    @Schema(
        name = "run_aggregated_scorer",
        description = "Runs the Aggregated Risk Scorer (Agent #4) to produce a single calibrated risk score (0-100), " +
                      "severity label (LOW/MED/HIGH/CRITICAL), and recommended action (ALLOW/REVIEW/CHALLENGE/BLOCK). " +
                      "Input JSON must include: transaction_id, customer_id, and the outputs of agents #1, #2, #3 as " +
                      "pattern_agent, behavioral_agent, evidence_agent (and optionally aml_agent). " +
                      "Returns final_risk_score, severity, recommended_action, source_contributions, explanation."
    )
    public static String runAggregatedScorer(
            @Schema(name = "aggregator_input_json",
                    description = "JSON object containing transaction_id, customer_id, and the outputs of upstream agents: " +
                                  "pattern_agent (Agent #1 result), behavioral_agent (Agent #2 result), " +
                                  "evidence_agent (Agent #3 result), and optionally aml_agent (AML result).")
            String aggregatorInputJson) {

        logger.info("🎛️ [Orchestrator] Running Aggregated Risk Scorer...");

        try {
            BaseAgent agent = AggregatedRiskScorer.initAgent();
            if (agent == null) {
                return "{\"error\": \"AggregatedRiskScorer agent failed to initialize\"}";
            }

            InMemoryRunner runner = new InMemoryRunner(agent);
            Session session = runner.sessionService()
                    .createSession(agent.name(), USER_ID)
                    .blockingGet();

            Content userMsg = Content.fromParts(Part.fromText(aggregatorInputJson));
            var events = runner.runAsync(USER_ID, session.id(), userMsg);

            StringBuilder result = new StringBuilder();
            events.blockingForEach(event -> {
                String content = event.stringifyContent();
                if (content != null && !content.isBlank()) {
                    result.append(content);
                }
            });

            String output = result.toString().trim();
            logger.info("✅ [Orchestrator] Aggregated Scorer complete. Output length: " + output.length());
            return output.isEmpty() ? "{\"error\": \"No output from AggregatedRiskScorer\"}" : output;

        } catch (Exception e) {
            logger.severe("❌ [Orchestrator] Aggregated Risk Scorer failed: " + e.getMessage());
            return "{\"error\": \"AggregatedRiskScorer exception: " + e.getMessage() + "\"}";
        }
    }
}
