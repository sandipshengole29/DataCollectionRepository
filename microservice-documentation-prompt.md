# GitHub Copilot Prompt — Microservice Documentation Generator
# Version: 1.0 | Author: Technical Lead
# Purpose: Generate complete technical + non-technical documentation for each microservice

---

## 🎯 OBJECTIVE

You are a **senior technical writer and architect assistant**. Your task is to generate **comprehensive, production-grade documentation** for a Spring Boot microservice.

The documentation must be written at **two levels**:
1. **Non-Technical Layer** — plain English, understandable by business stakeholders, product managers, and new team members with no coding background.
2. **Technical Layer** — in-depth details for developers, architects, DevOps, and QA engineers.

---

## 📁 WORKSPACE CONTEXT

Before generating documentation, analyze the following files for the target microservice:

```
@workspace

Service source:          #file:src/main/java/**/*.java
Configuration:           #file:src/main/resources/application.yml
                         #file:src/main/resources/application-*.yml
Build file:              #file:pom.xml
Database migrations:     #file:src/main/resources/db/migration/*.sql
Docker config:           #file:Dockerfile
                         #file:docker-compose.yml
OpenAPI/Swagger spec:    #file:src/main/resources/openapi.yml  (if present)
Tests:                   #file:src/test/java/**/*.java
Kubernetes/Infra:        #file:k8s/*.yml  (if present)
```

> 📌 **Instruction:** Read all available files above before generating any section below. If a file is missing, note it as "Not found / Not applicable" for that section and continue.

---

## 📋 DOCUMENTATION TEMPLATE

Generate the full documentation using the structure below. Use clear headings, tables, and bullet points. Keep non-technical sections jargon-free and add a "💡 Plain English" callout box wherever applicable.

---

### SECTION 1 — Service Identity Card

| Field                  | Value |
|------------------------|-------|
| **Service Name**       | _(Extract from spring.application.name in application.yml)_ |
| **Module / Domain**    | _(e.g., Customer Management, Authentication, Notifications)_ |
| **Version**            | _(Extract from pom.xml `<version>`)_ |
| **Spring Boot Version**| _(Extract from pom.xml parent)_ |
| **Java Version**       | _(Extract from pom.xml `<java.version>`)_ |
| **Owner / Team**       | _(Fill in manually)_ |
| **Status**             | `Active` / `Deprecated` / `In Development` |
| **Repository Path**    | _(Fill in manually)_ |
| **Last Updated**       | _(Fill in manually)_ |

---

### SECTION 2 — Purpose & Business Value

#### 2.1 What Does This Service Do? _(Non-Technical)_

> 💡 **Plain English:**
> Write 3–5 sentences explaining this service **as if explaining to a non-developer**. Focus on:
> - What real-world problem it solves
> - Who benefits from it (end users, other teams, business)
> - What happens if this service goes down (business impact)

#### 2.2 Business Capabilities

List the key business functions this service enables:

- _(e.g., "Allows customers to register and manage their profiles")_
- _(e.g., "Sends order confirmation emails and SMS alerts")_
- _(e.g., "Validates user identity before allowing access to the platform")_

#### 2.3 Key Stakeholders

| Stakeholder         | Interest / Dependency |
|---------------------|----------------------|
| _(e.g., Product Team)_ | _(e.g., Drives feature requirements)_ |
| _(e.g., Frontend Team)_ | _(e.g., Consumes REST APIs)_ |
| _(e.g., DevOps)_       | _(e.g., Manages deployments and monitoring)_ |

---

### SECTION 3 — Architecture Overview

#### 3.1 Architecture Diagram (Text-Based)

Generate an ASCII or Mermaid diagram showing:
- Upstream callers (who calls this service)
- This service (with its name and port)
- Downstream dependencies (databases, Kafka, other services)

```
Example format:
[API Gateway :8080]
       │
       ▼
[customer-service :8081]
       │               │
       ▼               ▼
[PostgreSQL DB]  [Kafka Topic: customer-events]
                        │
                        ▼
               [notification-service]
```

