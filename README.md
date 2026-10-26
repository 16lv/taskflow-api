# TaskFlow API

A RESTful project and task workflow management backend built with **Java 21** and **Spring Boot 3.3**. Designed to model structured engineering sprints with deterministic task state transitions, stateless JWT authentication, role-based access control, and RFC 7807 `ProblemDetail` error contracts.

## Why It Was Built

Many sample task trackers treat status updates as unrestricted string mutations and return inconsistent error payloads when validation fails. TaskFlow API enforces a domain-level finite state machine (`BACKLOG` -> `IN_PROGRESS` -> `IN_REVIEW` -> `DONE` / `ARCHIVED`), pairs multi-parameter JPA filtering with pagination, and ships with full `MockMvc` integration coverage so the API can be run and verified out of the box.

## Main Features

- **Stateless JWT Authentication & RBAC:** BCrypt password hashing, HMAC-SHA256 signed JWT tokens (`/api/v1/auth/register`, `/api/v1/auth/login`), and role claims (`MEMBER`, `MAINTAINER`, `ADMIN`).
- **Enforced Workflow State Machine:** Domain validation inside `TaskStatus` prevents invalid jumps (such as moving a task directly from `BACKLOG` to `DONE` without entering `IN_PROGRESS`), returning HTTP `422 Unprocessable Entity` with structured diagnostic details.
- **Multi-Criteria Filtering & Pagination:** Query tasks across project ID, status, priority, assignee, and case-insensitive title substring (`/api/v1/tasks?projectId=1&status=IN_PROGRESS&priority=HIGH&q=migration&page=0&size=20`).
- **RFC 7807 Error Responses:** `@RestControllerAdvice` maps Bean Validation errors, entity lookups, duplicate project codes, and illegal workflow transitions to standardized `ProblemDetail` payloads.
- **OpenAPI 3 / Swagger UI:** Auto-generated interactive documentation at `/swagger-ui.html` and `/v3/api-docs`.

## Tech Stack

- **Language:** Java 21 (Records, Pattern Matching Switch Expressions)
- **Framework:** Spring Boot 3.3 (Spring Web, Spring Data JPA, Spring Security, Bean Validation)
- **Database:** PostgreSQL 16 in Docker/production; H2 (in PostgreSQL compatibility mode) for zero-dependency local testing
- **Testing:** JUnit 5, Spring Boot Test, MockMvc, AssertJ
- **Build & Tooling:** Maven 3.9, Docker, Docker Compose, GitHub Actions CI

## Architecture Overview

```
src/main/java/agency/shss7/taskflow
├── controller/   # REST controllers & RFC 7807 GlobalExceptionHandler
├── domain/       # JPA entities (UserAccount, Project, TaskItem) & state machine enums
├── dto/          # Immutable Java 21 record request/response payloads with Jakarta validation
├── repository/   # Spring Data JPA repositories & parameterized JPQL search queries
├── security/     # Stateless JWT filter, token service, and SecurityFilterChain config
└── service/      # Transactional domain services (AuthService, WorkflowService)
```

## Getting Started

### Prerequisites

- **JDK 21** (Eclipse Temurin recommended)
- **Maven 3.9+**
- Optional: **Docker & Docker Compose** for running alongside PostgreSQL 16

### Run Locally (In-Memory PostgreSQL-Compatible Database)

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

### Run with PostgreSQL via Docker Compose

```bash
docker compose up --build
```

### Run Automated Tests

```bash
mvn test
```

## Example API Usage

### 1. Register an Account

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "engineer@cardiff.ac.uk",
    "fullName": "Cardiff Engineer",
    "password": "SecurePassword123!",
    "role": "MAINTAINER"
  }'
```

### 2. Create a Project

```bash
curl -X POST http://localhost:8080/api/v1/projects \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "CORE-API",
    "name": "Core Platform API Migration",
    "description": "Migrate legacy endpoints to Spring Boot 3"
  }'
```

### 3. Create & Transition a Task

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": 1,
    "title": "Implement RFC 7807 ProblemDetail error responses",
    "priority": "HIGH",
    "dueDate": "2026-11-15"
  }'

curl -X PATCH http://localhost:8080/api/v1/tasks/1/status \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"status": "IN_PROGRESS"}'
```

## Known Limitations & Future Improvements

- Token revocation currently relies on short JWT TTLs; adding a Redis-backed refresh token rotation store is a natural next step.
- Project membership is currently workspace-wide for authenticated users; per-project ACL tables would allow finer-grained multi-tenant isolation.
