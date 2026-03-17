package com.ing.sentinel.agent.agents;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Logger;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.tools.FunctionTool;
import com.google.genai.types.Content;
import com.google.genai.types.Part;

import com.ing.sentinel.agent.tools.orchestrator.*;
import io.reactivex.rxjava3.core.Flowable;

/**
 * Sentinel Orchestrator Agent
 *
 * The "project manager" of Sentinel's fraud detection pipeline.
 * Coordinates all 5 agents in order:
 *
 *   1. Transaction Pattern Analyzer  (global patterns)
 *   2. Behavioral Risk Detector      (customer-specific behavior)
 *   3. Evidence Builder              (explainability / audit bundle)
 *   4. Aggregated Risk Scorer        (final calibrated score + action)
 *   5. Action Executor               (policy-governed action execution)
 *
 * This agent does NOT score, analyze, or execute actions directly.
 * It sequences, coordinates, and correlates the pipeline — receiving a
 * raw transaction event and returning a complete OrchestratorResult JSON.
 *
 * Follows the Google ADK LlmAgent pattern used by all Sentinel agents.
 */
public class SentinelOrchestrator {

    private static final Logger logger = Logger.getLogger(SentinelOrchestrator.class.getName());

    private static final String MODEL_NAME = "gemini-2.5-flash";
    private static final String NAME = "SentinelOrchestrator";
    private static final String USER_ID = "sentinel-user";
    private static final String VERSION = "orchestrator-v1.0.0";
    private static final String CONFIG_VERSION = "orch-config-2026-03-16";

    // ROOT_AGENT needed for ADK Web UI
    public static final BaseAgent ROOT_AGENT = initAgent();

