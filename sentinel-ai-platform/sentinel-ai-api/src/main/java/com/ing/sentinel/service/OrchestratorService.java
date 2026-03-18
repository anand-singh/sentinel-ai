package com.ing.sentinel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.ing.sentinel.agent.agents.SentinelOrchestrator;
import com.ing.sentinel.store.CaseStoreService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Wraps the ADK InMemoryRunner for SentinelOrchestrator.
 * Called by SentinelApiController on POST /api/ingest.
 *
 * Converts the orchestrator's free-text/JSON output back into the
 * CaseDetail shape the frontend expects, then persists it in CaseStoreService.
 */
@Service
public class OrchestratorService {

    private static final Logger log = Logger.getLogger(OrchestratorService.class.getName());

    private final CaseStoreService store;
    private final ObjectMapper mapper = new ObjectMapper();

    private final InMemoryRunner runner;

    public OrchestratorService(CaseStoreService store) {
        this.store = store;
        this.runner = new InMemoryRunner(SentinelOrchestrator.ROOT_AGENT);
    }

    /**
     * Runs the full 5-agent pipeline for the incoming transaction JSON.
     * Returns the persisted CaseDetail node (ready to serialize as HTTP response).
     */
    public ObjectNode ingest(String transactionJson) throws Exception {
        String agentName = SentinelOrchestrator.ROOT_AGENT.name();
        String userId = "api-user-" + UUID.randomUUID().toString().substring(0, 8);

        Session session = runner.sessionService()
            .createSession(agentName, userId)
            .blockingGet();

        Content userMsg = Content.fromParts(Part.fromText(transactionJson));

        StringBuilder fullOutput = new StringBuilder();
        runner.runAsync(userId, session.id(), userMsg)
            .timeout(90, TimeUnit.SECONDS)
            .blockingForEach(event -> fullOutput.append(event.stringifyContent()).append("\n"));

        String raw = fullOutput.toString();
        log.info("Orchestrator raw output length: " + raw.length());

        // Extract JSON from the LLM response
        JsonNode orchResult = extractJson(raw);

        // Parse transaction metadata
        JsonNode txNode;
        try { txNode = mapper.readTree(transactionJson); }
        catch (Exception e) { txNode = mapper.createObjectNode(); }

        String txId = txNode.path("transaction_id").asText("TX-" + UUID.randomUUID().toString().substring(0, 8));
        String custId = txNode.path("customer_id").asText("CUST-UNKNOWN");
        String custName = txNode.path("customer_name").asText(custId);

        // Build Alert from final_decision
        JsonNode decision = orchResult.path("final_decision");
        int riskScore = decision.path("risk_score").asInt(50);
        String severity = mapSeverity(decision.path("severity").asText("MED"));
        String recAction = firstAction(decision.path("executed_actions"));
        if (recAction.isEmpty()) recAction = mapRecommendedAction(decision.path("recommended_action").asText("REVIEW"));

        String alertId = "ALERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        ObjectNode alertNode = mapper.createObjectNode();
        alertNode.put("id", alertId);
        alertNode.put("transactionId", txId);
        alertNode.put("customerId", custId);
        alertNode.put("customerName", custName);
        alertNode.put("finalRiskScore", riskScore);
        alertNode.put("severity", severity);
        alertNode.put("status", "QUEUED");
        alertNode.put("recommendedAction", recAction);
        alertNode.put("timestamp", Instant.now().toString());
        store.putAlert(alertNode);

        // Build CaseDetail
        String caseId = "F-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String correlationId = orchResult.path("correlation_id").asText(txId + "-" + Instant.now().toEpochMilli());

        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", caseId);
        caseNode.set("alert", alertNode);
        caseNode.put("status", "OPEN");
        caseNode.putNull("assignedTo");
        caseNode.put("policyVersion", "action-policy-2026-03-16");
        caseNode.put("agentVersion", "orchestrator-v1.0.0");
        caseNode.set("notes", mapper.createArrayNode());
        caseNode.set("relatedTransactionIds", mapper.createArrayNode().add(txId));

        // Agent outputs from pipeline_results
        JsonNode pipeline = orchResult.path("pipeline_results");
        ArrayNode agentOutputs = mapper.createArrayNode();
        addAgentOutput(agentOutputs, "Pattern Analyzer", pipeline.path("pattern_analyzer"));
        addAgentOutput(agentOutputs, "Behavioral Risk Agent", pipeline.path("behavioral_risk"));
        addAgentOutput(agentOutputs, "Evidence Builder", pipeline.path("evidence_builder"));
        addAgentOutput(agentOutputs, "Aggregated Risk Scorer", pipeline.path("aggregated_scorer"));
        caseNode.set("agentOutputs", agentOutputs);

        // Actions executed
        ArrayNode actionsExec = mapper.createArrayNode();
        JsonNode execActions = decision.path("executed_actions");
        if (execActions.isArray()) {
            int idx = 1;
            for (JsonNode a : execActions) {
                ObjectNode ae = mapper.createObjectNode();
                ae.put("id", "ACT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
                ae.put("type", a.asText());
                ae.put("performedBy", "orchestrator-v1.0.0");
                ae.put("timestamp", Instant.now().minus(idx * 10L, ChronoUnit.SECONDS).toString());
                actionsExec.add(ae);
                idx++;
            }
        }
        caseNode.set("actionsExecuted", actionsExec);

        // Audit trail from timeline
        ArrayNode auditTrail = mapper.createArrayNode();
        JsonNode timeline = orchResult.path("timeline");
        if (timeline.isArray()) {
            int idx = 1;
            for (JsonNode step : timeline) {
                ObjectNode entry = mapper.createObjectNode();
                entry.put("id", "AUD-" + String.format("%03d", idx));
                entry.put("event", step.path("agent").asText("Agent") + " — " + step.path("status").asText());
                entry.put("actor", "sentinel-orchestrator");
                entry.put("timestamp", Instant.now().minus((timeline.size() - idx) * 30L, ChronoUnit.SECONDS).toString());
                entry.put("correlationId", correlationId + "-" + String.format("%03d", idx));
                entry.put("policyVersion", "action-policy-2026-03-16");
                auditTrail.add(entry);
                idx++;
            }
        }
        caseNode.set("auditTrail", auditTrail);

        // Back-fill caseId onto the alert so the frontend can navigate directly
        alertNode.put("caseId", caseId);
        store.putAlert(alertNode);

        store.putCase(caseNode);
        log.info("Stored new case: " + caseId + " for transaction: " + txId);
        return caseNode;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void addAgentOutput(ArrayNode outputs, String name, JsonNode agentResult) {
        if (agentResult.isMissingNode() || agentResult.isNull()) return;

        ObjectNode o = mapper.createObjectNode();
        o.put("agentName", name);

        // Try common summary fields
        String summary = agentResult.path("reasoning").asText(
            agentResult.path("evidence_summary").asText(
                agentResult.path("explanation").asText("")));
        o.put("summary", summary.isEmpty() ? name + " completed." : summary);

        // Collect flags
        ArrayNode flags = mapper.createArrayNode();
        JsonNode f = agentResult.path("flags");
        if (f.isArray()) f.forEach(flags::add);
        JsonNode cf = agentResult.path("combined_flags");
        if (cf.isArray()) cf.forEach(flags::add);
        o.set("flags", flags);
        o.put("timestamp", Instant.now().toString());
        outputs.add(o);
    }

    private JsonNode extractJson(String text) {
        // Find the last JSON object block in the output
        int last = text.lastIndexOf('{');
        int end = text.lastIndexOf('}');
        if (last >= 0 && end > last) {
            try {
                return mapper.readTree(text.substring(last, end + 1));
            } catch (Exception ignored) {}
        }
        // Try the whole text
        try { return mapper.readTree(text.trim()); }
        catch (Exception e) { return mapper.createObjectNode(); }
    }

    private String mapSeverity(String raw) {
        return switch (raw.toUpperCase()) {
            case "CRITICAL" -> "CRITICAL";
            case "HIGH" -> "HIGH";
            case "MED", "MEDIUM" -> "MEDIUM";
            default -> "LOW";
        };
    }

    private String mapRecommendedAction(String raw) {
        return switch (raw.toUpperCase()) {
            case "BLOCK" -> "freeze_transaction";
            case "CHALLENGE" -> "request_step_up_auth";
            case "REVIEW" -> "create_case_report";
            default -> "create_case_report";
        };
    }

    private String firstAction(JsonNode actions) {
        if (actions.isArray() && actions.size() > 0) return actions.get(0).asText();
        return "";
    }
}
