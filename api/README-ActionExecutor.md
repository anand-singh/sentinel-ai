# Action Executor Agent - Implementation Guide

## Overview

The **Action Executor** is Agent #5 in Sentinel's fraud detection pipeline. It's the final step that executes **only approved, policy-governed actions** based on final risk decisions.

This implementation follows Google ADK (Agent Development Kit) patterns and integrates seamlessly with the existing Sentinel agents.

---

## ✨ Features

### Policy-Driven Execution
- Maps `(severity, recommended_action)` pairs to specific action tools
- No free-form actions - only registered, approved tools
- Fully deterministic and auditable

### Available Action Tools

1. **Freeze Transaction** (`freeze_transaction`)
   - Blocks/freezes a transaction in the core banking system
   - Idempotent - safe to retry

2. **Notify Security Team** (`notify_security_team`)
   - Sends structured alerts to SOC/analyst queue
   - Includes correlation ID for end-to-end tracing

3. **Create Case Report** (`create_case_report`)
   - Creates detailed case artifacts for investigation
   - Links evidence, scores, and flags

4. **Request Step-Up Auth** (`request_step_up_auth`)
   - Triggers additional customer authentication (OTP, push, biometric)
   - Used when risk is elevated but not critical

5. **Escalate to Human** (`escalate_to_human`)
   - Assigns case to human analyst queue
   - Supports priority levels and custom queues

### Safety & Governance
- ✅ Explicit tool registration (no improvisation)
- ✅ Deterministic execution order
- ✅ Idempotency keys for safe retries
- ✅ Full audit trail with correlation IDs
- ✅ Policy version tracking
- ✅ PII protection in logs

---

## 📁 Project Structure

```
api/src/main/java/com/ing/sentinel/
├── agents/
│   └── ActionExecutor.java              # Main agent implementation
└── tools/action/
    ├── FreezeTransactionTool.java       # Tool: Freeze/block transaction
    ├── NotifySecurityTeamTool.java      # Tool: Alert security team
    ├── CreateCaseReportTool.java        # Tool: Create case artifact
    ├── RequestStepUpAuthTool.java       # Tool: Trigger auth challenge
    └── EscalateToHumanTool.java         # Tool: Human escalation
```

---

## 🚀 Running the Agent

### 1. Interactive Console Mode

```bash
cd /Users/fd52bj/Developer/GIT/sentinel-ai/api
mvn exec:java@action-executor
```

### 2. Example Input

When the agent starts, provide a risk decision from Agent #4:

```json
{
  "transaction_id": "tx_123",
  "customer_id": "cust_456",
  "final_risk_score": 87,
  "severity": "CRITICAL",
  "recommended_action": "BLOCK",
  "explanation": "Amount spike + new device + geo mismatch; AML moderate.",
  "correlation_id": "8c6c-f1b2"
}
```

### 3. Expected Output

The agent will execute the mapped actions and return:

```json
{
  "executed_actions": ["freeze_transaction", "notify_security_team", "create_case_report"],
  "status": "SUCCESS",
  "timestamp_utc": "2026-03-16T14:40:00Z",
  "audit_id": "audit_99900221",
  "policy_version": "action-policy-2026-03-16",
  "version": "executor-v1.0.0",
  "correlation_id": "8c6c-f1b2",
  "details": [
    {
      "action": "freeze_transaction",
      "status": "SUCCESS",
      "transaction_id": "tx_123",
      "idempotency_key": "tx_123-freeze-..."
    },
    {
      "action": "notify_security_team",
      "status": "SUCCESS",
      "alert_id": "alert_...",
      "severity": "CRITICAL"
    },
    {
      "action": "create_case_report",
      "status": "SUCCESS",
      "case_id": "case_...",
      "final_risk_score": 87
    }
  ]
}
```

---

## 🎯 Policy Mapping (Built-in)

The agent uses the following policy mappings (version: `action-policy-2026-03-16`):

| Severity | Recommended Action | Executed Tools |
|----------|-------------------|----------------|
| LOW | ALLOW | *(no actions)* |
| MED | REVIEW | `create_case_report` |
| HIGH | CHALLENGE | `request_step_up_auth`, `create_case_report` |
| CRITICAL | BLOCK | `freeze_transaction`, `notify_security_team`, `create_case_report` |
| *(fallback)* | *(any)* | `create_case_report` |

---

## 🔧 Integration with Sentinel Pipeline

### Pipeline Flow

```
Transaction Input
    ↓
Agent #1: TransactionPatternAnalyzer
    ↓ (pattern analysis)
Agent #2: BehavioralRiskDetector
    ↓ (customer behavior analysis)
Agent #3: EvidenceBuilder
    ↓ (evidence collection)
Agent #4: AggregatedRiskScorer
    ↓ (final risk decision)
Agent #5: ActionExecutor ← YOU ARE HERE
    ↓
Executed Actions (freeze, notify, report, etc.)
```

### Calling from Agent #4

