# ==========================================
# Stage 1: Build Application with Maven
# ==========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy Maven descriptor and dependency definition for layer caching
COPY pom.xml .
COPY .mvn .mvn/
COPY mvnw .

RUN mvn dependency:go-offline -B || true

# Copy backend source code
COPY src ./src

# Build production executable JAR
ENV MAVEN_OPTS="-Xmx512m -XX:+UseSerialGC"
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Minimal Production JRE Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="XYZ Auto Service Team"
LABEL description="WorkshopOS Backend Production Container"

# Create unprivileged system user/group for security
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy artifact from build stage with correct ownership
COPY --from=builder --chown=spring:spring /build/target/*.jar app.jar

# Container awareness and memory management
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=postgres

USER spring:spring

EXPOSE 8080

HEALTHCHECK --interval=5s --timeout=3s --start-period=20s --retries=10 \
    CMD wget --quiet --tries=1 --spider http://127.0.0.1:8080/api/csrf || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
