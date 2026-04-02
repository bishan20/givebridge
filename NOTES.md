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