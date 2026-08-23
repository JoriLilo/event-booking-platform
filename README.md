# EventBookingPlatform

A fully-featured backend REST API for event booking, management, and review system built with Spring Boot. This project demonstrates production-grade practices including JWT authentication, role-based authorization, waitlist management with automatic promotion, comprehensive testing, and OpenAPI/Swagger documentation.



##  Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [API Documentation](#api-documentation)
- [Configuration Profiles](#configuration-profiles)
- [Authentication & Authorization](#authentication--authorization)
- [Key Business Logic](#key-business-logic)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)

---

## Features

### Core Functionality
- **Event Management** — Create, update, search, filter, and delete events
- **Seat Management** — Real-time seat availability tracking with automatic SOLD_OUT status
- **Booking System** — Attendees book seats with validation and seat deduction
- **Waitlist Management** — Automatic promotion when seats become available after cancellation
- **Review & Rating** — Attendees review completed events and view average ratings
- **Search & Filter** — Comprehensive event discovery by title, price, date, venue, category

### Technical Highlights
- **JWT Authentication** — Secure token-based auth with email-based login
- **Role-Based Access Control** — Three roles: ADMIN, ORGANIZER, ATTENDEE with strict permission boundaries
- **Three Query Types** — Derived queries, JPQL, and native SQL queries (per spec)
- **Comprehensive Testing** — 110 tests (unit + integration) using H2 in-memory database
- **OpenAPI/Swagger** — Full API documentation with interactive testing interface
- **Global Exception Handling** — Structured error responses with proper HTTP status codes
- **Configuration Profiles** — Separate configs for dev, integration testing, and production
- **Transactional Integrity** — @Transactional ensures data consistency across business operations

---

##  Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.3.4 |
| **Build Tool** | Maven 3.9.x |
| **Database** | MySQL 8 (prod/dev), H2 (testing) |
| **Authentication** | JWT (JJWT 0.12.6) + Spring Security |
| **ORM** | Spring Data JPA (Hibernate) |
| **Validation** | Jakarta Bean Validation |
| **API Documentation** | SpringDoc OpenAPI 2.6.0 + Swagger UI |
| **Logging** | Log4j2 (custom config) |
| **Testing** | JUnit 5, Mockito, H2 Database |
| **Utilities** | Lombok 1.18.38 |

---

## Architecture

### Entities (6 Core + Waitlist)

```
User (ADMIN, ORGANIZER, ATTENDEE)
  ├─ Event (created by ORGANIZER)
  │  ├─ Venue (created by ADMIN)
  │  ├─ Category (created by ADMIN)
  │  ├─ Booking (created by ATTENDEE)
  │  │  └─ Review (created by ATTENDEE)
  │  └─ Waitlist (created by ATTENDEE when event SOLD_OUT)
```

### Layers

```
Controller Layer (8 controllers)
    ↓
Service Layer (7 services + business logic)
    ↓
Repository Layer (7 repositories with custom queries)
    ↓
Database (MySQL)
```

### Security Flow

```
Login Request → UserService.login() → JwtUtil.generateToken() → AuthResponse (JWT)
                                                                        ↓
Subsequent Requests with JWT → JwtAuthenticationFilter → SecurityContext → @PreAuthorize checks
```

---

##  Prerequisites

- **Java 21** (or compatible JDK)
- **Maven 3.9.x**
- **MySQL 8** (for dev/prod; H2 is built-in for testing)
- **Git**

### Verify Installation

```bash
java -version     # Should show Java 21
mvn -version      # Should show Maven 3.9.x
mysql --version   # Should show MySQL 8.x (for dev/prod only)
```

---

##  Installation & Setup

### 1. Clone Repository

```bash
git clone https://github.com/JoriLilo/event-booking-platform.git
cd EventBookingPlatform
```

### 2. Create MySQL Database (Dev Only)

```bash
mysql -u root -p
CREATE DATABASE eventbooking;
```

Update `application-dev.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 3. Install Dependencies

```bash
mvn clean install
```

---

## Running the Application

### Development Mode (with MySQL)

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

Or via IDE:
- IntelliJ: Run → Edit Configurations → VM options: `-Dspring.profiles.active=dev`

**App runs on:** `http://localhost:8080`

### Production Mode (with MySQL)

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```



##  Testing

### Run All Tests (Unit + Integration)

```bash
mvn clean test
```

**Results:** ~110 tests passing 

### Run Specific Test Class

```bash
mvn test -Dtest=EventServiceTest
mvn test -Dtest=FullBookingFlowIntegrationTest
```

### Test Coverage

- **Unit Tests:** 7 service tests (mocked repositories)
- **Integration Tests:** 2 comprehensive flows using H2 in-memory database
    - Full booking flow: create event → book → cancel → verify seat restoration
    - Auth & authorization: registration, login, role-based access control

### Test Profile Configuration

Tests automatically use `application-test.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

---

##  API Documentation

### Swagger UI (Interactive)

**URL:** `http://localhost:8080/swagger-ui.html`

Features:
- Browse all endpoints organized by resource (Events, Bookings, Reviews, etc.)
- View request/response schemas
- Test endpoints directly with "Try it out"
- Paste JWT token to authenticate requests
- See all possible response codes and error messages

### OpenAPI JSON Spec

**URL:** `http://localhost:8080/v3/api-docs`

Machine-readable specification for API clients and code generation.

### Endpoints Overview

| Resource | Endpoints | Auth Required |
|----------|-----------|---------------|
| **Auth** | POST /register, /register-organizer, /login | No |
| **Events** | GET (public), POST/PUT/DELETE (ORGANIZER) | Partial |
| **Bookings** | POST/PATCH (ATTENDEE), GET (depends) | Yes |
| **Reviews** | POST/DELETE (ATTENDEE), GET (public) | Partial |
| **Waitlist** | POST/DELETE (ATTENDEE), GET | Partial |
| **Venues** | POST/PUT/DELETE (ADMIN), GET (public) | Partial |
| **Categories** | POST/PUT/DELETE (ADMIN), GET (public) | Partial |

---

## Configuration Profiles

The application uses Spring profiles to load environment-specific configuration.

### Development (`dev`)
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```
- Uses MySQL database (`jdbc:mysql://localhost:3306/eventbooking`)
- SQL query logging enabled (`spring.jpa.show-sql=true`)
- DDL: `create-drop` (recreates schema on restart)
- Debug logging level
- **Environment variables (optional):**
  - `SPRING_DATASOURCE_URL` (default: `jdbc:mysql://localhost:3306/eventbooking`)
  - `SPRING_DATASOURCE_USERNAME` (default: `root`)
  - `SPRING_DATASOURCE_PASSWORD` (default: empty)
  - `SERVER_PORT` (default: `8080`)

### Integration Testing (`int`)
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=int"
```
- Uses MySQL database (same defaults as dev)
- Minimal query logging (`spring.jpa.show-sql=false`)
- DDL: `update` (expects schema to exist)
- Info logging level
- **Environment variables (optional):** same as dev

### Production (`prod`)
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```
- MySQL database (external, **must be configured via environment variables**)
- No SQL logging
- DDL: `update`
- Error logging level only
- Stricter security settings
- **Required environment variables:**
  - `SPRING_DATASOURCE_URL` (e.g., `jdbc:mysql://prod-host:3306/eventbooking`)
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
  - `SERVER_PORT` (optional, defaults to `8080`)

### Testing (Automatic)
- Uses H2 in-memory database
- `application-test.properties` (auto-detected)
- DDL: `create-drop`

You can also override any property via command line, e.g.:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod --spring.datasource.username=admin --spring.datasource.password=secret"
```

---

##  Authentication & Authorization

### JWT Token Flow

1. **Registration/Login**
   ```bash
   POST /api/auth/register
   POST /api/auth/register-organizer
   POST /api/auth/login
   ```
   Returns JWT token valid for 1 hour

2. **Using Token**
   ```bash
   GET /api/event/1
   Authorization: Bearer <JWT_TOKEN>
   ```

3. **Token Validation**
    - `JwtAuthenticationFilter` extracts token from header
    - `JwtUtil` validates signature and expiration
    - `CustomUserDetailsService` loads user by email
    - `SecurityContext` stores authenticated principal

### Role-Based Access Control

**ADMIN**
- Create/update venues
- Create/update categories
- View all bookings
- Cancel any booking

**ORGANIZER**
- Create/update/delete events
- Update event status
- View their own events

**ATTENDEE**
- Book events
- Cancel own bookings
- Join/leave waitlist
- Create reviews for completed events
- View public events/venues/categories

---

## Key Business Logic

### Seat Management
- Seats decremented on booking confirmation
- Event status changes to `SOLD_OUT` when `availableSeats == 0`
- Seats restored when booking cancelled
- Event status reverts to `AVAILABLE` if seats become available

### Waitlist & Promotion
- Attendees can join waitlist only if event is `SOLD_OUT`
- Waitlist position tracked by insertion order
- **Automatic Promotion:** When a booking is cancelled:
    1. Seats restored to event
    2. `WaitlistService.promoteFromWaitlist()` triggered
    3. First user on waitlist → automatic booking created
    4. Waitlist status changed to `PROMOTED`

### Review Eligibility
- User must have a `CONFIRMED` booking for the event
- Event must be `completed` (endDateTime < now)
- Only one review per user per event (enforced by unique constraint)

### Query Implementation (Per Spec)

| Query Type | Example | Method |
|-----------|---------|--------|
| **Derived** | `findByPriceRange` | `Page<Event> findByPriceRange(float, float, Pageable)` |
| **JPQL** | Date range filter | `findByDateRange()` uses @Query with JPQL |
| **Native** | Price filter | `findByPriceRange()` uses @Query(nativeQuery=true) |

---

##  Project Structure

```
EventBookingPlatform/
├── src/main/java/com/example/EventBookingPlatform/
│   ├── entity/                    # 6 core entities + Waitlist
│   │   ├── User.java
│   │   ├── Event.java
│   │   ├── Booking.java
│   │   ├── Review.java
│   │   ├── Venue.java
│   │   ├── Category.java
│   │   └── Waitlist.java
│   ├── dto/                       # Request/Response DTOs
│   │   ├── EventRequest.java
│   │   ├── EventResponse.java
│   │   ├── BookingRequest.java
│   │   └── ... (13 total)
│   ├── repository/                # Data access layer
│   │   ├── EventRepository.java (custom queries)
│   │   ├── BookingRepository.java
│   │   └── ... (7 total)
│   ├── service/                   # Business logic layer
│   │   ├── EventService.java
│   │   ├── BookingService.java
│   │   ├── WaitlistService.java (promotion logic)
│   │   ├── ReviewService.java
│   │   └── ... (7 total)
│   ├── controller/                # REST API layer
│   │   ├── EventController.java (@Operation, @ApiResponse annotations)
│   │   ├── BookingController.java
│   │   ├── ReviewController.java
│   │   ├── WaitlistController.java
│   │   └── ... (8 total)
│   ├── security/                  # JWT & Spring Security
│   │   ├── JwtUtil.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── SecurityConfig.java
│   │   └── CustomUserDetailsService.java
│   ├── exception/                 # Global exception handling
│   │   ├── GlobalExceptionHandler.java
│   │   ├── EventNotFoundException.java
│   │   └── ... (6 custom exceptions)
│   ├── config/                    # Spring configuration
│   │   └── OpenApiConfig.java (Swagger configuration)
│   └── EventBookingPlatformApplication.java
├── src/test/java/
│   ├── service/                   # Unit tests (7 services)
│   │   ├── EventServiceTest.java
│   │   ├── BookingServiceTest.java
│   │   └── ... (7 total)
│   └── integration/               # Integration tests (H2)
│       ├── FullBookingFlowIntegrationTest.java
│       └── EventBookingIntegrationTest.java
├── src/main/resources/
│   ├── application.properties      # Default profile
│   ├── application-dev.properties
│   ├── application-int.properties
│   ├── application-prod.properties
│   ├── log4j2.xml                 # Logging configuration
│   └── templates/
├── src/test/resources/
│   └── application-test.properties (H2 config)
├── pom.xml                        # Maven dependencies 
└── README.md                      # This file
```

---

##  Database Schema

### Relationships

```sql
User (1) ──── (N) Event
User (1) ──── (N) Booking
User (1) ──── (N) Review
User (1) ──── (N) Waitlist

Event (1) ──── (N) Booking
Event (1) ──── (N) Review
Event (1) ──── (N) Waitlist
Event (N) ──── (N) Category (via event_categories join table)

Venue (1) ──── (N) Event
```

### Key Constraints

- `User.email` — UNIQUE
- `Event.user_id` — Foreign key to User (ORGANIZER)
- `Booking.user_id + Booking.event_id` — Composite unique index (one booking per event per user)
- `Review.user_id + Review.event_id` — UNIQUE (one review per user per event)
- `Waitlist.user_id + Waitlist.event_id` — UNIQUE

---
##  Example Workflows

### 1. Register & Login

```bash
# Register as Attendee
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123"
  }'

# Response
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "role": "ATTENDEE"
}

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'

# Response
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "ATTENDEE"
}
```

### 2. Create Event (Organizer)

```bash
curl -X POST http://localhost:8080/api/event \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Tech Conference 2024",
    "description": "A great tech conference",
    "startDateTime": "2024-12-15T09:00:00",
    "endDateTime": "2024-12-15T17:00:00",
    "price": 99.99,
    "totalSeats": 100,
    "venueId": 1,
    "categoryIds": [1, 2]
  }'
