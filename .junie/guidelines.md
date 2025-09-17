# Project Guidelines – Todo-platform

## Project Overview
Todo-platform is a multi-module Java 21/Spring Boot system composed of small services that collaborate via REST (Feign) and Kafka. The goal is to manage tasks and track activity derived from task changes.

### Modules
- task-service
  - Exposes REST endpoints to create/update tasks.
  - Publishes domain events (TaskCreated, TaskUpdated, TaskStatusChanged) to Kafka.
- activity-service
  - Consumes task events from Kafka and records activity entries for auditing/history.
  - Exposes endpoints to query recorded activities.
- api-gateway
  - Edge service exposing public REST APIs and a WebSocket for task activity updates.
  - Uses Feign clients to call downstream services and resilience configuration for retries/timeouts.
- common
  - Shared DTOs, event models, and HTTP helpers (logging, correlation IDs, error handling) used by all services.

### Communication & Data Flow
- Synchronous: api-gateway -> task-service/activity-service via Feign HTTP calls.
- Asynchronous: task-service -> Kafka -> activity-service. The gateway may stream updates to clients over WebSocket.

### Repository Layout (paths)
- activity-service/ … Spring Boot service for activities
- task-service/ … Spring Boot service for tasks
- api-gateway/ … Spring Boot gateway with Feign/WebSocket
- common/ … shared code (DTOs, events, HTTP config)
- docker-compose.yml … local infra (e.g., Kafka/ZooKeeper)
- pom.xml … Maven parent aggregator

## How Junie should operate in this repo
- Build tool: Maven (multi-module). Root build: `mvn -q clean package -DskipTests` unless tests are required for a change.
- Tests: The repo currently has no meaningful automated tests beyond archetype samples. Only run tests when you or the task adds them.
- When changing Java code:
  - Keep package naming under `com.viniss.todo.*`.
  - Do not modify files under `target/` (build outputs).
  - Keep code style conventional Java; prefer small, focused changes.
- When working with infrastructure:
  - Kafka is brought up via `docker-compose.yml` for local development; editing infra is out of scope unless explicitly requested.
- OS/Paths: This workspace uses Windows paths (backslashes). Use PowerShell syntax for any shell commands.

## Quick Commands (reference)
- Build all modules (skip tests): `mvn -q clean package -DskipTests`
- Build one module: `mvn -q -pl task-service -am clean package -DskipTests`

## Contribution Notes for Junie
- Prefer minimal diffs to satisfy the issue description.
- Keep the user informed using the update_status tool and provide a final summary with submit.


## Junie prompts available
- Full technical review: .junie/prompt-todo-platform-review.md
- Quick Wins only: .junie/prompt-todo-platform-quick-wins.md

How to use: Open the chosen prompt file, copy its content, and paste it as the instruction for Junie in a new session. The prompt is tailored to this repository and references concrete files/paths.
