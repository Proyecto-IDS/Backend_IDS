# Multi-stage build for Spring Boot application
# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create app directory and user for security
RUN addgroup -S appuser && adduser -S appuser -G appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Environment variables for configuration
# Database Configuration (PostgreSQL on AWS RDS)
# TODO: Replace with your actual AWS RDS PostgreSQL endpoint
# Example: your-db-instance.xxxxxxxxxx.us-east-1.rds.amazonaws.com
ENV DB_HOST=your-postgres-host.rds.amazonaws.com
ENV DB_PORT=5432
ENV DB_NAME=your_database_name
ENV DB_USERNAME=your_username
ENV DB_PASSWORD=your_password

# Kafka Configuration (AWS MSK - Managed Streaming for Apache Kafka)
# TODO: Replace with your actual AWS MSK bootstrap servers
# Example: b-1.your-cluster.xxxxxx.c2.kafka.us-east-1.amazonaws.com:9092,b-2.your-cluster.xxxxxx.c2.kafka.us-east-1.amazonaws.com:9092
ENV KAFKA_BOOTSTRAP_SERVERS=your-kafka-cluster.kafka.us-east-1.amazonaws.com:9092

# Application Configuration
ENV SPRING_PROFILES_ACTIVE=production
ENV SERVER_PORT=8080

# JVM Options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Build instructions:
# docker build -t ids-backend:latest .
#
# Run instructions:
# docker run -d \
#   --name ids-backend \
#   -p 8080:8080 \
#   -e DB_HOST=your-actual-rds-endpoint.amazonaws.com \
#   -e DB_USERNAME=your_db_user \
#   -e DB_PASSWORD=your_db_password \
#   -e KAFKA_BOOTSTRAP_SERVERS=your-msk-endpoint.amazonaws.com:9092 \
#   ids-backend:latest
