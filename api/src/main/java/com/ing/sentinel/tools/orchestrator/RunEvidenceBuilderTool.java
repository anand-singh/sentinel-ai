package com.ing.sentinel.tools.orchestrator;

import com.google.adk.agents.BaseAgent;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.tools.Annotations.Schema;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.ing.sentinel.agents.EvidenceBuilderAgent;

import java.util.logging.Logger;

/**
 * Orchestrator Tool — Step 3: Run Evidence Builder
 *
 * Invokes the EvidenceBuilderAgent LlmAgent in-process with the outputs
 * from both the Pattern Analyzer and Behavioral Risk Detector.
 * Returns an auditable evidence bundle with combined flags and summary.
 */
public class RunEvidenceBuilderTool {

    private static final Logger logger = Logger.getLogger(RunEvidenceBuilderTool.class.getName());
    private static final String USER_ID = "orchestrator";

    /**
     * Runs the Evidence Builder agent on the combined agent outputs.
     *
     * @param evidenceInputJson JSON combining pattern_agent_output and behavioral_agent_output (and optional aml_agent_output)
     * @return Evidence bundle JSON (evidence_summary, combined_flags, agent_scores, explanation_items, version)
     */
    @Schema(
        name = "run_evidence_builder",
        description = "Runs the Evidence Builder (Agent #3) to combine outputs from the Pattern Analyzer and " +
                      "Behavioral Risk Detector into a clear, auditable evidence bundle. " +
                      "Input JSON must contain: transaction_id, customer_id, pattern_agent_output, behavioral_agent_output, " +
                      "and optionally aml_agent_output. Returns evidence_summary, combined_flags, agent_scores, and explanation_items."
    )
    public static String runEvidenceBuilder(
            @Schema(name = "evidence_input_json",
                    description = "JSON object containing transaction_id, customer_id, and the outputs of the upstream agents: " +
                                  "pattern_agent_output (from Agent #1) and behavioral_agent_output (from Agent #2). " +
                                  "Also accepts an optional aml_agent_output field.")
            String evidenceInputJson) {

        logger.info("🧾 [Orchestrator] Running Evidence Builder...");

        try {
            BaseAgent agent = EvidenceBuilderAgent.initAgent();
            if (agent == null) {
                return "{\"error\": \"EvidenceBuilderAgent failed to initialize\"}";
            }

            InMemoryRunner runner = new InMemoryRunner(agent);
            Session session = runner.sessionService()
                    .createSession(agent.name(), USER_ID)
                    .blockingGet();

            Content userMsg = Content.fromParts(Part.fromText(evidenceInputJson));
            var events = runner.runAsync(USER_ID, session.id(), userMsg);

            StringBuilder result = new StringBuilder();
            events.blockingForEach(event -> {
                String content = event.stringifyContent();
                if (content != null && !content.isBlank()) {
                    result.append(content);
                }
            });

            String output = result.toString().trim();
            logger.info("✅ [Orchestrator] Evidence Builder complete. Output length: " + output.length());
            return output.isEmpty() ? "{\"error\": \"No output from EvidenceBuilder\"}" : output;

        } catch (Exception e) {
            logger.severe("❌ [Orchestrator] Evidence Builder failed: " + e.getMessage());
            return "{\"error\": \"EvidenceBuilder exception: " + e.getMessage() + "\"}";
        }
    }
}
