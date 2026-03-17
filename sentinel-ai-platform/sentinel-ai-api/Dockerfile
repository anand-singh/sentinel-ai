# Multi-stage build for Sentinel AI - Cloud Run optimized
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Production image
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Copy built JAR from builder
COPY --from=builder /app/target/sentinel-ai-api-*.jar app.jar

# Create non-root user
RUN addgroup -S sentinel && adduser -S sentinel -G sentinel
RUN chown -R sentinel:sentinel /app
USER sentinel

# Expose port (Cloud Run uses PORT env var, default 8080)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseContainerSupport"
ENV PORT=8080

# Run the orchestrator by default
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