#### 3.2 Where Does This Service Fit? _(Non-Technical)_

> 💡 **Plain English:**
> Describe the service's role using a real-world analogy. 
> _Example: "Think of this service as the front-desk receptionist — it handles all customer information requests and passes messages to the right department."_

#### 3.3 Service Layer

| Layer              | Technology Used |
|--------------------|-----------------|
| API Layer          | _(e.g., Spring MVC REST Controllers)_ |
| Business Logic     | _(e.g., Service classes, Domain model)_ |
| Data Access        | _(e.g., Spring Data JPA, Hibernate)_ |
| Messaging          | _(e.g., Apache Kafka / RabbitMQ / None)_ |
| Security           | _(e.g., Spring Security + JWT / OAuth2)_ |
| Cache              | _(e.g., Redis / None)_ |

---

### SECTION 4 — API Reference

#### 4.1 REST Endpoints

> Analyze all `@RestController`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping` annotations and generate the full endpoint table.

| Method   | Endpoint                     | Auth Required | Request Body         | Response            | Description |
|----------|------------------------------|---------------|----------------------|---------------------|-------------|
| `GET`    | `/api/v1/...`                | Yes / No      | N/A                  | `200 OK + JSON`     | _(what it does)_ |
| `POST`   | `/api/v1/...`                | Yes / No      | `{ ... }`            | `201 Created`       | _(what it does)_ |
| `PUT`    | `/api/v1/.../:{id}`          | Yes / No      | `{ ... }`            | `200 OK`            | _(what it does)_ |
| `DELETE` | `/api/v1/.../:{id}`          | Yes / No      | N/A                  | `204 No Content`    | _(what it does)_ |

#### 4.2 Request / Response Examples

For each non-trivial endpoint, provide a realistic JSON example:

```json
// POST /api/v1/customers — Request
{
  "firstName": "Sandip",
  "lastName": "Shengole",
  "email": "sandip@example.com",
  "phone": "+91-9876543210"
}

