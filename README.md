## Order Management (DDD) with H2, WebClient, and Resilience4j Circuit Breaker

This project is a Spring Boot Order Management service following a simple DDD layering approach. An order is only persisted after a successful authorization call to a Payment service. External calls use WebClient and are protected with a Resilience4j Circuit Breaker.

Features:
- DDD-ish layers: domain, application, infrastructure, interfaces
- H2 in-memory database with JPA
- REST API for orders
- External Payment API call via WebClient
- Resilience4j Circuit Breaker on payment call
- Tests: Mockito unit tests and MockMvc + WireMock integration tests

---

### How to run

Requirements:
- Java 21
- Maven 3.9+

Build and run:
```bash
mvn -DskipTests package
java -jar target/circuit-breaker-0.0.1-SNAPSHOT.jar
```

Default ports:
- App: 8085
- H2 Console: /h2-console (JDBC: `jdbc:h2:mem:orders`, user: `sa`, no password)

Note: The Payment service is assumed at `http://localhost:8081`. In tests we mock it with WireMock, you don’t need a real payment service to run the app, but submit will fail if 8081 isn’t serving 2xx.

---

### API Endpoints

Orders:
- POST `/api/orders` → body: `{ "customerEmail": "user@example.com", "amount": 120.50 }`
  - On 2xx from payment → 201 Created + Order(SUBMITTED)
  - On non-2xx or circuit open → 502 Bad Gateway
- GET `/api/orders` → list all orders
- GET `/api/orders/{id}` → get single order

H2 Console:
- GET `/h2-console` → use JDBC `jdbc:h2:mem:orders`, user `sa`

---

### Configuration

`src/main/resources/application.yml` (key parts):
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:orders;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false

resilience4j:
  circuitbreaker:
    instances:
      payment:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
```

Circuit breaker instance name: `payment`.

---

### DDD Structure (high-level)

```
src/main/java/org/example/
  domain/           # Aggregates, repository ports
    order/
      Order.java
      OrderStatus.java
      OrderRepository.java
  application/      # Use cases
    order/OrderService.java
  infrastructure/   # Adapters
    http/PaymentClient.java     # WebClient + Resilience4j
    persistence/order/
      OrderEntity.java
      OrderJpaRepository.java
      OrderRepositoryAdapter.java
      OrderPersistenceMapper.java
  interfaces/       # Inbound adapters (controllers)
    order/OrderController.java
    error/GlobalExceptionHandler.java
```

Flow when submitting an order:
1) `OrderController` → 2) `OrderService.submit` → 3) `PaymentClient.authorize` (CB protected)
→ if authorized → 4) persist via `OrderRepository` adapter.

---

### Tests

Run all tests:
```bash
mvn test
```

Types:
- Unit: `OrderServiceTest` with Mockito (stubs `PaymentClient` and `OrderRepository`).
- Integration: `OrderControllerIT` with MockMvc + WireMock. We stub the payment endpoint on port 8081 to return 2xx (success) or 5xx (failure) and assert behavior including circuit breaker impact.

---

### Notes on Resilience4j usage

- `PaymentClient.authorize` is annotated with `@CircuitBreaker(name = "payment", fallbackMethod = "authorizeFallback")`.
- Non-2xx responses throw an error; the fallback returns `false` so the service handles it gracefully.
- A simple global exception handler maps failures to HTTP 502 to reflect upstream failure.

---

### Customize

- Change payment base URL in `PaymentClient` if needed (e.g., from env or config property).
- Tune circuit breaker thresholds in `application.yml` per your needs (window size, thresholds, wait durations).
