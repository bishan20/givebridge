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

## Repository Pattern vs DAO Pattern

### What I used before in older projects (Spring Data JDBC + DAO pattern)
Previous job used `spring-boot-starter-data-jdbc` — lower level than JPA,
no Hibernate, no automatic entity mapping. Had to manually write SQL in DAO layer.
```
Controller
    ↓
ServiceInterface → ServiceImpl (business logic)
    ↓
DAO (manual SQL queries using JDBC)
    ↓
Database
```

### What we use now for this project (Spring Data JPA + Repository pattern)
```
Controller
    ↓
Service (business logic)
    ↓
Repository (Spring Data JPA — auto-generated)
    ↓
Hibernate (auto-generated SQL)
    ↓
JDBC
    ↓
Database
```
Spring Data JPA eliminates the DAO layer entirely by auto-generating it at runtime.

### Key Differences
| | JDBC + DAO | JPA + Repository |
|---|---|---|
| **SQL control** | Full manual control | Hibernate generates it |
| **Boilerplate code** | A lot | Very little |
| **Learning curve** | Lower (closer to raw SQL) | Higher (need to understand ORM) |
| **Complex queries** | Easier to write manually | Can get complicated |
| **Speed of development** | Slower | Much faster |

### Why old projects had ServiceInterface + ServiceImpl
Many enterprise teams write both a Service interface and a Service implementation:
- Older Spring versions needed interfaces to create proxies for transactions
- Supports multiple implementations (e.g. one for prod, one for testing)
- Often just team convention carried over from older Java EE days

Modern Spring Boot does NOT require a service interface unless you genuinely
need multiple implementations. One service class is the recommended approach now.

### Why we don't have a Service interface in GiveBridge
CampaignService is just one class — no interface needed for a project this size.
Clean, simple, and follows modern Spring Boot conventions.

### Spring Data JPA method name magic
Spring reads the method name and auto-generates SQL:

findAllByOrderByCreatedAtDesc()
→ SELECT * FROM campaigns ORDER BY created_at DESC

Keywords Spring understands:
- findBy      → WHERE
- OrderBy     → ORDER BY
- Desc        → DESC
- And         → AND
- Like        → LIKE

Example: findByTitleContainingOrderByCreatedAtDesc(String title)
→ SELECT * FROM campaigns WHERE title LIKE '%title%' ORDER BY created_at DESC

No DAO, no SQL, no implementation — just a method name.

### @RequiredArgsConstructor vs @Autowired
Old style (still works but not recommended):
@Autowired
private CampaignRepository campaignRepository;

Modern style (what we use):
@RequiredArgsConstructor  ← on the class
private final CampaignRepository campaignRepository;  ← note: final

Lombok generates a constructor that injects the dependency.
Constructor injection is recommended because:
- Makes dependencies explicit and required (final keyword)
- Easier to unit test — pass mock objects directly in constructor
- Officially recommended by the Spring team

---

## REST Controller

### What is a Controller?
The entry point for all incoming HTTP requests. Exposes the application
to the outside world via HTTP endpoints. Delegates business logic to the Service.

### Request lifecycle
```
Browser/Postman sends HTTP request
        ↓
Controller receives it — converts JSON to DTO, runs validation
        ↓
Service applies business logic
        ↓
Repository queries the database
        ↓
Result travels back up the chain
        ↓
Controller converts Java object to JSON and sends HTTP response
```

### REST HTTP Methods
| HTTP Method | Action | Example |
|---|---|---|
| `GET` | Read/fetch data | Get all campaigns |
| `POST` | Create new data | Create a campaign |
| `PUT` | Update existing data | Update a campaign |
| `DELETE` | Delete data | Delete a campaign |

### HTTP Status Codes we use
| Code | Meaning | When |
|---|---|---|
| 200 OK | Success | GET, PUT |
| 201 Created | Successfully created | POST |
| 204 No Content | Success, no body | DELETE |
| 400 Bad Request | Validation failed | @Valid catches bad input |
| 404 Not Found | Resource doesn't exist | EntityNotFoundException |

### Key Annotations
| Annotation | Purpose |
|---|---|
| `@RestController` | Combines @Controller + @ResponseBody, returns JSON automatically |
| `@RequestMapping` | Base URL path for all endpoints in the controller |
| `@GetMapping` | Handles GET requests |
| `@PostMapping` | Handles POST requests |
| `@PutMapping` | Handles PUT requests |
| `@DeleteMapping` | Handles DELETE requests |
| `@PathVariable` | Extracts value from URL e.g. /campaigns/{id} → id |
| `@RequestBody` | Converts incoming JSON body to Java DTO object |
| `@Valid` | Triggers validation annotations on the DTO |

