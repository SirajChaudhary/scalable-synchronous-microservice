
# scalable-synchronous-microservice

This microservice demonstrates how to scale a **blocking I/O-based (synchronous) microservice**
using modern Java and Spring technologies.

It shows:

- How to scale synchronous/blocking microservices using Virtual Threads (Java 21)
- How to achieve parallel execution using CompletableFuture
- How to scale without rewriting to reactive programming
- How to properly manage database connection scaling
- How to apply clean architecture and design patterns

This example proves that blocking microservices can be scaled efficiently with the right strategy.

---

# Tech Stack

- Java 21 (LTS, stable features only)
- Spring Boot 3.x
- Spring Data JPA (Hibernate 6) – Blocking JDBC
- PostgreSQL
- Maven
- HikariCP (Connection Pooling)
- Virtual Threads (Java 21 stable)
- CompletableFuture
- Stream API
- Records
- JPQL & Native Queries
- Transaction Management
- Global Exception Handling
- Versioned APIs (/api/v1)
- Proper layering & best practices

---

# Project Structure
```
scalable-synchronous-microservice
 ├── controller
 ├── service
 │     ├── impl
 ├── repository
 ├── entity
 ├── dto
 ├── exception
 ├── config
 └── util
```
---

# Design Patterns Used

- Layered Architecture
- DTO Pattern
- Builder Pattern
- Repository Pattern
- Service Pattern
- Global Exception Pattern
- Factory Method (static factories)
- Optional Usage for null safety

### what is Builder Pattern:

- Used to construct complex objects step-by-step
- Improves readability
- Avoids telescoping constructors
- Ensures clean object creation

How we use it in service layer:

Example snapshot:

```java
Ride ride = Ride.builder()
        .userId(request.userId())
        .driverId(selectedDriver.getId())
        .fare(fare)
        .requestedAt(LocalDateTime.now())
        .status("CONFIRMED")
        .build();
```

- Clean
- Readable
- Maintainable

### Why We May Need Mapper Class in Future

Currently object mapping DTO-to-entity is performed manually within the service layer using the builder pattern.

In larger systems:

- Mapping logic increases
- Conversion becomes repetitive
- We need centralized mapping logic

Mapper helps:

- Entity → DTO conversion
- DTO → Entity conversion
- Clean separation of concerns
- Better maintainability

### What is Factory Method (Static Factories)

Factory Method allows object creation using named static methods instead of constructors.

Example:

```java
public static Ride createConfirmedRide(Long userId, Long driverId, double fare) {
    return Ride.builder()
            .userId(userId)
            .driverId(driverId)
            .fare(fare)
            .status("CONFIRMED")
            .requestedAt(LocalDateTime.now())
            .build();
}
```

Benefits:

- Clear intention
- Better readability
- Centralized creation logic
- Domain-driven design friendly

---

# Virtual Threads (Java 21)

Enabled in application.yml:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

- 1 request = 1 virtual thread
- Lightweight threads
- Blocking code without OS thread exhaustion
- No reactive rewrite required

### Why We Still Need Custom Executor Bean

Even after enabling virtual threads in YAML, CompletableFuture does NOT automatically use Spring’s virtual thread executor.

By default:

- CompletableFuture uses ForkJoinPool (platform threads)

So we define:

```java
@Configuration
public class VirtualThreadConfig {

    @Bean
    public Executor virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
```

This ensures:

- Async tasks also run on virtual threads
- Consistent execution model
- Scalable demonstration

---

# Optimistic Locking Version Field

```java
@Version
private Long version;
```

### What Is It?

- Used for optimistic locking
- Acts as a version counter (0, 1, 2, 3...)
- Automatically incremented on every update
- Prevents concurrent update conflicts

### Who Manages It?

- Managed internally by JPA/Hibernate
- The application does not update it manually
- Hibernate automatically:
    - Checks the version during UPDATE
    - Increments the version if the update succeeds
    - Throws OptimisticLockException if a conflict occurs

### Example Scenario

#### Initial Database State

| id | available | version |
|----|-----------|---------|
| 5  | true      | 0       |

### First Concurrent Request

Hibernate generates:

```sql
UPDATE drivers
SET available = false, version = 1
WHERE id = 5 AND version = 0;
```

- Version matches (0)
- Update succeeds
- Version becomes 1

### Second Concurrent Request

Hibernate generates:

```sql
UPDATE drivers
SET available = false, version = 1
WHERE id = 5 AND version = 0;
```

- Version no longer equals 0
- No rows updated
- Hibernate throws OptimisticLockException
- Transaction rolls back

### Why It Is Important
- Prevents double driver allocation
- Avoids race conditions
- Ensures data consistency under high concurrency
- Critical for scalable blocking microservices using Virtual Threads and parallel execution

---

# JPQL Custom Query vs Native Query

JPQL Example:

```java
@Query("SELECT d FROM Driver d WHERE d.available = true AND d.rating >= :rating")
List<Driver> findAvailableDrivers(@Param("rating") double rating);
```

- Database independent
- Works with entity model

Native Query Example:

```java
@Query(value = "SELECT * FROM drivers WHERE rating >= 4.0 ORDER BY rating DESC",
       nativeQuery = true)
List<Driver> findTopDriversNative();
```

- Direct SQL
- Performance tuning
- DB specific optimization

---

# How This Blocking Service Scales

Traditional Blocking Model:

