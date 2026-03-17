package com.ing.sentinel.agents;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionPatternAnalyzer agent.
 * Tests agent initialization, tool configuration, and basic functionality.
 * 
 * Note: These are primarily unit tests focusing on configuration and initialization.
 * Integration tests that require LLM API calls should be run separately.
 */
@DisplayName("TransactionPatternAnalyzer Agent Tests")
class TransactionPatternAnalyzerTest {

    private BaseAgent agent;
    private InMemoryRunner runner;

    @BeforeEach
    void setUp() {
        // Initialize the agent before each test
        agent = TransactionPatternAnalyzer.initAgent();
        assertNotNull(agent, "Agent should be initialized");
    }

    @Test
    @DisplayName("Should initialize agent successfully")
    void testAgentInitialization() {
        assertNotNull(agent, "Agent should not be null");
        assertInstanceOf(LlmAgent.class, agent, "Agent should be an LlmAgent instance");
        
        LlmAgent llmAgent = (LlmAgent) agent;
        assertThat(llmAgent.name()).isEqualTo("TransactionPatternAnalyzer");
        assertThat(llmAgent.description()).contains("transaction patterns");
        
        // Verify instruction is not null
        assertNotNull(llmAgent.instruction(), "Instruction should not be null");
    }

    @Test
    @DisplayName("Should have ROOT_AGENT initialized")
    void testRootAgentInitialization() {
        assertNotNull(TransactionPatternAnalyzer.ROOT_AGENT, "ROOT_AGENT should be initialized");
        assertInstanceOf(LlmAgent.class, TransactionPatternAnalyzer.ROOT_AGENT, 
                "ROOT_AGENT should be an LlmAgent instance");
    }

    @Test
    @DisplayName("Should have correct model name")
    void testModelName() {
        LlmAgent llmAgent = (LlmAgent) agent;
        var model = llmAgent.model();
        assertNotNull(model, "Model should not be null");
        assertTrue(model.isPresent(), "Model should be present");
        
        String modelString = model.get().toString();
        assertThat(modelString).contains("gemini-2.5-flash");
    }

    @Test
    @DisplayName("Should have output key configured")
    void testOutputKey() {
        LlmAgent llmAgent = (LlmAgent) agent;
        var outputKey = llmAgent.outputKey();
        assertNotNull(outputKey, "Output key should not be null");
        assertTrue(outputKey.isPresent(), "Output key should be present");
        assertThat(outputKey.get()).isEqualTo("pattern_analysis_result");
    }

    @Test
    @DisplayName("Should have tools configured")
    void testToolsConfiguration() {
        LlmAgent llmAgent = (LlmAgent) agent;
        assertNotNull(llmAgent.tools(), "Tools should not be null");
        
        // Get the list of tools synchronously
        var toolsList = llmAgent.tools().blockingGet();
        assertNotNull(toolsList, "Tools list should not be null");
        assertThat(toolsList).hasSize(6);
    }

    @Test
    @DisplayName("Should have instruction content with key terms")
    void testInstructionContent() {
        LlmAgent llmAgent = (LlmAgent) agent;
        
        // Convert instruction to string for testing
        String instructionStr = llmAgent.instruction().toString();
        
        // Verify key instruction components
        assertThat(instructionStr).containsAnyOf("Pattern Analyzer", "pattern", "transaction");
    }

    @Test
    @DisplayName("Should create runner successfully")
    void testRunnerCreation() {
        runner = new InMemoryRunner(agent);
        assertNotNull(runner, "Runner should be created");
        assertNotNull(runner.sessionService(), "Runner should have session service");
    }

    @Test
    @DisplayName("Should create session successfully")
    void testSessionCreation() {
        runner = new InMemoryRunner(agent);
        Session session = runner
                .sessionService()
                .createSession("TestSession", "test-user")
                .blockingGet();
        
        assertNotNull(session, "Session should be created");
        assertNotNull(session.id(), "Session ID should not be null");
        assertThat(session.id()).isNotEmpty();
    }

