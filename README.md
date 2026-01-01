# Taskr - Task Management Service

A Spring Boot REST API for task management with CRUD operations, soft delete, pagination, and basic authentication.

## Features

- ✅ Task CRUD operations (Create, Read, Update, Delete)
- ✅ Soft delete functionality
- ✅ Pagination and sorting
- ✅ Input validation
- ✅ Global error handling
- ✅ Basic authentication
- ✅ PostgreSQL database with Flyway migrations
- ✅ DTO pattern with MapStruct
- ✅ Docker support

## Technologies

- **Java 17**
- **Spring Boot 4.0.1**
- **Spring Data JPA**
- **Spring Security**
- **PostgreSQL 16**
- **Flyway**
- **MapStruct 1.6.3**
- **Lombok**
- **Maven**
- **Docker & Docker Compose**

## Prerequisites

- Java 17
- Docker & Docker Compose
- Maven 3.x

## Getting Started

### 1. Start PostgreSQL Database

```bash
docker-compose up -d
```

This will start PostgreSQL on `localhost:5432` with:
- Database: `taskr_db`
- Username: `${DB_USERNAME}`
- Password: `${DB_PASSWORD}`

### 2. Build and Run the Application

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Authentication

The API uses Basic Authentication with default credentials:
- **Username**: `${APP_USERNAME}`
- **Password**: `${APP_PASSWORD}`

## API Endpoints

### Create a Task
```http
POST /api/tasks
Content-Type: application/json
Authorization: Basic ${APP_USERNAME}:${APP_PASSWORD}

{
  "title": "Complete project documentation",
  "description": "Write comprehensive README",
  "status": "TODO"
}
```

### Get All Tasks (with pagination)
```http
GET /api/tasks?page=0&size=10&sortBy=createdAt&direction=DESC
Authorization: Basic ${APP_USERNAME}:${APP_PASSWORD}
```

### Get Task by ID
```http
GET /api/tasks/{id}
Authorization: Basic ${APP_USERNAME}:${APP_PASSWORD}
```

### Update Task
```http
PUT /api/tasks/{id}
Content-Type: application/json
Authorization: Basic ${APP_USERNAME}:${APP_PASSWORD}

{
  "title": "Updated title",
  "description": "Updated description",
  "status": "IN_PROGRESS"
}
```

### Delete Task (Soft Delete)
```http
DELETE /api/tasks/{id}
Authorization: Basic ${APP_USERNAME}:${APP_PASSWORD}
```

## Task Status Values

- `TODO` - Task not started
- `IN_PROGRESS` - Task in progress
- `DONE` - Task completed

## Project Structure

```
src/main/java/com/gderuki/taskr/
├── config/              # Security configuration
├── controller/          # REST controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA entities
├── exception/           # Custom exceptions and global handler
├── mapper/              # MapStruct mappers
├── repository/          # JPA repositories
└── service/             # Business logic

src/main/resources/
├── db/migration/        # Flyway SQL migrations
└── application.properties
```

## Database Schema

**tasks** table:
- `id` (BIGSERIAL) - Primary key
- `title` (VARCHAR(100)) - Task title
- `description` (TEXT) - Task description
- `status` (VARCHAR(20)) - Task status (TODO, IN_PROGRESS, DONE)
- `created_at` (TIMESTAMP) - Creation timestamp
- `updated_at` (TIMESTAMP) - Last update timestamp
- `deleted_at` (TIMESTAMP) - Soft delete timestamp

## Docker Support

### Build Docker Image
```bash
docker build -t taskr:latest .
```

### Run with Docker Compose
You can add the Spring Boot service to `docker-compose.yml` to run everything together.

## Learning Points

This project demonstrates:
- **Layered Architecture**: Clear separation between Controller, Service, and Repository
- **DTO Pattern**: Never expose entities directly in REST API
- **Soft Delete**: Logical deletion with timestamp
- **Pagination**: Efficient handling of large datasets
- **MapStruct**: Compile-time DTO mapping
- **Flyway**: Version-controlled database migrations
- **Spring Security**: Basic authentication
- **Global Exception Handling**: Consistent error responses
- **Bean Validation**: Input validation with annotations
- **Docker**: Containerization for easy deployment

## Next Steps

Potential enhancements:
- JWT authentication
- Unit and integration tests
- API documentation with Swagger/OpenAPI
- Audit logging
- Search and filtering capabilities
- Task assignment to users
- CI/CD pipeline
