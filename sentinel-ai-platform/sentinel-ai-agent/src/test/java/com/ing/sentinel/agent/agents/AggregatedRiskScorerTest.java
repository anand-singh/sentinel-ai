package com.ing.sentinel.agent.agents;

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
 * Unit tests for AggregatedRiskScorer agent.
 * Tests agent initialization, tool configuration, and basic functionality.
 * 
 * Note: These are primarily unit tests focusing on configuration and initialization.
 * Integration tests that require LLM API calls should be run separately.
 */
@DisplayName("AggregatedRiskScorer Agent Tests")
class AggregatedRiskScorerTest {

    private BaseAgent agent;
    private InMemoryRunner runner;

    @BeforeEach
    void setUp() {
        // Initialize the agent before each test
        agent = AggregatedRiskScorer.initAgent();
        assertNotNull(agent, "Agent should be initialized");
    }

    @Test
    @DisplayName("Should initialize agent successfully")
    void testAgentInitialization() {
        assertNotNull(agent, "Agent should not be null");
        assertInstanceOf(LlmAgent.class, agent, "Agent should be an LlmAgent instance");
        
        LlmAgent llmAgent = (LlmAgent) agent;
        assertThat(llmAgent.name()).isEqualTo("AggregatedRiskScorer");
        assertThat(llmAgent.description()).contains("Aggregates risk scores");
        
        // Verify instruction is not null
        assertNotNull(llmAgent.instruction(), "Instruction should not be null");
    }

    @Test
    @DisplayName("Should have ROOT_AGENT initialized")
    void testRootAgentInitialization() {
        assertNotNull(AggregatedRiskScorer.ROOT_AGENT, "ROOT_AGENT should be initialized");
        assertInstanceOf(LlmAgent.class, AggregatedRiskScorer.ROOT_AGENT, 
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
        assertThat(outputKey.get()).isEqualTo("aggregated_risk_result");
    }

    @Test
    @DisplayName("Should have tools configured")
    void testToolsConfiguration() {
        LlmAgent llmAgent = (LlmAgent) agent;
        assertNotNull(llmAgent.tools(), "Tools should not be null");
        
        // Get the list of tools synchronously
        var toolsList = llmAgent.tools().blockingGet();
        assertNotNull(toolsList, "Tools list should not be null");
        assertThat(toolsList).hasSize(5);
    }

    @Test
    @DisplayName("Should have instruction content with key terms")
    void testInstructionContent() {
        LlmAgent llmAgent = (LlmAgent) agent;
        
        // Convert instruction to string for testing
        String instructionStr = llmAgent.instruction().toString();
        
        // Verify key instruction components
        assertThat(instructionStr).containsAnyOf("Aggregated Risk Scorer", "risk", "scorer", "aggregate");
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
    @DisplayName("Should have description mentioning aggregation")
    void testAgentDescription() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String description = llmAgent.description();
        assertThat(description)
                .containsAnyOf("Aggregates", "aggregates", "risk scores");
    }

    @Test
    @DisplayName("Agent should be accessible via ROOT_AGENT for ADK UI")
    void testRootAgentAccessibility() {
        assertNotNull(AggregatedRiskScorer.ROOT_AGENT, "ROOT_AGENT should be accessible");
        assertSame(AggregatedRiskScorer.ROOT_AGENT.getClass(), agent.getClass(), 
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
        assertEquals("AggregatedRiskScorer", llmAgent.name());
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
            BaseAgent newAgent = AggregatedRiskScorer.initAgent();
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
        assertInstanceOf(LlmAgent.class, AggregatedRiskScorer.ROOT_AGENT, 
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
        
        // AggregatedRiskScorer should have 5 tools:
        // normalizeScore, blendWeightedScores, applyRiskBoost, 
        // calibrateScore, classifySeverity
        assertEquals(5, tools.size(), "Should have exactly 5 tools configured");
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
        assertTrue(description.contains("risk") || description.contains("score") || description.contains("aggregate"), 
                "Description should mention risk, score, or aggregate");
    }

    @Test
    @DisplayName("Should initialize agent multiple times independently")
    void testMultipleInitializations() {
        BaseAgent agent1 = AggregatedRiskScorer.initAgent();
        BaseAgent agent2 = AggregatedRiskScorer.initAgent();
        
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
        assertInstanceOf(LlmAgent.class, AggregatedRiskScorer.ROOT_AGENT);
        assertInstanceOf(LlmAgent.class, agent);
        
        // Both should be LlmAgent instances
        LlmAgent rootAgent = (LlmAgent) AggregatedRiskScorer.ROOT_AGENT;
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
    @DisplayName("Should have instruction mentioning upstream agents")
    void testInstructionMentionsUpstreamAgents() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        // Should mention the upstream agents in the pipeline
        assertThat(instructionStr).containsAnyOf(
                "Pattern", "Behavioral", "Evidence", "upstream"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning severity levels")
    void testInstructionMentionsSeverityLevels() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        // Should mention severity levels
        assertThat(instructionStr).containsAnyOf(
                "LOW", "MED", "HIGH", "CRITICAL", "severity"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning actions")
    void testInstructionMentionsActions() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        // Should mention recommended actions
        assertThat(instructionStr).containsAnyOf(
                "ALLOW", "REVIEW", "CHALLENGE", "BLOCK", "action"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning score normalization")
    void testInstructionMentionsNormalization() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "normalize", "normalization", "Normalize"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning score blending")
    void testInstructionMentionsBlending() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "blend", "blending", "Blend", "weight"
        );
    }

    @Test
    @DisplayName("Should have instruction emphasizing no action execution")
    void testInstructionNoActionExecution() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        // Should emphasize that this agent does NOT execute actions
        assertThat(instructionStr).containsAnyOf(
                "DO NOT execute", "NOT execute", "only recommend", "Action Executor"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning policy version")
    void testInstructionMentionsPolicyVersion() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "policy_version", "policy", "version"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning deterministic logic")
    void testInstructionMentionsDeterministic() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "deterministic", "auditable", "transparent"
        );
    }
}

