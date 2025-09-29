# Backend IDS - Spring Boot Application# Backend IDS - Spring Boot Application



This is a Spring Boot backend application for the IDS project, featuring PostgreSQL database integration and Kafka messaging, designed to run in containerized environments.This is a Spring Boot backend application for the IDS project, featuring PostgreSQL database integration and Kafka messaging, designed to run in containerized environments.



## Prerequisites## Prerequisites



- Java 21+- Java 21+

- Maven 3.6+- Maven 3.6+

- Docker and Docker Compose- Docker and Docker Compose



## Local Development (with local PostgreSQL and Kafka)## Local Development (with local PostgreSQL and Kafka)



```bash```bash

# Start all services locally# Start all services locally

docker-compose up -ddocker-compose up -d



# Access the application# Access the application

# Application: http://localhost:8080# Application: http://localhost:8080

# Health check: http://localhost:8080/actuator/health# Health check: http://localhost:8080/actuator/health



# Stop services# Stop services

docker-compose downdocker-compose down

``````



## Production with AWS (requires .env configuration)## Production with AWS (requires .env configuration)



1. **Configure environment variables**1. **Configure environment variables**

   ```bash   ```bash

   cp .env.example .env   cp .env.example .env

   # Edit .env with your AWS RDS and MSK endpoints   # Edit .env with your AWS RDS and MSK endpoints

   ```   ```



2. **Run with AWS services**2. **Run with AWS services**

   ```bash   ```bash

   docker-compose -f docker-compose.production.yml up -d   docker-compose -f docker-compose.production.yml up -d

      

   # Check logs   # Check logs

   docker-compose -f docker-compose.production.yml logs -f   docker-compose -f docker-compose.production.yml logs -f

      

   # Stop   # Stop

   docker-compose -f docker-compose.production.yml down   docker-compose -f docker-compose.production.yml down

   ```   ```