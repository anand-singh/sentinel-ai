package com.ing.sentinel.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory case store. Pre-seeded with sample data matching the frontend mockDb.ts.
 * Thread-safe via ConcurrentHashMap.
 */
@Service
public class CaseStoreService {

    private final ObjectMapper mapper = new ObjectMapper();

    // alert id → alert object
    private final ConcurrentHashMap<String, ObjectNode> alerts = new ConcurrentHashMap<>();
    // case id → case detail object
    private final ConcurrentHashMap<String, ObjectNode> cases = new ConcurrentHashMap<>();

    private final AtomicLong idCounter = new AtomicLong(1000);

    public CaseStoreService() {
        seed();
    }

    // ── Alerts ─────────────────────────────────────────────────────────────────

    public Map<String, Object> getAlerts(int page, int limit, String severity, String status, String q) {
        List<ObjectNode> all = new ArrayList<>(alerts.values());

        // Sort newest first
        all.sort((a, b) -> b.path("timestamp").asText().compareTo(a.path("timestamp").asText()));

        // Filter
        List<ObjectNode> filtered = all.stream()
            .filter(a -> severity == null || severity.isBlank() || severity.equalsIgnoreCase(a.path("severity").asText()))
            .filter(a -> status == null || status.isBlank() || status.equalsIgnoreCase(a.path("status").asText()))
            .filter(a -> {
                if (q == null || q.isBlank()) return true;
                String lower = q.toLowerCase();
                return a.path("transactionId").asText().toLowerCase().contains(lower)
                    || a.path("customerId").asText().toLowerCase().contains(lower)
                    || a.path("customerName").asText().toLowerCase().contains(lower);
            })
            .collect(Collectors.toList());

        int total = filtered.size();
        int totalPages = (int) Math.ceil((double) total / limit);
        int from = Math.min((page - 1) * limit, total);
        int to = Math.min(from + limit, total);
        List<ObjectNode> page_data = filtered.subList(from, to);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data", page_data);
        result.put("meta", Map.of(
            "page", page,
            "limit", limit,
            "total", total,
            "totalPages", totalPages
        ));
        return result;
    }

    public Optional<ObjectNode> getAlert(String id) {
        return Optional.ofNullable(alerts.get(id));
    }

    public void putAlert(ObjectNode alert) {
        alerts.put(alert.path("id").asText(), alert);
    }

    // ── Cases ──────────────────────────────────────────────────────────────────

    public Optional<ObjectNode> getCase(String id) {
        return Optional.ofNullable(cases.get(id));
    }

    public void putCase(ObjectNode caseDetail) {
        cases.put(caseDetail.path("caseId").asText(), caseDetail);
    }

    public void assignCase(String caseId, String assignTo) {
        ObjectNode c = cases.get(caseId);
        if (c == null) throw new NoSuchElementException("Case not found: " + caseId);
        c.put("assignedTo", assignTo);
        c.put("status", "IN_REVIEW");
        appendAudit(c, "Case assigned to " + assignTo, "analyst");
    }

    public void addNote(String caseId, String content) {
        ObjectNode c = cases.get(caseId);
        if (c == null) throw new NoSuchElementException("Case not found: " + caseId);
        ArrayNode notes = (ArrayNode) c.get("notes");
        if (notes == null) { notes = mapper.createArrayNode(); c.set("notes", notes); }
        notes.add(content);
        appendAudit(c, "Note added: " + content.substring(0, Math.min(60, content.length())), "analyst");
    }

    public void escalateCase(String caseId) {
        ObjectNode c = cases.get(caseId);
        if (c == null) throw new NoSuchElementException("Case not found: " + caseId);
        c.put("status", "ESCALATED");
        appendAudit(c, "Case escalated", "analyst");
    }

    public void closeCase(String caseId) {
        ObjectNode c = cases.get(caseId);
        if (c == null) throw new NoSuchElementException("Case not found: " + caseId);
        c.put("status", "CLOSED");
        appendAudit(c, "Case closed", "analyst");
    }

