# Backend IDS - Spring Boot Application

This is a Spring Boot backend application for the IDS project, featuring PostgreSQL database integration, WebSocket real-time communication, and AI integration with Google Gemini, designed to run in containerized environments.

## Prerequisites

- Java 21+
- Maven 3.6+
- Docker and Docker Compose

## Local Development (with local PostgreSQL)

1. **Setup environment variables**
   ```bash
   # Copy the local development template
   cp .env.local.example .env.local
   
   # Edit .env.local with your API keys
   # Especially: GEMINI_API_KEY and GOOGLE_CLIENT_ID
   ```

2. **Start services**
   ```bash
   # Start all services locally
   docker-compose up -d
   
   # Access the application
   # Application: http://localhost:8080
   # Health check: http://localhost:8080/actuator/health
   
   # Stop services
   docker-compose down
   ```

## Database Management

### Clear all alerts and meetings data

To reset the database and remove all alerts and meetings (useful for testing):

```powershell
# Execute the cleanup script
Get-Content clean_database.sql | docker exec -i ids-postgres psql -U postgres -d ids_database
```

This will:
- Delete all meeting participants
- Delete all meetings
- Delete all alerts
- Reset all ID sequences to start from 1

The script is located at `clean_database.sql` in the project root.

## Production with AWS (requires .env configuration)

1. **Configure environment variables**
   ```bash
   # Solo necesitas configurar estas 2 variables críticas:
   # - GEMINI_API_KEY (tu API key de Gemini)  
   # - DB_PASSWORD (password de tu base de datos)
   # 
   # + Variables de database cuando despliegues:
   # - DB_HOST, DB_USERNAME, DB_NAME
   #
   # Ver DEPLOYMENT.md para más detalles
   ```

2. **Run with AWS services**
   ```bash
   docker-compose -f docker-compose.production.yml up -d
   
   # Check logs
   docker-compose -f docker-compose.production.yml logs -f
   
   # Stop
   docker-compose -f docker-compose.production.yml down
   ```