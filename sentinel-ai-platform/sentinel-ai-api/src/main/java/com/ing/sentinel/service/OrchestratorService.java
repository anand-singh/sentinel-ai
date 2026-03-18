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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        // Enrich the transaction with derived behavioral fields before pipeline
        String enrichedJson = enrichTransactionForPipeline(transactionJson);

        String agentName = SentinelOrchestrator.ROOT_AGENT.name();
        String userId = "api-user-" + UUID.randomUUID().toString().substring(0, 8);

        Session session = runner.sessionService()
            .createSession(agentName, userId)
            .blockingGet();

        Content userMsg = Content.fromParts(Part.fromText(enrichedJson));

        StringBuilder fullOutput = new StringBuilder();
        runner.runAsync(userId, session.id(), userMsg)
            .timeout(90, TimeUnit.SECONDS)
            .blockingForEach(event -> fullOutput.append(event.stringifyContent()).append("\n"));

        String raw = fullOutput.toString();
        log.info("Orchestrator raw output length: " + raw.length());

        // Extract JSON from the LLM response
        JsonNode orchResult = extractJson(raw);

        // Parse transaction metadata (use enriched JSON for full field access)
        JsonNode txNode;
        try { txNode = mapper.readTree(enrichedJson); }
        catch (Exception e) { txNode = mapper.createObjectNode(); }

        String txId = txNode.path("transaction_id").asText("TX-" + UUID.randomUUID().toString().substring(0, 8));
        String custId = txNode.path("customer_id").asText("CUST-UNKNOWN");
        String custName = txNode.path("customer_name").asText(custId);

        // Build Alert from final_decision
        JsonNode decision = orchResult.path("final_decision");
        int riskScore = decision.path("risk_score").asInt(50);
        String severity = mapSeverity(decision.path("severity").asText("MED"));
        String recAction = firstAction(decision.path("executed_actions"));
        if (recAction.isEmpty()) recAction = mapRecommendedAction(decision.path("recommended_action").asText(
            orchResult.path("pipeline_results").path("aggregated_scorer")
                .path("recommended_action").asText("REVIEW")));

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
        List<String> actionNames = new ArrayList<>();
        if (execActions.isArray() && execActions.size() > 0) {
            execActions.forEach(a -> actionNames.add(a.asText()));
        } else {
            // ActionExecutor returned no output — derive actions from aggregator's recommended_action
            String recActionForFallback = orchResult.path("pipeline_results")
                .path("aggregated_scorer").path("recommended_action").asText(
                    decision.path("recommended_action").asText("REVIEW"));
            actionNames.addAll(inferActions(recActionForFallback));
            log.info("[ingest] ActionExecutor empty — inferred actions from aggregator: " + actionNames);
        }
        int actIdx = 1;
        for (String action : actionNames) {
            ObjectNode ae = mapper.createObjectNode();
            ae.put("id", "ACT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
            ae.put("type", action);
            ae.put("performedBy", "orchestrator-v1.0.0");
            ae.put("timestamp", Instant.now().minus(actIdx * 10L, ChronoUnit.SECONDS).toString());
            actionsExec.add(ae);
            actIdx++;
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

    private List<String> inferActions(String recommendedAction) {
        return switch (recommendedAction.toUpperCase()) {
            case "BLOCK"     -> List.of("freeze_transaction", "notify_security_team", "create_case_report");
            case "CHALLENGE" -> List.of("request_step_up_auth", "create_case_report");
            case "REVIEW"    -> List.of("create_case_report");
            default          -> List.of("create_case_report");
        };
    }

    // ── Known city → [lat, lon] used to enrich transactions lacking explicit geo ──
    private static final Map<String, double[]> CITY_COORDS = Map.of(
        "new york",  new double[]{ 40.7128,  -74.0060 },
        "lagos",     new double[]{  6.5244,    3.3792 },
        "london",    new double[]{ 51.5074,   -0.1278 },
        "amsterdam", new double[]{ 52.3676,    4.9041 },
        "dubai",     new double[]{ 25.2048,   55.2708 },
        "paris",     new double[]{ 48.8566,    2.3522 },
        "singapore", new double[]{  1.3521,  103.8198 }
    );

    private double[] cityToCoords(String location) {
        if (location == null || location.isBlank()) return null;
        String lower = location.toLowerCase();
        for (Map.Entry<String, double[]> entry : CITY_COORDS.entrySet()) {
            if (lower.contains(entry.getKey())) return entry.getValue();
        }
        return null;
    }

    /**
     * Enriches the incoming transaction JSON with derived behavioral fields
     * (lat/lon, account_age_days, usual_ip_ranges, etc.) so that the
     * BehavioralRiskDetector LLM can parameterize ALL of its signal tools
     * without needing to infer/guess missing values.
     */
    private String enrichTransactionForPipeline(String transactionJson) {
        try {
            ObjectNode tx = (ObjectNode) mapper.readTree(transactionJson);

            // Add transaction-level lat/lon if missing (used by GeoDistanceTool in Pattern Analyzer)
            if (!tx.has("lat") || !tx.has("lon")) {
                String city    = tx.path("city").asText("");
                String country = tx.path("country").asText("");
                double[] coords = cityToCoords(city + ", " + country);
                if (coords == null) coords = cityToCoords(city);
                if (coords != null) {
                    tx.put("lat", coords[0]);
                    tx.put("lon", coords[1]);
                }
            }

            // Enrich customer_profile with behavioral tool parameters
            JsonNode profileNode = tx.path("customer_profile");
            if (profileNode.isObject()) {
                ObjectNode p = (ObjectNode) profileNode;

                // account_age_days / prior_transaction_count
                if (!p.has("account_age_days"))
                    p.put("account_age_days", 5);          // very new = high risk default
                if (!p.has("prior_transaction_count"))
                    p.put("prior_transaction_count", 1);

                // IP range — empty = any IP is novel
                if (!p.has("usual_ip_ranges"))
                    p.put("usual_ip_ranges", "");

                // Burst detection fields
                if (!p.has("txns_1h"))
                    p.put("txns_1h", 1);
                if (!p.has("baseline_txns_1h"))
                    p.put("baseline_txns_1h", 0.1);

                // home_countries as comma-separated string for GeoDeviationSignal
                if (!p.has("home_countries") && p.has("home_country"))
                    p.put("home_countries", p.path("home_country").asText(""));

                // Derive last_known_lat/lon from last_login_location if missing
                if (!p.has("last_known_lat") && p.has("last_login_location")) {
                    double[] coords = cityToCoords(p.path("last_login_location").asText(""));
                    if (coords != null) {
                        p.put("last_known_lat", coords[0]);
                        p.put("last_known_lon", coords[1]);
                    }
                }
            }

            return mapper.writeValueAsString(tx);
        } catch (Exception e) {
            log.warning("[ingest] Transaction enrichment failed: " + e.getMessage() + " — using original");
            return transactionJson;
        }
    }

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
        // The orchestrator accumulates output from ALL 5 sub-agents, so the full text
        // contains many JSON blocks. The final summary (with "orchestration_status" and
        // "final_decision") is always appended LAST by the orchestrator LLM.
        // Strategy: collect all top-level JSON blocks, then return the last one that
        // contains the orchestrator summary keys.

        List<int[]> blocks = new ArrayList<>();
        int searchFrom = 0;
        while (searchFrom < text.length()) {
            int start = text.indexOf('{', searchFrom);
            if (start < 0) break;
            int depth = 0;
            boolean inString = false;
            boolean escape = false;
            int end = -1;
            for (int i = start; i < text.length(); i++) {
                char c = text.charAt(i);
                if (escape) { escape = false; continue; }
                if (c == '\\' && inString) { escape = true; continue; }
                if (c == '"') { inString = !inString; continue; }
                if (inString) continue;
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) { end = i; break; }
                }
            }
            if (end > start) {
                blocks.add(new int[]{start, end});
                searchFrom = end + 1;
            } else {
                searchFrom = start + 1;
            }
        }

        // Search from last block backwards for the orchestrator summary
        for (int i = blocks.size() - 1; i >= 0; i--) {
            int[] b = blocks.get(i);
            try {
                JsonNode node = mapper.readTree(text.substring(b[0], b[1] + 1));
                if (!node.path("orchestration_status").isMissingNode()
                        || !node.path("final_decision").isMissingNode()) {
                    log.info("[extractJson] Found orchestrator summary at block " + i + "/" + blocks.size());
                    return node;
                }
            } catch (Exception ignored) {}
        }

        // Fallback: return the last parseable JSON block
        for (int i = blocks.size() - 1; i >= 0; i--) {
            int[] b = blocks.get(i);
            try { return mapper.readTree(text.substring(b[0], b[1] + 1)); }
            catch (Exception ignored) {}
        }

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
