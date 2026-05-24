# AutoRABIT Pipeline Monitor 🚀

A **production-ready, real-time CI/CD Pipeline Monitoring System** built with Java Spring Boot — designed to mirror the core capabilities of AutoRABIT's DevOps platform.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   CLIENT (Browser)                       │
│  REST API calls ──► HTTP/JSON                           │
│  Live Logs      ──► WebSocket (STOMP/SockJS)            │
│  Live Metrics   ──► Server-Sent Events (SSE)            │
└──────────────┬──────────────────────────────────────────┘
               │
┌──────────────▼──────────────────────────────────────────┐
│              Spring Boot 3.x Application                 │
│                                                          │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────┐  │
│  │ REST API     │  │ WebSocket    │  │ SSE Stream    │  │
│  │ (JWT Auth)   │  │ (STOMP)      │  │ (Metrics)     │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬────────┘  │
│         │                 │                  │           │
│  ┌──────▼─────────────────▼──────────────────▼────────┐  │
│  │           Service Layer                            │  │
│  │  PipelineExecutionService (Async Thread Pool)      │  │
│  │  MetricsService (Scheduled SSE Push)               │  │
│  │  WebSocketNotificationService                      │  │
│  └──────────────────────────────────────────────────┘  │
│                          │                              │
│  ┌───────────────────────▼──────────────────────────┐  │
│  │           Data Layer (Spring Data JPA)           │  │
│  └──────────────────────────────────────────────────┘  │
└──────────────────────┬──────────────────────────────────┘
                       │
          ┌────────────┴────────────┐
          │                         │
   ┌──────▼──────┐         ┌────────▼────────┐
   │  H2 (Dev)   │         │  PostgreSQL      │
   │  In-Memory  │         │  (Production)    │
   └─────────────┘         └─────────────────┘
```

---

## ✨ Features

| Feature | Technology |
|---------|-----------|
| 🔐 JWT Authentication | Spring Security + JJWT |
| 🔴 Real-time log streaming | WebSocket (STOMP/SockJS) |
| 📊 Live metrics dashboard | Server-Sent Events (SSE) |
| ⚡ Async pipeline engine | Spring @Async + ThreadPool |
| 🗄️ Database | H2 (dev) / PostgreSQL (prod) |
| 🐳 Containerized | Docker + Docker Compose |
| 📈 Observability | Spring Actuator + Micrometer |
| 🔔 Notifications | WebSocket broadcast |

---

## 🚀 Quick Start (Dev — No Docker needed)

```bash
cd springboot
mvn spring-boot:run
```

The app starts with **H2 in-memory database** automatically.

**Console output will show:**
```
✅ Demo data seeded successfully
=================================================
  AutoRABIT Pipeline Monitor is READY!
  URL        : http://localhost:8080
  H2 Console : http://localhost:8080/h2-console
  Health     : http://localhost:8080/actuator/health
  WebSocket  : ws://localhost:8080/ws
-------------------------------------------------
  Admin login: admin / admin123
  Dev login  : devuser / dev123
  Viewer     : viewer / view123
=================================================
```

---

## 🐳 Production (Docker Compose)

```bash
docker-compose up -d
```

---

## 📡 API Reference

### Authentication

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Use token in subsequent requests
TOKEN="<jwt-token-from-login>"
```

### Pipelines

```bash
# List all pipelines
curl http://localhost:8080/api/pipelines \
  -H "Authorization: Bearer $TOKEN"

# Get pipeline by ID
curl http://localhost:8080/api/pipelines/1 \
  -H "Authorization: Bearer $TOKEN"

# Create pipeline
curl -X POST http://localhost:8080/api/pipelines \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Deploy Pipeline",
    "description": "Custom pipeline",
    "type": "SALESFORCE_DEPLOY",
    "repository": "https://github.com/myorg/myrepo",
    "branchName": "main"
  }'
```

### Trigger & Monitor

```bash
# Trigger pipeline execution
curl -X POST http://localhost:8080/api/pipelines/1/trigger \
  -H "Authorization: Bearer $TOKEN"

# Get execution details
curl http://localhost:8080/api/executions/1 \
  -H "Authorization: Bearer $TOKEN"

# Get execution logs
curl http://localhost:8080/api/executions/1/logs \
  -H "Authorization: Bearer $TOKEN"

# Cancel execution
curl -X POST http://localhost:8080/api/executions/1/cancel \
  -H "Authorization: Bearer $TOKEN"

# Recent executions
curl http://localhost:8080/api/executions/recent?limit=10 \
  -H "Authorization: Bearer $TOKEN"
```

