# 🔔 Notification Hub

A full-stack notification scheduling and delivery system built with **Spring Boot**, **React**, **MySQL**, and **RabbitMQ** — fully containerized with Docker.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [API Reference](#api-reference)
- [Business Logic](#business-logic)
- [Getting Started](#getting-started)
  - [Method 1 — Docker Compose (Recommended)](#method-1--docker-compose-recommended)
  - [Method 2 — Pull Code & Run Directly](#method-2--pull-code--run-directly)
  - [Method 3 — Build JAR & Run](#method-3--build-jar--run)
  - [Method 4 — Run Frontend & Backend Separately](#method-4--run-frontend--backend-separately)
- [Environment Variables](#environment-variables)
- [Frontend Overview](#frontend-overview)
- [Scheduler](#scheduler)
- [RabbitMQ Integration](#rabbitmq-integration)
- [Accessing Services](#accessing-services)

---

## Overview

Notification Hub is an open-source notification scheduling system that allows you to:

- Schedule notifications of type **EMAIL**, **SMS**, or **PUSH** for any user
- Automatically process and deliver pending notifications every second via a built-in scheduler
- Retry failed notifications with configurable backoff logic
- Prevent duplicate notifications per user
- Monitor delivery stats from a real-time dashboard with charts and filters

The React frontend is compiled and bundled inside the Spring Boot static resources folder, so a single Java process serves both the API and the UI.

---

## Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| Java 17 + Spring Boot | Core application framework |
| Spring Scheduler | Periodic notification dispatch (every second) |
| Spring Data JPA | ORM and database access |
| Spring AMQP | RabbitMQ producer/consumer integration |
| MySQL 8.0 | Persistent relational storage |
| RabbitMQ 3 | Message broker for async notification delivery |
| Lombok `@Slf4j` | Structured logging throughout the application |
| Docker + Docker Compose | Containerization and orchestration |

### Frontend
| Technology | Purpose |
|---|---|
| React + Vite | UI framework and build tool |
| Redux Toolkit | Global state management |
| React Router DOM (HashRouter) | Client-side routing (served as static files) |
| PrimeReact | UI component library (Cards, Charts, DataTable, Toast, etc.) |
| Axios | HTTP client for API communication |

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Docker Network                    │
│                                                     │
│  ┌──────────────────┐     ┌────────────────────┐   │
│  │   notification-  │────▶│     mysql:3306      │   │
│  │   service:8080   │     │  notification_db    │   │
│  │                  │     └────────────────────┘   │
│  │  Spring Boot App │     ┌────────────────────┐   │
│  │  + React (dist)  │────▶│   rabbitmq:5672    │   │
│  │                  │     │  mgmt UI: 15672    │   │
│  └──────────────────┘     └────────────────────┘   │
│           │                                         │
└───────────┼─────────────────────────────────────────┘
            │
     ┌──────▼──────┐
     │   Browser   │
     │  :8080/     │
     └─────────────┘
```

**Flow:**
1. Client creates a notification via REST API → saved to MySQL with `PENDING` status
2. Scheduler runs every second → picks up `PENDING` notifications whose `scheduleTime` has passed
3. Notifications are published to a **RabbitMQ queue**
4. Consumer picks from the queue → checks divisibility logic → marks `SENT` or `FAILED`
5. Failed notifications are retried automatically based on retry logic

---

## Project Structure

```
notification-hub/
├── src/
│   └── main/
│       ├── java/com/example/notif/
│       │   ├── config/             # RabbitMQ, ModelMapper Configuration
│       │   ├── controller/         # REST Controllers
│       │   ├── dto/                # Request/Response DTOs with validation
│       │   ├── entity/             # JPA Entities
│       │   ├── enums/              # NotificationType, NotificationStatus
│       │   ├── repository/         # Spring Data JPA Repositories
│       │   ├── scheduler/          # @Scheduled notification dispatcher
│       │   └── service/            # Business logic layer
│       └── resources/
│           ├── static/             # React dist build (served by Spring Boot)
│           └── application.properties
├── frontend/                       # React source code
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   │   ├── Dashboard.jsx       # Stats cards + pie charts
│   │   │   └── Notifications.jsx   # DataTable with filters
│   │   ├── store/                  # Redux slices
│   │   └── main.jsx
│   └── vite.config.js
├── Dockerfile
├── docker-compose.yml
└── README.md
```

---

## Database Schema

### `notifications`

| Column | Type | Constraints |
|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| `user_id` | BIGINT | NOT NULL |
| `type` | ENUM(`EMAIL`, `SMS`, `PUSH`) | NOT NULL |
| `message` | VARCHAR(200) | NOT NULL |
| `schedule_time` | DATETIME | NOT NULL |
| `status` | ENUM(`PENDING`, `SENT`, `FAILED`, `RETRY`) | NOT NULL |
| `total_retries` | INT | DEFAULT 0 |
| `last_retried_at` | DATETIME | NULLABLE |
| `sent_at` | DATETIME | NULLABLE |
| `created_at` | DATETIME | Auto-set on create |
| `updated_at` | DATETIME | Auto-set on update |

### `notification_retries`

| Column | Type | Constraints |
|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| `notification_id` | BIGINT | FK → `notifications(id)` |
| `retried_at` | DATETIME | NOT NULL |
| `status` | ENUM(`PENDING`, `SENT`, `FAILED`, `RETRY`) | NOT NULL |

> **Note:** `@Version` is used on the `Notification` entity to handle optimistic locking between the Scheduler thread and concurrent API requests.

---

## API Reference

### Base URL
```
http://localhost:8080/api
```

---

### 1. Create Notification
**`POST /api/notifications`**

**Request Body:**
```json
{
  "userId": 101,
  "type": "EMAIL",
  "message": "Welcome User",
  "scheduleTime": "2026-05-28T10:00:00"
}
```

**Validations:**
- `userId` — required, must be positive
- `type` — required, must be one of `EMAIL`, `SMS`, `PUSH`
- `message` — required, max 200 characters
- `scheduleTime` — required, must be a future date-time

**Success Response `201`:**
```json
{
  "notificationId": 1,
  "status": true
}
```

**Error Response `500`:**
```json
{
  "message": "Failed to schedule notification",
  "status": false
}
```

---

### 2. Get All Notifications
**`GET /api/notifications?page=0&size=10&status=FAILED&type=EMAIL`**

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `page` | int | No | Page number (default: 0) |
| `size` | int | No | Page size (default: 10) |
| `status` | String | No | Filter by status |
| `type` | String | No | Filter by type |

**Success Response `200`:**
```json
{
  "notificationId": 1,
  "userId": 101,
  "type": "EMAIL",
  "message": "Welcome User",
  "scheduleTime": "2026-05-28T10:00:00",
  "status": true
}
```

---

### 3. Retry Notification
**`POST /api/notifications/{id}/retry`**

Manually triggers a retry for a specific failed notification.

**Success Response `200`:**
```json
{
  "notificationId": 1,
  "userId": 101,
  "type": "EMAIL",
  "message": "Welcome User",
  "scheduleTime": "2026-05-28T10:00:00",
  "status": true
}
```

**Error Response `500`:**
```json
{
  "message": "Again Failed to schedule notification",
  "status": false
}
```

---

### 4. Dashboard Stats
**`GET /api/dashboard`**

**Success Response `200`:**
```json
{
  "totalNotifications": 1000,
  "sentNotifications": 850,
  "failedNotifications": 100,
  "retryNotifications": 50,
  "typeWiseStatistics": {
    "EMAIL": {
      "totalNotifications": 500,
      "sentNotifications": 450,
      "failedNotifications": 30,
      "retryNotifications": 20
    },
    "SMS": {
      "totalNotifications": 300,
      "sentNotifications": 250,
      "failedNotifications": 40,
      "retryNotifications": 10
    },
    "PUSH": {
      "totalNotifications": 200,
      "sentNotifications": 150,
      "failedNotifications": 30,
      "retryNotifications": 20
    }
  },
  "status": true
}
```

---

## Business Logic

### Failure Simulation (~30% Random Failures)

The system simulates real-world delivery failures using a divisibility check:

```
if (randomNumber is divisible by 3)
    → Mark notification as FAILED
```

This results in approximately **30% of notifications failing** randomly, making the system realistic for testing retry and monitoring flows.

### Retry Logic

A notification is eligible for automatic retry only when **all three** conditions are true:

```
status == FAILED
  AND total_retries < 3
  AND now().isAfter(last_retried_at + 2 minutes)
```

- Maximum **3 retry attempts** per notification
- Minimum **2-minute gap** between retries
- A log entry is written to `notification_retries` for each attempt

### Duplicate Notification Prevention

Before creating a new notification, the system fetches the most recent notification for the same `userId`:

```
latestNotification = findFirstByUserIdOrderByCreatedAtDesc(userId)
```

If the latest notification exists and **all** of the following is true, creation is **rejected**:

| Condition | Description |
|---|---|
| `type` matches | Same notification type as the last one |
| `message` matches | Identical message text |
| `createdAt` within 5 minutes | Notification created less than 5 minutes ago |

Additionally, any message containing a **single word repeated 3 or more times** is rejected. This is checked in O(N) time using a `HashMap` of word → count.

---

## Getting Started

### Prerequisites

- **Docker & Docker Compose** (for Methods 1 & 2)
- **Java 17+** and **Maven** (for Methods 2, 3 & 4)
- **Node.js 18+** and **npm** (for Method 4 only)

---

### Method 1 — Docker Compose (Recommended)

The fastest way. Pulls all images and starts MySQL, RabbitMQ, and the application in one command. The React frontend is already embedded in the Spring Boot static folder.

**Step 1:** Copy the `docker-compose.yml` file to your local machine.

```yml
version: '3.8'

services:

  mysql:
    image: mysql:8.0
    container_name: mysql-db
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: notification_db
      TZ: Asia/Kolkata
    healthcheck:
      test: [ "CMD", "mysqladmin", "ping", "-h", "localhost", "-proot" ]
      interval: 10s
      timeout: 5s
      retries: 10
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  rabbitmq:
    image: rabbitmq:3-management
    container_name: rabbitmq
    restart: unless-stopped
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
      TZ: Asia/Kolkata

  app:
    build: .
    container_name: notification-service
    restart: unless-stopped
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
      rabbitmq:
        condition: service_started
    environment:
      TZ: Asia/Kolkata
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/notification_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_RABBITMQ_PORT: 5672
      SPRING_RABBITMQ_USERNAME: guest
      SPRING_RABBITMQ_PASSWORD: guest

volumes:
  mysql_data:
```

**Step 2:** Run the following command in the same directory:

```bash
docker compose up -d
```

Docker will:
1. Pull `mysql:8.0` and `rabbitmq:3-management` images
2. Build the application image from the `Dockerfile`
3. Wait for MySQL to be healthy before starting the app
4. Expose the app on port `8080`

**Step 3:** Open the app in your browser:

```
http://localhost:8080
```

> To stop all services:
> ```bash
> docker compose down
> ```
> To stop and remove all data (volumes):
> ```bash
> docker compose down -v
> ```

---

### Method 2 — Pull Code & Run Directly

Use this if you want to run without rebuilding the Docker image. The React `dist` folder is already included in `src/main/resources/static/`.

**Step 1:** Clone the repository:

```bash
git clone https://github.com/<your-username>/notification-hub.git
cd notification-hub
```

**Step 2:** Make sure MySQL and RabbitMQ are running (either locally or via Docker):

```bash
# Start only the infrastructure services
docker compose up -d mysql rabbitmq
```

**Step 3:** Run the Spring Boot application:

```bash
./mvnw spring-boot:run
```

Or with Maven installed globally:

```bash
mvn spring-boot:run
```

**Step 4:** Open the app:

```
http://localhost:8080
```

---

### Method 3 — Build JAR & Run

Best for production-like deployment without Docker for the app itself.

**Step 1:** Clone and build:

```bash
git clone https://github.com/<your-username>/notification-hub.git
cd notification-hub
mvn clean package -DskipTests
```

This generates the JAR at:
```
target/notif-0.0.1-SNAPSHOT.jar
```

**Step 2:** Start infrastructure services:

```bash
docker compose up -d mysql rabbitmq
```

**Step 3:** Run the JAR:

```bash
java -jar target/notif-0.0.1-SNAPSHOT.jar
```

**Step 4:** Open the app:

```
http://localhost:8080
```

---

### Method 4 — Run Frontend & Backend Separately

Use this for active frontend development with hot module replacement.

**Step 1:** Clone the repository:

```bash
git clone https://github.com/<your-username>/notification-hub.git
cd notification-hub
```

**Step 2:** Start infrastructure:

```bash
docker compose up -d mysql rabbitmq
```

**Step 3:** Run the backend:

```bash
mvn clean install
mvn spring-boot:run
```

Backend runs on: `http://localhost:8080`

**Step 4:** In a new terminal, run the frontend:

```bash
cd frontend
npm install
npm run dev
```

Frontend dev server runs on: `http://localhost:5173`

> **Note:** Ensure the Vite proxy is configured to forward `/api` requests to `http://localhost:8080` in `vite.config.js`.

---

## Environment Variables

These are passed automatically via `docker-compose.yml`. For local runs, they can be set in `src/main/resources/application.properties`.

| Variable | Default | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/notification_db` | MySQL connection URL |
| `SPRING_DATASOURCE_USERNAME` | `root` | MySQL username |
| `SPRING_DATASOURCE_PASSWORD` | `root` | MySQL password |
| `SPRING_RABBITMQ_HOST` | `localhost` | RabbitMQ host |
| `SPRING_RABBITMQ_PORT` | `5672` | RabbitMQ AMQP port |
| `SPRING_RABBITMQ_USERNAME` | `guest` | RabbitMQ username |
| `SPRING_RABBITMQ_PASSWORD` | `guest` | RabbitMQ password |
| `TZ` | `Asia/Kolkata` | Timezone for scheduler and timestamps |

---

## Frontend Overview

The React app is a dark-themed, single-page application with two main views:

### Dashboard Page (`/`)
- **4 Summary Cards** — Total, Sent, Failed, and Retry notification counts
- **Pie Charts** — Type-wise breakdown (EMAIL / SMS / PUSH) for each status
- **Navigation** — Button to open the Notifications listing page

### Notifications Page (`/notifications`)
- **DataTable** with pagination, sortable columns, and filters
- **Filter bar** — Filter by `status` and `type` using dropdowns
- **Retry button** — Per-row action to manually retry a failed notification
- **Toast notifications** — Success/error feedback on all user actions

> `HashRouter` is used instead of `BrowserRouter` to prevent 404 errors when React is served as static files from Spring Boot (page refresh on `/notifications` would otherwise fail with a direct path).

---

## Scheduler

### Scheduler

A `@Scheduled` task runs **every minute** and:

1. Queries all `PENDING` notifications whose `scheduleTime <= now()`
2. Publishes them in batches to the RabbitMQ queue

---

## RabbitMQ Integration

| Property | Value |
|---|---|
| Queue | `notification.queue` |
| Exchange | `notification.exchange` |
| Routing Key | `notification.routing` |
| Default User | `guest` |
| Default Password | `guest` |

The **producer** (scheduler) publishes notification IDs to the queue.  
The **consumer** (listener) receives messages, processes them, and updates status.

> A temporary diagnostic listener is included to verify RabbitMQ message delivery during development. It logs every received message to confirm the broker-consumer connection is working.

---

## Accessing Services

| Service | URL | Credentials |
|---|---|---|
| Application (UI + API) | http://localhost:8080 | — |
| RabbitMQ Management UI | http://localhost:15672 | `guest` / `guest` |
| MySQL | `localhost:3306` | `root` / `root` |

---

## Notes

- The `@Version` annotation on the `Notification` entity ensures **optimistic locking**, preventing race conditions when the Scheduler and a manual API retry update the same row simultaneously.
- The React `dist` build is committed inside `src/main/resources/static/`, so a backend-only build serves the full application without needing Node.js.
---

## License

This project is open-source and available under the [MIT License](LICENSE).
