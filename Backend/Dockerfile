# Multi-Stage Dockerfile tuned for Render 512MB RAM ceiling
# Stage 1: Build executable JAR using Maven & Java 17
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Cache dependency layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compile and package production JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production Runtime Container (Lightweight JRE 17 Alpine)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root system user for security
RUN addgroup -S livo && adduser -S livo -G livo

# Copy JAR from build stage
COPY --from=build /app/target/livo-backend-1.0.0-SNAPSHOT.jar app.jar
RUN chown -R livo:livo /app

# Switch to non-root user
USER livo

# JVM Memory Tuning for Render 512MB RAM ceiling (strictly capped under 366MB total)
# -Xmx200m -Xms128m: Cap heap at 200MB max to guarantee >145MB buffer for Render
# -XX:MaxMetaspaceSize=110m: Cap class metadata
# -XX:ReservedCodeCacheSize=48m: Cap JIT compilation cache
# -Xss384k: Reduce thread stack footprint
# -XX:+UseSerialGC: Minimum memory & CPU overhead GC for single-core / low-RAM containers
# -Dspring.main.lazy-initialization=true: On-demand bean loading, dramatically reducing cold-start RAM
ENV JAVA_OPTS="-Xmx200m -Xms128m -Xss384k -XX:MaxMetaspaceSize=110m -XX:ReservedCodeCacheSize=48m -XX:+UseSerialGC -Dspring.main.lazy-initialization=true -XX:+ExitOnOutOfMemoryError -Djava.awt.headless=true"
ENV PORT=8080

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
