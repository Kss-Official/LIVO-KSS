# Multi-Stage Dockerfile tuned for Render 512MB RAM ceiling
# Stage 1: Build JAR using Maven
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime Container (Ultra-Lightweight JRE 21)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/livo-backend-1.0.0-SNAPSHOT.jar app.jar

# JVM Memory Tuning for Render 512MB RAM Limit
ENV JAVA_OPTS="-Xmx220m -Xms128m -Xss384k -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
