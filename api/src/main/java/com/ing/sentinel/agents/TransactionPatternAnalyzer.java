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

import com.ing.sentinel.tools.pattern.*;
import io.reactivex.rxjava3.core.Flowable;

/**
 * Transaction Pattern Analyzer Agent
 * 
 * The first agent in Sentinel's fraud pipeline. Analyzes individual transactions
 * against global & contextual patterns (amount, merchant, location, time, velocity)
 * and emits a risk signal with clear flags and deterministic reasoning.
 * 
 * This agent does NOT take actions. It only scores & explains.
 * Actions are performed later by the Action Executor Agent.
 */
public class TransactionPatternAnalyzer {

    private static final Logger logger = Logger.getLogger(TransactionPatternAnalyzer.class.getName());

    private static final String MODEL_NAME = "gemini-2.5-flash";
    private static final String NAME = "TransactionPatternAnalyzer";
    private static final String USER_ID = "sentinel-user";
    private static final String VERSION = "pattern-v1.0.0";

    // ROOT_AGENT needed for ADK Web UI
    public static final BaseAgent ROOT_AGENT = initAgent();

    public static void main(String[] args) {
        logger.info("🛡️ Starting Transaction Pattern Analyzer Agent v" + VERSION);
        
        InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT);
        Session session = runner
                .sessionService()
                .createSession(NAME, USER_ID)
                .blockingGet();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛡️ Exiting Transaction Pattern Analyzer. Goodbye!");
        }));

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.println("\n🛡️ Transaction Pattern Analyzer Ready");
            System.out.println("Enter transaction details or 'quit' to exit.\n");
            
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
        logger.info("🕰️ Initializing Transaction Pattern Analyzer tools...");

        try {
            // Initialize all signal analysis tools using FunctionTool
            FunctionTool amountTool = FunctionTool.create(
                    AmountSpikeTool.class, "analyzeAmountSpike");
            FunctionTool geoTool = FunctionTool.create(
                    GeoDistanceTool.class, "analyzeGeoDistance");
            FunctionTool velocityTool = FunctionTool.create(
                    VelocityTool.class, "analyzeVelocity");
            FunctionTool mccTool = FunctionTool.create(
                    RareMccTool.class, "analyzeRareMcc");
            FunctionTool timeTool = FunctionTool.create(
                    TimeWindowTool.class, "analyzeTimeWindow");
            FunctionTool blenderTool = FunctionTool.create(
                    ScoreBlenderTool.class, "blendRiskScores");

            logger.info("🛠️ Loaded 6 pattern analysis tools");

            return LlmAgent.builder()
                    .model(MODEL_NAME)
                    .name(NAME)
                    .description("Analyzes transaction patterns to detect fraud signals")
                    .instruction("""
                        You are the Transaction Pattern Analyzer, the first agent in Sentinel's fraud detection pipeline.
                        
                        Your role is to analyze individual transactions against global and contextual patterns
                        to detect potential fraud. You score and explain risks but do NOT take actions.
                        
                        ## Your Process:
                        
                        1. **Parse the transaction** - Extract transaction details: amount, merchant, location, time, etc.
                        
                        2. **Run signal analysis tools** - For each transaction, run ALL relevant tools:
                           - `analyze_amount_spike` - Detect unusual transaction amounts (z-score based)
                           - `analyze_geo_distance` - Check geographic anomalies (impossible travel)
                           - `analyze_velocity` - Check transaction frequency patterns
                           - `analyze_rare_mcc` - Detect unusual merchant categories
                           - `analyze_time_window` - Check for unusual transaction times
                        
                        3. **Blend scores** - Use `blend_risk_scores` to combine all signals into a final risk score
                        
                        4. **Generate response** - Return a structured analysis with:
                           - `risk_score` (0-100): Overall risk level
                           - `flags`: List of triggered flags (AMOUNT_SPIKE, GEO_MISMATCH, etc.)
                           - `reasoning`: Human-readable explanation
                           - `feature_contributions`: Numeric details for each signal
                           - `version`: Agent version for audit trail
                        
                        ## Output Format:
                        Always respond with a JSON object containing:
                        ```json
                        {
                          "risk_score": <0-100>,
                          "flags": ["FLAG1", "FLAG2"],
                          "reasoning": "<short factual explanation>",
                          "feature_contributions": {
                            "amount_zscore": <value>,
                            "geo_distance_km": <value>,
                            "velocity_1h": <count>,
                            "mcc_rarity": <0-1>,
                            "time_anomaly": <0-1>
                          },
                          "version": "pattern-v1.0.0",
                          "config_version": "weights-2026-03-16"
                        }
                        ```
                        
                        ## Important Rules:
                        - Be deterministic and auditable
                        - Never take actions - only analyze and score
                        - Always include version info for compliance
                        - Keep reasoning factual and concise
                        - Flag anything suspicious, let downstream agents decide actions
                        """)
                    .tools(amountTool, geoTool, velocityTool, mccTool, timeTool, blenderTool)
                    .outputKey("pattern_analysis_result")
                    .build();

        } catch (Exception e) {
            logger.severe("❌ Error initializing Transaction Pattern Analyzer: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

