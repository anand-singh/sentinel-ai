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
 * Unit tests for EvidenceBuilder agent.
 * Tests agent initialization, tool configuration, and basic functionality.
 * 
 * Note: These are primarily unit tests focusing on configuration and initialization.
 * Integration tests that require LLM API calls should be run separately.
 */
@DisplayName("EvidenceBuilder Agent Tests")
class EvidenceBuilderAgentTest {

    private BaseAgent agent;
    private InMemoryRunner runner;

    @BeforeEach
    void setUp() {
        // Initialize the agent before each test
        agent = EvidenceBuilderAgent.initAgent();
        assertNotNull(agent, "Agent should be initialized");
    }

    @Test
    @DisplayName("Should initialize agent successfully")
    void testAgentInitialization() {
        assertNotNull(agent, "Agent should not be null");
        assertInstanceOf(LlmAgent.class, agent, "Agent should be an LlmAgent instance");
        
        LlmAgent llmAgent = (LlmAgent) agent;
        assertThat(llmAgent.name()).isEqualTo("EvidenceBuilderAgent");
        assertThat(llmAgent.description()).contains("upstream agents");
        
        // Verify instruction is not null
        assertNotNull(llmAgent.instruction(), "Instruction should not be null");
    }

    @Test
    @DisplayName("Should have ROOT_AGENT initialized")
    void testRootAgentInitialization() {
        assertNotNull(EvidenceBuilderAgent.ROOT_AGENT, "ROOT_AGENT should be initialized");
        assertInstanceOf(LlmAgent.class, EvidenceBuilderAgent.ROOT_AGENT, 
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
        assertThat(outputKey.get()).isEqualTo("evidence_bundle");
    }

    @Test
    @DisplayName("Should have tools configured")
    void testToolsConfiguration() {
        LlmAgent llmAgent = (LlmAgent) agent;
        assertNotNull(llmAgent.tools(), "Tools should not be null");
        
        // Get the list of tools synchronously
        var toolsList = llmAgent.tools().blockingGet();
        assertNotNull(toolsList, "Tools list should not be null");
        assertThat(toolsList).hasSize(3);
    }

    @Test
    @DisplayName("Should have instruction content with key terms")
    void testInstructionContent() {
        LlmAgent llmAgent = (LlmAgent) agent;
        
        // Convert instruction to string for testing
        String instructionStr = llmAgent.instruction().toString();
        
        // Verify key instruction components
        assertThat(instructionStr).containsAnyOf("Evidence Builder", "evidence", "builder");
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
    @DisplayName("Should have description mentioning auditable explanations")
    void testAgentDescription() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String description = llmAgent.description();
        assertThat(description)
                .containsAnyOf("auditable", "explanations", "upstream agents");
    }

    @Test
    @DisplayName("Agent should be accessible via ROOT_AGENT for ADK UI")
    void testRootAgentAccessibility() {
        assertNotNull(EvidenceBuilderAgent.ROOT_AGENT, "ROOT_AGENT should be accessible");
        assertSame(EvidenceBuilderAgent.ROOT_AGENT.getClass(), agent.getClass(), 
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
        assertEquals("EvidenceBuilderAgent", llmAgent.name());
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
            BaseAgent newAgent = EvidenceBuilderAgent.initAgent();
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
        assertInstanceOf(LlmAgent.class, EvidenceBuilderAgent.ROOT_AGENT, 
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
        
        // EvidenceBuilderAgent should have 3 tools:
        // mergeFlags, composeSummary, buildEvidenceBundle
        assertEquals(3, tools.size(), "Should have exactly 3 tools configured");
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
        assertTrue(description.contains("evidence") || description.contains("explanation") || description.contains("upstream"), 
                "Description should mention evidence, explanation, or upstream");
    }

    @Test
    @DisplayName("Should initialize agent multiple times independently")
    void testMultipleInitializations() {
        BaseAgent agent1 = EvidenceBuilderAgent.initAgent();
        BaseAgent agent2 = EvidenceBuilderAgent.initAgent();
        
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
        assertInstanceOf(LlmAgent.class, EvidenceBuilderAgent.ROOT_AGENT);
        assertInstanceOf(LlmAgent.class, agent);
        
        // Both should be LlmAgent instances
        LlmAgent rootAgent = (LlmAgent) EvidenceBuilderAgent.ROOT_AGENT;
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
                "Pattern Analyzer", "Behavioral Risk", "AML", "upstream"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning evidence bundle")
    void testInstructionMentionsEvidenceBundle() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        // Should mention evidence bundle
        assertThat(instructionStr).containsAnyOf(
                "evidence_bundle", "evidence bundle", "bundle"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning flags merging")
    void testInstructionMentionsFlagsMerging() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        // Should mention flags merging
        assertThat(instructionStr).containsAnyOf(
                "merge_flags", "Merge flags", "combine", "deduplicate"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning summary composition")
    void testInstructionMentionsSummaryComposition() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "compose_summary", "summary", "human-readable"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning traceability")
    void testInstructionMentionsTraceability() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "traceability", "traceable", "source agent"
        );
    }

    @Test
    @DisplayName("Should have instruction emphasizing no scoring or actions")
    void testInstructionNoScoringOrActions() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        // Should emphasize that this agent does NOT score or take actions
        assertThat(instructionStr).containsAnyOf(
                "NOT score", "do NOT score", "NOT take actions", "aggregate, normalize, and explain"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning version info")
    void testInstructionMentionsVersionInfo() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "version", "audit", "compliance"
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

    @Test
    @DisplayName("Should have instruction with process steps")
    void testInstructionProcessSteps() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        // Should have a clear process with numbered steps
        assertThat(instructionStr).containsAnyOf(
                "Process", "steps", "1.", "2.", "3."
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning agent scores")
    void testInstructionMentionsAgentScores() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "agent_scores", "Per-agent scores", "transparency"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning explanation items")
    void testInstructionMentionsExplanationItems() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "explanation_items", "source agent", "details"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning combined flags")
    void testInstructionMentionsCombinedFlags() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "combined_flags", "Deduplicated", "severity"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning evidence summary")
    void testInstructionMentionsEvidenceSummary() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "evidence_summary", "Clear", "explanation"
        );
    }

    @Test
    @DisplayName("Should have instruction with input format")
    void testInstructionInputFormat() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "Input Format", "transaction_id", "customer_id"
        );
    }

    @Test
    @DisplayName("Should have instruction with output format")
    void testInstructionOutputFormat() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "Output Format", "JSON", "json"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning pattern agent")
    void testInstructionMentionsPatternAgent() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "pattern_agent", "Pattern Analyzer", "pattern"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning behavioral agent")
    void testInstructionMentionsBehavioralAgent() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "behavioral_agent", "Behavioral Risk", "behavioral"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning AML agent")
    void testInstructionMentionsAmlAgent() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "aml_agent", "AML", "Compliance"
        );
    }

    @Test
    @DisplayName("Should have instruction emphasizing no hallucination")
    void testInstructionNoHallucination() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "no hallucination", "factual", "use the tools"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning config version")
    void testInstructionMentionsConfigVersion() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "config_version", "evidence-policy"
        );
    }

    @Test
    @DisplayName("Should have tools with correct count for evidence building")
    void testEvidenceBuildingToolCount() {
        LlmAgent llmAgent = (LlmAgent) agent;
        var tools = llmAgent.tools().blockingGet();
        
        // Verify we have exactly 3 evidence building tools
        assertEquals(3, tools.size(), "Should have 3 evidence building tools: mergeFlags, composeSummary, buildEvidenceBundle");
    }


    @Test
    @DisplayName("Should have instruction mentioning flag priority")
    void testInstructionMentionsFlagPriority() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "priority", "critical flags", "PEP_MATCH_PENDING"
        );
    }

    @Test
    @DisplayName("Should have instruction mentioning graceful handling")
    void testInstructionMentionsGracefulHandling() {
        LlmAgent llmAgent = (LlmAgent) agent;
        String instructionStr = llmAgent.instruction().toString();
        
        assertThat(instructionStr).containsAnyOf(
                "gracefully", "Handle missing", "optional"
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
}

