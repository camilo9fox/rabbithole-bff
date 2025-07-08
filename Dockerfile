# ---------------------------------------------------------------------------
# Build stage: compile Spring Boot Gateway application
# ---------------------------------------------------------------------------
FROM maven:3.9-amazoncorretto-17 AS build
WORKDIR /app

# Copy pom and resolve dependencies first (for layer caching)
COPY pom.xml .
RUN mvn -q -B dependency:resolve dependency:resolve-plugins

# Copy source and build jar (skip tests for speed in CI/CD)
COPY src ./src
RUN mvn -q -B package -DskipTests

# ---------------------------------------------------------------------------
# Runtime stage
# ---------------------------------------------------------------------------
FROM amazoncorretto:17-alpine
LABEL maintainer="Rabbit Hole DevOps"
WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy built jar
ARG JAR_FILE=/app/target/bff-0.0.1-SNAPSHOT.jar
COPY --from=build ${JAR_FILE} app.jar

# JVM memory settings can be overridden via JVM_OPTS env var
ENV JVM_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s CMD curl -f http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar /app/app.jar --server.port=${SERVER_PORT:-8080}"]