```

### 3. Book Event (Attendee)

```bash
curl -X POST http://localhost:8080/api/booking \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": 1,
    "seatsBooked": 2
  }'

# Response
{
  "id": 1,
  "eventTitle": "Tech Conference 2024",
  "seatsBooked": 2,
  "bookingDate": "2024-08-23T19:52:00",
  "status": "CONFIRMED",
  "attendeeUsername": "john_doe"
}
```

### 4. Join Waitlist (Attendee)

```bash
curl -X POST http://localhost:8080/api/waitlist \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": 1
  }'
```

### 5. Leave Review (Attendee, After Event)

```bash
curl -X POST http://localhost:8080/api/review/event/1 \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "rating": 5,
    "comment": "Great event, highly recommend!"
  }'
```

---

##  Troubleshooting

| Issue | Solution |
|-------|----------|
| **MySQL connection refused** | Verify MySQL is running: `mysql -u root -p` |
| **401 Unauthorized** | Check JWT token in `Authorization: Bearer` header |
| **403 Forbidden** | Verify your user role matches endpoint requirements |
| **Event SOLD_OUT** | Join waitlist; you'll be promoted automatically when seats available |
| **Cannot review event** | Ensure booking is CONFIRMED and event has ended |
| **Tests fail** | Run `mvn clean test`; H2 and JUnit should be in pom.xml |

---

## Spec Compliance

This project fully implements the EventBookingPlatform specification:

- ✅ **6 Core Entities** + Waitlist entity
- ✅ **3 User Roles** with strict authorization (ADMIN, ORGANIZER, ATTENDEE)
- ✅ **Waitlist** with automatic promotion on cancellation
- ✅ **Cancellation Policy** with time-window enforcement
- ✅ **Event Search/Filter/Pagination** with sorting
- ✅ **Average Rating** on event details; review eligibility gated on confirmed booking + past event
- ✅ **Three Query Types** — Derived, JPQL, and Native SQL queries
- ✅ **Multiple Config Profiles** — dev, int, prod
- ✅ **Unit + Integration Tests** — 110 passing tests (no Docker required)
- ✅ **Swagger/OpenAPI** — Full API documentation
- ✅ **JWT Authentication** — Secure token-based auth
- ✅ **Global Exception Handling** — Structured error responses
- ✅ **Logging** — Log4j2 with custom configuration
- ✅ **Single Maven Module** — Clean, organized project structure