### Why ResponseEntity?
Gives full control over HTTP response including status code and body.
Without it, Spring always returns 200 OK even for created resources.
ResponseEntity.ok(data)              → 200 OK
ResponseEntity.status(201).body(x)  → 201 Created
ResponseEntity.noContent().build()  → 204 No Content

### JSON ↔ Java ↔ Database naming convention
```
JSON (camelCase)  →  Java DTO (camelCase)  →  Database (snake_case)
  goalAmount      →    goalAmount           →    goal_amount
```
- Jackson handles JSON ↔ Java conversion automatically (camelCase)
- Hibernate handles Java ↔ Database conversion automatically (snake_case)
- Never use snake_case in JSON or Java code

### Why @Valid and @RequestBody work together
@RequestBody converts JSON → DTO first
@Valid then runs all validation annotations on that DTO
If validation fails → Spring returns 400 Bad Request automatically
Your service and repository are never even called

### Why DTO and Entity are separate
Client sends CampaignRequestDTO (only what they're allowed to provide):
title, description, goalAmount, deadline

Server controls the rest on the Campaign entity:
id          → generated by database
raisedAmount → defaults to 0 via @Builder.Default
createdAt   → set automatically via @PrePersist

---

## Donation Entity

### Design Decision — No Donor Account System
Each row in the donations table represents a single donation transaction,
not a unique donor/person. If John Doe donates to 3 campaigns, he appears
3 times as separate rows. No way to link donations to the same person.

To uniquely identify donors you would need a separate donors table with
a donor_id foreign key in donations — essentially a full user account system
with authentication. Deferred as a future enhancement for GiveBridge.

### One-to-Many Relationship (Campaign → Donations)
One campaign can have many donations.

```
campaigns table              donations table
id | title | goal     ←───  id | amount | campaign_id (FK)
1  | Fund1 | 2500            1  | 100    | 1
                              2  | 250    | 1
```

In Java this is expressed with @ManyToOne on the Donation side:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "campaign_id", nullable = false)
private Campaign campaign;
```

### FetchType.LAZY vs FetchType.EAGER
- LAZY  — loads Campaign data only when you explicitly access it. More efficient.
- EAGER — loads Campaign data immediately every time a Donation is loaded.
  Less efficient — loads data you may not even need.
  Always prefer LAZY for @ManyToOne and @ManyToMany relationships.

### @JoinColumn
Tells Hibernate to create a campaign_id column in the donations table
that stores the foreign key reference to the campaigns table.
Always specify the name explicitly — without it Hibernate auto-generates
an unpredictable column name.

```
Java field          →    Database column
Campaign campaign   →    campaign_id (foreign key → campaigns.id)
```

### Why @PrePersist is repeated in each Entity
Even though Campaign and Donation are in the same package, you cannot
reuse Campaign's @PrePersist in Donation because `this` refers to the
instance of the class the method belongs to. Calling it from Donation
would set Campaign's createdAt field, not Donation's donatedAt field.

### @MappedSuperclass — the right way to reuse @PrePersist
For entities that share common fields, create a base class:

```java
@MappedSuperclass
public abstract class BaseEntity {
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```

Then extend it:
```java
public class Campaign extends BaseEntity { ... }
public class Donation extends BaseEntity { ... }
```

@MappedSuperclass tells Hibernate "this class is not a table itself,
but share its fields with all subclasses that extend it."
Not used in GiveBridge because Campaign uses createdAt while Donation
uses donatedAt — different field names make a shared base class awkward.
Common in enterprise projects where many entities share createdAt,
updatedAt, createdBy etc.

### @Transactional — critical for createDonation
When creating a donation, two database operations must happen together:
1. Insert into donations table
2. Update raisedAmount in campaigns table

If step 1 succeeds but step 2 fails, the donation is recorded but the
campaign total is wrong — data is now inconsistent.

@Transactional wraps both operations in a single transaction:
- Both succeed → changes committed to database
- Either fails  → both operations rolled back automatically

```java
@Transactional
public Donation createDonation(DonationRequestDTO dto) {
    // 1. save donation
    // 2. update campaign raisedAmount
    // if anything fails → both rolled back automatically
}
```

Always put @Transactional on the Service method, never on the Controller.


---