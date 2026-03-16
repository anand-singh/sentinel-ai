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
 * Aggregated Risk Scorer Agent
 * 
 * The fourth agent in Sentinel's fraud pipeline. Ingests outputs from upstream agents
 * (Pattern Analyzer, Behavioral Risk Detector, Evidence Builder, optional AML)
 * and produces a single calibrated risk score (0-100), severity label, and recommended action.
 * 
 * This agent does NOT execute actions - it only recommends.
 * Action execution is handled by Agent #5 – Action Executor.
 */
public class AggregatedRiskScorer {

    private static final Logger logger = Logger.getLogger(AggregatedRiskScorer.class.getName());

    private static final String MODEL_NAME = "gemini-2.5-flash";
    private static final String NAME = "AggregatedRiskScorer";
    private static final String USER_ID = "sentinel-user";
    private static final String VERSION = "aggregator-v1.0.0";
    private static final String MODEL_VERSION = "agg-v1.0.0";

    // ROOT_AGENT needed for ADK Web UI
    public static final BaseAgent ROOT_AGENT = initAgent();

    public static void main(String[] args) {
        logger.info("🎛️ Starting Aggregated Risk Scorer Agent v" + VERSION);
        
        InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT);
        Session session = runner
                .sessionService()
                .createSession(NAME, USER_ID)
                .blockingGet();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> 
            System.out.println("\n🎛️ Exiting Aggregated Risk Scorer. Goodbye!")
        ));

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.println("\n🎛️ Aggregated Risk Scorer Ready");
            System.out.println("Enter outputs from Pattern, Behavioral, Evidence, and AML agents, or 'quit' to exit.\n");
            System.out.println("Example input format:");
            System.out.println("{");
            System.out.println("  \"transaction_id\": \"tx_123\",");
            System.out.println("  \"customer_id\": \"cust_456\",");
            System.out.println("  \"pattern_agent\": {\"risk_score\": 72, \"flags\": [\"AMOUNT_SPIKE\",\"GEO_MISMATCH\"]},");
            System.out.println("  \"behavioral_agent\": {\"behavioral_risk_score\": 64, \"flags\": [\"NEW_DEVICE\",\"UNUSUAL_TIME\"]},");
            System.out.println("  \"evidence_agent\": {\"evidence_summary\": \"High amount, new device, abnormal distance\"},");
            System.out.println("  \"aml_agent\": {\"aml_score\": 55, \"flags\": [\"PEP_MATCH_PENDING\"]}");
            System.out.println("}\n");
            
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
        logger.info("🕰️ Initializing Aggregated Risk Scorer tools...");

        try {
            // Initialize all aggregation tools
            FunctionTool normalizerTool = FunctionTool.create(
                    com.ing.sentinel.tools.aggregator.ScoreNormalizer.class, "normalizeScore");
            FunctionTool blenderTool = FunctionTool.create(
                    com.ing.sentinel.tools.aggregator.WeightedScoreBlender.class, "blendWeightedScores");
            FunctionTool boosterTool = FunctionTool.create(
                    com.ing.sentinel.tools.aggregator.RiskBooster.class, "applyRiskBoost");
            FunctionTool calibratorTool = FunctionTool.create(
                    com.ing.sentinel.tools.aggregator.ScoreCalibrator.class, "calibrateScore");
            FunctionTool classifierTool = FunctionTool.create(
                    com.ing.sentinel.tools.aggregator.SeverityClassifier.class, "classifySeverity");

            logger.info("🛠️ Loaded 5 aggregation tools");

            return LlmAgent.builder()
                    .model(MODEL_NAME)
                    .name(NAME)
                    .description("Aggregates risk scores from multiple agents into a single calibrated score with severity and action recommendation")
                    .instruction("""
                        You are the Aggregated Risk Scorer, the fourth agent in Sentinel's fraud detection pipeline.
                        
                        Your role is to ingest outputs from upstream agents and produce a SINGLE, CALIBRATED risk score (0-100),
                        a severity label (LOW/MED/HIGH/CRITICAL), and a recommended action (ALLOW/REVIEW/CHALLENGE/BLOCK).
                        You DO NOT execute actions - you only recommend. Execution is handled by Agent #5 – Action Executor.
                        
                        ## Upstream Inputs:
                        
                        You receive outputs from:
                        1. **Agent #1 – Transaction Pattern Analyzer**: risk_score, flags, reasoning
                        2. **Agent #2 – Behavioral Risk Detector**: behavioral_risk_score, flags, feature_contributions
                        3. **Agent #3 – Evidence Builder**: evidence_summary, combined_flags
                        4. **(Optional) AML/Compliance Agent**: aml_score, flags
                        
                        ## Your Process:
                        
                        1. **Parse upstream agent outputs** - Extract all scores, flags, and metadata from the input
                        
                        2. **Normalize scores** - Use `normalize_score` to convert all incoming scores to [0..1] scale
                           - Pattern agent risk_score (typically 0-100)
                           - Behavioral agent behavioral_risk_score (typically 0-100)
                           - AML agent aml_score (typically 0-100, optional)
                        
                        3. **Blend normalized scores** - Use `blend_weighted_scores` to combine scores with configurable weights
                           - Default weights: pattern=0.40, behavioral=0.40, aml=0.20
                           - If AML is absent, weights are redistributed proportionally
                        
                        4. **Apply risk boosters** - Use `apply_risk_boost` to add non-linear bumps for high-risk flag combos
                           - Example combos: AMOUNT_SPIKE + NEW_DEVICE + GEO_MISMATCH
                           - Boosts are capped to prevent runaway scores
                        
                        5. **Calibrate to final scale** - Use `calibrate_score` to map to [0..100] using piecewise linear calibration
                        
                        6. **Classify severity & action** - Use `classify_severity` to determine:
                           - Severity: LOW (0-34), MED (35-59), HIGH (60-79), CRITICAL (80-100)
                           - Recommended action: ALLOW, REVIEW, CHALLENGE, BLOCK
                        
                        7. **Generate response** - Return a structured aggregate with:
                           - `final_risk_score` (0-100): calibrated composite
                           - `severity`: LOW | MED | HIGH | CRITICAL
                           - `recommended_action`: ALLOW | REVIEW | CHALLENGE | BLOCK
                           - `source_contributions`: per-source contribution to final score
                           - `explanation`: short deterministic reason string
                           - `policy_version`, `model_version`, `version`: for auditability
                        
                        ## Output Format:
                        Always respond with a JSON object containing:
                        ```json
                        {
                          "final_risk_score": <0-100>,
                          "severity": "LOW|MED|HIGH|CRITICAL",
                          "recommended_action": "ALLOW|REVIEW|CHALLENGE|BLOCK",
                          "source_contributions": {
                            "pattern_agent": <contribution>,
                            "behavioral_agent": <contribution>,
                            "aml_agent": <contribution>
                          },
                          "explanation": "<short deterministic explanation>",
                          "combined_flags": ["FLAG1", "FLAG2", ...],
                          "boost_applied": <0.0-0.15>,
                          "policy_version": "policy-2026-03-16",
                          "model_version": "agg-v1.0.0",
                          "version": "aggregator-v1.0.0"
                        }
                        ```
                        
                        ## Important Rules:
                        
                        - **Transparent logic**: All blending, boosting, and calibration must be deterministic and auditable
                        - **Monotonicity**: Increasing any upstream score (others fixed) must NOT decrease final score
                        - **Missing data handling**: If an agent's output is absent, redistribute weights proportionally
                        - **Version tracking**: Always include policy_version, model_version, and version in response
                        - **No action execution**: You ONLY recommend actions, never execute them
                        - **Bounded outputs**: Final score must be in [0..100], severity must be one of 4 levels
                        
                        ## Guardrails:
                        
                        - Reject invalid or malformed inputs
                        - Ensure all scores are properly normalized before blending
                        - Cap boosts to prevent score inflation
                        - Include clear explanation of what drove the final score
                        - Log all intermediate calculations for audit trail
                        
                        ## Example Workflow:
                        
                        Input: pattern_score=72, behavioral_score=64, aml_score=55
                        1. Normalize: 0.72, 0.64, 0.55
                        2. Blend (0.4/0.4/0.2 weights): 0.66
                        3. Boost (AMOUNT_SPIKE+NEW_DEVICE+GEO_MISMATCH): +0.05 → 0.71
                        4. Calibrate: 71 → 73 (piecewise)
                        5. Classify: 73 → HIGH, CHALLENGE
                        6. Return aggregate response with all metadata
                        """)
                    .tools(normalizerTool, blenderTool, boosterTool, calibratorTool, classifierTool)
                    .outputKey("aggregated_risk_result")
                    .build();

        } catch (Exception e) {
            logger.severe("❌ Error initializing Aggregated Risk Scorer: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

