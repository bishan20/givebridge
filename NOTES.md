# GiveBridge - Personal Learning Notes

## Purpose
This file documents my learning notes while building GiveBridge.
Not meant for production — personal reference and interview prep.

---

## Project Architecture
```
Request → Controller → Service → Repository → Database
```
- **Controller** — handles HTTP requests/responses
- **Service** — contains business logic
- **Repository** — talks to the database
- **Model/Entity** — represents database tables as Java classes
- **DTO** — Data Transfer Objects, controls what data is exposed in API

---

## Campaign Entity

### What is an Entity?
A Java class decorated with @Entity that maps directly to a
database table. Hibernate reads it and creates/manages the table.

### Key Annotations
| Annotation | Purpose |
|---|---|
| `@Entity` | Marks class as a database table |
| `@Table(name="campaigns")` | Explicitly names the table in PostgreSQL |
| `@Id` | Marks the primary key field |
| `@GeneratedValue` | DB auto-increments the ID value |
| `@Column` | Configures column properties (nullable, length etc.) |
| `@NotBlank` | Validation — field cannot be empty string |
| `@NotNull` | Validation — field cannot be null |
| `@Future` | Validation — date must be in the future |
| `@PrePersist` | Runs a method automatically before saving to DB |

### Why BigDecimal for money?
Double/float use binary floating point which causes precision errors:
0.1 + 0.2 = 0.30000000000000004  ← wrong!
BigDecimal is exact and always use it for any currency/financial data.

### Lombok Annotations
| Annotation | What it generates |
|---|---|
| `@Data` | Getters, setters, toString, equals, hashCode |
| `@Builder` | Builder pattern — Campaign.builder().title("x").build() |
| `@NoArgsConstructor` | Empty constructor — required by JPA/Hibernate |
| `@AllArgsConstructor` | Constructor with all fields |
| `@Builder.Default` | Sets a default value when using the builder |

### Why @NoArgsConstructor is required
JPA/Hibernate needs to instantiate entity objects using reflection
when loading data from the database. Reflection requires an empty
no-argument constructor to exist. Without it, Hibernate crashes.

### ddl-auto: update (in application.yml)
Tells Hibernate to automatically create or update database tables
based on entity classes. Options:
- `create` — drops and recreates tables on every startup (loses data)
- `update` — adds new columns/tables but never drops existing ones
- `validate` — checks schema matches entities but makes no changes
- `none` — does nothing, you manage schema yourself (used in production)

---

## Docker Setup

### docker-compose.yml
Orchestrates multiple containers together locally.
Currently runs PostgreSQL so we don't need a local install.

### Why Docker for the database?
- No need to install PostgreSQL directly on your machine
- Easy to reset — just delete the volume and recreate
- Matches production environment closely
- Anyone cloning the repo can run the DB with one command: docker compose up -d

### Dockerfile (coming later)
Will package the Spring Boot app itself into a container.
Final setup: PostgreSQL container + Spring Boot container running together.

---

## JDBC, JPA, and Hibernate

### How they relate
```
Your Code (Campaign entity, Repository)
        ↓
    JPA (the rules/interface)
        ↓
  Hibernate (the implementation)
        ↓
    JDBC (the low-level connector)
        ↓
  PostgreSQL Database
```

### JDBC
- Java's built-in standard for talking to any database
- Very low level and verbose — you write raw SQL and manage connections manually
- Every operation requires opening a connection, preparing a statement, setting
  parameters, executing, and closing the connection manually
- JPA/Hibernate sits on top of JDBC so we don't have to deal with it directly

### JPA (Java Persistence API)
- NOT a library — it's a specification (a set of rules and interfaces)
- Defines annotations like @Entity, @Table, @Id, @Column that we use in our entities
- Says "here's how things should work" but doesn't implement anything itself
- Think of JPA like a job description — defines what needs to be done, not how

### Hibernate
- The actual implementation of JPA — does the real work
- Reads @Entity annotations, generates SQL, manages connections, handles caching
- When you call campaignRepository.save(campaign), Hibernate translates that into
  INSERT INTO campaigns... SQL and sends it through JDBC to PostgreSQL
- Think of Hibernate like the employee hired based on the JPA job description

### Summary
- JPA     = the contract/rules (what to do)
- Hibernate = the implementation (how to do it)
- JDBC    = the low-level pipe to the database

---

## How Spring Boot Connects to Docker PostgreSQL

### Port mapping in docker-compose.yml
```yaml
ports:
  - "5432:5432"
```
Maps port 5432 on your laptop → port 5432 inside the container.
PostgreSQL inside Docker is accessible at localhost:5432 from your laptop.

### The full connection flow
```
Spring Boot app
      ↓
HikariCP tries to connect to localhost:5432
      ↓
Docker intercepts the request at port 5432 on your laptop
      ↓
Docker forwards it to port 5432 inside givebridge-db container
      ↓
PostgreSQL inside the container responds
      ↓
Connection established ✅
```

### Why order matters
If Docker isn't running when Spring Boot starts, localhost:5432 isn't
responding and the app crashes with a connection error. Always:
1. Start Docker container first → docker compose up -d
2. Then start Spring Boot app

### localhost vs container name
When Spring Boot runs OUTSIDE Docker (current setup):
url: jdbc:postgresql://localhost:5432/givebridge

When Spring Boot runs INSIDE Docker (later when we add Dockerfile):
url: jdbc:postgresql://givebridge-db:5432/givebridge

When both containers run inside Docker Compose together, they communicate
using container names as hostnames instead of localhost. Docker Compose
automatically sets up an internal network between them.

---