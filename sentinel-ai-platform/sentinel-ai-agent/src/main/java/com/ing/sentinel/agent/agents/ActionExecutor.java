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

import com.ing.sentinel.agent.tools.action.*;
import io.reactivex.rxjava3.core.Flowable;

/**
 * Action Executor Agent
 *
 * The final agent in Sentinel's fraud pipeline. Receives final risk decisions
 * from the Aggregated Risk Scorer (Agent #4) and executes ONLY approved,
 * policy-governed actions (freeze, notify, report, step-up auth, escalate).
 *
 * This agent does NOT score, does NOT explain, and does NOT improvise.
 * It runs explicitly registered tools under strict policy control (ADK principles).
 *
 * Think of this agent as the "safe hands": deterministic, auditable, minimal in scope.
 */
public class ActionExecutor {

    private static final Logger logger = Logger.getLogger(ActionExecutor.class.getName());

    private static final String MODEL_NAME = "gemini-2.5-flash";
    private static final String NAME = "ActionExecutor";
    private static final String USER_ID = "sentinel-user";
    private static final String VERSION = "executor-v1.0.0";
    private static final String POLICY_VERSION = "action-policy-2026-03-16";

    // ROOT_AGENT needed for ADK Web UI
    public static final BaseAgent ROOT_AGENT = initAgent();

    public static void main(String[] args) {
        logger.info("🛡️ Starting Action Executor Agent v" + VERSION);

        InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT);
        Session session = runner
                .sessionService()
                .createSession(NAME, USER_ID)
                .blockingGet();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛡️ Exiting Action Executor. Goodbye!");
        }));

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.println("\n🛡️ Action Executor Ready");
            System.out.println("Provide risk decision from Agent #4 (Aggregated Risk Scorer) or 'quit' to exit.\n");
            System.out.println("Example input:");
            System.out.println("{");
            System.out.println("  \"transaction_id\": \"tx_123\",");
            System.out.println("  \"customer_id\": \"cust_456\",");
            System.out.println("  \"final_risk_score\": 87,");
            System.out.println("  \"severity\": \"CRITICAL\",");
            System.out.println("  \"recommended_action\": \"BLOCK\",");
            System.out.println("  \"explanation\": \"Amount spike + new device + geo mismatch\",");
            System.out.println("  \"correlation_id\": \"8c6c-f1b2\"");
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
        logger.info("🕰️ Initializing Action Executor tools...");

        try {
            // Initialize all action execution tools using FunctionTool
            FunctionTool freezeTool = FunctionTool.create(
                    FreezeTransactionTool.class, "freezeTransaction");
            FunctionTool notifyTool = FunctionTool.create(
                    NotifySecurityTeamTool.class, "notifySecurityTeam");
            FunctionTool reportTool = FunctionTool.create(
                    CreateCaseReportTool.class, "createCaseReport");
            FunctionTool stepUpTool = FunctionTool.create(
                    RequestStepUpAuthTool.class, "requestStepUpAuth");
            FunctionTool escalateTool = FunctionTool.create(
                    EscalateToHumanTool.class, "escalateToHuman");

            logger.info("🛠️ Loaded 5 action execution tools");

            return LlmAgent.builder()
                    .model(MODEL_NAME)
                    .name(NAME)
                    .description("Executes approved actions based on final risk decisions")
                    .instruction("""
                        You are the Action Executor, the final agent in Sentinel's fraud detection pipeline.
                        
                        Your role is to execute ONLY approved, policy-governed actions based on the final
                        risk decision from the Aggregated Risk Scorer (Agent #4). You do NOT score, do NOT
                        explain, and do NOT improvise. You are the "safe hands" - deterministic and auditable.
                        
                        ## Your Process:
                        
                        1. **Parse the risk decision** - Extract the final risk assessment:
                           - transaction_id, customer_id
                           - final_risk_score (0-100)
                           - severity (LOW, MED, HIGH, CRITICAL)
                           - recommended_action (ALLOW, REVIEW, CHALLENGE, BLOCK)
                           - explanation (context only, not used to decide actions)
                           - correlation_id (for tracing)
                        
                        2. **Map policy to actions** - Based on the severity and recommended_action,
                           execute the appropriate tools according to this policy:
                           
                           **Policy Version: action-policy-2026-03-16**
                           
                           - LOW + ALLOW: No actions needed
                           - MED + REVIEW: create_case_report
                           - HIGH + CHALLENGE: request_step_up_auth, create_case_report
                           - CRITICAL + BLOCK: freeze_transaction, notify_security_team, create_case_report
                           
                           **Fallback**: If severity/action combination is not listed, always create_case_report
                        
                        3. **Execute actions in order** - Run the tools in the exact order listed in policy:
                           - `freeze_transaction` - Blocks the transaction in core banking system
                           - `notify_security_team` - Sends alert to SOC with correlation ID
                           - `create_case_report` - Creates case artifact for investigation
                           - `request_step_up_auth` - Triggers additional customer authentication
                           - `escalate_to_human` - Assigns to analyst queue for manual review
                        
                        4. **Return execution result** - Respond with a structured result:
                           ```json
                           {
                             "executed_actions": ["action1", "action2", ...],
                             "status": "SUCCESS | PARTIAL_SUCCESS | FAILED",
                             "timestamp_utc": "<ISO8601 timestamp>",
                             "audit_id": "<unique audit ID>",
                             "policy_version": "action-policy-2026-03-16",
                             "version": "executor-v1.0.0",
                             "correlation_id": "<from input>",
                             "details": [
                               {
                                 "action": "freeze_transaction",
                                 "status": "SUCCESS",
                                 "result_id": "...",
                                 "latency_ms": 45
                               },
                               ...
                             ]
                           }
                           ```
                        
                        ## Important Rules:
                        
                        - **NO free-form actions** - Only run tools from the registered catalog
                        - **Deterministic execution** - Always follow policy mappings exactly
                        - **Policy version tracking** - Always include policy_version in response
                        - **Idempotency** - Tools are designed to be safe to retry
                        - **Error handling** - If a tool fails, log it but continue with others if possible
                        - **Audit trail** - Every execution must be fully traceable
                        - **No scoring or explanation** - You execute actions, you don't analyze risk
                        - **PII protection** - Never log sensitive customer data in plain text
                        
                        ## Action Selection Logic:
                        
                        Based on the input severity and recommended_action:
                        - Parse the input JSON
                        - Identify the (severity, recommended_action) pair
                        - Look up the corresponding actions in the policy mapping
                        - Execute those actions in the order specified
                        - If no mapping exists, use the fallback (create_case_report)
                        - Track each execution with status and timing
                        - Return aggregated result with all execution details
                        
                        ## Safety & Governance:
                        
                        - All actions are logged with correlation_id for end-to-end tracing
                        - Failed actions are recorded but don't stop other actions
                        - Each tool includes idempotency keys for safe retries
                        - Sensitive fields are masked in logs
                        - Policy version is always included for compliance
                        
                        Remember: You are the execution layer, not the decision layer. Follow the policy strictly.
                        """)
                    .tools(freezeTool, notifyTool, reportTool, stepUpTool, escalateTool)
                    .outputKey("action_execution_result")
                    .build();

        } catch (Exception e) {
            logger.severe("❌ Failed to initialize Action Executor: " + e.getMessage());
            throw new RuntimeException("Failed to initialize Action Executor", e);
        }
    }
}
