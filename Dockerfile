FROM maven:3.9.4-openjdk-17-slim AS builder

# Set working directory
WORKDIR /app

# Copy Maven configuration files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:17-jdk-slim

# Install necessary packages
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Create application directory
WORKDIR /app

# Create non-root user for security
RUN groupadd -r gatekeeper && useradd -r -g gatekeeper gatekeeper

# Copy JAR file from builder stage
COPY --from=builder /app/target/gatekeeper-abac-*.jar app.jar

# Change ownership to non-root user
RUN chown -R gatekeeper:gatekeeper /app

# Switch to non-root user
USER gatekeeper

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Environment variables with defaults
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xmx1024m -Xms512m" \
    SERVER_PORT=8080

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