// Response — 201 Created
{
  "customerId": "cust-uuid-001",
  "status": "ACTIVE",
  "createdAt": "2025-04-22T10:30:00Z"
}
```

#### 4.3 Error Response Reference

| HTTP Status | Error Code         | Meaning (Non-Technical)                          |
|-------------|-------------------|--------------------------------------------------|
| `400`       | `VALIDATION_ERROR` | "The data you sent was incomplete or incorrect." |
| `401`       | `UNAUTHORIZED`     | "You need to log in first."                      |
| `403`       | `FORBIDDEN`        | "You don't have permission to do this."          |
| `404`       | `NOT_FOUND`        | "The item you're looking for doesn't exist."     |
| `500`       | `INTERNAL_ERROR`   | "Something went wrong on our side."              |

---

### SECTION 5 — Data Model

#### 5.1 Database Overview

> Analyze all `@Entity` classes and Flyway migration scripts.

| Detail             | Value |
|--------------------|-------|
| **Database Type**  | _(e.g., PostgreSQL 15)_ |
| **Schema Name**    | _(Extract from application.yml)_ |
| **Migration Tool** | _(e.g., Flyway / Liquibase / None)_ |
| **Connection Pool**| _(e.g., HikariCP)_ |

#### 5.2 Entity / Table Reference

For each `@Entity` class found, generate:

**Table: `<table_name>`**

| Column Name      | Data Type     | Nullable | Constraints          | Description |
|------------------|---------------|----------|----------------------|-------------|
| `id`             | `UUID / BIGINT` | No     | Primary Key          | Unique identifier |
| `created_at`     | `TIMESTAMP`   | No       | Default: NOW()       | Record creation time |
| _(other columns)_| _(type)_      | _(Y/N)_  | _(FK, Unique, Index)_| _(purpose)_ |

#### 5.3 Entity Relationships

> 💡 **Plain English:**
> Explain how data tables relate to each other in simple terms.
> _Example: "Each customer can have many orders. Each order belongs to exactly one customer — like a receipt tied to a specific person."_

Generate a relationship summary:
- `Customer` **has many** `Orders` (One-to-Many)
- `Order` **has one** `Customer` (Many-to-One)
- _(continue for all relationships)_

---

### SECTION 6 — Messaging & Events (Kafka / Queue)

> Analyze `@KafkaListener`, `KafkaTemplate`, `@EventListener`, and `application.yml` Kafka config.

#### 6.1 Events Published (Outbound)

| Topic Name              | Event Type          | Trigger                    | Payload Summary |
|-------------------------|---------------------|----------------------------|-----------------|
| `customer-events`       | `CUSTOMER_CREATED`  | New customer registered    | `{ customerId, email, timestamp }` |
| _(topic name)_          | _(event name)_      | _(what causes it)_         | _(key fields)_ |

#### 6.2 Events Consumed (Inbound)

| Topic Name              | Consumer Group         | Action Taken               |
|-------------------------|------------------------|----------------------------|
| `order-events`          | `customer-service-grp` | Updates customer order count |
| _(topic name)_          | _(group id)_           | _(what this service does)_ |

#### 6.3 Messaging in Plain English

> 💡 **Plain English:**
> "When a customer registers, this service sends a notification to a shared message board (Kafka topic). Other services — like the email service — are watching that board and automatically send a welcome email. No direct phone call between services needed."

---

### SECTION 7 — Configuration Reference

#### 7.1 Application Properties

> Analyze `application.yml` and all profile-specific files (`application-dev.yml`, `application-prod.yml`).

| Property Key                            | Default Value     | Description |
|-----------------------------------------|-------------------|-------------|
| `server.port`                           | `8081`            | Port the service runs on |
| `spring.datasource.url`                 | _(from config)_   | Database connection URL |
| `spring.kafka.bootstrap-servers`        | _(from config)_   | Kafka broker address |
| `eureka.client.service-url.defaultZone` | _(from config)_   | Service discovery endpoint |
| _(other key properties)_               | _(value)_         | _(purpose)_ |

#### 7.2 Environment Variables (Secrets / Externalized Config)

| Variable Name              | Required | Description                        |
|----------------------------|----------|------------------------------------|
| `DB_PASSWORD`              | Yes      | Database password (never hardcoded)|
| `JWT_SECRET`               | Yes      | Secret key for JWT validation      |
| `KAFKA_BOOTSTRAP_SERVERS`  | Yes      | Kafka broker connection string     |
| _(variable name)_          | _(Y/N)_  | _(what it controls)_              |

#### 7.3 Spring Profiles

| Profile  | Purpose                              | Key Differences from Default |
|----------|--------------------------------------|------------------------------|
| `dev`    | Local development                    | In-memory DB, debug logging  |
| `test`   | Integration & unit testing           | Testcontainers, mock Kafka   |
| `prod`   | Production deployment                | External secrets, min logging|

---

### SECTION 8 — Security

#### 8.1 Authentication & Authorization

> Analyze `SecurityFilterChain`, `@PreAuthorize`, JWT filter classes, and OAuth2 config.

| Aspect               | Detail |
|----------------------|--------|
| **Auth Mechanism**   | _(e.g., JWT Bearer Token / OAuth2 / Basic Auth)_ |
| **Token Issuer**     | _(e.g., Keycloak / Auth Service / None)_ |
| **Protected Routes** | _(list endpoints requiring auth)_ |
| **Public Routes**    | _(list open endpoints)_ |
| **Role-Based Access**| _(e.g., ROLE_ADMIN, ROLE_USER)_ |

#### 8.2 Security in Plain English

> 💡 **Plain English:**
> "This service works like a secure building. Anyone can ring the doorbell (public endpoints), but to get inside, you need a valid ID badge (JWT token). Some rooms inside require special clearance — like admin-only areas — which only certain people can enter."

---

### SECTION 9 — Service Dependencies

#### 9.1 Upstream Dependencies (Services That Call This)

| Service Name          | Communication | Protocol  | Purpose |
|-----------------------|---------------|-----------|---------|
| `api-gateway`         | Synchronous   | HTTP/REST | Routes external traffic to this service |
| _(service name)_      | _(type)_      | _(REST/Kafka/gRPC)_ | _(why it calls this service)_ |

#### 9.2 Downstream Dependencies (Services This Calls)

| Service / System      | Communication | Protocol  | Purpose |
|-----------------------|---------------|-----------|---------|
| `notification-service`| Async         | Kafka     | Sends email/SMS on events |
| `PostgreSQL DB`       | Sync          | JDBC      | Persists all service data |
| _(name)_              | _(type)_      | _(protocol)_ | _(purpose)_ |

#### 9.3 Dependency Health Impact

> 💡 **Plain English:**
> List what happens to users if each dependency goes down:
> - **If DB goes down:** "Users cannot register, log in, or retrieve their data."
> - **If Kafka goes down:** "Email notifications are delayed but core features still work."
> - **If auth-service goes down:** "Users cannot log in; all protected features are unavailable."

---

### SECTION 10 — Service Discovery & Load Balancing

| Aspect                     | Detail |
|----------------------------|--------|
| **Discovery Tool**         | _(e.g., Eureka / Consul / Kubernetes DNS)_ |
| **Service Name (Registry)**| _(Extract from `spring.application.name`)_ |
| **Load Balancer**          | _(e.g., Spring Cloud LoadBalancer / None)_ |
| **Health Check Endpoint**  | `/actuator/health` |
| **Registered Port**        | _(from config)_ |

---

### SECTION 11 — Observability (Monitoring & Logging)

#### 11.1 Health & Metrics Endpoints

| Endpoint                     | Purpose |
|------------------------------|---------|
| `/actuator/health`           | Is the service up and running? |
| `/actuator/metrics`          | CPU, memory, request counts |
| `/actuator/info`             | Build version, environment info |
| `/actuator/prometheus`       | Metrics for Prometheus scraping |

#### 11.2 Logging Strategy

| Log Level | When Used |
|-----------|-----------|
| `ERROR`   | Exceptions, system failures — requires immediate attention |
| `WARN`    | Unexpected but recoverable situations |
| `INFO`    | Normal business events (user created, order placed) |
| `DEBUG`   | Detailed flow for development troubleshooting only |

#### 11.3 Key Business Events to Log

> Analyze service classes and identify important operations that should be logged.

- `[INFO] Customer created: customerId={}, email={}`
- `[INFO] Kafka event published: topic={}, eventType={}`
- `[ERROR] Database connection failed: {exception}`
- _(add others found in codebase)_

---

### SECTION 12 — Build, Deployment & Infrastructure

#### 12.1 Build Details

| Detail                | Value |
|-----------------------|-------|
| **Build Tool**        | Maven |
| **Packaging**         | JAR / WAR |
| **Build Command**     | `mvn clean package -DskipTests` |
| **Test Command**      | `mvn verify` |
| **Docker Build**      | `docker build -t <service-name>:<version> .` |

#### 12.2 Docker Configuration

> Analyze `Dockerfile` and summarize:

| Stage          | Base Image        | Purpose |
|----------------|-------------------|---------|
| Build Stage    | `eclipse-temurin:21-jdk` | Compiles and packages the app |
| Runtime Stage  | `eclipse-temurin:21-jre` | Minimal image for running the app |

**Exposed Port:** _(from Dockerfile `EXPOSE`)_
**Entry Point:** _(from `ENTRYPOINT` / `CMD`)_

#### 12.3 Infrastructure Requirements

| Resource        | Minimum   | Recommended |
|-----------------|-----------|-------------|
| CPU             | 0.5 core  | 1 core      |
| Memory (RAM)    | 256 MB    | 512 MB      |
| Disk            | 500 MB    | 1 GB        |
| JVM Heap        | 128m      | 256m        |

---

### SECTION 13 — Testing Strategy

#### 13.1 Test Coverage Summary

> Analyze `src/test/java` for test classes and summarize:

| Test Type            | Class Count | Tools Used                   | Coverage Area |
|----------------------|-------------|------------------------------|---------------|
| Unit Tests           | _(count)_   | JUnit 5, Mockito             | Service & utility logic |
| Integration Tests    | _(count)_   | Testcontainers, Spring Boot Test | DB, Kafka, REST |
| Contract Tests       | _(count)_   | Spring Cloud Contract / Pact | API contracts with consumers |
| End-to-End Tests     | _(count)_   | RestAssured                  | Full request flow |

#### 13.2 How to Run Tests

```bash
# Run all tests
mvn verify

