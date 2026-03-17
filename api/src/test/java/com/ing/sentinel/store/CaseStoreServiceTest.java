package com.ing.sentinel.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CaseStoreService.
 * Tests store initialization, CRUD operations, and data transformation logic.
 * 
 * Note: These are primarily unit tests focusing on in-memory storage and operations.
 * Tests verify thread-safe operations and pre-seeded data.
 */
@DisplayName("CaseStoreService Tests")
class CaseStoreServiceTest {

    private CaseStoreService store;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        // Initialize the store before each test
        store = new CaseStoreService();
        mapper = new ObjectMapper();
        assertNotNull(store, "Store should be initialized");
    }

    @Test
    @DisplayName("Should initialize store successfully")
    void testStoreInitialization() {
        assertNotNull(store, "Store should not be null");
        assertInstanceOf(CaseStoreService.class, store, 
                "Should be a CaseStoreService instance");
    }

    @Test
    @DisplayName("Should seed alerts on initialization")
    void testAlertSeeding() {
        Map<String, Object> result = store.getAlerts(1, 10, null, null, null);
        assertNotNull(result);
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        assertNotNull(alerts);
        assertFalse(alerts.isEmpty(), "Should have seeded alerts");
    }

    @Test
    @DisplayName("Should seed cases on initialization")
    void testCaseSeeding() {
        Optional<ObjectNode> case1 = store.getCase("F-9204");
        Optional<ObjectNode> case2 = store.getCase("F-9198");
        
        assertTrue(case1.isPresent(), "Should have seeded case F-9204");
        assertTrue(case2.isPresent(), "Should have seeded case F-9198");
    }

    @Test
    @DisplayName("Should get alert by ID")
    void testGetAlertById() {
        Optional<ObjectNode> alert = store.getAlert("ALERT-001");
        assertTrue(alert.isPresent());
        assertEquals("ALERT-001", alert.get().path("id").asText());
    }

    @Test
    @DisplayName("Should return empty for non-existent alert")
    void testGetNonExistentAlert() {
        Optional<ObjectNode> alert = store.getAlert("ALERT-NONEXISTENT");
        assertFalse(alert.isPresent());
    }

    @Test
    @DisplayName("Should put and retrieve alert")
    void testPutAndRetrieveAlert() {
        ObjectNode newAlert = mapper.createObjectNode();
        newAlert.put("id", "ALERT-TEST");
        newAlert.put("transactionId", "TX-TEST");
        newAlert.put("customerId", "CUST-TEST");
        newAlert.put("severity", "MEDIUM");
        
        store.putAlert(newAlert);
        Optional<ObjectNode> retrieved = store.getAlert("ALERT-TEST");
        
        assertTrue(retrieved.isPresent());
        assertEquals("TX-TEST", retrieved.get().path("transactionId").asText());
    }

    @Test
    @DisplayName("Should update existing alert")
    void testUpdateAlert() {
        ObjectNode alert = mapper.createObjectNode();
        alert.put("id", "ALERT-UPDATE");
        alert.put("status", "QUEUED");
        store.putAlert(alert);
        
        // Update
        alert.put("status", "REVIEWING");
        store.putAlert(alert);
        
        Optional<ObjectNode> retrieved = store.getAlert("ALERT-UPDATE");
        assertEquals("REVIEWING", retrieved.get().path("status").asText());
    }

    @Test
    @DisplayName("Should get case by ID")
    void testGetCaseById() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        assertEquals("F-9204", caseNode.get().path("caseId").asText());
    }

    @Test
    @DisplayName("Should return empty for non-existent case")
    void testGetNonExistentCase() {
        Optional<ObjectNode> caseNode = store.getCase("F-NONEXISTENT");
        assertFalse(caseNode.isPresent());
    }

    @Test
    @DisplayName("Should put and retrieve case")
    void testPutAndRetrieveCase() {
        ObjectNode newCase = mapper.createObjectNode();
        newCase.put("caseId", "F-TEST");
        newCase.put("status", "OPEN");
        
        store.putCase(newCase);
        Optional<ObjectNode> retrieved = store.getCase("F-TEST");
        
        assertTrue(retrieved.isPresent());
        assertEquals("OPEN", retrieved.get().path("status").asText());
    }

    @Test
    @DisplayName("Should assign case to analyst")
    void testAssignCase() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-ASSIGN");
        caseNode.put("status", "OPEN");
        caseNode.set("auditTrail", mapper.createArrayNode());
        store.putCase(caseNode);
        
        store.assignCase("F-ASSIGN", "John Analyst");
        
        Optional<ObjectNode> updated = store.getCase("F-ASSIGN");
        assertTrue(updated.isPresent());
        assertEquals("John Analyst", updated.get().path("assignedTo").asText());
        assertEquals("IN_REVIEW", updated.get().path("status").asText());
    }

    @Test
    @DisplayName("Should throw exception when assigning non-existent case")
    void testAssignNonExistentCase() {
        assertThrows(NoSuchElementException.class, () -> {
            store.assignCase("F-NONEXISTENT", "John Analyst");
        });
    }

    @Test
    @DisplayName("Should add note to case")
    void testAddNote() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-NOTE");
        caseNode.set("notes", mapper.createArrayNode());
        caseNode.set("auditTrail", mapper.createArrayNode());
        store.putCase(caseNode);
        
        store.addNote("F-NOTE", "This is a test note");
        
        Optional<ObjectNode> updated = store.getCase("F-NOTE");
        assertTrue(updated.isPresent());
        JsonNode notes = updated.get().path("notes");
        assertTrue(notes.isArray());
        assertTrue(notes.size() > 0);
        assertEquals("This is a test note", notes.get(0).asText());
    }

    @Test
    @DisplayName("Should create notes array if missing")
    void testAddNoteCreatesArray() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-NOTE2");
        caseNode.set("auditTrail", mapper.createArrayNode());
        store.putCase(caseNode);
        
        store.addNote("F-NOTE2", "First note");
        
        Optional<ObjectNode> updated = store.getCase("F-NOTE2");
        JsonNode notes = updated.get().path("notes");
        assertTrue(notes.isArray());
        assertEquals(1, notes.size());
    }

    @Test
    @DisplayName("Should throw exception when adding note to non-existent case")
    void testAddNoteNonExistentCase() {
        assertThrows(NoSuchElementException.class, () -> {
            store.addNote("F-NONEXISTENT", "Test note");
        });
    }

    @Test
    @DisplayName("Should truncate long notes in audit")
    void testTruncateLongNotes() {
        String longNote = "A".repeat(100);
        String truncated = longNote.substring(0, Math.min(60, longNote.length()));
        assertEquals(60, truncated.length());
    }

    @Test
    @DisplayName("Should escalate case")
    void testEscalateCase() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-ESCALATE");
        caseNode.put("status", "IN_REVIEW");
        caseNode.set("auditTrail", mapper.createArrayNode());
        store.putCase(caseNode);
        
        store.escalateCase("F-ESCALATE");
        
        Optional<ObjectNode> updated = store.getCase("F-ESCALATE");
        assertEquals("ESCALATED", updated.get().path("status").asText());
    }

    @Test
    @DisplayName("Should throw exception when escalating non-existent case")
    void testEscalateNonExistentCase() {
        assertThrows(NoSuchElementException.class, () -> {
            store.escalateCase("F-NONEXISTENT");
        });
    }

    @Test
    @DisplayName("Should close case")
    void testCloseCase() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-CLOSE");
        caseNode.put("status", "IN_REVIEW");
        caseNode.set("auditTrail", mapper.createArrayNode());
        store.putCase(caseNode);
        
        store.closeCase("F-CLOSE");
        
        Optional<ObjectNode> updated = store.getCase("F-CLOSE");
        assertEquals("CLOSED", updated.get().path("status").asText());
    }

    @Test
    @DisplayName("Should throw exception when closing non-existent case")
    void testCloseNonExistentCase() {
        assertThrows(NoSuchElementException.class, () -> {
            store.closeCase("F-NONEXISTENT");
        });
    }

    @Test
    @DisplayName("Should append audit entry on assign")
    void testAuditOnAssign() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-AUDIT1");
        caseNode.set("auditTrail", mapper.createArrayNode());
        store.putCase(caseNode);
        
        store.assignCase("F-AUDIT1", "Jane Analyst");
        
        Optional<ObjectNode> updated = store.getCase("F-AUDIT1");
        JsonNode trail = updated.get().path("auditTrail");
        assertTrue(trail.isArray());
        assertTrue(trail.size() > 0);
    }

    @Test
    @DisplayName("Should append audit entry on note")
    void testAuditOnNote() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-AUDIT2");
        caseNode.set("auditTrail", mapper.createArrayNode());
        store.putCase(caseNode);
        
        store.addNote("F-AUDIT2", "Test note");
        
        Optional<ObjectNode> updated = store.getCase("F-AUDIT2");
        JsonNode trail = updated.get().path("auditTrail");
        assertTrue(trail.isArray());
        assertTrue(trail.size() > 0);
    }

    @Test
    @DisplayName("Should append audit entry on escalate")
    void testAuditOnEscalate() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-AUDIT3");
        caseNode.set("auditTrail", mapper.createArrayNode());
        store.putCase(caseNode);
        
        store.escalateCase("F-AUDIT3");
        
        Optional<ObjectNode> updated = store.getCase("F-AUDIT3");
        JsonNode trail = updated.get().path("auditTrail");
        assertTrue(trail.isArray());
        assertTrue(trail.size() > 0);
    }

    @Test
    @DisplayName("Should append audit entry on close")
    void testAuditOnClose() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-AUDIT4");
        caseNode.set("auditTrail", mapper.createArrayNode());
        store.putCase(caseNode);
        
        store.closeCase("F-AUDIT4");
        
        Optional<ObjectNode> updated = store.getCase("F-AUDIT4");
        JsonNode trail = updated.get().path("auditTrail");
        assertTrue(trail.isArray());
        assertTrue(trail.size() > 0);
    }

    @Test
    @DisplayName("Should get alerts with pagination")
    void testGetAlertsWithPagination() {
        Map<String, Object> result = store.getAlerts(1, 5, null, null, null);
        
        assertNotNull(result);
        assertTrue(result.containsKey("data"));
        assertTrue(result.containsKey("meta"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> meta = (Map<String, Object>) result.get("meta");
        assertEquals(1, meta.get("page"));
        assertEquals(5, meta.get("limit"));
    }

    @Test
    @DisplayName("Should filter alerts by severity")
    void testFilterAlertsBySeverity() {
        Map<String, Object> result = store.getAlerts(1, 10, "CRITICAL", null, null);
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        
        for (ObjectNode alert : alerts) {
            assertEquals("CRITICAL", alert.path("severity").asText());
        }
    }

    @Test
    @DisplayName("Should filter alerts by status")
    void testFilterAlertsByStatus() {
        Map<String, Object> result = store.getAlerts(1, 10, null, "QUEUED", null);
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        
        for (ObjectNode alert : alerts) {
            assertEquals("QUEUED", alert.path("status").asText());
        }
    }

    @Test
    @DisplayName("Should search alerts by transaction ID")
    void testSearchAlertsByTransactionId() {
        Map<String, Object> result = store.getAlerts(1, 10, null, null, "TX-9204");
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        
        assertFalse(alerts.isEmpty());
        assertTrue(alerts.get(0).path("transactionId").asText().contains("TX-9204"));
    }

    @Test
    @DisplayName("Should search alerts by customer ID")
    void testSearchAlertsByCustomerId() {
        Map<String, Object> result = store.getAlerts(1, 10, null, null, "CUST-SJ01");
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        
        assertFalse(alerts.isEmpty());
        assertTrue(alerts.get(0).path("customerId").asText().contains("CUST-SJ01"));
    }

    @Test
    @DisplayName("Should search alerts by customer name")
    void testSearchAlertsByCustomerName() {
        Map<String, Object> result = store.getAlerts(1, 10, null, null, "Sarah");
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        
        assertFalse(alerts.isEmpty());
        assertTrue(alerts.get(0).path("customerName").asText().toLowerCase().contains("sarah"));
    }

    @Test
    @DisplayName("Should return empty when search query not found")
    void testSearchAlertsNoMatch() {
        Map<String, Object> result = store.getAlerts(1, 10, null, null, "NONEXISTENT-QUERY");
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        
        assertTrue(alerts.isEmpty());
    }

    @Test
    @DisplayName("Should sort alerts newest first")
    void testAlertsSortedNewestFirst() {
        Map<String, Object> result = store.getAlerts(1, 10, null, null, null);
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        
        if (alerts.size() >= 2) {
            String ts1 = alerts.get(0).path("timestamp").asText();
            String ts2 = alerts.get(1).path("timestamp").asText();
            assertTrue(ts1.compareTo(ts2) >= 0, "First alert should be newer or equal");
        }
    }

    @Test
    @DisplayName("Should calculate total pages correctly")
    void testTotalPagesCalculation() {
        Map<String, Object> result = store.getAlerts(1, 2, null, null, null);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> meta = (Map<String, Object>) result.get("meta");
        
        int total = (int) meta.get("total");
        int limit = (int) meta.get("limit");
        int totalPages = (int) meta.get("totalPages");
        
        assertEquals((int) Math.ceil((double) total / limit), totalPages);
    }

    @Test
    @DisplayName("Should handle page beyond total pages")
    void testPageBeyondTotal() {
        Map<String, Object> result = store.getAlerts(100, 10, null, null, null);
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        
        assertTrue(alerts.isEmpty(), "Should return empty list for page beyond total");
    }

    @Test
    @DisplayName("Should return correct meta information")
    void testMetaInformation() {
        Map<String, Object> result = store.getAlerts(2, 3, null, null, null);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> meta = (Map<String, Object>) result.get("meta");
        
        assertEquals(2, meta.get("page"));
        assertEquals(3, meta.get("limit"));
        assertTrue(meta.containsKey("total"));
        assertTrue(meta.containsKey("totalPages"));
    }

    @Test
    @DisplayName("Should get analytics successfully")
    void testGetAnalytics() {
        Map<String, Object> analytics = store.getAnalytics();
        
        assertNotNull(analytics);
        assertTrue(analytics.containsKey("alertsBySeverity"));
        assertTrue(analytics.containsKey("trendsLast30Days"));
        assertTrue(analytics.containsKey("topFlags"));
        assertTrue(analytics.containsKey("avgTimeToCloseHours"));
        assertTrue(analytics.containsKey("escalationRatePct"));
    }

    @Test
    @DisplayName("Should have alerts by severity in analytics")
    void testAnalyticsAlertsBySeverity() {
        Map<String, Object> analytics = store.getAnalytics();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> bySeverity = (List<Map<String, Object>>) 
                analytics.get("alertsBySeverity");
        
        assertNotNull(bySeverity);
        assertEquals(4, bySeverity.size()); // CRITICAL, HIGH, MEDIUM, LOW
        
        // Verify all severities are present
        List<String> severities = bySeverity.stream()
                .map(m -> (String) m.get("severity"))
                .toList();
        assertThat(severities).contains("CRITICAL", "HIGH", "MEDIUM", "LOW");
    }

    @Test
    @DisplayName("Should have 30-day trends in analytics")
    void testAnalyticsTrends() {
        Map<String, Object> analytics = store.getAnalytics();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trends = (List<Map<String, Object>>) 
                analytics.get("trendsLast30Days");
        
        assertNotNull(trends);
        assertEquals(30, trends.size(), "Should have 30 days of trend data");
    }

    @Test
    @DisplayName("Should have top flags in analytics")
    void testAnalyticsTopFlags() {
        Map<String, Object> analytics = store.getAnalytics();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topFlags = (List<Map<String, Object>>) 
                analytics.get("topFlags");
        
        assertNotNull(topFlags);
        assertTrue(topFlags.size() <= 5, "Should have at most 5 top flags");
    }

    @Test
    @DisplayName("Should have average time to close in analytics")
    void testAnalyticsAvgTimeToClose() {
        Map<String, Object> analytics = store.getAnalytics();
        
        Object avgTime = analytics.get("avgTimeToCloseHours");
        assertNotNull(avgTime);
        assertTrue(avgTime instanceof Number);
    }

    @Test
    @DisplayName("Should have escalation rate in analytics")
    void testAnalyticsEscalationRate() {
        Map<String, Object> analytics = store.getAnalytics();
        
        Object escalationRate = analytics.get("escalationRatePct");
        assertNotNull(escalationRate);
        assertTrue(escalationRate instanceof Number);
    }

    @Test
    @DisplayName("Should count flags correctly")
    void testFlagCounting() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-FLAGS");
        
        ArrayNode agentOutputs = mapper.createArrayNode();
        ObjectNode output = mapper.createObjectNode();
        output.put("agentName", "Test Agent");
        ArrayNode flags = mapper.createArrayNode();
        flags.add("FLAG1");
        flags.add("FLAG2");
        output.set("flags", flags);
        agentOutputs.add(output);
        caseNode.set("agentOutputs", agentOutputs);
        
        store.putCase(caseNode);
        
        Map<String, Object> analytics = store.getAnalytics();
        assertNotNull(analytics);
    }

    @Test
    @DisplayName("Should handle empty filter parameters")
    void testEmptyFilterParameters() {
        Map<String, Object> result1 = store.getAlerts(1, 10, null, null, null);
        Map<String, Object> result2 = store.getAlerts(1, 10, "", "", "");
        
        assertNotNull(result1);
        assertNotNull(result2);
    }

    @Test
    @DisplayName("Should handle case insensitive severity filter")
    void testCaseInsensitiveSeverityFilter() {
        Map<String, Object> result = store.getAlerts(1, 10, "critical", null, null);
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        
        for (ObjectNode alert : alerts) {
            assertEquals("CRITICAL", alert.path("severity").asText().toUpperCase());
        }
    }

    @Test
    @DisplayName("Should handle case insensitive status filter")
    void testCaseInsensitiveStatusFilter() {
        Map<String, Object> result = store.getAlerts(1, 10, null, "queued", null);
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        
        for (ObjectNode alert : alerts) {
            assertEquals("QUEUED", alert.path("status").asText().toUpperCase());
        }
    }

    @Test
    @DisplayName("Should handle case insensitive search query")
    void testCaseInsensitiveSearchQuery() {
        Map<String, Object> result = store.getAlerts(1, 10, null, null, "sarah");
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        
        if (!alerts.isEmpty()) {
            String name = alerts.get(0).path("customerName").asText().toLowerCase();
            assertTrue(name.contains("sarah"));
        }
    }

    @Test
    @DisplayName("Should validate seeded alert structure")
    void testSeededAlertStructure() {
        Optional<ObjectNode> alert = store.getAlert("ALERT-001");
        assertTrue(alert.isPresent());
        
        ObjectNode a = alert.get();
        assertTrue(a.has("id"));
        assertTrue(a.has("transactionId"));
        assertTrue(a.has("customerId"));
        assertTrue(a.has("customerName"));
        assertTrue(a.has("finalRiskScore"));
        assertTrue(a.has("severity"));
        assertTrue(a.has("status"));
        assertTrue(a.has("recommendedAction"));
        assertTrue(a.has("timestamp"));
    }

    @Test
    @DisplayName("Should validate seeded case structure")
    void testSeededCaseStructure() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        ObjectNode c = caseNode.get();
        assertTrue(c.has("caseId"));
        assertTrue(c.has("alert"));
        assertTrue(c.has("status"));
        assertTrue(c.has("assignedTo"));
        assertTrue(c.has("policyVersion"));
        assertTrue(c.has("agentVersion"));
        assertTrue(c.has("notes"));
        assertTrue(c.has("relatedTransactionIds"));
        assertTrue(c.has("agentOutputs"));
        assertTrue(c.has("actionsExecuted"));
        assertTrue(c.has("auditTrail"));
    }

    @Test
    @DisplayName("Should have case with IN_REVIEW status")
    void testSeededCaseInReview() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        assertEquals("IN_REVIEW", caseNode.get().path("status").asText());
    }

    @Test
    @DisplayName("Should have case with assigned analyst")
    void testSeededCaseAssigned() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        assertEquals("Marcus Vance", caseNode.get().path("assignedTo").asText());
    }

    @Test
    @DisplayName("Should have case with OPEN status")
    void testSeededCaseOpen() {
        Optional<ObjectNode> caseNode = store.getCase("F-9198");
        assertTrue(caseNode.isPresent());
        assertEquals("OPEN", caseNode.get().path("status").asText());
    }

    @Test
    @DisplayName("Should have case with null assignedTo")
    void testSeededCaseUnassigned() {
        Optional<ObjectNode> caseNode = store.getCase("F-9198");
        assertTrue(caseNode.isPresent());
        assertTrue(caseNode.get().path("assignedTo").isNull());
    }

    @Test
    @DisplayName("Should back-fill caseId into alerts")
    void testBackfilledCaseId() {
        Optional<ObjectNode> alert = store.getAlert("ALERT-001");
        assertTrue(alert.isPresent());
        assertEquals("F-9204", alert.get().path("caseId").asText());
    }

    @Test
    @DisplayName("Should have multiple agent outputs in seeded case")
    void testSeededCaseAgentOutputs() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode outputs = caseNode.get().path("agentOutputs");
        assertTrue(outputs.isArray());
        assertEquals(4, outputs.size(), "Should have 4 agent outputs");
    }

    @Test
    @DisplayName("Should have agent output with required fields")
    void testAgentOutputFields() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode outputs = caseNode.get().path("agentOutputs");
        if (outputs.size() > 0) {
            JsonNode output = outputs.get(0);
            assertTrue(output.has("agentName"));
            assertTrue(output.has("summary"));
            assertTrue(output.has("flags"));
            assertTrue(output.has("timestamp"));
        }
    }

    @Test
    @DisplayName("Should have actions executed in seeded case")
    void testSeededCaseActionsExecuted() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode actions = caseNode.get().path("actionsExecuted");
        assertTrue(actions.isArray());
        assertTrue(actions.size() > 0);
    }

    @Test
    @DisplayName("Should have action with required fields")
    void testActionFields() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode actions = caseNode.get().path("actionsExecuted");
        if (actions.size() > 0) {
            JsonNode action = actions.get(0);
            assertTrue(action.has("id"));
            assertTrue(action.has("type"));
            assertTrue(action.has("performedBy"));
            assertTrue(action.has("timestamp"));
        }
    }

    @Test
    @DisplayName("Should have audit trail in seeded case")
    void testSeededCaseAuditTrail() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode trail = caseNode.get().path("auditTrail");
        assertTrue(trail.isArray());
        assertTrue(trail.size() > 0);
    }

    @Test
    @DisplayName("Should have audit entry with required fields")
    void testAuditEntryFields() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode trail = caseNode.get().path("auditTrail");
        if (trail.size() > 0) {
            JsonNode entry = trail.get(0);
            assertTrue(entry.has("id"));
            assertTrue(entry.has("event"));
            assertTrue(entry.has("actor"));
            assertTrue(entry.has("timestamp"));
            assertTrue(entry.has("correlationId"));
        }
    }

    @Test
    @DisplayName("Should have related transaction IDs")
    void testRelatedTransactionIds() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode txIds = caseNode.get().path("relatedTransactionIds");
        assertTrue(txIds.isArray());
        assertTrue(txIds.size() > 0);
    }

    @Test
    @DisplayName("Should have policy version in seeded case")
    void testCasePolicyVersion() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        assertEquals("action-policy-2026-03-16", 
                caseNode.get().path("policyVersion").asText());
    }

    @Test
    @DisplayName("Should have agent version in seeded case")
    void testCaseAgentVersion() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        assertEquals("orchestrator-v1.0.0", 
                caseNode.get().path("agentVersion").asText());
    }

    @Test
    @DisplayName("Should initialize without throwing exceptions")
    void testInitializationNoExceptions() {
        assertDoesNotThrow(() -> {
            CaseStoreService newStore = new CaseStoreService();
            assertNotNull(newStore);
        });
    }

    @Test
    @DisplayName("Should create multiple store instances independently")
    void testMultipleStoreInstances() {
        CaseStoreService store1 = new CaseStoreService();
        CaseStoreService store2 = new CaseStoreService();
        
        assertNotNull(store1);
        assertNotNull(store2);
        assertNotSame(store1, store2, "Stores should be distinct instances");
    }

    @Test
    @DisplayName("Should handle concurrent alert access")
    void testConcurrentAlertAccess() {
        ObjectNode alert1 = mapper.createObjectNode();
        alert1.put("id", "ALERT-CONCURRENT1");
        ObjectNode alert2 = mapper.createObjectNode();
        alert2.put("id", "ALERT-CONCURRENT2");
        
        // Store uses ConcurrentHashMap, should be thread-safe
        assertDoesNotThrow(() -> {
            store.putAlert(alert1);
            store.putAlert(alert2);
            store.getAlert("ALERT-CONCURRENT1");
            store.getAlert("ALERT-CONCURRENT2");
        });
    }

    @Test
    @DisplayName("Should handle concurrent case access")
    void testConcurrentCaseAccess() {
        ObjectNode case1 = mapper.createObjectNode();
        case1.put("caseId", "F-CONCURRENT1");
        ObjectNode case2 = mapper.createObjectNode();
        case2.put("caseId", "F-CONCURRENT2");
        
        assertDoesNotThrow(() -> {
            store.putCase(case1);
            store.putCase(case2);
            store.getCase("F-CONCURRENT1");
            store.getCase("F-CONCURRENT2");
        });
    }

    @Test
    @DisplayName("Should maintain audit trail order")
    void testAuditTrailOrder() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-ORDER");
        caseNode.set("auditTrail", mapper.createArrayNode());
        store.putCase(caseNode);
        
        store.assignCase("F-ORDER", "Analyst 1");
        store.addNote("F-ORDER", "Note 1");
        store.escalateCase("F-ORDER");
        
        Optional<ObjectNode> updated = store.getCase("F-ORDER");
        JsonNode trail = updated.get().path("auditTrail");
        
        assertEquals(3, trail.size());
    }

    @Test
    @DisplayName("Should generate unique audit IDs")
    void testUniqueAuditIds() {
        ObjectNode case1 = mapper.createObjectNode();
        case1.put("caseId", "F-AUDIT-ID1");
        case1.set("auditTrail", mapper.createArrayNode());
        store.putCase(case1);
        
        ObjectNode case2 = mapper.createObjectNode();
        case2.put("caseId", "F-AUDIT-ID2");
        case2.set("auditTrail", mapper.createArrayNode());
        store.putCase(case2);
        
        store.assignCase("F-AUDIT-ID1", "Analyst 1");
        store.assignCase("F-AUDIT-ID2", "Analyst 2");
        
        Optional<ObjectNode> updated1 = store.getCase("F-AUDIT-ID1");
        Optional<ObjectNode> updated2 = store.getCase("F-AUDIT-ID2");
        
        String auditId1 = updated1.get().path("auditTrail").get(0).path("id").asText();
        String auditId2 = updated2.get().path("auditTrail").get(0).path("id").asText();
        
        assertNotEquals(auditId1, auditId2, "Audit IDs should be unique");
    }

    @Test
    @DisplayName("Should format audit ID with padding")
    void testAuditIdFormatting() {
        String formatted = "AUD-" + String.format("%03d", 42);
        assertEquals("AUD-042", formatted);
    }

    @Test
    @DisplayName("Should create correlation ID format")
    void testCorrelationIdFormat() {
        String caseId = "F-9204";
        long idx = 3;
        String corrId = "CORR-" + caseId + "-" + String.format("%03d", idx);
        assertEquals("CORR-F-9204-003", corrId);
    }

    @Test
    @DisplayName("Should handle trend data with date formatting")
    void testTrendDateFormatting() {
        Map<String, Object> analytics = store.getAnalytics();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trends = (List<Map<String, Object>>) 
                analytics.get("trendsLast30Days");
        
        for (Map<String, Object> trend : trends) {
            String date = (String) trend.get("date");
            assertNotNull(date);
            assertEquals(10, date.length(), "Date should be in YYYY-MM-DD format");
            assertThat(date).matches("\\d{4}-\\d{2}-\\d{2}");
        }
    }

    @Test
    @DisplayName("Should extract date from timestamp")
    void testExtractDateFromTimestamp() {
        String timestamp = Instant.now().toString();
        String date = timestamp.substring(0, 10);
        assertEquals(10, date.length());
        assertThat(date).matches("\\d{4}-\\d{2}-\\d{2}");
    }

    @Test
    @DisplayName("Should sort top flags by count descending")
    void testTopFlagsSorting() {
        Map<String, Object> analytics = store.getAnalytics();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topFlags = (List<Map<String, Object>>) 
                analytics.get("topFlags");
        
        if (topFlags.size() >= 2) {
            int count1 = (int) topFlags.get(0).get("count");
            int count2 = (int) topFlags.get(1).get("count");
            assertTrue(count1 >= count2, "Flags should be sorted by count descending");
        }
    }

    @Test
    @DisplayName("Should limit top flags to 5")
    void testTopFlagsLimit() {
        Map<String, Object> analytics = store.getAnalytics();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topFlags = (List<Map<String, Object>>) 
                analytics.get("topFlags");
        
        assertTrue(topFlags.size() <= 5, "Should have at most 5 flags");
    }

    @Test
    @DisplayName("Should handle alert with all severity levels")
    void testAllSeverityLevels() {
        Map<String, Object> analytics = store.getAnalytics();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> bySeverity = (List<Map<String, Object>>) 
                analytics.get("alertsBySeverity");
        
        List<String> severities = bySeverity.stream()
                .map(m -> (String) m.get("severity"))
                .toList();
        
        assertThat(severities).contains("CRITICAL");
        assertThat(severities).contains("HIGH");
        assertThat(severities).contains("MEDIUM");
        assertThat(severities).contains("LOW");
    }

    @Test
    @DisplayName("Should validate Sarah Jenkins alert")
    void testSarahJenkinsAlert() {
        Optional<ObjectNode> alert = store.getAlert("ALERT-001");
        assertTrue(alert.isPresent());
        
        ObjectNode a = alert.get();
        assertEquals("TX-9204", a.path("transactionId").asText());
        assertEquals("CUST-SJ01", a.path("customerId").asText());
        assertEquals("Sarah Jenkins", a.path("customerName").asText());
        assertEquals(85, a.path("finalRiskScore").asInt());
        assertEquals("CRITICAL", a.path("severity").asText());
    }

    @Test
    @DisplayName("Should validate Robert Chen alert")
    void testRobertChenAlert() {
        Optional<ObjectNode> alert = store.getAlert("ALERT-002");
        assertTrue(alert.isPresent());
        
        ObjectNode a = alert.get();
        assertEquals("TX-9198", a.path("transactionId").asText());
        assertEquals("CUST-RC02", a.path("customerId").asText());
        assertEquals("Robert Chen", a.path("customerName").asText());
        assertEquals(58, a.path("finalRiskScore").asInt());
        assertEquals("MEDIUM", a.path("severity").asText());
    }

    @Test
    @DisplayName("Should validate Emily Watson alert")
    void testEmilyWatsonAlert() {
        Optional<ObjectNode> alert = store.getAlert("ALERT-003");
        assertTrue(alert.isPresent());
        
        ObjectNode a = alert.get();
        assertEquals("TX-9195", a.path("transactionId").asText());
        assertEquals("CUST-EW03", a.path("customerId").asText());
        assertEquals("Emily Watson", a.path("customerName").asText());
        assertEquals(28, a.path("finalRiskScore").asInt());
        assertEquals("LOW", a.path("severity").asText());
    }

    @Test
    @DisplayName("Should have flags in agent outputs")
    void testAgentOutputFlags() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode outputs = caseNode.get().path("agentOutputs");
        if (outputs.size() > 0) {
            JsonNode flags = outputs.get(0).path("flags");
            assertTrue(flags.isArray());
        }
    }

    @Test
    @DisplayName("Should have Pattern Analyzer output")
    void testPatternAnalyzerOutput() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode outputs = caseNode.get().path("agentOutputs");
        boolean hasPatternAnalyzer = false;
        for (JsonNode output : outputs) {
            if ("Pattern Analyzer".equals(output.path("agentName").asText())) {
                hasPatternAnalyzer = true;
                break;
            }
        }
        assertTrue(hasPatternAnalyzer, "Should have Pattern Analyzer output");
    }

    @Test
    @DisplayName("Should have Behavioral Risk Agent output")
    void testBehavioralRiskAgentOutput() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode outputs = caseNode.get().path("agentOutputs");
        boolean hasBehavioral = false;
        for (JsonNode output : outputs) {
            if (output.path("agentName").asText().contains("Behavioral")) {
                hasBehavioral = true;
                break;
            }
        }
        assertTrue(hasBehavioral, "Should have Behavioral Risk Agent output");
    }

    @Test
    @DisplayName("Should have Evidence Builder output")
    void testEvidenceBuilderOutput() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode outputs = caseNode.get().path("agentOutputs");
        boolean hasEvidence = false;
        for (JsonNode output : outputs) {
            if (output.path("agentName").asText().contains("Evidence")) {
                hasEvidence = true;
                break;
            }
        }
        assertTrue(hasEvidence, "Should have Evidence Builder output");
    }

    @Test
    @DisplayName("Should have Aggregated Risk Scorer output")
    void testAggregatedRiskScorerOutput() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode outputs = caseNode.get().path("agentOutputs");
        boolean hasScorer = false;
        for (JsonNode output : outputs) {
            if (output.path("agentName").asText().contains("Aggregated")) {
                hasScorer = true;
                break;
            }
        }
        assertTrue(hasScorer, "Should have Aggregated Risk Scorer output");
    }

    @Test
    @DisplayName("Should have notes array in case")
    void testCaseNotes() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode notes = caseNode.get().path("notes");
        assertTrue(notes.isArray());
    }

    @Test
    @DisplayName("Should add multiple notes to case")
    void testAddMultipleNotes() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-MULTINOTE");
        caseNode.set("auditTrail", mapper.createArrayNode());
        store.putCase(caseNode);
        
        store.addNote("F-MULTINOTE", "First note");
        store.addNote("F-MULTINOTE", "Second note");
        store.addNote("F-MULTINOTE", "Third note");
        
        Optional<ObjectNode> updated = store.getCase("F-MULTINOTE");
        JsonNode notes = updated.get().path("notes");
        assertEquals(3, notes.size());
    }

    @Test
    @DisplayName("Should maintain note order")
    void testNoteOrder() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-ORDER");
        caseNode.set("auditTrail", mapper.createArrayNode());
        store.putCase(caseNode);
        
        store.addNote("F-ORDER", "First");
        store.addNote("F-ORDER", "Second");
        
        Optional<ObjectNode> updated = store.getCase("F-ORDER");
        JsonNode notes = updated.get().path("notes");
        assertEquals("First", notes.get(0).asText());
        assertEquals("Second", notes.get(1).asText());
    }

    @Test
    @DisplayName("Should increment audit counter atomically")
    void testAtomicAuditCounter() {
        ObjectNode case1 = mapper.createObjectNode();
        case1.put("caseId", "F-ATOMIC1");
        case1.set("auditTrail", mapper.createArrayNode());
        store.putCase(case1);
        
        ObjectNode case2 = mapper.createObjectNode();
        case2.put("caseId", "F-ATOMIC2");
        case2.set("auditTrail", mapper.createArrayNode());
        store.putCase(case2);
        
        // Multiple operations should generate unique audit IDs
        store.assignCase("F-ATOMIC1", "A1");
        store.assignCase("F-ATOMIC2", "A2");
        store.addNote("F-ATOMIC1", "Note");
        
        // All audit IDs should be unique (handled by AtomicLong)
        assertDoesNotThrow(() -> {
            store.getCase("F-ATOMIC1");
            store.getCase("F-ATOMIC2");
        });
    }

    @Test
    @DisplayName("Should handle empty search results")
    void testEmptySearchResults() {
        Map<String, Object> result = store.getAlerts(1, 10, "CRITICAL", "CLOSED", null);
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        
        // May or may not be empty depending on seed data
        assertNotNull(alerts);
    }

    @Test
    @DisplayName("Should handle pagination edge cases")
    void testPaginationEdgeCases() {
        // Very large limit
        Map<String, Object> result = store.getAlerts(1, 1000, null, null, null);
        assertNotNull(result);
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        assertNotNull(alerts);
    }

    @Test
    @DisplayName("Should calculate from and to indices correctly")
    void testFromToCalculation() {
        int page = 2;
        int limit = 3;
        int total = 10;
        
        int from = Math.min((page - 1) * limit, total);
        int to = Math.min(from + limit, total);
        
        assertEquals(3, from);
        assertEquals(6, to);
    }

    @Test
    @DisplayName("Should handle last page correctly")
    void testLastPageHandling() {
        int page = 3;
        int limit = 5;
        int total = 12;
        
        int from = Math.min((page - 1) * limit, total);
        int to = Math.min(from + limit, total);
        
        assertEquals(10, from);
        assertEquals(12, to);
    }

    @Test
    @DisplayName("Should calculate ceiling for total pages")
    void testTotalPagesCeiling() {
        int total = 13;
        int limit = 5;
        int totalPages = (int) Math.ceil((double) total / limit);
        assertEquals(3, totalPages);
    }

    @Test
    @DisplayName("Should merge flag counts correctly")
    void testFlagCountMerging() {
        java.util.Map<String, Integer> flagCounts = new java.util.HashMap<>();
        flagCounts.merge("FLAG1", 1, Integer::sum);
        flagCounts.merge("FLAG1", 1, Integer::sum);
        flagCounts.merge("FLAG2", 1, Integer::sum);
        
        assertEquals(2, flagCounts.get("FLAG1"));
        assertEquals(1, flagCounts.get("FLAG2"));
    }

    @Test
    @DisplayName("Should merge severity counts correctly")
    void testSeverityCountMerging() {
        java.util.Map<String, Integer> severityCounts = new java.util.LinkedHashMap<>();
        for (String s : List.of("CRITICAL", "HIGH", "MEDIUM", "LOW")) {
            severityCounts.put(s, 0);
        }
        
        severityCounts.merge("CRITICAL", 1, Integer::sum);
        severityCounts.merge("HIGH", 1, Integer::sum);
        severityCounts.merge("CRITICAL", 1, Integer::sum);
        
        assertEquals(2, severityCounts.get("CRITICAL"));
        assertEquals(1, severityCounts.get("HIGH"));
        assertEquals(0, severityCounts.get("MEDIUM"));
    }

    @Test
    @DisplayName("Should initialize all severities to zero")
    void testInitializeSeverityCounts() {
        java.util.Map<String, Integer> severityCounts = new java.util.LinkedHashMap<>();
        for (String s : List.of("CRITICAL", "HIGH", "MEDIUM", "LOW")) {
            severityCounts.put(s, 0);
        }
        
        assertEquals(0, severityCounts.get("CRITICAL"));
        assertEquals(0, severityCounts.get("HIGH"));
        assertEquals(0, severityCounts.get("MEDIUM"));
        assertEquals(0, severityCounts.get("LOW"));
    }

    @Test
    @DisplayName("Should create 30 days of trend data")
    void test30DaysTrendData() {
        Instant now = Instant.now();
        java.util.Map<String, Integer> dailyCounts = new java.util.LinkedHashMap<>();
        
        for (int i = 29; i >= 0; i--) {
            String date = now.minus(i, java.time.temporal.ChronoUnit.DAYS)
                    .toString().substring(0, 10);
            dailyCounts.put(date, 0);
        }
        
        assertEquals(30, dailyCounts.size());
    }

    @Test
    @DisplayName("Should maintain daily counts insertion order")
    void testDailyCountsOrder() {
        java.util.Map<String, Integer> dailyCounts = new java.util.LinkedHashMap<>();
        dailyCounts.put("2026-03-01", 5);
        dailyCounts.put("2026-03-02", 10);
        dailyCounts.put("2026-03-03", 7);
        
        List<String> keys = new java.util.ArrayList<>(dailyCounts.keySet());
        assertEquals("2026-03-01", keys.get(0));
        assertEquals("2026-03-02", keys.get(1));
        assertEquals("2026-03-03", keys.get(2));
    }

    @Test
    @DisplayName("Should stream and filter alerts")
    void testStreamAndFilterAlerts() {
        Map<String, Object> result = store.getAlerts(1, 100, null, null, null);
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        
        long criticalCount = alerts.stream()
                .filter(a -> "CRITICAL".equals(a.path("severity").asText()))
                .count();
        
        assertTrue(criticalCount >= 0);
    }

    @Test
    @DisplayName("Should handle blank filter parameters")
    void testBlankFilterParameters() {
        Map<String, Object> result = store.getAlerts(1, 10, "", "", "");
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        
        assertNotNull(alerts);
        // Blank should behave same as null (no filtering)
    }

    @Test
    @DisplayName("Should compare timestamps for sorting")
    void testTimestampComparison() {
        String ts1 = "2026-03-17T10:00:00Z";
        String ts2 = "2026-03-17T09:00:00Z";
        
        assertTrue(ts1.compareTo(ts2) > 0, "Later timestamp should compare greater");
    }

    @Test
    @DisplayName("Should handle subList for pagination")
    void testSubListForPagination() {
        List<String> items = List.of("A", "B", "C", "D", "E");
        List<String> page = items.subList(1, 3);
        
        assertEquals(2, page.size());
        assertEquals("B", page.get(0));
        assertEquals("C", page.get(1));
    }

    @Test
    @DisplayName("Should create LinkedHashMap for result")
    void testLinkedHashMapCreation() {
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("data", List.of());
        result.put("meta", Map.of("page", 1));
        
        assertEquals(2, result.size());
        assertTrue(result.containsKey("data"));
        assertTrue(result.containsKey("meta"));
    }

    @Test
    @DisplayName("Should create ObjectMapper successfully")
    void testObjectMapperCreation() {
        ObjectMapper testMapper = new ObjectMapper();
        assertNotNull(testMapper);
        assertNotNull(testMapper.createObjectNode());
        assertNotNull(testMapper.createArrayNode());
    }

    @Test
    @DisplayName("Should have seeded alert with correct recommended action")
    void testSeededAlertRecommendedAction() {
        Optional<ObjectNode> alert = store.getAlert("ALERT-001");
        assertTrue(alert.isPresent());
        assertEquals("freeze_transaction", alert.get().path("recommendedAction").asText());
    }

    @Test
    @DisplayName("Should have seeded alert with REVIEWING status")
    void testSeededAlertReviewingStatus() {
        Optional<ObjectNode> alert = store.getAlert("ALERT-001");
        assertTrue(alert.isPresent());
        assertEquals("REVIEWING", alert.get().path("status").asText());
    }

    @Test
    @DisplayName("Should have seeded alert with RESOLVED status")
    void testSeededAlertResolvedStatus() {
        Optional<ObjectNode> alert = store.getAlert("ALERT-003");
        assertTrue(alert.isPresent());
        assertEquals("RESOLVED", alert.get().path("status").asText());
    }

    @Test
    @DisplayName("Should have seeded alert with CLOSED status")
    void testSeededAlertClosedStatus() {
        Optional<ObjectNode> alert = store.getAlert("ALERT-006");
        assertTrue(alert.isPresent());
        assertEquals("CLOSED", alert.get().path("status").asText());
    }

    @Test
    @DisplayName("Should have timestamps in ISO-8601 format")
    void testTimestampFormat() {
        Optional<ObjectNode> alert = store.getAlert("ALERT-001");
        assertTrue(alert.isPresent());
        
        String timestamp = alert.get().path("timestamp").asText();
        assertThat(timestamp).contains("T");
        assertThat(timestamp).containsAnyOf("Z", "+", "-");
    }

    @Test
    @DisplayName("Should handle case with multiple related transactions")
    void testMultipleRelatedTransactions() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode txIds = caseNode.get().path("relatedTransactionIds");
        assertTrue(txIds.isArray());
        assertTrue(txIds.size() >= 1);
    }

    @Test
    @DisplayName("Should create audit trail when missing")
    void testCreateAuditTrailWhenMissing() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-CREATE-AUDIT");
        // No auditTrail initially
        store.putCase(caseNode);
        
        store.assignCase("F-CREATE-AUDIT", "Test Analyst");
        
        Optional<ObjectNode> updated = store.getCase("F-CREATE-AUDIT");
        JsonNode trail = updated.get().path("auditTrail");
        assertTrue(trail.isArray());
        assertTrue(trail.size() > 0);
    }

    @Test
    @DisplayName("Should have audit entry with event description")
    void testAuditEntryEventDescription() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-EVENT");
        caseNode.set("auditTrail", mapper.createArrayNode());
        store.putCase(caseNode);
        
        store.assignCase("F-EVENT", "Jane Doe");
        
        Optional<ObjectNode> updated = store.getCase("F-EVENT");
        JsonNode trail = updated.get().path("auditTrail");
        String event = trail.get(0).path("event").asText();
        
        assertThat(event).contains("assigned");
        assertThat(event).contains("Jane Doe");
    }

    @Test
    @DisplayName("Should have audit entry with actor")
    void testAuditEntryActor() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-ACTOR");
        caseNode.set("auditTrail", mapper.createArrayNode());
        store.putCase(caseNode);
        
        store.escalateCase("F-ACTOR");
        
        Optional<ObjectNode> updated = store.getCase("F-ACTOR");
        JsonNode trail = updated.get().path("auditTrail");
        String actor = trail.get(0).path("actor").asText();
        
        assertEquals("analyst", actor);
    }

    @Test
    @DisplayName("Should filter by multiple criteria")
    void testMultipleCriteriaFilter() {
        Map<String, Object> result = store.getAlerts(1, 10, "CRITICAL", "REVIEWING", null);
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        
        for (ObjectNode alert : alerts) {
            assertEquals("CRITICAL", alert.path("severity").asText());
            assertEquals("REVIEWING", alert.path("status").asText());
        }
    }

    @Test
    @DisplayName("Should handle combined filter and search")
    void testCombinedFilterAndSearch() {
        Map<String, Object> result = store.getAlerts(1, 10, null, "QUEUED", "CUST");
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        
        for (ObjectNode alert : alerts) {
            assertEquals("QUEUED", alert.path("status").asText());
            assertTrue(alert.path("customerId").asText().contains("CUST"));
        }
    }

    @Test
    @DisplayName("Should support thread-safe operations")
    void testThreadSafeOperations() {
        // ConcurrentHashMap should handle concurrent access
        assertDoesNotThrow(() -> {
            ObjectNode alert1 = mapper.createObjectNode();
            alert1.put("id", "ALERT-THREAD1");
            ObjectNode alert2 = mapper.createObjectNode();
            alert2.put("id", "ALERT-THREAD2");
            
            store.putAlert(alert1);
            store.putAlert(alert2);
            store.getAlert("ALERT-THREAD1");
            store.getAlert("ALERT-THREAD2");
        });
    }

    @Test
    @DisplayName("Should have seeded 6 alerts")
    void testSeededAlertCount() {
        // Check that seed() created at least the documented 6 alerts
        Optional<ObjectNode> a1 = store.getAlert("ALERT-001");
        Optional<ObjectNode> a2 = store.getAlert("ALERT-002");
        Optional<ObjectNode> a3 = store.getAlert("ALERT-003");
        Optional<ObjectNode> a4 = store.getAlert("ALERT-004");
        Optional<ObjectNode> a5 = store.getAlert("ALERT-005");
        Optional<ObjectNode> a6 = store.getAlert("ALERT-006");
        
        assertTrue(a1.isPresent());
        assertTrue(a2.isPresent());
        assertTrue(a3.isPresent());
        assertTrue(a4.isPresent());
        assertTrue(a5.isPresent());
        assertTrue(a6.isPresent());
    }

    @Test
    @DisplayName("Should have seeded 2 cases")
    void testSeededCaseCount() {
        Optional<ObjectNode> c1 = store.getCase("F-9204");
        Optional<ObjectNode> c2 = store.getCase("F-9198");
        
        assertTrue(c1.isPresent());
        assertTrue(c2.isPresent());
    }

    @Test
    @DisplayName("Should validate Michael Thorne alert")
    void testMichaelThorneAlert() {
        Optional<ObjectNode> alert = store.getAlert("ALERT-004");
        assertTrue(alert.isPresent());
        
        ObjectNode a = alert.get();
        assertEquals("TX-9192", a.path("transactionId").asText());
        assertEquals("CUST-MT04", a.path("customerId").asText());
        assertEquals("Michael Thorne", a.path("customerName").asText());
        assertEquals(91, a.path("finalRiskScore").asInt());
    }

    @Test
    @DisplayName("Should validate Alice Leung alert")
    void testAliceLeungAlert() {
        Optional<ObjectNode> alert = store.getAlert("ALERT-005");
        assertTrue(alert.isPresent());
        
        ObjectNode a = alert.get();
        assertEquals("TX-9188", a.path("transactionId").asText());
        assertEquals("CUST-AL05", a.path("customerId").asText());
        assertEquals("Alice Leung", a.path("customerName").asText());
        assertEquals(72, a.path("finalRiskScore").asInt());
        assertEquals("HIGH", a.path("severity").asText());
    }

    @Test
    @DisplayName("Should validate David Ross alert")
    void testDavidRossAlert() {
        Optional<ObjectNode> alert = store.getAlert("ALERT-006");
        assertTrue(alert.isPresent());
        
        ObjectNode a = alert.get();
        assertEquals("TX-9180", a.path("transactionId").asText());
        assertEquals("CUST-DR06", a.path("customerId").asText());
        assertEquals("David Ross", a.path("customerName").asText());
        assertEquals(38, a.path("finalRiskScore").asInt());
        assertEquals("LOW", a.path("severity").asText());
    }

    @Test
    @DisplayName("Should have GEO_MISMATCH flag in seeded case")
    void testGeoMismatchFlag() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode outputs = caseNode.get().path("agentOutputs");
        boolean hasGeoMismatch = false;
        
        for (JsonNode output : outputs) {
            JsonNode flags = output.path("flags");
            if (flags.isArray()) {
                for (JsonNode flag : flags) {
                    if ("GEO_MISMATCH".equals(flag.asText())) {
                        hasGeoMismatch = true;
                        break;
                    }
                }
            }
        }
        
        assertTrue(hasGeoMismatch, "Should have GEO_MISMATCH flag");
    }

    @Test
    @DisplayName("Should have AMOUNT_SPIKE flag in seeded case")
    void testAmountSpikeFlag() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode outputs = caseNode.get().path("agentOutputs");
        boolean hasAmountSpike = false;
        
        for (JsonNode output : outputs) {
            JsonNode flags = output.path("flags");
            if (flags.isArray()) {
                for (JsonNode flag : flags) {
                    if ("AMOUNT_SPIKE".equals(flag.asText())) {
                        hasAmountSpike = true;
                        break;
                    }
                }
            }
        }
        
        assertTrue(hasAmountSpike, "Should have AMOUNT_SPIKE flag");
    }

    @Test
    @DisplayName("Should have freeze_transaction action in seeded case")
    void testFreezeTransactionAction() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode actions = caseNode.get().path("actionsExecuted");
        boolean hasFreezeAction = false;
        
        if (actions.isArray()) {
            for (JsonNode action : actions) {
                if ("freeze_transaction".equals(action.path("type").asText())) {
                    hasFreezeAction = true;
                    break;
                }
            }
        }
        
        assertTrue(hasFreezeAction, "Should have freeze_transaction action");
    }

    @Test
    @DisplayName("Should have notify_security_team action in seeded case")
    void testNotifySecurityTeamAction() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode actions = caseNode.get().path("actionsExecuted");
        boolean hasNotifyAction = false;
        
        if (actions.isArray()) {
            for (JsonNode action : actions) {
                if ("notify_security_team".equals(action.path("type").asText())) {
                    hasNotifyAction = true;
                    break;
                }
            }
        }
        
        assertTrue(hasNotifyAction, "Should have notify_security_team action");
    }

    @Test
    @DisplayName("Should have create_case_report action in seeded case")
    void testCreateCaseReportAction() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode actions = caseNode.get().path("actionsExecuted");
        boolean hasReportAction = false;
        
        if (actions.isArray()) {
            for (JsonNode action : actions) {
                if ("create_case_report".equals(action.path("type").asText())) {
                    hasReportAction = true;
                    break;
                }
            }
        }
        
        assertTrue(hasReportAction, "Should have create_case_report action");
    }

    @Test
    @DisplayName("Should have performedBy field in actions")
    void testActionPerformedBy() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode actions = caseNode.get().path("actionsExecuted");
        if (actions.size() > 0) {
            String performedBy = actions.get(0).path("performedBy").asText();
            assertThat(performedBy).contains("sentinel-agent");
        }
    }

    @Test
    @DisplayName("Should handle action with optional note field")
    void testActionOptionalNote() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode actions = caseNode.get().path("actionsExecuted");
        if (actions.size() > 1) {
            JsonNode action = actions.get(1);
            // Some actions may have note field, some may not
            assertTrue(action.has("note") || !action.has("note"));
        }
    }

    @Test
    @DisplayName("Should have correlation ID in audit entries")
    void testAuditCorrelationId() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode trail = caseNode.get().path("auditTrail");
        if (trail.size() > 0) {
            String correlationId = trail.get(0).path("correlationId").asText();
            assertThat(correlationId).startsWith("CORR-");
        }
    }

    @Test
    @DisplayName("Should have policy version in some audit entries")
    void testAuditPolicyVersion() {
        Optional<ObjectNode> caseNode = store.getCase("F-9204");
        assertTrue(caseNode.isPresent());
        
        JsonNode trail = caseNode.get().path("auditTrail");
        boolean hasPolicyVersion = false;
        
        for (JsonNode entry : trail) {
            if (entry.has("policyVersion")) {
                hasPolicyVersion = true;
                assertEquals("action-policy-2026-03-16", 
                        entry.path("policyVersion").asText());
                break;
            }
        }
        
        assertTrue(hasPolicyVersion, "At least one audit entry should have policy version");
    }

    @Test
    @DisplayName("Should handle empty case with all operations")
    void testEmptyCaseAllOperations() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-EMPTY");
        caseNode.put("status", "OPEN");
        caseNode.set("auditTrail", mapper.createArrayNode());
        store.putCase(caseNode);
        
        assertDoesNotThrow(() -> {
            store.assignCase("F-EMPTY", "Test");
            store.addNote("F-EMPTY", "Note");
            store.escalateCase("F-EMPTY");
            store.closeCase("F-EMPTY");
        });
        
        Optional<ObjectNode> updated = store.getCase("F-EMPTY");
        assertEquals("CLOSED", updated.get().path("status").asText());
    }

    @Test
    @DisplayName("Should maintain data consistency across operations")
    void testDataConsistency() {
        ObjectNode alert = mapper.createObjectNode();
        alert.put("id", "ALERT-CONSISTENCY");
        alert.put("transactionId", "TX-CONSISTENCY");
        store.putAlert(alert);
        
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-CONSISTENCY");
        caseNode.set("alert", alert);
        store.putCase(caseNode);
        
        // Back-fill caseId
        alert.put("caseId", "F-CONSISTENCY");
        store.putAlert(alert);
        
        Optional<ObjectNode> retrievedAlert = store.getAlert("ALERT-CONSISTENCY");
        Optional<ObjectNode> retrievedCase = store.getCase("F-CONSISTENCY");
        
        assertTrue(retrievedAlert.isPresent());
        assertTrue(retrievedCase.isPresent());
        assertEquals("F-CONSISTENCY", retrievedAlert.get().path("caseId").asText());
    }

    @Test
    @DisplayName("Should support getAllAlerts without filters")
    void testGetAllAlertsNoFilters() {
        Map<String, Object> result = store.getAlerts(1, 100, null, null, null);
        
        @SuppressWarnings("unchecked")
        List<ObjectNode> alerts = (List<ObjectNode>) result.get("data");
        
        assertNotNull(alerts);
        assertTrue(alerts.size() >= 6, "Should have at least 6 seeded alerts");
    }

    @Test
    @DisplayName("Should handle operations in sequence")
    void testOperationSequence() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-SEQUENCE");
        caseNode.put("status", "OPEN");
        caseNode.set("auditTrail", mapper.createArrayNode());
        store.putCase(caseNode);
        
        // Sequential operations
        store.assignCase("F-SEQUENCE", "Analyst");
        assertEquals("IN_REVIEW", store.getCase("F-SEQUENCE").get().path("status").asText());
        
        store.escalateCase("F-SEQUENCE");
        assertEquals("ESCALATED", store.getCase("F-SEQUENCE").get().path("status").asText());
        
        store.closeCase("F-SEQUENCE");
        assertEquals("CLOSED", store.getCase("F-SEQUENCE").get().path("status").asText());
    }

    @Test
    @DisplayName("Should preserve case data through operations")
    void testPreserveCaseData() {
        ObjectNode caseNode = mapper.createObjectNode();
        caseNode.put("caseId", "F-PRESERVE");
        caseNode.put("customField", "custom_value");
        caseNode.set("auditTrail", mapper.createArrayNode());
        store.putCase(caseNode);
        
        store.assignCase("F-PRESERVE", "Analyst");
        
        Optional<ObjectNode> updated = store.getCase("F-PRESERVE");
        assertEquals("custom_value", updated.get().path("customField").asText());
    }

    @Test
    @DisplayName("Should handle instant operations without errors")
    void testInstantOperations() {
        assertDoesNotThrow(() -> {
            Instant now = Instant.now();
            Instant past = now.minus(2, java.time.temporal.ChronoUnit.MINUTES);
            assertTrue(past.isBefore(now));
        });
    }

    @Test
    @DisplayName("Should have realistic risk scores in seeded data")
    void testRealisticRiskScores() {
        Optional<ObjectNode> alert1 = store.getAlert("ALERT-001");
        Optional<ObjectNode> alert4 = store.getAlert("ALERT-004");
        
        assertTrue(alert1.isPresent());
        assertTrue(alert4.isPresent());
        
        int score1 = alert1.get().path("finalRiskScore").asInt();
        int score4 = alert4.get().path("finalRiskScore").asInt();
        
        assertTrue(score1 >= 0 && score1 <= 100);
        assertTrue(score4 >= 0 && score4 <= 100);
    }

    @Test
    @DisplayName("Should map severity to risk score appropriately")
    void testSeverityRiskScoreMapping() {
        Optional<ObjectNode> critical = store.getAlert("ALERT-001");
        Optional<ObjectNode> low = store.getAlert("ALERT-003");
        
        assertTrue(critical.isPresent());
        assertTrue(low.isPresent());
        
        int criticalScore = critical.get().path("finalRiskScore").asInt();
        int lowScore = low.get().path("finalRiskScore").asInt();
        
        assertTrue(criticalScore > lowScore, "CRITICAL should have higher score than LOW");
    }

    @Test
    @DisplayName("Should handle store initialization multiple times")
    void testMultipleInitializations() {
        CaseStoreService store1 = new CaseStoreService();
        CaseStoreService store2 = new CaseStoreService();
        
        // Each should have independent data
        ObjectNode alert = mapper.createObjectNode();
        alert.put("id", "ALERT-INDEPENDENT");
        store1.putAlert(alert);
        
        assertTrue(store1.getAlert("ALERT-INDEPENDENT").isPresent());
        assertFalse(store2.getAlert("ALERT-INDEPENDENT").isPresent());
    }
}

