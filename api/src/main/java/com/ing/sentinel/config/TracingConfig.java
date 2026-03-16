package com.ing.sentinel.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

import java.util.logging.Logger;

/**
 * TracingConfig
 *
 * Initializes OpenTelemetry for Sentinel — called once at startup.
 * Registers a global TracerProvider with:
 *  - SimpleSpanProcessor → LoggingSpanExporter  (prints spans to console)
 *
 * This wires up the ADK's built-in OTel instrumentation so that:
 *  1. Every LLM call, tool call, and tool response appears as a span in terminal logs.
 *  2. The ADK Web UI Trace tab is populated (ADK reads from the global TracerProvider).
 *
 * Call TracingConfig.init() before starting InMemoryRunner or AdkWebServer.
 */
public class TracingConfig {

    private static final Logger logger = Logger.getLogger(TracingConfig.class.getName());
    private static volatile boolean initialized = false;

    /**
     * Initialize OpenTelemetry with a logging exporter.
     * Safe to call multiple times — subsequent calls are no-ops.
     */
    public static synchronized OpenTelemetry init() {
        if (initialized) {
            return GlobalOpenTelemetry.get();
        }

        logger.info("🔭 [Tracing] Initializing OpenTelemetry...");

        // Span exporter: prints every span to console (JUL logger)
        LoggingSpanExporter spanExporter = LoggingSpanExporter.create();

        // Tracer provider wired to the exporter
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                .build();

        // Build the SDK and register it as the global OTel instance
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .buildAndRegisterGlobal();

        initialized = true;
        logger.info("✅ [Tracing] OpenTelemetry initialized. Spans will appear in logs and ADK Trace tab.");

        // Shutdown hook to flush remaining spans cleanly
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("🔭 [Tracing] Flushing spans on shutdown...");
            tracerProvider.close();
        }));

        return openTelemetry;
    }
}
