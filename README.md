# Taskr - Task Management Service

A Spring Boot REST API for task management with CRUD operations, soft delete, pagination, and JWT authentication.

## Features

- ✅ Task CRUD operations (Create, Read, Update, Delete)
- ✅ Soft delete functionality
- ✅ Pagination and sorting
- ✅ Input validation
- ✅ Global error handling
- ✅ JWT authentication with refresh tokens
- ✅ PostgreSQL database with Flyway migrations
- ✅ DTO pattern with MapStruct
- ✅ Docker support

## Technologies

- **Java 21**
- **Spring Boot 4.0.1**
- **Spring Data JPA**
- **Spring Security**
- **JWT (jjwt 0.12.6)**
- **PostgreSQL 16**
- **Flyway**
- **MapStruct 1.6.3**
- **Lombok**
- **Maven**
- **Docker & Docker Compose**

## Prerequisites

- Java 21
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

The API uses JWT authentication. First, obtain an access token by logging in.

## API Endpoints

### Authentication

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password"
}
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

#### Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### Logout
```http
POST /api/auth/logout
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Task Management

All task endpoints require authentication with Bearer token:
```
Authorization: Bearer <accessToken>
```

#### Create a Task
```http
POST /api/tasks
Content-Type: application/json
Authorization: Bearer <accessToken>

{
  "title": "Complete project documentation",
  "description": "Write comprehensive README",
  "status": "TODO"
}
```

#### Get All Tasks (with pagination)
```http
GET /api/tasks?page=0&size=10&sortBy=createdAt&direction=DESC
Authorization: Bearer <accessToken>
```

#### Get Task by ID
```http
GET /api/tasks/{id}
Authorization: Bearer <accessToken>
```

#### Update Task
```http
PUT /api/tasks/{id}
Content-Type: application/json
Authorization: Bearer <accessToken>

{
  "title": "Updated title",
  "description": "Updated description",
  "status": "IN_PROGRESS"
}
```

#### Delete Task (Soft Delete)
```http
DELETE /api/tasks/{id}
Authorization: Bearer <accessToken>
```

## Task Status Values

- `TODO` - Task not started
- `IN_PROGRESS` - Task in progress
- `DONE` - Task completed

## Authentication Details

- **Access Token**: JWT token valid for 15 minutes
- **Refresh Token**: UUID token valid for 24 hours, stored in database
- **Token Type**: Bearer
- **Security**: Stateless JWT with BCrypt password hashing

## Project Structure

```
src/main/java/com/gderuki/taskr/
├── config/              # Security and application configuration
├── controller/          # REST controllers (Auth, Task)
├── dto/                 # Data Transfer Objects
├── entity/              # JPA entities (Task, User, RefreshToken)
├── exception/           # Custom exceptions and global handler
├── mapper/              # MapStruct mappers
├── repository/          # JPA repositories
├── security/            # JWT utilities and filters
└── service/             # Business logic

src/main/resources/
├── db/migration/        # Flyway SQL migrations
└── application.yml
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

**users** table:
- `id` (BIGSERIAL) - Primary key
- `username` (VARCHAR(100)) - Unique username
- `email` (VARCHAR(255)) - Unique email
- `password` (VARCHAR(255)) - BCrypt hashed password
- `enabled` (BOOLEAN) - Account status
- `created_at` (TIMESTAMP) - Creation timestamp
- `updated_at` (TIMESTAMP) - Last update timestamp

**refresh_tokens** table:
- `id` (BIGSERIAL) - Primary key
- `token` (VARCHAR(255)) - Unique refresh token UUID
- `user_id` (BIGINT) - Foreign key to users table
- `expiry_date` (TIMESTAMP) - Token expiration date
- `created_at` (TIMESTAMP) - Creation timestamp
