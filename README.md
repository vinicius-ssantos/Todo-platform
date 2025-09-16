# 📝 Todo Platform — Microservices + WebSocket + Virtual Threads

Projeto de estudo que implementa uma **plataforma de To-Do List distribuída**, usando:

- **Java 21 + Spring Boot 3**
- **Virtual Threads** (`server.virtual-threads.enabled=true`)
- **Maven (multi-módulo)**
- **Feign** (HTTP sync entre serviços)
- **Kafka** (eventos assíncronos)
- **WebSocket** (notificações em tempo real)
- **Postgres** (persistência)

---

## 📂 Estrutura dos módulos

```
todo-platform/
 ├─ common/          # DTOs, eventos, config Feign
 ├─ api-gateway/     # REST + WebSocket + Feign + consumidor Kafka
 ├─ task-service/    # CRUD de Task + produtor Kafka
 ├─ activity-service # Feed de atividades (consome eventos)
 ├─ docker-compose.yml
 └─ pom.xml (parent)
```

---

## 🚀 Como rodar

### 1. Subir infraestrutura
```bash
docker compose up -d
```

### 2. Compilar tudo
```bash
mvn clean package -DskipTests
```

### 3. Rodar serviços (cada um em um terminal)
```bash
mvn -pl task-service spring-boot:run
mvn -pl activity-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

---

## ⚡ Testando

### Criar uma task
```bash
curl -X POST http://localhost:8080/tasks   -H "Content-Type: application/json"   -d '{
        "projectId":"p1",
        "title":"Primeira task",
        "description":"demo",
        "labels":["study","java"]
      }'
```

### Assinar eventos via WebSocket
No browser console:
```js
const ws = new WebSocket("ws://localhost:8080/ws");
ws.onmessage = (e)=>console.log("WS:", e.data);
ws.onopen = ()=> ws.send(JSON.stringify({type:"subscribe", projectId:"p1"}));
```

Você deve receber `task:created` ou `task:updated` em tempo real.

### Atualizar uma task
```bash
curl -X PUT http://localhost:8080/tasks/{TASK_ID}   -H "Content-Type: application/json"   -d '{"title":"Atualizada","status":"DOING"}'
```

---

## 🔌 Endpoints principais

### API Gateway (`localhost:8080`)
- `POST /tasks` → cria uma task (chama `task-service`)
- `PUT /tasks/{id}` → atualiza task
- `GET /tasks/{id}` → busca task
- `GET /activities/project/{projectId}` → feed de atividades
- `WS /ws` → canal em tempo real (`subscribe` por `projectId`)

### Task Service (`localhost:8081`)
- CRUD de tasks
- Publica eventos `task.created` e `task.updated` (Kafka)

### Activity Service (`localhost:8082`)
- Consome eventos de task
- Armazena feed de atividades
- Expõe `GET /activities/project/{projectId}`

---

## 📡 Arquitetura (simplificada)

```
[ Client ] ⇄ REST ⇄ [ API Gateway ]
     ↑                   │
   WebSocket ◄───────────┘
     │
 [ Kafka ] ⇄ [ Task Service ] ⇄ [ Activity Service ]
     │
  [ Postgres ]
```

- Cliente fala com o **Gateway** (REST + WS).
- **Gateway** chama `task-service` via Feign.
- `task-service` persiste e publica eventos no **Kafka**.
- `activity-service` consome eventos e grava feed.
- `gateway` consome os mesmos eventos e transmite para clientes WS.

---

## 🧠 Próximos passos

- [ ] Implementar **Outbox Pattern** no `task-service` para garantir entrega dos eventos.
- [ ] Adicionar **Resilience4j** aos clientes Feign.
- [ ] Melhorar **fan-out WS** (debounce/coalescência).
- [ ] Adicionar **search-service** (indexação de tarefas).
- [ ] Escrever **testes de contrato** (Feign) e **testes de carga WS** (K6/Gatling).

---

## 👤 Autor

Vinicius Oliveira Santos — projeto de estudo para aplicar:
- **arquitetura modular com Spring Boot**
- **comunicação síncrona (Feign) e assíncrona (Kafka)**
- **tempo real via WebSocket**
- **Virtual Threads (Java 21)**