### Metrics (SSE)

```bash
# Snapshot metrics
curl http://localhost:8080/api/metrics \
  -H "Authorization: Bearer $TOKEN"

# Subscribe to live metrics stream (SSE)
curl -N http://localhost:8080/api/metrics/stream
```

---

## 🔌 WebSocket (STOMP)

Connect with SockJS + STOMP client:

```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  // Subscribe to live logs for execution #1
  stompClient.subscribe('/topic/execution/1/logs', (msg) => {
    const log = JSON.parse(msg.body);
    console.log(`[${log.level}] ${log.stepName}: ${log.message}`);
  });

  // Subscribe to execution status changes
  stompClient.subscribe('/topic/execution/1/status', (msg) => {
    console.log('Status:', msg.body);
  });

  // Subscribe to global notifications
  stompClient.subscribe('/topic/notifications', (msg) => {
    const notification = JSON.parse(msg.body);
    alert(notification.message);
  });

  // Subscribe to metrics refresh signal
  stompClient.subscribe('/topic/metrics/refresh', () => {
    fetchLatestMetrics();
  });

  // Heartbeat ping
  stompClient.send('/app/ping', {}, JSON.stringify({}));
});
```

---

## 🏢 AutoRABIT Alignment

This project mirrors key AutoRABIT platform capabilities:

| AutoRABIT Feature | This Project |
|-------------------|-------------|
| ARM (AutoRABIT Release Manager) | `RELEASE_MANAGER` pipeline type |
| Salesforce Metadata Deployment | `SALESFORCE_DEPLOY` pipeline type |
| nCino Backup & Recovery | `METADATA_BACKUP` pipeline type |
| CodeScan Static Analysis | `CODE_QUALITY_SCAN` pipeline type |
| Data Loader Pro | `DATA_MIGRATION` pipeline type |
| Real-time deployment logs | WebSocket log streaming |
| Pipeline status dashboard | SSE metrics + REST API |

---

## 🗂️ Project Structure

```
src/main/java/com/autorabit/pipeline/
├── PipelineMonitorApplication.java
├── config/
│   ├── SecurityConfig.java       ← JWT + Spring Security
│   ├── WebSocketConfig.java      ← STOMP broker config
│   ├── AsyncConfig.java          ← Thread pools
│   └── DataInitializer.java      ← Demo data seeding
├── controller/
│   ├── AuthController.java       ← POST /api/auth/login
│   ├── PipelineController.java   ← CRUD /api/pipelines
│   ├── ExecutionController.java  ← Trigger + monitor
│   ├── MetricsController.java    ← REST + SSE
│   └── WebSocketController.java  ← STOMP handlers
├── service/
│   ├── PipelineExecutionService.java  ← ⭐ Core async engine
│   ├── MetricsService.java            ← SSE push + metrics
│   ├── WebSocketNotificationService.java
│   ├── PipelineService.java
│   └── AuthService.java
├── model/
│   ├── Pipeline.java
│   ├── PipelineExecution.java
│   ├── PipelineStep.java
│   ├── StepLog.java
│   └── User.java
├── repository/        ← Spring Data JPA
├── dto/               ← Request/Response objects
├── security/          ← JWT + UserDetails
└── exception/         ← Global error handling
```

---

## 🔑 Default Credentials

| Username | Password | Roles |
|----------|----------|-------|
| `admin`   | `admin123` | ADMIN, DEVELOPER, VIEWER |
| `devuser` | `dev123`   | DEVELOPER, VIEWER |
| `viewer`  | `view123`  | VIEWER |

---

## 🛠️ Tech Stack

- **Java 17** + **Spring Boot 3.2**
- **Spring Security** (JWT / BCrypt)
- **Spring WebSocket** (STOMP + SockJS)
- **Server-Sent Events** (SSE)
- **Spring Data JPA** (H2 / PostgreSQL)
- **Spring @Async** (ThreadPoolTaskExecutor)
- **Spring Actuator** (Health + Metrics)
- **Docker** + **Docker Compose**
- **Lombok** (boilerplate reduction)
