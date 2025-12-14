# ToDo Application

Task management web application enabling users to create, organize, and assign tasks to team members. Features full CRUD operations for task management with a user-friendly interface for tracking and organizing work assignments.

## Features

-  **Task Management**: Create, read, update, and delete tasks
-  **Assignee Management**: Assign tasks to team members
-  **Status Tracking**: Monitor task progress with activity feed
-  **Weekly Overview**: Track tasks by due date
-  **Export**: Export tasks to CSV format
-  **Validation**: Input validation for data integrity
-  **Notifications**: Toast notifications for user feedback

## Tech Stack

### Frontend
- **Framework**: Vue 3 with TypeScript
- **Build Tool**: Vite
- **Routing**: Vue Router
- **UI Components**: Agnostic UI, FontAwesome Icons

### Backend
- **Framework**: Spring Boot 3.3.5
- **Language**: Java 21
- **Database**: MariaDB
- **API Documentation**: Swagger UI / OpenAPI

### DevOps
- **Containerization**: Docker & Docker Compose
- **Development**: Maven Wrapper, Hot Reload


## Prerequisites

- **Java**: JDK 21 or later
- **Node.js**: Latest LTS version
- **Database**: MariaDB (or use Docker Compose)
- **Maven**: Included via Maven Wrapper
- **Docker** (optional): For containerized database

## Quick Start

### 1. Database Setup

#### Option A: Using Docker Compose (Recommended)
```bash
docker-compose up -d
```

#### Option B: Local MariaDB
Install and start MariaDB locally. Update `api/src/main/resources/application.properties` if needed.

### 2. Backend Setup

```bash
cd api

# Install dependencies and run
./mvnw spring-boot:run
```

Backend runs at: **http://localhost:8080/api/v1/**

- Swagger UI: http://localhost:8080/api/v1/swagger-ui
- API Docs: http://localhost:8080/api/v1/api-docs

### 3. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Run development server
npm run dev
```

Frontend runs at: **http://localhost:5173/**


## Development

### Backend Development

```bash
cd api

# Run with live reload
./mvnw spring-boot:run

# Run tests
./mvnw test

# Generate test coverage report
./mvnw jacoco:report
# Report available at: target/site/jacoco/index.html

# Build without tests
./mvnw clean install -DskipTests
```

### Frontend Development

```bash
cd frontend

# Development server with hot reload
npm run dev

# Type checking
npm run type-check

# Linting
npm run lint

# Format code
npm run format

# Build for production
npm run build
```

