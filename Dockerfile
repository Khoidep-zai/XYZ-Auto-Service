# ==========================================
# Stage 1: Build Application with Maven & Packaging
# ==========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy Maven settings and dependencies cache first for efficient Docker layer caching
COPY backend/pom.xml ./backend/
COPY backend/.mvn ./backend/.mvn/
COPY backend/mvnw ./backend/

WORKDIR /build/backend
RUN mvn dependency:go-offline -B || true

# Copy frontend assets and backend source code
WORKDIR /build
COPY frontend ./frontend/
COPY backend/src ./backend/src/

# Package executable JAR (including frontend packaging into static classpath)
WORKDIR /build/backend
ENV MAVEN_OPTS="-Xmx512m -XX:+UseSerialGC"
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Minimal Production JRE Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="XYZ Auto Service Team"
LABEL description="WorkshopOS XYZ Auto Service Production Container"

# Create unprivileged system user/group for security
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy artifact from build stage with correct ownership
COPY --from=builder --chown=spring:spring /build/backend/target/*.jar app.jar

# Configuration for container awareness and memory management
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=postgres

USER spring:spring

EXPOSE 8080

HEALTHCHECK --interval=5s --timeout=3s --start-period=20s --retries=10 \
    CMD wget --quiet --tries=1 --spider http://127.0.0.1:8080/api/csrf || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
