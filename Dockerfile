# Multi-stage build for Consistent Hash Load Balancer
FROM gradle:8.5.0-jdk21 AS builder

WORKDIR /app

# Copy only Gradle files first for better caching
COPY settings.gradle.kts ./
COPY gradle.properties ./
COPY app/build.gradle.kts ./app/
COPY gradle/ ./gradle/

# Download dependencies (this layer will be cached)
RUN gradle dependencies --no-daemon || true

# Now copy the rest of the source code
COPY . .

# Build the Java application
RUN ./gradlew :app:build -x test --no-daemon

# Runtime image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy built application JAR from builder
COPY --from=builder /app/app/build/libs/app.jar .

# Copy config files
COPY config.properties ./
COPY prometheus.yml ./

# Expose ports
EXPOSE 8080 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/stats > /dev/null 2>&1 || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
