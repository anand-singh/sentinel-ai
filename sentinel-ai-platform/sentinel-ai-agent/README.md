# Sentinel AI Agent Module

This module contains all Google ADK (Agent Development Kit) related code for the Sentinel AI fraud detection system.

## Overview

The Sentinel AI Agent module implements a sophisticated multi-agent fraud detection pipeline using Google's Agent Development Kit. It consists of 6 specialized agents that work together to analyze transactions, detect fraud patterns, and recommend actions.

## Architecture

### Agents

1. **TransactionPatternAnalyzer** - Detects global fraud patterns across transactions
2. **BehavioralRiskDetector** - Analyzes customer-specific behavioral anomalies
3. **EvidenceBuilderAgent** - Builds explainable evidence bundles for audit trails
4. **AggregatedRiskScorer** - Calculates final risk scores and recommends actions
5. **ActionExecutor** - Executes policy-governed fraud prevention actions
6. **SentinelOrchestrator** - Coordinates the entire multi-agent pipeline

### Tools

Each agent has a set of specialized tools organized by function:
- `action/` - Tools for executing fraud prevention actions (freeze, notify, escalate)
- `aggregator/` - Tools for combining risk signals and scoring (NEW: RiskBooster, ScoreCalibrator, ScoreNormalizer, SeverityClassifier, WeightedScoreBlender)
- `behavioral/` - Tools for analyzing customer behavior patterns
- `evidence/` - Tools for building audit evidence and explanations
- `orchestrator/` - Tools for pipeline coordination
- `pattern/` - Tools for detecting transaction patterns

### Configuration

- **TracingConfig** - OpenTelemetry tracing configuration for ADK instrumentation

## Running Agents

### Development UI (Recommended)

Start the ADK interactive development UI to test agents:

```bash
cd sentinel-ai-agent
GEMINI_API_KEY=your_api_key mvn exec:java@dev-ui
```

Then open http://localhost:8090 to interact with the SentinelOrchestrator agent (6-agent pipeline).

### Individual Agents (CLI)

Run individual agents standalone for testing:

```bash
# Pattern Analyzer
GEMINI_API_KEY=your_key mvn exec:java@pattern-analyzer

# Behavioral Risk Detector
GEMINI_API_KEY=your_key mvn exec:java@behavioral-risk

# Evidence Builder
GEMINI_API_KEY=your_key mvn exec:java@evidence-builder

# Aggregated Risk Scorer
GEMINI_API_KEY=your_key mvn exec:java@aggregated-risk

# Action Executor
GEMINI_API_KEY=your_key mvn exec:java@action-executor

# Orchestrator
GEMINI_API_KEY=your_key mvn exec:java@orchestrator
```

## Building

```bash
# Build the agent module
mvn clean install

# Skip tests during build
mvn clean install -DskipTests
```

## Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ActionExecutorTest
```

## Package Structure

```
com.ing.sentinel.agent
├── agents/              # ADK LlmAgent implementations
├── tools/              # FunctionTool implementations
```

## Dependencies

- **Google ADK** (0.9.0) - Agent Development Kit framework
- **Google Gemini** - LLM for agent reasoning
- **Jackson** - JSON processing
- **OpenTelemetry** - Distributed tracing
- **JUnit 5** - Testing framework

## Integration

This module is consumed by the `sentinel-ai-api` module as a Maven dependency. The API module uses `OrchestratorService` to invoke the SentinelOrchestrator agent programmatically via the ADK `InMemoryRunner`.

## Environment Variables

- `GEMINI_API_KEY` - Required for all agent operations
- `server.port` - Port for ADK Web UI (default: 8090)

## Notes

- This module is pure Java with no Spring Boot dependencies
- All agents follow the Google ADK LlmAgent pattern
- Tracing is automatically wired through OpenTelemetry
- The dev UI runs on port 8090 to avoid conflicts with the API (8080)