    // ── Analytics ──────────────────────────────────────────────────────────────

    public Map<String, Object> getAnalytics() {
        // Compute from live case data
        Map<String, Integer> bySeverity = new LinkedHashMap<>();
        for (String s : List.of("CRITICAL", "HIGH", "MEDIUM", "LOW")) bySeverity.put(s, 0);

        Map<String, Integer> flagCounts = new HashMap<>();

        for (ObjectNode alert : alerts.values()) {
            String sev = alert.path("severity").asText();
            bySeverity.merge(sev, 1, Integer::sum);
        }

        for (ObjectNode c : cases.values()) {
            JsonNode outputs = c.path("agentOutputs");
            if (outputs.isArray()) {
                for (JsonNode o : outputs) {
                    JsonNode flags = o.path("flags");
                    if (flags.isArray()) {
                        for (JsonNode f : flags) flagCounts.merge(f.asText(), 1, Integer::sum);
                    }
                }
            }
        }

        List<Map<String, Object>> alertsBySeverity = bySeverity.entrySet().stream()
            .map(e -> Map.<String, Object>of("severity", e.getKey(), "count", e.getValue()))
            .collect(Collectors.toList());

        List<Map<String, Object>> topFlags = flagCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .map(e -> Map.<String, Object>of("flag", e.getKey(), "count", e.getValue()))
            .collect(Collectors.toList());

        // Generate 30-day trend from stored cases
        Map<String, Integer> dailyCounts = new LinkedHashMap<>();
        Instant now = Instant.now();
        for (int i = 29; i >= 0; i--) {
            dailyCounts.put(now.minus(i, ChronoUnit.DAYS).toString().substring(0, 10), 0);
        }
        for (ObjectNode alert : alerts.values()) {
            String ts = alert.path("timestamp").asText();
            if (ts.length() >= 10) {
                String day = ts.substring(0, 10);
                dailyCounts.merge(day, 1, Integer::sum);
            }
        }
        List<Map<String, Object>> trends = dailyCounts.entrySet().stream()
            .map(e -> Map.<String, Object>of("date", e.getKey(), "count", e.getValue()))
            .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("alertsBySeverity", alertsBySeverity);
        result.put("trendsLast30Days", trends);
        result.put("topFlags", topFlags);
        result.put("avgTimeToCloseHours", 3.4);
        result.put("escalationRatePct", 12);
        return result;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void appendAudit(ObjectNode caseNode, String event, String actor) {
        ArrayNode trail = (ArrayNode) caseNode.get("auditTrail");
        if (trail == null) { trail = mapper.createArrayNode(); caseNode.set("auditTrail", trail); }
        long idx = trail.size() + 1;
        String caseId = caseNode.path("caseId").asText();
        ObjectNode entry = mapper.createObjectNode();
        entry.put("id", "AUD-" + String.format("%03d", idCounter.incrementAndGet()));
        entry.put("event", event);
        entry.put("actor", actor);
        entry.put("timestamp", Instant.now().toString());
        entry.put("correlationId", "CORR-" + caseId + "-" + String.format("%03d", idx));
        trail.add(entry);
    }

    // ── Seed data (mirrors web/src/lib/mockDb.ts) ──────────────────────────────

    private void seed() {
        Instant now = Instant.now();

        // ── Alerts ──
        putAlert(alert("ALERT-001", "TX-9204", "CUST-SJ01", "Sarah Jenkins",
            85, "CRITICAL", "REVIEWING", "freeze_transaction", now.minus(2, ChronoUnit.MINUTES)));
        putAlert(alert("ALERT-002", "TX-9198", "CUST-RC02", "Robert Chen",
            58, "MEDIUM", "QUEUED", "notify_security_team", now.minus(15, ChronoUnit.MINUTES)));
        putAlert(alert("ALERT-003", "TX-9195", "CUST-EW03", "Emily Watson",
            28, "LOW", "RESOLVED", "create_case_report", now.minus(42, ChronoUnit.MINUTES)));
        putAlert(alert("ALERT-004", "TX-9192", "CUST-MT04", "Michael Thorne",
            91, "CRITICAL", "REVIEWING", "freeze_transaction", now.minus(60, ChronoUnit.MINUTES)));
        putAlert(alert("ALERT-005", "TX-9188", "CUST-AL05", "Alice Leung",
            72, "HIGH", "QUEUED", "notify_security_team", now.minus(120, ChronoUnit.MINUTES)));
        putAlert(alert("ALERT-006", "TX-9180", "CUST-DR06", "David Ross",
            38, "LOW", "CLOSED", "create_case_report", now.minus(180, ChronoUnit.MINUTES)));

        // ── Case F-9204 ──
        ObjectNode c1 = mapper.createObjectNode();
        c1.put("caseId", "F-9204");
        c1.set("alert", alerts.get("ALERT-001"));
        c1.put("status", "IN_REVIEW");
        c1.put("assignedTo", "Marcus Vance");
        c1.put("policyVersion", "action-policy-2026-03-16");
        c1.put("agentVersion", "orchestrator-v1.0.0");
        c1.set("notes", mapper.createArrayNode()
            .add("Initial review started. Transaction from Lagos vs history of New York."));
        c1.set("relatedTransactionIds", mapper.createArrayNode()
            .add("TX-9204").add("TX-9201").add("TX-9199"));
        c1.set("agentOutputs", agentOutputs(
            agentOut("Pattern Analyzer",
                "Transaction pattern shows rapid geographic shift — login from Lagos, NG after history of New York, US logins.",
                List.of("GEO_MISMATCH", "RAPID_LOCATION_CHANGE"), now.minus(3, ChronoUnit.MINUTES)),
            agentOut("Behavioral Risk Agent",
                "Amount ($4,950.00) is 340% above this customer's 30-day average. New device fingerprint detected.",
                List.of("AMOUNT_SPIKE", "NEW_DEVICE"), now.minus(3, ChronoUnit.MINUTES)),
            agentOut("Evidence Builder",
                "4 transactions in under 30 seconds (velocity check failed). Transaction to previously-unseen merchant.",
                List.of("VELOCITY_CHECK_FAILED", "NEW_MERCHANT"), now.minus(2, ChronoUnit.MINUTES)),
            agentOut("Aggregated Risk Scorer",
                "Combined signal weight produces a final risk score of 85/100 — classified as VERY HIGH.",
                List.of("HIGH_RISK_COMPOSITE"), now.minus(2, ChronoUnit.MINUTES))
        ));
        c1.set("actionsExecuted", actionEntries(
            actionEntry("ACT-001", "freeze_transaction", "sentinel-agent-v2.4.1", now.minus(2, ChronoUnit.MINUTES), null),
            actionEntry("ACT-002", "notify_security_team", "sentinel-agent-v2.4.1",
                now.minusMillis((long)(1.5*60*1000)), "Security team notified via PagerDuty."),
            actionEntry("ACT-003", "create_case_report", "sentinel-agent-v2.4.1", now.minus(1, ChronoUnit.MINUTES), null)
        ));
        c1.set("auditTrail", auditTrail(
            audit("AUD-001", "Transaction flagged by Pattern Analyzer", "sentinel-agent-v2.4.1",
                now.minus(3, ChronoUnit.MINUTES), "CORR-TX9204-001", "action-policy-2026-03-16"),
            audit("AUD-002", "Risk score computed: 85 (VERY HIGH)", "sentinel-agent-v2.4.1",
                now.minusMillis((long)(2.5*60*1000)), "CORR-TX9204-002", null),
            audit("AUD-003", "Transaction frozen", "sentinel-agent-v2.4.1",
                now.minus(2, ChronoUnit.MINUTES), "CORR-TX9204-003", "action-policy-2026-03-16"),
            audit("AUD-004", "Case assigned to Marcus Vance", "system",
                now.minus(1, ChronoUnit.MINUTES), "CORR-TX9204-004", null)
        ));
        putCase(c1);

        // ── Case F-9198 ──
        ObjectNode c2 = mapper.createObjectNode();
        c2.put("caseId", "F-9198");
        c2.set("alert", alerts.get("ALERT-002"));
        c2.put("status", "OPEN");
        c2.putNull("assignedTo");
        c2.put("policyVersion", "action-policy-2026-03-16");
        c2.put("agentVersion", "orchestrator-v1.0.0");
        c2.set("notes", mapper.createArrayNode());
        c2.set("relatedTransactionIds", mapper.createArrayNode().add("TX-9198"));
        c2.set("agentOutputs", agentOutputs(
            agentOut("Pattern Analyzer",
                "Medium-risk transaction. Slightly elevated amount, known merchant.",
                List.of("AMOUNT_ELEVATED"), now.minus(16, ChronoUnit.MINUTES))
        ));
        c2.set("actionsExecuted", actionEntries(
            actionEntry("ACT-010", "notify_security_team", "sentinel-agent-v2.4.1",
                now.minus(15, ChronoUnit.MINUTES), null)
        ));
        c2.set("auditTrail", auditTrail(
            audit("AUD-010", "Transaction flagged", "sentinel-agent-v2.4.1",
                now.minus(16, ChronoUnit.MINUTES), "CORR-TX9198-001", null)
        ));
        putCase(c2);
    }

    private ObjectNode alert(String id, String txId, String custId, String name,
                             int score, String severity, String status, String action, Instant ts) {
        ObjectNode a = mapper.createObjectNode();
        a.put("id", id);
        a.put("transactionId", txId);
        a.put("customerId", custId);
        a.put("customerName", name);
        a.put("finalRiskScore", score);
        a.put("severity", severity);
        a.put("status", status);
        a.put("recommendedAction", action);
        a.put("timestamp", ts.toString());
        return a;
    }

    private ObjectNode agentOut(String name, String summary, List<String> flags, Instant ts) {
        ObjectNode o = mapper.createObjectNode();
        o.put("agentName", name);
        o.put("summary", summary);
        ArrayNode f = mapper.createArrayNode();
        flags.forEach(f::add);
        o.set("flags", f);
        o.put("timestamp", ts.toString());
        return o;
    }

    @SafeVarargs
    private ArrayNode agentOutputs(ObjectNode... items) {
        ArrayNode a = mapper.createArrayNode();
        for (ObjectNode n : items) a.add(n);
        return a;
    }

    private ObjectNode actionEntry(String id, String type, String by, Instant ts, String note) {
        ObjectNode e = mapper.createObjectNode();
        e.put("id", id);
        e.put("type", type);
        e.put("performedBy", by);
        e.put("timestamp", ts.toString());
        if (note != null) e.put("note", note);
        return e;
    }

    @SafeVarargs
    private ArrayNode actionEntries(ObjectNode... items) {
        ArrayNode a = mapper.createArrayNode();
        for (ObjectNode n : items) a.add(n);
        return a;
    }

    private ObjectNode audit(String id, String event, String actor, Instant ts,
                             String corrId, String policy) {
        ObjectNode e = mapper.createObjectNode();
        e.put("id", id);
        e.put("event", event);
        e.put("actor", actor);
        e.put("timestamp", ts.toString());
        e.put("correlationId", corrId);
        if (policy != null) e.put("policyVersion", policy);
        return e;
    }

    @SafeVarargs
    private ArrayNode auditTrail(ObjectNode... items) {
        ArrayNode a = mapper.createArrayNode();
        for (ObjectNode n : items) a.add(n);
        return a;
    }
}