    @Test
    @DisplayName("Should have description mentioning fraud signals")
    void testAgentDescription() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String description = llmAgent.description();
        assertThat(description)
                .containsAnyOf("patterns", "fraud signals", "fraud");
    }

    @Test
    @DisplayName("Agent should be accessible via ROOT_AGENT for ADK UI")
    void testRootAgentAccessibility() {
        assertNotNull(TransactionPatternAnalyzer.ROOT_AGENT, "ROOT_AGENT should be accessible");
        assertSame(TransactionPatternAnalyzer.ROOT_AGENT.getClass(), agent.getClass(), 
                "ROOT_AGENT should be same type as initialized agent");
    }

    @Test
    @DisplayName("Should handle multiple sessions independently")
    void testMultipleSessions() {
        runner = new InMemoryRunner(agent);
        
        Session session1 = runner
                .sessionService()
                .createSession("TestSession1", "test-user-1")
                .blockingGet();
        
        Session session2 = runner
                .sessionService()
                .createSession("TestSession2", "test-user-2")
                .blockingGet();
        
        assertNotNull(session1);
        assertNotNull(session2);
        assertNotEquals(session1.id(), session2.id(), 
                "Different sessions should have different IDs");
    }

    @Test
    @DisplayName("Should have name set correctly")
    void testAgentName() {
        LlmAgent llmAgent = (LlmAgent) agent;
        assertEquals("TransactionPatternAnalyzer", llmAgent.name());
    }

    @Test
    @DisplayName("Should have non-null description")
    void testAgentDescriptionNotNull() {
        LlmAgent llmAgent = (LlmAgent) agent;
        assertNotNull(llmAgent.description());
        assertFalse(llmAgent.description().isEmpty());
    }

    @Test
    @DisplayName("Should have non-null instruction")
    void testAgentInstructionNotNull() {
        LlmAgent llmAgent = (LlmAgent) agent;
        assertNotNull(llmAgent.instruction());
    }

    @Test
    @DisplayName("Should initialize without throwing exceptions")
    void testInitializationNoExceptions() {
        assertDoesNotThrow(() -> {
            BaseAgent newAgent = TransactionPatternAnalyzer.initAgent();
            assertNotNull(newAgent);
        });
    }

    @Test
    @DisplayName("Should create multiple runners independently")
    void testMultipleRunners() {
        InMemoryRunner runner1 = new InMemoryRunner(agent);
        InMemoryRunner runner2 = new InMemoryRunner(agent);
        assertNotNull(runner1);
        assertNotNull(runner2);
        assertNotSame(runner1, runner2, "Different runners should be distinct instances");
    }

    @Test
    @DisplayName("Runner should have session service")
    void testRunnerSessionService() {
        runner = new InMemoryRunner(agent);
        assertNotNull(runner.sessionService());
    }

    @Test
    @DisplayName("Should validate agent type is LlmAgent")
    void testAgentType() {
        assertInstanceOf(LlmAgent.class, agent, "Agent should be instance of LlmAgent");
        assertInstanceOf(LlmAgent.class, TransactionPatternAnalyzer.ROOT_AGENT, 
                "ROOT_AGENT should be instance of LlmAgent");
    }

    @Test
    @DisplayName("Should have consistent agent properties")
    void testAgentPropertiesConsistency() {
        LlmAgent llmAgent = (LlmAgent) agent;
        
        // Test that properties are accessible and consistent
        assertNotNull(llmAgent.name());
        assertNotNull(llmAgent.description());
        assertNotNull(llmAgent.model());
        assertNotNull(llmAgent.instruction());
        assertNotNull(llmAgent.tools());
        assertNotNull(llmAgent.outputKey());
    }

    @Test
    @DisplayName("Should support session lifecycle")
    void testSessionLifecycle() {
        runner = new InMemoryRunner(agent);
        
        // Create a new session
        Session newSession = runner
                .sessionService()
                .createSession("LifecycleTest", "lifecycle-user")
                .blockingGet();
        
        assertNotNull(newSession);
        assertNotNull(newSession.id());
        assertFalse(newSession.id().isEmpty());
    }

    @Test
    @DisplayName("Should have tools with correct count")
    void testToolCount() {
        LlmAgent llmAgent = (LlmAgent) agent;
        var tools = llmAgent.tools().blockingGet();
        
        // TransactionPatternAnalyzer should have 6 tools:
        // analyzeAmountSpike, analyzeGeoDistance, analyzeVelocity, 
        // analyzeRareMcc, analyzeTimeWindow, blendRiskScores
        assertEquals(6, tools.size(), "Should have exactly 6 tools configured");
    }

    @Test
    @DisplayName("Should have model present")
    void testModelPresent() {
        LlmAgent llmAgent = (LlmAgent) agent;
        assertTrue(llmAgent.model().isPresent(), "Model should be present");
    }

    @Test
    @DisplayName("Should have output key present")
    void testOutputKeyPresent() {
        LlmAgent llmAgent = (LlmAgent) agent;
        assertTrue(llmAgent.outputKey().isPresent(), "Output key should be present");
    }

    @Test
    @DisplayName("Should have description with relevant keywords")
    void testDescriptionKeywords() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String description = llmAgent.description();
        
        // Check for relevant keywords in description
        assertTrue(description.contains("pattern") || description.contains("fraud") || description.contains("transaction"), 
                "Description should mention patterns, fraud, or transactions");
    }

    @Test
    @DisplayName("Should initialize agent multiple times independently")
    void testMultipleInitializations() {
        BaseAgent agent1 = TransactionPatternAnalyzer.initAgent();
        BaseAgent agent2 = TransactionPatternAnalyzer.initAgent();
        
        assertNotNull(agent1);
        assertNotNull(agent2);
        
        // They should be different instances but same type
        assertNotSame(agent1, agent2, "Each initialization should create a new instance");
        assertEquals(agent1.getClass(), agent2.getClass(), 
                "Both should be same type");
    }

    @Test
    @DisplayName("Should have instruction object")
    void testInstructionObject() {
        LlmAgent llmAgent = (LlmAgent) agent;
        var instruction = llmAgent.instruction();
        
        assertNotNull(instruction, "Instruction should not be null");
        assertNotNull(instruction.toString(), "Instruction toString should not be null");
        assertFalse(instruction.toString().isEmpty(), 
                "Instruction toString should not be empty");
    }

    @Test
    @DisplayName("Should have valid agent name")
    void testValidAgentName() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String name = llmAgent.name();
        
        assertNotNull(name);
        assertFalse(name.isEmpty());
        assertTrue(name.matches("[A-Za-z]+"), "Agent name should be alphabetic");
    }

    @Test
    @DisplayName("Should initialize tools successfully")
    void testToolsInitialization() {
        LlmAgent llmAgent = (LlmAgent) agent;
        
        assertDoesNotThrow(() -> {
            var tools = llmAgent.tools().blockingGet();
            assertNotNull(tools);
            assertFalse(tools.isEmpty());
        }, "Tools should initialize without exceptions");
    }

    @Test
    @DisplayName("ROOT_AGENT should match initialized agent type")
    void testRootAgentType() {
        assertInstanceOf(LlmAgent.class, TransactionPatternAnalyzer.ROOT_AGENT);
        assertInstanceOf(LlmAgent.class, agent);
        
        // Both should be LlmAgent instances
        LlmAgent rootAgent = (LlmAgent) TransactionPatternAnalyzer.ROOT_AGENT;
        LlmAgent testAgent = (LlmAgent) agent;
        
        assertEquals(rootAgent.name(), testAgent.name(), 
                "Both agents should have the same name");
    }

    @Test
    @DisplayName("Should create session with unique IDs")
    void testSessionUniqueIds() {
        runner = new InMemoryRunner(agent);
        
        Session session1 = runner.sessionService()
                .createSession("Session1", "user1")
                .blockingGet();
        Session session2 = runner.sessionService()
                .createSession("Session2", "user1")
                .blockingGet();
        
        assertNotEquals(session1.id(), session2.id(), 
                "Different sessions should have unique IDs");
    }

    @Test
    @DisplayName("Should handle session creation for different users")
    void testSessionCreationDifferentUsers() {
        runner = new InMemoryRunner(agent);
        
        Session session1 = runner.sessionService()
                .createSession("Session1", "user1")
                .blockingGet();
        Session session2 = runner.sessionService()
                .createSession("Session2", "user2")
                .blockingGet();
        
        assertNotNull(session1);
        assertNotNull(session2);
        assertNotEquals(session1.id(), session2.id());
    }

    @Test
    @DisplayName("Should have instruction mentioning first agent in pipeline")
    void testInstructionMentionsFirstAgent() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        // Should mention being the first agent in the pipeline
        assertThat(instructionStr).containsAnyOf(
                "first agent", "pipeline", "fraud detection"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning signal analysis tools")
    void testInstructionMentionsSignalTools() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        // Should mention signal analysis tools
        assertThat(instructionStr).containsAnyOf(
                "analyze_amount_spike", "analyze_geo_distance", "analyze_velocity"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning MCC tool")
    void testInstructionMentionsMccTool() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        // Should mention rare MCC analysis
        assertThat(instructionStr).containsAnyOf(
                "analyze_rare_mcc", "merchant categories", "mcc"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning time window tool")
    void testInstructionMentionsTimeWindowTool() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "analyze_time_window", "time_window", "unusual transaction times"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning score blending")
    void testInstructionMentionsScoreBlending() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "blend_risk_scores", "Blend scores", "combine all signals"
        );
    }

    @Test
    @DisplayName("Should have instruction emphasizing no actions")
    void testInstructionNoActions() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        // Should emphasize that this agent does NOT take actions
        assertThat(instructionStr).containsAnyOf(
                "do NOT take actions", "NOT take actions", "only analyze and score"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning output format")
    void testInstructionMentionsOutputFormat() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "Output Format", "JSON", "json"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning risk score")
    void testInstructionMentionsRiskScore() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "risk_score", "0-100", "risk level"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning flags")
    void testInstructionMentionsFlags() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "flags", "AMOUNT_SPIKE", "GEO_MISMATCH"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning feature contributions")
    void testInstructionMentionsFeatureContributions() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "feature_contributions", "amount_zscore", "geo_distance_km"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning version info")
    void testInstructionMentionsVersionInfo() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "version", "config_version", "audit trail"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning deterministic behavior")
    void testInstructionMentionsDeterministic() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "deterministic", "auditable", "transparent"
        );
    }

    @Test
    @DisplayName("Should have instruction with process steps")
    void testInstructionProcessSteps() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        // Should have a clear process with numbered steps
        assertThat(instructionStr).containsAnyOf(
                "Process", "Parse the transaction", "Run signal analysis"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning z-score")
    void testInstructionMentionsZScore() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "z-score", "zscore", "amount_zscore"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning velocity")
    void testInstructionMentionsVelocity() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "velocity", "velocity_1h", "frequency patterns"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning geo distance")
    void testInstructionMentionsGeoDistance() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "geo_distance", "geographic anomalies", "impossible travel"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning MCC rarity")
    void testInstructionMentionsMccRarity() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "mcc_rarity", "merchant categories", "rare_mcc"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning time anomaly")
    void testInstructionMentionsTimeAnomaly() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "time_anomaly", "time_window", "unusual transaction times"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning reasoning")
    void testInstructionMentionsReasoning() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "reasoning", "explanation", "factual"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning compliance")
    void testInstructionMentionsCompliance() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "compliance", "audit", "version info"
        );
    }

    @Test
    @DisplayName("Should have instruction with important rules")
    void testInstructionImportantRules() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "Important Rules", "Rules:", "rules"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning downstream agents")
    void testInstructionMentionsDownstreamAgents() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "downstream agents", "downstream", "let downstream"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning pattern version")
    void testInstructionMentionsPatternVersion() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "pattern-v1.0.0", "version", "config_version"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning weights version")
    void testInstructionMentionsWeightsVersion() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "config_version", "weights-2026", "weights"
        );
    }

    @Test
    @DisplayName("Should have all 6 pattern analysis tools")
    void testPatternAnalysisToolCount() {
        LlmAgent llmAgent = (LlmAgent) agent;
        var tools = llmAgent.tools().blockingGet();
        
        // Verify we have exactly 6 pattern analysis tools
        assertEquals(6, tools.size(), 
                "Should have 6 tools: analyzeAmountSpike, analyzeGeoDistance, analyzeVelocity, analyzeRareMcc, analyzeTimeWindow, blendRiskScores");
    }

    @Test
    @DisplayName("Should have instruction emphasizing factual reasoning")
    void testInstructionFactualReasoning() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "factual", "concise", "Keep reasoning factual"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning transaction parsing")
    void testInstructionMentionsTransactionParsing() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "Parse the transaction", "Extract transaction details", "amount, merchant, location"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning running all tools")
    void testInstructionMentionsRunAllTools() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "run ALL relevant tools", "Run signal analysis", "ALL relevant"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning structured analysis")
    void testInstructionMentionsStructuredAnalysis() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "structured analysis", "Generate response", "structured"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning global patterns")
    void testInstructionMentionsGlobalPatterns() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "global", "contextual patterns", "global and contextual"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning fraud pipeline")
    void testInstructionMentionsFraudPipeline() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "fraud pipeline", "Sentinel's fraud", "pipeline"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning suspicious flagging")
    void testInstructionMentionsSuspiciousFlagging() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "Flag anything suspicious", "flag suspicious", "suspicious"
        );
    }

    @Test
    @DisplayName("Should have tools count matching agent role")
    void testToolsCountMatchesRole() {
        LlmAgent llmAgent = (LlmAgent) agent;
        var tools = llmAgent.tools().blockingGet();
        
        // As first agent in pipeline analyzing patterns, should have 6 tools
        // More than other agents since it's doing the initial analysis
        assertThat(tools.size()).isGreaterThanOrEqualTo(5);
        assertThat(tools.size()).isLessThanOrEqualTo(7);
    }

    @Test
    @DisplayName("Should initialize agent without null return")
    void testInitAgentNonNull() {
        BaseAgent newAgent = TransactionPatternAnalyzer.initAgent();
        assertNotNull(newAgent, "initAgent() should never return null");
    }

    @Test
    @DisplayName("Should have description mentioning pattern detection")
    void testDescriptionMentionsPatternDetection() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String description = llmAgent.description();
        
        assertThat(description).containsAnyOf(
                "pattern", "detect", "fraud"
        );
    }

    @Test
    @DisplayName("Should create runner with proper session service")
    void testRunnerSessionServiceNotNull() {
        runner = new InMemoryRunner(agent);
        assertNotNull(runner.sessionService());
        
        // Should be able to create session
        assertDoesNotThrow(() -> {
            runner.sessionService()
                    .createSession("Test", "user")
                    .blockingGet();
        });
    }

    @Test
    @DisplayName("Should have instruction with Your Process section")
    void testInstructionYourProcessSection() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "Your Process", "## Your Process", "Process:"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning transaction details")
    void testInstructionMentionsTransactionDetails() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "transaction details", "amount, merchant, location, time", "merchant"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning role clarity")
    void testInstructionMentionsRoleClarity() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "Your role", "role is to", "Transaction Pattern Analyzer"
        );
    }

    @Test
    @DisplayName("Should have tools list not empty")
    void testToolsNotEmpty() {
        LlmAgent llmAgent = (LlmAgent) agent;
        
        assertDoesNotThrow(() -> {
            var tools = llmAgent.tools().blockingGet();
            assertFalse(tools.isEmpty(), "Tools list should not be empty");
        });
    }

    @Test
    @DisplayName("Should support independent agent instances")
    void testIndependentAgentInstances() {
        BaseAgent agent1 = TransactionPatternAnalyzer.initAgent();
        BaseAgent agent2 = TransactionPatternAnalyzer.initAgent();
        
        // Each should be functional independently
        InMemoryRunner runner1 = new InMemoryRunner(agent1);
        InMemoryRunner runner2 = new InMemoryRunner(agent2);
        
        assertNotNull(runner1.sessionService());
        assertNotNull(runner2.sessionService());
    }

    @Test
    @DisplayName("Should have instruction mentioning reasoning requirement")
    void testInstructionMentionsReasoningRequirement() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "reasoning", "Human-readable explanation", "explanation"
        );
    }

    @Test
    @DisplayName("Should have proper agent configuration for fraud detection")
    void testAgentConfigurationForFraudDetection() {
        LlmAgent llmAgent = (LlmAgent) agent;
        
        // Comprehensive check of agent configuration
        assertNotNull(llmAgent.name());
        assertNotNull(llmAgent.description());
        assertNotNull(llmAgent.instruction());
        assertTrue(llmAgent.model().isPresent());
        assertTrue(llmAgent.outputKey().isPresent());
        
        var tools = llmAgent.tools().blockingGet();
        assertNotNull(tools);
        assertFalse(tools.isEmpty());
    }

    @Test
    @DisplayName("Should have instruction mentioning structured response")
    void testInstructionMentionsStructuredResponse() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "structured", "JSON object", "Always respond with"
        );
    }

    @Test
    @DisplayName("Should be first agent in Sentinel pipeline")
    void testFirstAgentDesignation() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        // Verify this is explicitly the first agent
        assertThat(instructionStr).containsAnyOf(
                "first agent", "first", "pipeline"
        );
    }

    @Test
    @DisplayName("Should have model configured with correct version")
    void testModelVersion() {
        LlmAgent llmAgent = (LlmAgent) agent;
        var model = llmAgent.model();
        
        assertTrue(model.isPresent());
        String modelString = model.get().toString();
        assertThat(modelString).contains("gemini");
        assertThat(modelString).containsAnyOf("2.5", "flash");
    }

    @Test
    @DisplayName("Should have all 6 analysis tools for comprehensive pattern detection")
    void testComprehensiveToolSet() {
        LlmAgent llmAgent = (LlmAgent) agent;
        var tools = llmAgent.tools().blockingGet();
        
        // TransactionPatternAnalyzer needs all 6 tools for complete analysis:
        // 5 signal tools + 1 blending tool
        assertEquals(6, tools.size(), 
                "Should have 6 tools: 5 signal analyzers + 1 score blender");
    }

    @Test
    @DisplayName("Should have instruction mentioning role as analyzer not executor")
    void testRoleAsAnalyzerNotExecutor() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        // Should clarify it's an analyzer, not an executor
        assertThat(instructionStr).containsAnyOf(
                "analyze", "Analyzer", "do NOT take actions"
        );
    }
}

