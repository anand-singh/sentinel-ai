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

import io.reactivex.rxjava3.core.Flowable;

/**
 * Behavioral Risk Detector
 * 
 * The second agent in Sentinel's fraud pipeline. Scores how unusual a transaction
 * is for a specific customer by comparing it with the customer's historical behavior
 * (amount ranges, active hours, geo patterns, device/IP, merchant mix, velocity).
 * 
 * This agent does NOT execute actions. It only scores & explains.
 * Actions are performed by Agent #5 – Action Executor after Agent #4 aggregates scores.
 */
public class BehavioralRiskDetector {

    private static final Logger logger = Logger.getLogger(BehavioralRiskDetector.class.getName());

    private static final String MODEL_NAME = "gemini-2.5-flash";
    private static final String NAME = "BehavioralRiskAgent";
    private static final String USER_ID = "sentinel-user";
    private static final String VERSION = "behavior-v1.0.0";

    // ROOT_AGENT needed for ADK Web UI
    public static final BaseAgent ROOT_AGENT = initAgent();

    public static void main(String[] args) {
        logger.info("🧩 Starting Behavioral Risk Agent v" + VERSION);
        
        InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT);
        Session session = runner
                .sessionService()
                .createSession(NAME, USER_ID)
                .blockingGet();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> 
            System.out.println("\n🧩 Exiting Behavioral Risk Agent. Goodbye!")
        ));

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.println("\n🧩 Behavioral Risk Agent Ready");
            System.out.println("Enter transaction + customer profile details or 'quit' to exit.\n");
            
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
        logger.info("🕰️ Initializing Behavioral Risk Agent tools...");

        try {
            // Initialize all behavioral signal analysis tools
            FunctionTool amountDeviationTool = FunctionTool.create(
                    com.ing.sentinel.agent.tools.behavioral.AmountDeviationSignal.class, "analyzeAmountDeviation");
            FunctionTool timeDeviationTool = FunctionTool.create(
                    com.ing.sentinel.agent.tools.behavioral.TimeDeviationSignal.class, "analyzeTimeDeviation");
            FunctionTool geoDeviationTool = FunctionTool.create(
                    com.ing.sentinel.agent.tools.behavioral.GeoDeviationSignal.class, "analyzeGeoDeviation");
            FunctionTool newDeviceTool = FunctionTool.create(
                    com.ing.sentinel.agent.tools.behavioral.NewDeviceSignal.class, "analyzeNewDevice");
            FunctionTool newIpRangeTool = FunctionTool.create(
                    com.ing.sentinel.agent.tools.behavioral.NewIpRangeSignal.class, "analyzeNewIpRange");
            FunctionTool merchantNoveltyTool = FunctionTool.create(
                    com.ing.sentinel.agent.tools.behavioral.MerchantNoveltySignal.class, "analyzeMerchantNovelty");
            FunctionTool burstActivityTool = FunctionTool.create(
                    com.ing.sentinel.agent.tools.behavioral.BurstActivitySignal.class, "analyzeBurstActivity");
            FunctionTool newAccountTool = FunctionTool.create(
                    com.ing.sentinel.agent.tools.behavioral.NewAccountSignal.class, "analyzeNewAccount");
            FunctionTool behavioralBlenderTool = FunctionTool.create(
                    com.ing.sentinel.agent.tools.behavioral.BehavioralScoreBlender.class, "blendBehavioralScores");

            logger.info("🛠️ Loaded 9 behavioral analysis tools");

            return LlmAgent.builder()
                    .model(MODEL_NAME)
                    .name(NAME)
                    .description("Analyzes transaction behavior against customer profile to detect anomalies")
                    .instruction("""
                        You are the Behavioral Risk Agent, the second agent in Sentinel's fraud detection pipeline.
                        
                        Your role is to score how unusual a transaction is for a SPECIFIC CUSTOMER by comparing
                        it with that customer's historical behavior. You score and explain risks but do NOT take actions.
                        
                        ## Your Process:
                        
                        1. **Parse the transaction and customer profile** - Extract transaction details and the customer's
                           behavioral profile (avg amounts, usual hours, devices, IPs, merchants, locations, etc.)
                           CRITICAL: Look for account_age_days, prior_transaction_count, known_devices (empty = NEW_DEVICE),
                           large_inbound_transfer_today, outbound_today.
                        
                        2. **Run behavioral signal analysis tools** - For each transaction, run ALL relevant tools:
                           - `analyze_amount_deviation` - Compare amount to customer's personal baseline (z-score)
                           - `analyze_time_deviation` - Check if transaction time is during customer's sleep hours
                           - `analyze_geo_deviation` - Check distance/speed from customer's last known location
                           - `analyze_new_device` - Detect if device fingerprint is new for this customer
                           - `analyze_new_ip_range` - Detect if IP is from an unusual range for this customer
                           - `analyze_merchant_novelty` - Check if merchant/MCC is unusual for this customer
                           - `analyze_burst_activity` - Check if transaction velocity exceeds customer's baseline
                           - `analyze_new_account` - CRITICAL: Check if account_age_days < 30 (high risk for money mule)
                        
                        3. **Blend scores** - Use `blend_behavioral_scores` to combine all signals into a final behavioral risk score.
                           IMPORTANT: For new accounts with large transfers, use high signals (0.9+) for amount and velocity.
                        
                        4. **Generate response** - Return a structured analysis with:
                           - `behavioral_risk_score` (0-100): How unusual this is for THIS customer
                           - `flags`: List of triggered flags (NEW_DEVICE, NEW_ACCOUNT, BURST_ACTIVITY, GEO_DEVIATION, etc.)
                           - `reasoning`: Human-readable explanation specific to customer behavior
                           - `feature_contributions`: Numeric details for each signal
                           - `version`: Agent version for audit trail
                        
                        ## CRITICAL: New Account + Large Transfer Detection
                        When you see these patterns, the behavioral risk is HIGH:
                        - account_age_days < 30 = NEW_ACCOUNT flag (very new = very high risk)
                        - known_devices is empty [] = NEW_DEVICE flag
                        - Large transfers (>$1000) from new account = BURST_ACTIVITY
                        - outbound_today approaching large_inbound_transfer_today = money mule pattern
                        
                        For new accounts with large inbound/outbound transfers, ALWAYS call analyze_new_account
                        and analyze_burst_activity with high velocity values.
                        
                        ## Output Format:
                        Always respond with a JSON object containing:
                        ```json
                        {
                          "behavioral_risk_score": <0-100>,
                          "flags": ["FLAG1", "FLAG2"],
                          "reasoning": "<short factual explanation about customer-specific behavior>",
                          "feature_contributions": {
                            "amount_zscore_customer": <value>,
                            "hour_deviation": <0-1>,
                            "geo_distance_km": <value>,
                            "new_device": <0 or 1>,
                            "new_ip_range": <0 or 1>,
                            "merchant_novelty": <0-1>,
                            "burst_in_window": <0 or 1>
                          },
                          "version": "behavior-v1.0.0",
                          "config_version": "behavior-weights-2026-03-16"
                        }
                        ```
                        
                        ## Important Rules:
                        - Focus on CUSTOMER-SPECIFIC behavior, not global patterns
                        - Be deterministic and auditable
                        - Never take actions - only analyze and score
                        - Always include version info for compliance
                        - Keep reasoning factual and specific to this customer
                        """)
                    .tools(amountDeviationTool, timeDeviationTool, geoDeviationTool, 
                           newDeviceTool, newIpRangeTool, merchantNoveltyTool, 
                           burstActivityTool, newAccountTool, behavioralBlenderTool)
                    .outputKey("behavioral_risk_result")
                    .build();

        } catch (Exception e) {
            logger.severe("❌ Error initializing Behavioral Risk Agent: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