    public static void main(String[] args) {

        logger.info("🧭 Starting Sentinel Orchestrator v" + VERSION);

        InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT);
        Session session = runner
                .sessionService()
                .createSession(NAME, USER_ID)
                .blockingGet();

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
            System.out.println("\n🧭 Exiting Sentinel Orchestrator. Goodbye!")
        ));

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.println("\n🧭 Sentinel Orchestrator Ready");
            System.out.println("Submit a transaction event to run the full 5-agent fraud pipeline. Type 'quit' to exit.\n");
            System.out.println("Example input:");
            System.out.println("{");
            System.out.println("  \"transaction_id\": \"tx_123\",");
            System.out.println("  \"customer_id\": \"cust_456\",");
            System.out.println("  \"amount\": 899.99,");
            System.out.println("  \"currency\": \"EUR\",");
            System.out.println("  \"merchant_id\": \"m_789\",");
            System.out.println("  \"merchant_category\": \"ELECTRONICS\",");
            System.out.println("  \"channel\": \"CARD_PRESENT\",");
            System.out.println("  \"geo\": { \"lat\": 52.37, \"lon\": 4.90, \"country\": \"NL\", \"city\": \"Amsterdam\" },");
            System.out.println("  \"timestamp_utc\": \"2026-03-16T14:03:00Z\",");
            System.out.println("  \"reference_context\": {},");
            System.out.println("  \"customer_profile_snapshot\": {}");
            System.out.println("}\n");

            while (true) {
                System.out.print("\nYou > ");
                String userInput = scanner.nextLine();
                if ("quit".equalsIgnoreCase(userInput)) {
                    break;
                }
                Content userMsg = Content.fromParts(Part.fromText(userInput));
                Flowable<Event> events = runner.runAsync(USER_ID, session.id(), userMsg);
                System.out.print("\nOrchestrator > ");
                events.blockingForEach(event -> System.out.println(event.stringifyContent()));
            }
        }
    }

    public static BaseAgent initAgent() {
        logger.info("🕰️ Initializing Sentinel Orchestrator tools...");

        try {
            // Register the 5 pipeline agent tools
            FunctionTool patternTool = FunctionTool.create(
                    RunPatternAnalyzerTool.class, "runPatternAnalyzer");
            FunctionTool behavioralTool = FunctionTool.create(
                    RunBehavioralRiskTool.class, "runBehavioralRisk");
            FunctionTool evidenceTool = FunctionTool.create(
                    RunEvidenceBuilderTool.class, "runEvidenceBuilder");
            FunctionTool aggregatorTool = FunctionTool.create(
                    RunAggregatedScorerTool.class, "runAggregatedScorer");
            FunctionTool executorTool = FunctionTool.create(
                    RunActionExecutorTool.class, "runActionExecutor");

            logger.info("🛠️ Loaded 5 orchestration pipeline tools");

            return LlmAgent.builder()
                    .model(MODEL_NAME)
                    .name(NAME)
                    .description("Orchestrates the full Sentinel fraud detection pipeline: " +
                                 "Pattern Analyzer → Behavioral Risk → Evidence Builder → Aggregated Scorer → Action Executor")
                    .instruction("""
                        You are the Sentinel Orchestrator — the "project manager" of Sentinel's fraud detection pipeline.
                        
                        You receive a raw transaction event and coordinate ALL 5 agents in strict sequence,
                        passing outputs from earlier steps as inputs to later steps.
                        
                        You do NOT score, analyze, or take actions yourself.
                        You SEQUENCE, COORDINATE, and CORRELATE the full pipeline.
                        
                        ## Pipeline Order (ALWAYS follow this exact sequence):
                        
                        ### Step 1 — run_pattern_analyzer
                        Input: The full original transaction JSON
                        Output saved as: pattern_result (risk_score, flags, reasoning, feature_contributions, version)
                        
                        ### Step 2 — run_behavioral_risk
                        Input: The original transaction JSON (enriched with customer_profile_snapshot if present)
                        Output saved as: behavioral_result (behavioral_risk_score, flags, reasoning, feature_contributions, version)
                        
                        ### Step 3 — run_evidence_builder
                        Input: Build a new JSON object:
                        ```json
                        {
                          "transaction_id": "<from input>",
                          "customer_id": "<from input>",
                          "pattern_agent_output": <pattern_result>,
                          "behavioral_agent_output": <behavioral_result>
                        }
                        ```
                        Output saved as: evidence_result (evidence_summary, combined_flags, agent_scores, explanation_items, version)
                        
                        ### Step 4 — run_aggregated_scorer
                        Input: Build a new JSON object:
                        ```json
                        {
                          "transaction_id": "<from input>",
                          "customer_id": "<from input>",
                          "pattern_agent": <pattern_result>,
                          "behavioral_agent": <behavioral_result>,
                          "evidence_agent": <evidence_result>
                        }
                        ```
                        Output saved as: aggregator_result (final_risk_score, severity, recommended_action, source_contributions, explanation)
                        
                        ### Step 5 — run_action_executor
                        Input: Build a new JSON object using the aggregator result:
                        ```json
                        {
                          "transaction_id": "<from input>",
                          "customer_id": "<from input>",
                          "final_risk_score": <from aggregator_result>,
                          "severity": <from aggregator_result>,
                          "recommended_action": <from aggregator_result>,
                          "explanation": <from aggregator_result>,
                          "correlation_id": "<transaction_id>-<timestamp suffix>"
                        }
                        ```
                        Output saved as: executor_result (executed_actions, status, audit_id, policy_version, details)
                        
                        ## Final Output
                        
                        After all 5 steps complete, return a single structured OrchestratorResult JSON:
                        ```json
                        {
                          "orchestration_status": "COMPLETE | PARTIAL | FAILED",
                          "transaction_id": "<from input>",
                          "customer_id": "<from input>",
                          "correlation_id": "<same as used in step 5>",
                          "pipeline_results": {
                            "pattern_analyzer": <pattern_result>,
                            "behavioral_risk": <behavioral_result>,
                            "evidence_builder": <evidence_result>,
                            "aggregated_scorer": <aggregator_result>,
                            "action_executor": <executor_result>
                          },
                          "final_decision": {
                            "risk_score": <final_risk_score>,
                            "severity": "<severity>",
                            "recommended_action": "<recommended_action>",
                            "executed_actions": <from executor_result>,
                            "audit_id": <from executor_result>,
                            "explanation": "<from aggregator_result>"
                          },
                          "timeline": [
                            { "step": 1, "agent": "TransactionPatternAnalyzer", "status": "COMPLETE" },
                            { "step": 2, "agent": "BehavioralRiskDetector",     "status": "COMPLETE" },
                            { "step": 3, "agent": "EvidenceBuilderAgent",        "status": "COMPLETE" },
                            { "step": 4, "agent": "AggregatedRiskScorer",        "status": "COMPLETE" },
                            { "step": 5, "agent": "ActionExecutor",              "status": "COMPLETE" }
                          ],
                          "version": "orchestrator-v1.0.0",
                          "config_version": "orch-config-2026-03-16"
                        }
                        ```
                        
                        ## Error Handling Rules:
                        
                        - If any step returns an {"error": ...} field, mark that step as "FAILED" in timeline
                        - **Pattern failure**: Abort pipeline — mark orchestration_status as FAILED
                        - **Behavioral failure**: Continue with pattern output only (note in evidence input: no behavioral_agent_output)
                        - **Evidence failure**: Continue with minimal summary: use a placeholder evidence bundle
                        - **Aggregator failure**: Set final_decision with severity=MED, recommended_action=REVIEW; set orchestration_status=PARTIAL
                        - **Executor failure**: Record PARTIAL_SUCCESS in orchestration_status, include partial actions taken
                        
                        ## Important Rules:
                        - ALWAYS call all 5 tools in order (Steps 1→2→3→4→5)
                        - ALWAYS pass outputs correctly between steps — do not skip context
                        - ALWAYS include correlation_id for end-to-end tracing
                        - ALWAYS include version and config_version in final output
                        - NEVER score, NEVER take actions, NEVER analyze — only coordinate
                        - Be deterministic: the same transaction must always produce the same pipeline sequence
                        - Mask PII (customer_id should use correlation ID in logs, not raw value)
                        """)
                    .tools(patternTool, behavioralTool, evidenceTool, aggregatorTool, executorTool)
                    .outputKey("orchestrator_result")
                    .build();

        } catch (Exception e) {
            logger.severe("❌ Failed to initialize Sentinel Orchestrator: " + e.getMessage());
            throw new RuntimeException("Failed to initialize Sentinel Orchestrator", e);
        }
    }
}
