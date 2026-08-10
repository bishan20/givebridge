# GiveBridge 🌉

A full-stack community fundraising platform built with Java Spring Boot and jQuery.
GiveBridge allows organizers to create fundraising campaigns and donors to contribute,
track progress, and support causes they care about.

> 🚧 **Currently in active development** — core features complete, tests and CI/CD in progress.

---

## Tech Stack

**Backend**
- Java 21
- Spring Boot 3.5
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven

**Frontend**
- HTML5, CSS3
- JavaScript + jQuery
- AJAX (REST API calls)

**DevOps**
- Docker + Docker Compose
- GitHub Actions CI/CD (in progress)

---

## Features

- ✅ Create and manage fundraising campaigns
- ✅ Track fundraising progress with real-time progress bars
- ✅ Make donations to campaigns
- ✅ View donor list per campaign
- ✅ Campaign status badges (Active, Expired, Funded)
- ✅ Standardized REST API with global error handling
- ✅ Responsive frontend with live campaign preview
- 🚧 JUnit unit tests (in progress)
- 🚧 Integration tests (in progress)
- 🚧 Dockerfile + containerized deployment (in progress)
- 🚧 GitHub Actions CI/CD pipeline (in progress)

---

## Project Structure

```
givebridge/
├── src/
│   ├── main/
│   │   ├── java/com/givebridge/
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── service/        # Business logic
│   │   │   ├── repository/     # JPA repositories
│   │   │   ├── model/          # JPA entities
│   │   │   ├── dto/            # Request/Response DTOs
│   │   │   └── exception/      # Global exception handler
│   │   └── resources/
│   │       ├── static/         # HTML, CSS, JS frontend
│   │       └── application.yml
│   └── test/                   # Unit and integration tests (in progress)
├── docker-compose.yml
└── NOTES.md                    # Personal learning notes
```

---

## API Endpoints

### Campaigns
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/campaigns` | Get all campaigns |
| `GET` | `/api/campaigns/{id}` | Get campaign by ID |
| `POST` | `/api/campaigns` | Create a new campaign |
| `PUT` | `/api/campaigns/{id}` | Update a campaign |
| `DELETE` | `/api/campaigns/{id}` | Delete a campaign |

### Donations
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/donations` | Get all donations |
| `GET` | `/api/donations/{id}` | Get donation by ID |
| `GET` | `/api/donations/campaign/{id}` | Get donations for a campaign |
| `POST` | `/api/donations` | Create a donation |

---

## Running Locally

### Prerequisites
- Java 21
- Docker + Docker Compose
- Maven

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/bishan20/givebridge.git
cd givebridge
```

**2. Start the PostgreSQL database**
```bash
docker compose up -d
```

**3. Run the Spring Boot application**
```bash
./mvnw spring-boot:run
```

**4. Open in browser**
```
http://localhost:8080
```

---

## Planned Enhancements
- Donor account system with authentication
- Stripe payment integration
- Email confirmation on donation
- Admin dashboard
- Fix N+1 query on GET /api/donations

---

## Author
**Bishan Rasaili**
- GitHub: [@bishan20](https://github.com/bishan20)
- LinkedIn: [linkedin.com/in/bishan20](https://linkedin.com/in/bishan20)
- Portfolio: [bishanrasaili.com](https://bishanrasaili.com)