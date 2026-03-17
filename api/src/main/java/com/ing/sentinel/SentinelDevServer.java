package com.ing.sentinel;

import com.google.adk.web.AdkWebServer;
import com.ing.sentinel.agents.SentinelOrchestrator;
import com.ing.sentinel.config.TracingConfig;

/**
 * Sentinel ADK Dev UI Server
 *
 * Starts the Google ADK interactive development UI on port 8090.
 * Exposes the full 5-agent SentinelOrchestrator pipeline so you can:
 *
 *   - Send raw transaction JSON and watch the pipeline execute step-by-step
 *   - Inspect each agent's tool calls, inputs, and outputs in real time
 *   - View distributed traces in the ADK trace viewer
 *   - Test individual agent prompts interactively
 *
 * Run with:
 *   cd api
 *   GEMINI_API_KEY=your_key mvn exec:java@dev-ui
 *
 * Then open: http://localhost:8090
 *
 * NOTE: This is a development tool only — not deployed to Cloud Run.
 *       The production REST API runs on port 8080 via SentinelApplication.
 */
public class SentinelDevServer {

    public static void main(String[] args) {
        // Run on 8090 so it doesn't clash with the main Spring Boot API (8080)
        System.setProperty("server.port", "8090");

        TracingConfig.init();

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║         Sentinel AI  —  ADK Dev UI                      ║");
        System.out.println("║  Starting on http://localhost:8090                       ║");
        System.out.println("║  Agent: SentinelOrchestrator (5-agent pipeline)          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        AdkWebServer.start(SentinelOrchestrator.ROOT_AGENT);
    }
}
