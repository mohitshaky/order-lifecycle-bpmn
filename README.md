# Order Lifecycle BPMN — Fulfillment Service

![Java 17](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot 3.2](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?style=flat-square&logo=spring)
![Flowable](https://img.shields.io/badge/Flowable-6.8.0-blue?style=flat-square)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.x-black?style=flat-square&logo=apache-kafka)
![MongoDB](https://img.shields.io/badge/MongoDB-7.x-green?style=flat-square&logo=mongodb)
![Gradle](https://img.shields.io/badge/Gradle-8.x-blue?style=flat-square&logo=gradle)
[![Portfolio](https://img.shields.io/badge/Portfolio-mohitshaky.github.io-blue?style=flat-square)](https://mohitshaky.github.io)

> **Enterprise-grade event-driven order orchestration — Flowable BPMN 2.0 + Apache Kafka + MongoDB**

Built from 6+ years of real-world telecom/banking order management experience. Demonstrates production patterns: async BPMN workflow, Kafka event-driven state transitions, multi-tenancy headers, correlated tracing.

---

## Overview

The Order Management Service orchestrates end-to-end order fulfillment using **Flowable BPMN 2.0** workflows driven by **Apache Kafka** events. When a customer places an order, the service:

1. Starts a Flowable process instance for the given BPMN process key
2. Flowable delegates execute business logic (validate, provision, notify)
3. Kafka events from downstream systems complete Flowable user tasks to advance the workflow
4. Order state is persisted in **MongoDB** for audit, replay, and query

---

## Architecture

```
Customer Request (REST)
        │
        ▼
 OrderController
        │  POST /order/start/{processKey}
        ▼
 OrderService ──► OrderWorkflowWrapper
                        │
                        ▼
               Flowable RuntimeService
               (starts process instance)
                        │
              ┌─────────▼──────────────────┐
              │      BPMN Process Flow      │
              │                            │
              │  [Start: Order Received]   │
              │           ↓               │
              │  [Validate Order]          │ ◄── ValidateOrderDelegate
              │           ↓               │       └─► publishes OM_VALIDATION_STATUS
              │  [UserTask: Wait Provision]│ ◄── awaits OM_PROVISIONING_STATUS (Kafka)
              │           ↓               │
              │  [Update Order Status]     │ ◄── UpdateOrderStatusDelegate
              │           ↓               │       └─► publishes OM_ORDER_STATUS
              │  [Notify Customer]         │ ◄── NotifyCustomerDelegate
              │           ↓               │       └─► publishes OM_NOTIFICATION
              │  [End: Order Fulfilled]    │
              └────────────────────────────┘
                        │
              Kafka Event Listeners
              ┌───────────────────────────────────────────────────┐
              │ OM_ORDER_STATUS      → OrderStatusEventListener   │
              │ OM_PROVISIONING_STATUS → ProvisioningEventListener│
              └───────────────────────────────────────────────────┘
                        │
              MongoDB (process_instance_details)
              - orderId → processInstanceId mapping
              - order status, timestamps, correlation data
```

---

## Tech Stack

| Technology | Version |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.5 |
| Flowable BPMN | 6.8.0 |
| Spring Kafka | 3.x |
| Spring Data MongoDB | 4.x |
| SpringDoc OpenAPI | 2.3.0 |
| Lombok | latest |
| Gradle | 8.x |
| Docker | 24+ |

---

## Prerequisites

- Java 17+
- Docker & Docker Compose (for quickstart)
- OR: MongoDB `localhost:27017` + Kafka `localhost:9092` running locally

---

## Quickstart with Docker Compose

```bash
git clone https://github.com/mohitshaky/order-lifecycle-bpmn.git
cd order-management-service
docker-compose up --build
```

Service starts at **http://localhost:8083**

> Docker Compose spins up MongoDB (port 27019), Kafka (port 29094), and the app (port 8083) — no manual setup required.

---

## Run Locally (without Docker)

### 1. Clone and build
```bash
git clone https://github.com/mohitshaky/order-lifecycle-bpmn.git
cd order-management-service
./gradlew clean build
```

### 2. Start app (requires local MongoDB + Kafka)
```bash
./gradlew bootRun
```

### 3. Custom environment
```bash
MONGO_URI=mongodb://localhost:27017/orderdb \
KAFKA_SERVERS=localhost:9092 \
SERVER_PORT=8083 \
./gradlew bootRun
```

### 4. Run tests
```bash
./gradlew test
```

---

## API Endpoints

### Base URL: `http://localhost:8083`
### Authentication: HTTP Basic — `admin` / `admin123`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/order/start/{processKey}` | Start a new order fulfillment process |
| `PATCH` | `/order/task/{taskId}/complete` | Complete a Flowable user task |
| `PATCH` | `/order/{processInstanceId}/{signalName}/signal` | Send a signal to a running process |
| `GET` | `/actuator/health` | Application health check |
| `GET` | `/swagger-ui.html` | Interactive API documentation |

### Required Headers (all endpoints)

| Header | Example | Description |
|--------|---------|-------------|
| `transactionId` | `TXN-20240501-001` | Unique transaction identifier |
| `correlationId` | `COR-XYZ-789` | Distributed tracing correlation ID |
| `sourceChannel` | `WEB` | Origin channel (WEB, MOBILE, API) |
| `tenantId` | `TENANT-001` | Multi-tenancy identifier |

---

## Example API Calls

### Start Order Process
```bash
curl -X POST http://localhost:8083/order/start/orderFulfillmentProcess \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -H "transactionId: TXN-20240501-001" \
  -H "correlationId: COR-XYZ-789" \
  -H "sourceChannel: WEB" \
  -H "tenantId: TENANT-001" \
  -d '{
    "orderId": "ORD-98765",
    "customerId": "CUST-12345",
    "tenantId": "TENANT-001",
    "orderType": "NEW",
    "productId": "PROD-BROADBAND-100",
    "serviceType": "BROADBAND",
    "additionalVariables": {
      "priority": "HIGH",
      "region": "SOUTH"
    }
  }'
```

**Response:**
```json
{
  "status": "SUCCESS",
  "message": "Order process started successfully",
  "data": {
    "orderId": "ORD-98765",
    "processInstanceId": "abc123-def456-...",
    "status": "INITIATED"
  }
}
```

### Complete a User Task (advance workflow)
```bash
curl -X PATCH http://localhost:8083/order/task/{taskId}/complete \
  -u admin:admin123 \
  -H "transactionId: TXN-20240501-001" \
  -H "correlationId: COR-XYZ-789" \
  -H "sourceChannel: API" \
  -H "tenantId: TENANT-001" \
  -H "Content-Type: application/json" \
  -d '{"provisioningResult": "SUCCESS"}'
```

### Send Signal to Process
```bash
curl -X PATCH http://localhost:8083/order/{processInstanceId}/ORDER_COMPLETE/signal \
  -u admin:admin123 \
  -H "transactionId: TXN-20240501-001" \
  -H "correlationId: COR-XYZ-789" \
  -H "sourceChannel: API" \
  -H "tenantId: TENANT-001"
```

### Health Check
```bash
curl http://localhost:8083/actuator/health
```

---

## Swagger UI

```
http://localhost:8083/swagger-ui.html
```

API Docs (JSON):
```
http://localhost:8083/api-docs
```

---

## BPMN Process: Order Fulfillment

**Process Key**: `orderFulfillmentProcess`  
**BPMN file**: `src/main/resources/processes/order-fulfillment-process.bpmn20.xml`

```
[Start: Order Received]
    → [Service Task: Validate Order]       ValidateOrderDelegate
         └─► Publishes: OM_VALIDATION_STATUS
    → [User Task: Wait for Provisioning]   (completed by ProvisioningEventListener on Kafka event)
    → [Service Task: Update Order Status]  UpdateOrderStatusDelegate
         └─► Publishes: OM_ORDER_STATUS
    → [Service Task: Notify Customer]      NotifyCustomerDelegate
         └─► Publishes: OM_NOTIFICATION
    → [End: Order Fulfilled]
```

---

## Kafka Topics

| Topic | Direction | Publisher | Subscriber | Description |
|-------|-----------|-----------|------------|-------------|
| `OM_VALIDATION_STATUS` | Outbound | `ValidateOrderDelegate` | Downstream | Order validation result |
| `OM_PROVISIONING_STATUS` | Inbound | Provisioning service | `ProvisioningEventListener` | Provisioning completion event — completes Flowable user task |
| `OM_ORDER_STATUS` | Outbound/Inbound | `UpdateOrderStatusDelegate` | `OrderStatusEventListener` | Order status update events |
| `OM_NOTIFICATION` | Outbound | `NotifyCustomerDelegate` | Notification service | Customer notification trigger |

> Kafka host (external): `localhost:29094`  
> Kafka host (container-internal): `kafka:9092`

---

## MongoDB Collections

| Collection | Description |
|------------|-------------|
| `process_instance_details` | Maps `orderId` → `processInstanceId`, stores status, timestamps, correlation data |

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `MONGO_URI` | `mongodb://localhost:27017/orderdb` | MongoDB connection string |
| `KAFKA_SERVERS` | `localhost:9092` | Kafka bootstrap servers |
| `SERVER_PORT` | `8083` | HTTP server port |

---

## Project Structure

```
order-management-service/
├── src/main/java/com/mohit/om/service/
│   ├── Application.java
│   ├── config/          # Kafka, MongoDB, Flowable, Security config
│   ├── controller/      # REST endpoints (OrderController)
│   ├── service/         # Business logic (OrderService, OrderQueryService)
│   ├── wrapper/         # Flowable RuntimeService wrapper (IOrderWorkflowWrapper)
│   ├── delegate/        # Flowable JavaDelegates (Validate, Provision, UpdateStatus, Notify)
│   ├── listener/        # Kafka @KafkaListener beans (OrderStatus, Provisioning)
│   ├── handler/         # Process event handler
│   ├── model/           # Kafka event models (OrderStatusEvent, ProvisioningEvent)
│   ├── request/         # REST request DTOs (OrderRequest)
│   ├── response/        # REST response DTOs (SuccessResponse, OrderResponse, ErrorResponse)
│   ├── exception/       # Custom exceptions + GlobalExceptionHandler
│   └── constants/       # Kafka topic names and process variable constants
├── src/main/resources/
│   ├── application.yml
│   └── processes/
│       └── order-fulfillment-process.bpmn20.xml
├── src/test/            # Unit tests (controller, service, delegate)
├── Dockerfile
├── docker-compose.yml
└── build.gradle
```

---

## Author

**Mohit** — Senior Enterprise Java Developer  
6+ years building order management, fulfillment, and provisioning systems in telecom/banking domains.

🌐 [Portfolio](https://mohitshaky.github.io) · [GitHub](https://github.com/mohitshaky) · [LinkedIn](https://linkedin.com/in/mohit-shakya-9ab944110)


![Java 17](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot 3.2](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?style=flat-square&logo=spring)
![Flowable](https://img.shields.io/badge/Flowable-6.8.0-blue?style=flat-square)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.x-black?style=flat-square&logo=apache-kafka)
![MongoDB](https://img.shields.io/badge/MongoDB-6.x-green?style=flat-square&logo=mongodb)
![Gradle](https://img.shields.io/badge/Gradle-8.x-blue?style=flat-square&logo=gradle)

> **Order Management Fulfillment Service — event-driven BPMN-based order orchestration for telecom/banking domains**

Built based on 6+ years of enterprise order management experience in telecom domain.

---

## Overview

The Order Management Service orchestrates end-to-end order fulfillment using **Flowable BPMN 2.0** workflows driven by **Apache Kafka** events. When a customer places an order, the service:

1. Starts a Flowable process instance for the given BPMN process key
2. Service task delegates execute business logic (validate, provision, notify)
3. Kafka events from downstream systems (provisioning, billing) signal Flowable user tasks to advance the workflow
4. Order state is persisted in **MongoDB** for audit and query

---

## Architecture

```
Customer Request (REST)
        │
        ▼
 OrderController
        │  POST /order/start/{processKey}
        ▼
 OrderService ──► OrderWorkflowWrapper
                        │
                        ▼
               Flowable RuntimeService
               (starts process instance)
                        │
              ┌─────────▼──────────┐
              │  BPMN Process Flow  │
              │                    │
              │  [Start]           │
              │     ↓              │
              │  [ValidateOrder]   │ ◄── ValidateOrderDelegate
              │     ↓              │       └─► publishes OM_VALIDATION_STATUS
              │  [UserTask:Wait]   │ ◄── awaits OM_PROVISIONING_STATUS Kafka event
              │     ↓              │
              │  [UpdateStatus]    │ ◄── UpdateOrderStatusDelegate
              │     ↓              │       └─► publishes OM_ORDER_STATUS
              │  [NotifyCustomer]  │ ◄── NotifyCustomerDelegate
              │     ↓              │
              │  [End]             │
              └────────────────────┘
                        │
              Kafka Listeners
              ┌───────────────────────────────────────┐
              │ OM_ORDER_STATUS      → OrderStatusEventListener    │
              │ OM_PROVISIONING_STATUS → ProvisioningEventListener │
              └───────────────────────────────────────┘
                        │
              MongoDB (process_instance_details)
              - stores process instance ID, order ID, status
```

---

## Tech Stack

| Technology | Version |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.5 |
| Flowable | 6.8.0 |
| Spring Kafka | 3.x |
| Spring Data MongoDB | 4.x |
| SpringDoc OpenAPI | 2.3.0 |
| Lombok | latest |
| Gradle | 8.x |

---

## Prerequisites

- Java 17+
- MongoDB running on `localhost:27017` (or set `MONGO_URI` env var)
- Apache Kafka running on `localhost:9092` (or set `KAFKA_SERVERS` env var)
- Gradle 8+ (or use `./gradlew`)

---

## How to Run

### 1. Clone and build
```bash
git clone <repo-url>
cd order-management-service
./gradlew clean build
```

### 2. Run with defaults (local MongoDB + Kafka)
```bash
./gradlew bootRun
```

### 3. Run with custom environment
```bash
MONGO_URI=mongodb://user:pass@mongohost:27017/orderdb \
KAFKA_SERVERS=kafkahost:9092 \
./gradlew bootRun
```

### 4. Run tests
```bash
./gradlew test
```

---

## API Endpoints

### Base URL: `http://localhost:8080`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/order/start/{processKey}` | Start a new order fulfillment process |
| PATCH | `/order/task/{taskId}/complete` | Complete a Flowable user task |
| PATCH | `/order/{processInstanceId}/{signalName}/signal` | Send a signal to a running process instance |

### Required Headers (all endpoints)
| Header | Description |
|--------|-------------|
| `transactionId` | Unique transaction identifier |
| `correlationId` | Correlation ID for distributed tracing |
| `sourceChannel` | Source channel (e.g., WEB, MOBILE, API) |
| `tenantId` | Tenant identifier for multi-tenancy |

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### API Docs (JSON)
```
http://localhost:8080/api-docs
```

---

## BPMN Process: Order Fulfillment

**Process Key**: `orderFulfillmentProcess`

**Flow**:
```
startEvent
    → serviceTask: Validate Order        (ValidateOrderDelegate)
    → userTask:    Wait for Provisioning (completed by ProvisioningEventListener via Kafka)
    → serviceTask: Update Order Status   (UpdateOrderStatusDelegate → publishes OM_ORDER_STATUS)
    → serviceTask: Notify Customer       (NotifyCustomerDelegate)
    → endEvent
```

**BPMN file location**: `src/main/resources/processes/order-fulfillment-process.bpmn20.xml`

---

## Kafka Topics

| Topic | Direction | Description |
|-------|-----------|-------------|
| `OM_ORDER_STATUS` | Outbound/Inbound | Order status events |
| `OM_PROVISIONING_STATUS` | Inbound | Provisioning completion events |
| `OM_VALIDATION_STATUS` | Outbound | Order validation events |
| `OM_NOTIFICATION` | Outbound | Customer notification events |

---

## MongoDB Collections

| Collection | Description |
|------------|-------------|
| `process_instance_details` | Maps orderId → processInstanceId, stores status, timestamps |

---

## Configuration

See `src/main/resources/application.yml` for full configuration reference.

Key properties:
- `MONGO_URI` — MongoDB connection string
- `KAFKA_SERVERS` — Kafka bootstrap servers
- `orderStatusConcurrency` — Kafka listener concurrency (default: 3)
- `provisioningConcurrency` — Provisioning listener concurrency (default: 3)

---

## Author

**Mohit** — Senior Enterprise Java Developer  
6+ years of enterprise order management experience in telecom/banking domain.
