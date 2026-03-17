# Module Migration Summary

## Overview

The Sentinel AI project has been restructured from a single-module Maven project into a multi-module Maven project with clear separation of concerns.

## New Structure

```
sentinel-ai/
├── pom.xml                          # Parent POM
├── sentinel-ai-agent/               # NEW: Google ADK agents module
│   ├── pom.xml
│   ├── README.md
│   └── src/
│       ├── main/java/com/ing/sentinel/agent/
│       │   ├── agents/              # 6 ADK agents
│       │   ├── tools/               # Agent tools
│       │   ├── config/              # TracingConfig
│       │   └── SentinelDevServer.java
│       └── test/java/com/ing/sentinel/agent/
│           └── agents/              # Agent tests
└── api/                             # UPDATED: REST API module
    ├── pom.xml
    └── src/
        ├── main/java/com/ing/sentinel/
        │   ├── api/                 # REST controllers
        │   ├── config/              # CorsConfig only
        │   ├── service/             # OrchestratorService
        │   ├── store/               # CaseStoreService
        │   └── SentinelApplication.java
        └── test/java/com/ing/sentinel/
            ├── service/             # Service tests
            └── store/               # Store tests
```

## What Was Moved

### From `api` to `sentinel-ai-agent`:

**Agents:**
- `ActionExecutor.java`
- `AggregatedRiskScorer.java`
- `BehavioralRiskDetector.java`
- `EvidenceBuilderAgent.java`
- `SentinelOrchestrator.java`
- `TransactionPatternAnalyzer.java`

**Tools (entire directory):**
- `action/` - Freeze, notify, report, step-up, escalate tools
- `aggregator/` - Risk scoring and signal combination tools
- `behavioral/` - Customer behavior analysis tools
- `evidence/` - Evidence building and audit tools
- `orchestrator/` - Pipeline coordination tools
- `pattern/` - Transaction pattern detection tools

**Configuration:**
- `TracingConfig.java` - OpenTelemetry setup

**Development Tools:**
- `SentinelDevServer.java` - ADK Web UI server

**Tests:**
- All agent test classes

### Package Renaming

All classes were updated from:
- `com.ing.sentinel.*` → `com.ing.sentinel.agent.*`

For example:
- `com.ing.sentinel.agents.SentinelOrchestrator` → `com.ing.sentinel.agent.agents.SentinelOrchestrator`
- `com.ing.sentinel.tools.action.*` → `com.ing.sentinel.agent.tools.action.*`
- `com.ing.sentinel.config.TracingConfig` → `com.ing.sentinel.agent.config.TracingConfig`

## Dependencies

### sentinel-ai-agent module:
- Google ADK (0.9.0)
- Google ADK Dev (0.9.0)
- Jackson (2.17.2)
- OpenTelemetry (1.43.0)
- JUnit 5, Mockito, AssertJ

### api module:
- **sentinel-ai-agent** (0.1.0) ← dependency on agent module
- Spring Boot Web (4.0.2)
- Spring Boot Test (4.0.2)
- Jackson (2.17.2)
- OpenTelemetry (1.43.0)
- JUnit 5, Mockito, AssertJ

## Building

From the root directory:

```bash
# Build all modules
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Build only agent module
mvn clean install -pl sentinel-ai-agent

# Build only API module (depends on agent)
mvn clean install -pl api
```

## Running

### ADK Development UI (port 8090)

```bash
cd sentinel-ai-agent
GEMINI_API_KEY=your_key mvn exec:java@dev-ui
```

### Spring Boot REST API (port 8080)

```bash
cd api
GEMINI_API_KEY=your_key mvn spring-boot:run
```

Or using the packaged jar:

```bash
GEMINI_API_KEY=your_key java -jar api/target/sentinel-ai-api-0.1.0.jar
```

## Benefits of This Structure

1. **Separation of Concerns**: ADK agents are isolated from Spring Boot REST API code
2. **Independent Development**: Agent module can be developed and tested independently
3. **Reusability**: Agent module can be used by other applications/services
4. **Clear Dependencies**: API module explicitly depends on agent module
5. **Better Build Management**: Parent POM manages common dependencies and versions
6. **Cleaner Dockerfiles**: Can build separate containers for agents vs API if needed

## Test Results

**Before Migration:**
- Total tests: 521 (all in api module)

**After Migration:**
- sentinel-ai-agent: 248 tests ✅
- api: 273 tests ✅
- **Total: 521 tests** ✅ (all passing)

## Breaking Changes

Any external code importing Sentinel classes must update imports:
- `com.ing.sentinel.agents.*` → `com.ing.sentinel.agent.agents.*`
- `com.ing.sentinel.tools.*` → `com.ing.sentinel.agent.tools.*`
- `com.ing.sentinel.config.TracingConfig` → `com.ing.sentinel.agent.config.TracingConfig`

## Next Steps

1. Update CI/CD pipelines to build from root pom.xml
2. Update Dockerfiles if needed to reference new module structure
3. Update documentation references to new package names
4. Consider splitting Docker images for agent-only vs API-only deployments

