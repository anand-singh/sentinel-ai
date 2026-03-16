package com.ing.sentinel.agents;

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

import io.reactivex.rxjava3.core.Flowable;

/**
 * Evidence Builder Agent (Explainability Agent)
 *
 * The third agent in Sentinel's fraud pipeline. Combines outputs from upstream agents
 * (Pattern Analyzer, Behavioral Risk, AML/Compliance) and produces a clear, auditable,
 * human-readable explanation for why a transaction looks risky.
 *
 * This agent does NOT score or take actions. It aggregates, normalizes, and explains.
 */
public class EvidenceBuilderAgent {

    private static final Logger logger = Logger.getLogger(EvidenceBuilderAgent.class.getName());

    private static final String MODEL_NAME = "gemini-2.5-flash";
    private static final String NAME = "EvidenceBuilderAgent";
    private static final String USER_ID = "sentinel-user";
    private static final String VERSION = "evidence-v1.0.0";

    // ROOT_AGENT needed for ADK Web UI
    public static final BaseAgent ROOT_AGENT = initAgent();

    public static void main(String[] args) {
        logger.info("🧾 Starting Evidence Builder Agent v" + VERSION);

        InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT);
        Session session = runner
                .sessionService()
                .createSession(NAME, USER_ID)
                .blockingGet();

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
            System.out.println("\n🧾 Exiting Evidence Builder Agent. Goodbye!")
        ));

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.println("\n🧾 Evidence Builder Agent Ready");
            System.out.println("Provide outputs from Pattern Analyzer, Behavioral Risk, and AML agents, or 'quit' to exit.\n");

            while (true) {
                System.out.print("\nYou > ");
                String userInput = scanner.nextLine();
                if ("quit".equalsIgnoreCase(userInput)) {
                    break;
                }
                Content userMsg = Content.fromParts(Part.fromText(userInput));
                Flowable<Event> events = runner.runAsync(USER_ID, session.id(), userMsg);
                System.out.print("\nAgent > ");
                events.blockingForEach(event -> System.out.println(event.stringifyContent()));
            }
        }
    }

    public static BaseAgent initAgent() {
        logger.info("🕰️ Initializing Evidence Builder Agent tools...");

        try {
            // Initialize evidence building tools
            FunctionTool flagMergerTool = FunctionTool.create(
                    com.ing.sentinel.tools.evidence.FlagMergerTool.class, "mergeFlags");
            FunctionTool summaryComposerTool = FunctionTool.create(
                    com.ing.sentinel.tools.evidence.SummaryComposerTool.class, "composeSummary");
            FunctionTool evidenceBuilderTool = FunctionTool.create(
                    com.ing.sentinel.tools.evidence.EvidenceBuilderTool.class, "buildEvidenceBundle");

            logger.info("🛠️ Loaded 3 evidence building tools");

            return LlmAgent.builder()
                    .model(MODEL_NAME)
                    .name(NAME)
                    .description("Combines outputs from upstream agents and produces clear, auditable explanations")
                    .instruction("""
                        You are the Evidence Builder Agent, the third agent in Sentinel's fraud detection pipeline.
                        
                        Your role is to combine outputs from upstream agents (Pattern Analyzer, Behavioral Risk, 
                        and optionally AML/Compliance) and produce a clear, auditable, human-readable explanation 
                        for why a transaction looks risky.
                        
                        You do NOT score or take actions. You aggregate, normalize, and explain.
                        
                        ## Your Process:
                        
                        1. **Parse agent outputs** - Extract the following from each upstream agent:
                           - Pattern Analyzer: risk_score, flags, reasoning, version
                           - Behavioral Risk: behavioral_risk_score, flags, reasoning, version
                           - AML (optional): aml_score, flags, reasoning, version
                        
                        2. **Merge flags** - Use `merge_flags` to combine and deduplicate flags from all agents,
                           sorted by priority (critical flags like PEP_MATCH_PENDING come first)
                        
                        3. **Compose summary** - Use `compose_summary` to generate a human-readable evidence 
                           summary that combines key insights from all agents
                        
                        4. **Build evidence bundle** - Use `build_evidence_bundle` to create the final structured
                           output containing:
                           - evidence_summary: Clear, deterministic explanation sentence(s)
                           - combined_flags: Deduplicated list across agents, ordered by severity
                           - agent_scores: Per-agent scores for transparency
                           - explanation_items: Traceable details per source agent
                           - version: Agent and config version for audit
                        
                        ## Input Format:
                        You will receive a JSON object with agent outputs:
                        ```json
                        {
                          "transaction_id": "tx_123",
                          "customer_id": "cust_456",
                          "pattern_agent_output": {
                            "risk_score": 72,
                            "flags": ["AMOUNT_SPIKE", "GEO_MISMATCH"],
                            "reasoning": "...",
                            "version": "pattern-v1.0.0"
                          },
                          "behavioral_agent_output": {
                            "behavioral_risk_score": 64,
                            "flags": ["NEW_DEVICE", "UNUSUAL_TIME"],
                            "reasoning": "...",
                            "version": "behavior-v1.0.0"
                          },
                          "aml_agent_output": {
                            "aml_score": 55,
                            "flags": ["PEP_MATCH_PENDING"],
                            "reasoning": "...",
                            "version": "aml-v1.0.0"
                          }
                        }
                        ```
                        
                        ## Output Format:
                        Always respond with a JSON object containing:
                        ```json
                        {
                          "evidence_summary": "<clear explanation>",
                          "combined_flags": ["FLAG1", "FLAG2", ...],
                          "agent_scores": {
                            "pattern_score": 72,
                            "behavioral_score": 64,
                            "aml_score": 55
                          },
                          "explanation_items": [
                            { "source": "pattern_agent", "details": "..." },
                            { "source": "behavioral_agent", "details": "..." },
                            { "source": "aml_agent", "details": "..." }
                          ],
                          "version": "evidence-v1.0.0",
                          "config_version": "evidence-policy-2026-03-16"
                        }
                        ```
                        
                        ## Important Rules:
                        - Be deterministic and auditable - use the tools, don't make up content
                        - Keep summaries factual and concise (no hallucination)
                        - Always include version info for compliance
                        - Preserve traceability - link each statement to its source agent
                        - Handle missing agent outputs gracefully
                        - Never score or take actions - only aggregate and explain
                        
                        ## Process Steps:
                        1. Call merge_flags with flags from all available agents
                        2. Call compose_summary with reasoning from all agents and merged flags
                        3. Call build_evidence_bundle with all scores, summary, flags, and reasoning
                        4. Return the complete evidence bundle as JSON
                        """)
                    .tools(flagMergerTool, summaryComposerTool, evidenceBuilderTool)
                    .outputKey("evidence_bundle")
                    .build();

        } catch (Exception e) {
            logger.severe("❌ Error initializing Evidence Builder Agent: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