# Run only unit tests
mvn test

# Run integration tests
mvn verify -P integration-test

# Run with coverage report
mvn verify jacoco:report
```

---

### SECTION 14 — Known Limitations & Technical Debt

> List known issues, trade-offs, and improvement areas. Be honest — this helps future developers.

| Item                  | Type            | Impact   | Suggested Fix |
|-----------------------|-----------------|----------|---------------|
| _(e.g., No circuit breaker on DB calls)_ | Technical Debt | Medium | Add Resilience4j |
| _(e.g., No pagination on list endpoints)_| Limitation     | High   | Implement Page/Slice |
| _(other items found)_ | _(type)_        | _(Low/Med/High)_ | _(suggestion)_ |

---

### SECTION 15 — Glossary _(Non-Technical)_

Generate a glossary of all technical terms used in this document, explained in plain English:

| Term              | Plain English Explanation |
|-------------------|--------------------------|
| **Microservice**  | A small, independent program that does one specific job within a larger application. |
| **REST API**      | A way for programs to talk to each other over the internet, like a waiter taking your order and bringing food back. |
| **JWT Token**     | A digital ID badge that proves who you are without needing to check your password every time. |
| **Kafka**         | A digital message board where services can post updates and other services can read them in real-time. |
| **Docker**        | A container that packages the service with everything it needs to run — like a lunchbox that includes the food AND the cutlery. |
| **Eureka**        | A phone directory for services — instead of memorizing IP addresses, services look each other up by name. |
| **Flyway**        | A tool that automatically manages and upgrades the database structure, like a migration crew for your data. |
| **PostgreSQL**    | The database where all information is stored permanently — think of it as a highly organized filing cabinet. |
| _(add more terms found in service)_ | _(plain language explanation)_ |

---

### SECTION 16 — Changelog & Version History

| Version  | Date       | Author       | Change Summary |
|----------|------------|--------------|----------------|
| `1.0.0`  | _(date)_   | _(name)_     | Initial release |
| _(ver)_  | _(date)_   | _(name)_     | _(what changed)_ |

---

## ✅ POST-GENERATION CHECKLIST

After generating this documentation, verify:

- [ ] All endpoint URLs match actual controller mappings
- [ ] All table/column names match JPA entities and Flyway scripts
- [ ] All Kafka topics match producer/consumer configuration
- [ ] All environment variables are listed (no secrets leaked)
- [ ] Non-technical sections use zero jargon (reviewed by a non-developer)
- [ ] Architecture diagram accurately shows all integrations
- [ ] Glossary covers all technical terms used in the document
- [ ] Known limitations section is honest and complete

---

## 📌 USAGE INSTRUCTIONS FOR COPILOT CHAT

To generate documentation for a specific microservice, open GitHub Copilot Chat and run:

```
@workspace Using the microservice documentation prompt template, generate complete documentation 
for the [SERVICE-NAME] service. Analyze #file:src/main/java #file:src/main/resources/application.yml 
#file:pom.xml #file:src/main/resources/db/migration and produce all 16 sections of the documentation. 
For non-technical sections, use plain English suitable for business stakeholders.
```

Replace `[SERVICE-NAME]` with one of:
- `customer-service`
- `auth-service`
- `api-gateway`
- `notification-service`
- _(add all your 10 services here)_

---

*Generated by: Technical Lead | Platform: Spring Boot Microservices*
*Prompt Version: 1.0 | Compatible with: GitHub Copilot Chat, Claude, ChatGPT*
