
# Prompt – Revisão Técnica Sênior (Todo-platform)

Quero que você atue como **revisor técnico sênior** do meu projeto Java/Spring Boot chamado **Todo-platform**.

O projeto é multi-módulo (Maven) e contém:
- [ ] **common** (DTOs, eventos, Correlation-Id, exception handler, logging)
- [ ] **api-gateway** (Spring Cloud OpenFeign, WebSocket para broadcast de eventos)
- [ ] **task-service** (CRUD de tarefas, publica eventos no Kafka)
- [ ] **activity-service** (consome eventos do Kafka e registra atividades)

**Stack:** Java 21, Spring Boot 3.x, Kafka, Postgres, Feign, Docker Compose.  
**Extras:** Virtual threads habilitados, logs com Correlation-Id/MDC. Jib está configurado apenas no task-service (avaliar extensão para os demais).

**Caminhos úteis:**
- [ ] Pom raiz: `pom.xml`
- [ ] Docker Compose: `docker-compose.yml`
- [ ] Common: `common/src/main/java/com/viniss/todo/common`
- [ ] Task-service: `task-service/src/main/...`
- [ ] Activity-service: `activity-service/src/main/...`
- [ ] Api-gateway: `api-gateway/src/main/...`

**Observações já identificadas:**
- [ ] Kafka consumer com `spring.json.trusted.packages: "*"` em api-gateway e activity-service.
- [ ] JPA com `ddl-auto: update` em task-service e activity-service.
- [ ] Ausência de suíte de testes significativa.
- [ ] Arquivo `.env` na raiz (possível vazamento de segredos).

---

## 1) Arquitetura e boas práticas
- [ ] Revisar separação de responsabilidades entre módulos.
- [ ] Identificar acoplamentos desnecessários e risco de dependências circulares.
- [ ] Indicar padrões aplicáveis (DDD, hexagonal, CQRS/event sourcing).
- [ ] Exemplificar reorganização (application vs domain vs adapters).

## 2) Segurança e confiabilidade
- [ ] Corrigir Kafka `trusted.packages: "*"`, restringindo a `com.viniss.todo.common.events`.
- [ ] Validar `.env` (tokens/credenciais). Sugerir uso de env vars/GitHub Secrets.
- [ ] Hardening WebSocket: CORS/allowed-origins, autenticação mínima, heartbeat, limites de payload.
- [ ] Configuração por profiles (`dev/test/prod`) e externalização via env vars.

## 3) Resiliência e comunicação entre serviços
- [ ] Avaliar Feign + Resilience4j (CB, retry, timeout). Verificar thresholds.
- [ ] Propor OkHttp com timeouts + connection pool. Incluir snippet de Bean.
- [ ] Sugerir retry com backoff exponencial + jitter.

## 4) Modelagem de dados
- [ ] Substituir `String status` por `TaskStatus enum` em entidades.
- [ ] Sugerir índices em `projectId` e `createdAt`.
- [ ] Recomendar migrações com Flyway/Liquibase em vez de `ddl-auto`.

## 5) Observabilidade
- [ ] Melhorar logging (estrutura, correlationId, WS payload).
- [ ] Padronizar logs HTTP (entrada/saída).
- [ ] Ativar Actuator + Micrometer/OpenTelemetry (health, readiness, liveness, métricas Kafka/DB/Feign).

## 6) Testes
- [ ] Unitários para serviços/domínio.
- [ ] Integração com Testcontainers (Kafka, Postgres).
- [ ] Contratos para Feign (WireMock).
- [ ] Cenários ponta-a-ponta com profiles de teste.

## 7) CI/CD e empacotamento
- [ ] Avaliar uso de Jib em todos os serviços.
- [ ] Melhorar Docker Compose (healthchecks, depends_on, env vars).
- [ ] Pipeline GitHub Actions (build, test, security scan, publicação de imagens, cache Maven).

## 8) Evolução futura
- [ ] Documentar APIs com OpenAPI/Swagger no gateway.
- [ ] Padronizar mensagens WS/Kafka com envelopes (`version`, `occurredAt`, `correlationId`).
- [ ] Avaliar Schema Registry (Kafka) ou alternativas (RabbitMQ) com prós/contras.

---

## Entregável esperado
- [ ] Lista priorizada de melhorias, separada em:
  1. Quick wins (baixo esforço, alto retorno)
  2. Melhorias estruturais (refatorações)
  3. Evolução futura (visão sênior para produção/escala)
- [ ] Cada item deve conter:
  - Problema
  - Por quê (risco/impacto)
  - Como corrigir (com exemplo de código/config)
  - Referência a arquivos específicos
- [ ] Linguagem objetiva, acionável, sem jargão desnecessário.
- [ ] Incluir snippets prontos (`application.yml`, Beans, configs).

---

## Critérios de qualidade
- [ ] Priorizar impacto/risco real.
- [ ] Considerar ambiente Windows para comandos locais.
- [ ] Não assumir remoção de dados sem plano de migração.
- [ ] Sugestões devem ser incrementalmente aplicáveis.
- [ ] Se identificar alto risco (ex.: segredos versionados), destacar como **Ação imediata**.