```java
// Agent #4 produces a decision
Map<String, Object> decision = Map.of(
    "transaction_id", "tx_123",
    "final_risk_score", 87,
    "severity", "CRITICAL",
    "recommended_action", "BLOCK",
    "correlation_id", correlationId
);

// Send to Action Executor
String decisionJson = objectMapper.writeValueAsString(decision);
Content userMsg = Content.fromParts(Part.fromText(decisionJson));
Flowable<Event> events = runner.runAsync(USER_ID, session.id(), userMsg);
```

---

## 🧪 Testing Different Scenarios

### Scenario 1: Low Risk - Allow Transaction
```json
{
  "transaction_id": "tx_001",
  "customer_id": "cust_001",
  "final_risk_score": 15,
  "severity": "LOW",
  "recommended_action": "ALLOW",
  "explanation": "Normal transaction pattern",
  "correlation_id": "corr-001"
}
```
**Expected**: No actions executed

### Scenario 2: Medium Risk - Review
```json
{
  "transaction_id": "tx_002",
  "customer_id": "cust_002",
  "final_risk_score": 55,
  "severity": "MED",
  "recommended_action": "REVIEW",
  "explanation": "Slightly elevated amount",
  "correlation_id": "corr-002"
}
```
**Expected**: `create_case_report`

### Scenario 3: High Risk - Challenge
```json
{
  "transaction_id": "tx_003",
  "customer_id": "cust_003",
  "final_risk_score": 75,
  "severity": "HIGH",
  "recommended_action": "CHALLENGE",
  "explanation": "New device detected",
  "correlation_id": "corr-003"
}
```
**Expected**: `request_step_up_auth`, `create_case_report`

### Scenario 4: Critical Risk - Block
```json
{
  "transaction_id": "tx_004",
  "customer_id": "cust_004",
  "final_risk_score": 92,
  "severity": "CRITICAL",
  "recommended_action": "BLOCK",
  "explanation": "Multiple fraud signals",
  "correlation_id": "corr-004"
}
```
**Expected**: `freeze_transaction`, `notify_security_team`, `create_case_report`

---

## 📊 Observability & Audit

### Structured Logging
Every action execution logs:
- `correlation_id` - for end-to-end tracing
- `transaction_id` - transaction identifier
- `action` - tool name executed
- `status` - SUCCESS / FAILED
- `timestamp_utc` - execution time
- `policy_version` - policy version applied
- `version` - agent version

### Example Log Entry
```
2026-03-16T14:40:00Z INFO  ActionExecutor - 🚫 Freezing transaction: id=tx_123, reason=CRITICAL risk
2026-03-16T14:40:00Z INFO  FreezeTransactionTool - ✅ Transaction frozen successfully: tx_123
2026-03-16T14:40:00Z INFO  ActionExecutor - 📢 Notifying security team: tx=tx_123, severity=CRITICAL
2026-03-16T14:40:01Z INFO  NotifySecurityTeamTool - ✅ Security team notified, alert ID: alert_1234567890
```

---

## 🔐 Security Considerations

1. **No Dynamic Actions**: Only pre-registered tools can be executed
2. **Policy Versioning**: Every execution includes policy version for compliance
3. **Idempotency**: All tools support safe retries via idempotency keys
4. **PII Masking**: Sensitive data is masked in logs
5. **Audit Trail**: Full traceability with correlation IDs
6. **Least Privilege**: Tools should have minimal required permissions

---

## 🛠️ Development & Extension

### Adding a New Action Tool

1. **Create the tool class** in `tools/action/`:
```java
public class MyNewActionTool {
    @Schema(name = "my_new_action", description = "...")
    public static Map<String, Object> executeAction(
        @Schema(name = "param1", description = "...") String param1) {
        // Implementation
        return result;
    }
}
```

2. **Register in ActionExecutor.java**:
```java
FunctionTool myNewTool = FunctionTool.create(
    MyNewActionTool.class, "executeAction");
```

3. **Update policy mapping** in agent instructions

4. **Add tests** for the new tool

---

## 📝 Next Steps

### Production Readiness
- [ ] Integrate with actual core banking API (replace simulations)
- [ ] Connect to real alerting systems (PagerDuty, ServiceNow)
- [ ] Implement case store database
- [ ] Add authentication service integration
- [ ] Set up metrics & monitoring dashboards
- [ ] Implement asynchronous job orchestration for long-running actions
- [ ] Add rollback/compensating actions where supported

### Policy Enhancement
- [ ] Externalize policy to YAML config file
- [ ] Support per-product policies (cards, payments, wire)
- [ ] Add human-in-the-loop approvals for CRITICAL actions
- [ ] Implement SLA tracking per action

---

## 📚 Related Documentation

- [Agent #1 - Transaction Pattern Analyzer](../../doc/agents/1.TransactionPatternAnalyzer.md)
- [Agent #2 - Behavioral Risk Detector](../../doc/agents/2.BehavioralRiskAgent.md)
- [Agent #5 - Action Executor](../../doc/agents/5.ActionExecutor.md) ← Design Document

---

## 📜 License

See root [LICENSE](../LICENSE) file.

---

## 🤝 Contributing

This agent follows ADK best practices:
- Explicit tool registration
- Deterministic behavior
- Full auditability
- No improvisation or free-form actions

When contributing, ensure all new tools follow these principles.
