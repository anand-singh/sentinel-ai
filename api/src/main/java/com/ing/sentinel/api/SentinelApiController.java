package com.ing.sentinel.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ing.sentinel.service.OrchestratorService;
import com.ing.sentinel.store.CaseStoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.logging.Logger;

/**
 * REST API for the Sentinel dashboard.
 *
 * All paths mirror the Next.js mock routes in web/src/app/api/ so the
 * frontend can proxy to this server with zero path changes.
 *
 * NOTE: Endpoints that return ObjectNode (Jackson 2.x) or Map<String,Object>
 * containing ObjectNode values serialize to JSON String manually to avoid a
 * Jackson 2.x vs Jackson 3.x conflict (Spring Boot 4.0 uses Jackson 3.x,
 * while google-adk uses Jackson 2.x ObjectMapper internally).
 */
@RestController
@RequestMapping("/api")
public class SentinelApiController {

    private static final Logger log = Logger.getLogger(SentinelApiController.class.getName());

    private final CaseStoreService store;
    private final OrchestratorService orchestrator;
    private final ObjectMapper jackson2 = new ObjectMapper(); // Jackson 2.x for our data model

    public SentinelApiController(CaseStoreService store, OrchestratorService orchestrator) {
        this.store = store;
        this.orchestrator = orchestrator;
    }

    // ── Alerts ─────────────────────────────────────────────────────────────────

    @GetMapping("/alerts")
    public ResponseEntity<String> listAlerts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q) {
        try {
            String json = jackson2.writeValueAsString(store.getAlerts(page, limit, severity, status, q));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
        } catch (Exception e) {
            return error500(e);
        }
    }

    // ── Cases ──────────────────────────────────────────────────────────────────

    @GetMapping("/cases/{id}")
    public ResponseEntity<String> getCase(@PathVariable String id) {
        try {
            return store.getCase(id)
                .map(node -> {
                    try {
                        return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(jackson2.writeValueAsString(node));
                    } catch (Exception e) {
                        return ResponseEntity.status(500)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"error\":\"Serialization failed\"}");
                    }
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Case not found\"}"));
        } catch (Exception e) {
            return error500(e);
        }
    }

    @PostMapping("/cases/{id}/assign")
    public ResponseEntity<String> assignCase(@PathVariable String id,
                                             @RequestBody Map<String, String> body) {
        String assignTo = body.getOrDefault("assignTo", "");
        if (assignTo.isBlank()) return badRequest("assignTo required");
        try {
            store.assignCase(id, assignTo);
            return caseResponse(id);
        } catch (Exception e) {
            return notFound(e.getMessage());
        }
    }

    @PostMapping("/cases/{id}/note")
    public ResponseEntity<String> addNote(@PathVariable String id,
                                          @RequestBody Map<String, String> body) {
        String content = body.getOrDefault("content", "");
        if (content.isBlank()) return badRequest("content required");
        try {
            store.addNote(id, content);
            return caseResponse(id);
        } catch (Exception e) {
            return notFound(e.getMessage());
        }
    }

    @PostMapping("/cases/{id}/escalate")
    public ResponseEntity<String> escalateCase(@PathVariable String id) {
        try {
            store.escalateCase(id);
            return caseResponse(id);
        } catch (Exception e) {
            return notFound(e.getMessage());
        }
    }

    @PostMapping("/cases/{id}/close")
    public ResponseEntity<String> closeCase(@PathVariable String id) {
        try {
            store.closeCase(id);
            return caseResponse(id);
        } catch (Exception e) {
            return notFound(e.getMessage());
        }
    }

    // ── Analytics ──────────────────────────────────────────────────────────────

    @GetMapping("/analytics")
    public ResponseEntity<String> analytics() {
        try {
            String json = jackson2.writeValueAsString(store.getAnalytics());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
        } catch (Exception e) {
            return error500(e);
        }
    }

    // ── Ingest ─────────────────────────────────────────────────────────────────

    @PostMapping("/ingest")
    public ResponseEntity<String> ingest(@RequestBody String transactionJson) {
        log.info("POST /api/ingest — running orchestrator pipeline");
        try {
            ObjectNode caseDetail = orchestrator.ingest(transactionJson);
            String json = jackson2.writeValueAsString(caseDetail);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
        } catch (Exception e) {
            log.severe("Orchestrator error: " + e.getMessage());
            return error500(e);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private ResponseEntity<String> caseResponse(String id) throws Exception {
        return store.getCase(id)
            .map(node -> {
                try {
                    return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(jackson2.writeValueAsString(node));
                } catch (Exception ex) {
                    return ResponseEntity.status(500)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"Serialization failed\"}");
                }
            })
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\":\"Case not found\"}"));
    }

    private ResponseEntity<String> error500(Exception e) {
        log.severe(e.getMessage());
        return ResponseEntity.status(500)
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}");
    }

    private ResponseEntity<String> badRequest(String msg) {
        return ResponseEntity.badRequest()
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"error\":\"" + msg + "\"}");
    }

    private ResponseEntity<String> notFound(String msg) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"error\":\"" + msg + "\"}");
    }
}
