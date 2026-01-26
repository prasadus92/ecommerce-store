# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and pom.xml first for better layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application
RUN ./mvnw package -DskipTests -B && \
    java -Djarmode=layertools -jar target/*.jar extract --destination extracted

# Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy layers from builder (for better caching)
COPY --from=builder /build/extracted/dependencies/ ./
COPY --from=builder /build/extracted/spring-boot-loader/ ./
COPY --from=builder /build/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/extracted/application/ ./

# Set ownership
RUN chown -R appuser:appgroup /app

USER appuser

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
