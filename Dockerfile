# syntax=docker/dockerfile:1

# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only the POM first so dependency resolution is cached
# separately from source changes (speeds up rebuilds a lot).
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Now copy the source and build the jar
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Run as a non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# wget is used by the HEALTHCHECK below
RUN apk add --no-cache wget

COPY --from=build /app/target/*.jar app.jar

USER spring:spring

EXPOSE 8080

# Allow JVM/container tuning at runtime, e.g.
# JAVA_OPTS="-Xmx512m -Xms256m"
ENV JAVA_OPTS=""

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/livez || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
