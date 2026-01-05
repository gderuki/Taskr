# Taskr – Task Management Service

## Purpose
Taskr is a Spring Boot REST API for managing tasks with CRUD operations, soft deletes, pagination, and JWT authentication.

## Technologies
- **Java 21**
- **Spring Boot 3.x** (Web, Data JPA, Security, Actuator)
- **PostgreSQL** + **Flyway** for database migrations
- **MinIO** for object storage (attachments)
- **Docker**, **Docker Compose**
- **Maven 3.x**

## Requirements
- Java 21 (JDK)
- Docker & Docker Compose
- Maven 3.x

## Quick Start (Docker Compose)
1. Copy `.env.example` to `.env` and set required variables (`DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, etc.)
2. Build and start containers:
   ```bash
   docker-compose up --build -d
   ```
   App will be available at **http://localhost:8080**

## Local Run (Maven)
1. Make sure PostgreSQL and MinIO are running (or use `STORAGE_PROVIDER=local` to store files locally)
2. Start the app:
   ```bash
   ./mvnw spring-boot:run
   ```
   Dev profile is used by default.

## API Documentation
Swagger UI is available at:
**[`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html)**

## Project Structure
- `src/main/java` – application source code (controllers, services, repositories, DTOs, etc.)
- `src/main/resources` – config files (`application-*.yml`, DB migrations under `db/migration`, etc.)
- `src/test` – unit/integration tests (JUnit, Testcontainers)
- `Dockerfile` – builds the app Docker image
- `docker-compose.yml` – defines app, PostgreSQL, and MinIO services
- `pom.xml` – Maven configuration (dependencies, plugins, JDK version)

## Authentication (JWT + Refresh Tokens)
Stateless JWT authentication. After login, the client receives an **access token** (JWT) and a **refresh token**. The refresh token is stored in the database and used to obtain a new access token when the previous one expires.

## Tests (JUnit + Testcontainers)
The project includes unit and integration tests. Integration tests use **Testcontainers** to run PostgreSQL in a temporary container. Run all tests with:
```bash
mvn test
```

