package com.ing.sentinel.tools.orchestrator;

import com.google.adk.agents.BaseAgent;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.tools.Annotations.Schema;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.ing.sentinel.agents.ActionExecutor;

import java.util.logging.Logger;

/**
 * Orchestrator Tool — Step 5: Run Action Executor
 *
 * Invokes the ActionExecutor LlmAgent in-process with the final risk decision
 * from the Aggregated Risk Scorer (Agent #4).
 * Executes policy-governed actions (freeze, notify, report, step-up auth, escalate).
 * Returns an auditable execution result with all actions taken.
 */
public class RunActionExecutorTool {

    private static final Logger logger = Logger.getLogger(RunActionExecutorTool.class.getName());
    private static final String USER_ID = "orchestrator";

    /**
     * Runs the Action Executor agent on the final risk decision from Agent #4.
     *
     * @param riskDecisionJson Final risk decision JSON from Aggregated Risk Scorer:
     *                         transaction_id, customer_id, final_risk_score, severity,
     *                         recommended_action, explanation, correlation_id
     * @return Action execution result JSON (executed_actions, status, audit_id, policy_version, details)
     */
    @Schema(
        name = "run_action_executor",
        description = "Runs the Action Executor (Agent #5) — the final pipeline step. " +
                      "Receives the final risk decision from the Aggregated Risk Scorer and executes " +
                      "policy-approved actions based on severity and recommended_action. " +
                      "Policy mapping: LOW+ALLOW → no action; MED+REVIEW → create_case_report; " +
                      "HIGH+CHALLENGE → request_step_up_auth + create_case_report; " +
                      "CRITICAL+BLOCK → freeze_transaction + notify_security_team + create_case_report. " +
                      "Input JSON must include: transaction_id, customer_id, final_risk_score, severity, " +
                      "recommended_action, explanation, correlation_id. " +
                      "Returns executed_actions, status, audit_id, policy_version, and per-action details."
    )
    public static String runActionExecutor(
            @Schema(name = "risk_decision_json",
                    description = "Final risk decision JSON from Agent #4 (AggregatedRiskScorer), containing: " +
                                  "transaction_id, customer_id, final_risk_score (0-100), " +
                                  "severity (LOW/MED/HIGH/CRITICAL), recommended_action (ALLOW/REVIEW/CHALLENGE/BLOCK), " +
                                  "explanation (short string), and correlation_id (for tracing).")
            String riskDecisionJson) {

        logger.info("⚡ [Orchestrator] Running Action Executor...");

        try {
            BaseAgent agent = ActionExecutor.initAgent();
            if (agent == null) {
                return "{\"error\": \"ActionExecutor agent failed to initialize\"}";
            }

            InMemoryRunner runner = new InMemoryRunner(agent);
            Session session = runner.sessionService()
                    .createSession(agent.name(), USER_ID)
                    .blockingGet();

            Content userMsg = Content.fromParts(Part.fromText(riskDecisionJson));
            var events = runner.runAsync(USER_ID, session.id(), userMsg);

            StringBuilder result = new StringBuilder();
            events.blockingForEach(event -> {
                String content = event.stringifyContent();
                if (content != null && !content.isBlank()) {
                    result.append(content);
                }
            });

            String output = result.toString().trim();
            logger.info("✅ [Orchestrator] Action Executor complete. Output length: " + output.length());
            return output.isEmpty() ? "{\"error\": \"No output from ActionExecutor\"}" : output;

        } catch (Exception e) {
            logger.severe("❌ [Orchestrator] Action Executor failed: " + e.getMessage());
            return "{\"error\": \"ActionExecutor exception: " + e.getMessage() + "\"}";
        }
    }
}