- 1 request = 1 platform thread
- Limited by OS threads
- Memory heavy

With Virtual Threads (Java 21):

- 1 request = 1 virtual thread
- Millions possible
- Lightweight
- Still blocking code
- No reactive rewrite needed

---

# Scaling Strategy

1. Use Virtual Threads
2. Use Proper HikariCP sizing
3. Enable DB batching
4. Use Stream API for in-memory processing
5. Use CompletableFuture for parallel DB calls
6. Use caching (optional)
7. Horizontal scaling via Kubernetes

---

# Connection Pool Scaling Strategy

- Increase pool size carefully based on DB CPU & memory.
- Virtual threads allow high request concurrency without OS thread exhaustion.
- BUT DB connections are still physical resources.
- If 10,000 virtual threads try DB access but pool = 60 than 9,940 will wait.
- Always load test before increasing pool size.
- Monitor:
  - /actuator/metrics/hikaricp.connections.active
  - /actuator/metrics/hikaricp.connections.idle
- For heavy read traffic, use read replicas.

---

# CompletableFuture Usage

- supplyAsync
- thenApply
- thenAcceptAsync
- thenCombine

Parallel Execution:

- DB fetch
- Fare calculation
- Driver allocation

All still BLOCKING (JPA) but:

- Running on virtual threads
- Parallelized
- Scalable

---

# Logging & Validation

- Request validation using @Valid
- Logs in controller (request received)
- Logs in service layer (business flow)
- Uses SLF4J + Lombok
- Clean structured logging

---

# How To Run

1. Clone repository
```bash
git clone https://github.com/SirajChaudhary/scalable-synchronous-microservice.git
```
2. Create database using PostgreSQL client (e.g. pgAdmin):
   ```sql
   CREATE DATABASE rides_db;
   ```
3. Run ```src/main/resources/db/rides_db_init.sql``` to create tables and sample data 

<br>
<img width="2310" height="722" alt="image" src="https://github.com/user-attachments/assets/506bf894-8ce9-4f80-a1aa-3a751f2f168a" />

<br><br>
<img width="2334" height="558" alt="image" src="https://github.com/user-attachments/assets/7542ca47-1dce-4716-bd0f-e6748682d24e" />


4. Run application:
   ```bash
   ScalableSynchronousMicroserviceApplication.java
   ```
5. Import the Postman collection and run the APIs
    - Run APIs
---

# API Overview

### 1) Create Driver

- Registers a new driver in the system.

#### URL

    POST http://localhost:8080/api/v1/drivers

#### Request Body

``` json
{
  "name": "Ravi",
  "latitude": 18.5204,
  "longitude": 73.8567,
  "rating": 4.7
}
```

#### Response (201 Created)

``` json
{
  "id": 6,
  "name": "Ravi",
  "latitude": 18.5204,
  "longitude": 73.8567,
  "rating": 4.7,
  "available": true,
  "createdAt": "2026-03-03T00:21:24.756512"
}
```
#### Updated DB
<img width="2316" height="784" alt="image" src="https://github.com/user-attachments/assets/5123d08f-4817-48e4-a016-497a68dc2275" />

### 2) Create Ride (Synchronous)

- Synchronous ride booking
- Sequential execution

#### URL

    POST http://localhost:8080/api/v1/rides

#### Request Body

``` json
{
  "userId": 101,
  "pickupLatitude": 18.5204,
  "pickupLongitude": 73.8567
}
```

#### Response (201 Created)

``` json
{
  "rideId": 1,
  "driverId": 1,
  "fare": 146.18855,
  "status": "CONFIRMED"
}
```
#### Updated DB
<img width="2314" height="480" alt="image" src="https://github.com/user-attachments/assets/c542b2b0-fa80-4512-b367-7c29ea0d8be8" />

### 3) Create Ride (Asynchronous)

- Asynchronous ride booking
- Parallel execution using CompletableFuture
- Improved throughput

#### URL

    POST http://localhost:8080/api/v1/rides/async

#### Request Body

``` json
{
  "userId": 102,
  "pickupLatitude": 19.076,
  "pickupLongitude": 72.8777
}
```

#### Response (201 Created)

``` json
{
  "rideId": 2,
  "driverId": 2,
  "fare": 145.97685,
  "status": "CONFIRMED"
}
```

#### Updated DB
<img width="2310" height="628" alt="image" src="https://github.com/user-attachments/assets/c4fa6901-a1b2-421a-9532-bc8d9676b3a5" />

---

# Summary

This example demonstrates a scalable blocking Spring MVC architecture built with modern Java.

### Core Components

- Spring Web (Blocking MVC)
- Virtual Threads (Java 21)
- CompletableFuture for parallel execution
- Optimized HikariCP connection pooling
- JPA (Hibernate)
- Clean layered architecture (Controller → Service → Repository)
- DTOs using Records
- Stream API and Optional
- Global exception handling
- Versioned APIs (/api/v1)

### Outcome

- High concurrency using Virtual Threads
- Parallel task execution without reactive rewrite
- Production-ready scalable design

Modern Java + Proper architecture = Scalable blocking microservices.

---

🙋🏻‍♂️ Give me the right opportunity, and I’ll independently design, develop, and deploy a highly scalable ride-booking platform like Uber or Ola from scratch.

---
## License

Free software, by [Siraj Chaudhary](https://www.linkedin.com/in/sirajchaudhary/)
