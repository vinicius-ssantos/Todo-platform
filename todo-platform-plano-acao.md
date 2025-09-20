# Plano de Ação — Todo-platform (Checklist)
_Gerado em 2025-09-17_

> Objetivo: levar o projeto `Todo-platform` a um nível **prod-ready**, cobrindo segurança, confiabilidade, observabilidade, qualidade, performance, testes e operação.

---

## 1) Segurança **imediata**
- [ ] **Revogar/rotacionar** quaisquer tokens expostos no repositório (ex.: `.env`).
- [ ] Remover segredos do repo e adicionar entradas à `.gitignore`.
- [ ] Mover segredos para **secrets do CI** / Vault / Secrets Manager.
- [ ] Restringir **CORS** no REST: permitir apenas domínios confiáveis (produção e pré-produção).
- [ ] Restringir **origins** do WebSocket (sem `*`).
- [ ] Implementar **autenticação JWT** no `api-gateway` (OAuth2 Resource Server): validação de token e escopos.
- [ ] Implementar **autorização por `projectId`** (ex.: usuário deve ter acesso ao projeto nas rotas REST e no handshake do WS).
- [ ] Configurar **headers de segurança** (Spring Security: `X-Content-Type-Options`, `X-Frame-Options`, `Content-Security-Policy`, `Referrer-Policy`).

## 2) Confiabilidade & Resiliência
- [ ] Ativar **Circuit Breaker** (Resilience4j) para clientes Feign (CRUD de tasks).
- [ ] Padronizar **Retries** e **TimeLimiter** (timeouts) por cliente.
- [ ] Adicionar **fallbacks** explícitos (mensagens claras e/ou fila transiente).
- [ ] Configurar **Dead-Letter Topic (DLT)** para consumidores no `activity-service`.
- [ ] Definir **política de retry** com backoff exponencial (Kafka listener).
- [ ] Garantir **entrega atômica**: usar **Transactional Outbox** ou publicação de eventos dentro de **transação** (min. 1x).
- [ ] Projetar **idempotência** para `POST /tasks` via `Idempotency-Key` (gateway).

## 3) Observabilidade
- [ ] Padronizar **JSON logs** (service, env, cid, path, status, latency_ms, userId/projId quando aplicável).
- [ ] Instrumentar **tracing distribuído** com **OpenTelemetry** (export OTLP).
- [ ] Configurar **collector** (OTel Collector) + backend (Jaeger/Tempo).
- [ ] Expor **métricas Micrometer** → Prometheus; criar **dashboards Grafana**.
- [ ] Métricas essenciais: HTTP (p50/p95/p99), erros por rota, **Kafka lag** por consumer, **WS conexões ativas** por `projectId`.
- [ ] Health/Readiness: endpoints **`/actuator/health`** com checagem de DB, Kafka e dependências.

## 4) Qualidade de Domínio & Código
- [ ] Converter `status` de `Task` para **enum forte** (`TaskStatus`) com `@Enumerated(EnumType.STRING)`.
- [ ] Validar estados no DTO (`@Pattern` ou `@EnumValidator`) e nas regras de negócio.
- [ ] Padronizar **UTC** (Hibernate, Jackson) em todos os serviços.
- [ ] Introduzir **MapStruct** se a complexidade de mapeamento aumentar.
- [ ] Normalizar **labels** caso haja necessidade de filtro/relatórios (tabela dedicada + índices).

## 5) Banco de Dados & Migrações
- [ ] Introduzir **Flyway**/**Liquibase** em todos os serviços que usam DB.
- [ ] Criar **índices**: `projectId`, `status`, `createdAt` (e relacionamentos necessários).
- [ ] Revisar **pool** do Hikari: `maxPoolSize` condizente ao limite do Postgres (mesmo com Virtual Threads).
- [ ] Políticas de retenção e purga (se aplicável) para dados de atividade.

## 6) Kafka (Topologia & Segurança)
- [ ] Definir **tópicos** com particionamento/replicação adequados e **retention** por tipo de evento.
- [ ] Habilitar **TLS/SASL** entre serviços e broker(s) em ambientes não-dev.
- [ ] Configurar **ACLs** por aplicativo (produce/consume).
- [ ] Declarar **DLTs** e **retry topics** explícitos por consumidor crítico.
- [ ] Versionar **eventos** (`version`, `schema`/`contentType`) para evolução compatível.

## 7) Performance & WS
- [ ] Tratar **exceções** no broadcast WS e **remover sessões quebradas** imediatamente.
- [ ] Implementar **coalescência/debounce** de eventos por `projectId` (redução de fan-out).
- [ ] Limitar **tamanho** de mensagens e **frequência** (rate limit/backpressure).
- [ ] Testes de **carga** (K6/Gatling) para WS e HTTP (criação/atualização de tasks).

## 8) Testes
- [ ] **Unitários**: `TaskAppService` (regras de estado, criação, update, patch).
- [ ] **WebMvc**: `TaskRestController` (validações, erros padronizados).
- [ ] **Contract Tests** (Spring Cloud Contract) para `api-gateway` ↔ `task-service`.
- [ ] **Kafka**: Embedded Kafka (produtor/consumidor; DLT; retries).
- [ ] **Arquiteturais** (ArchUnit) garantindo boundaries entre módulos.
- [ ] **Integração** com DB (Testcontainers ou perfil dockerized).
- [ ] **Carga** (K6/Gatling) com metas de p95 e throughput.
- [ ] Cobertura alvo: **≥70%** nos serviços críticos.

## 9) CI/CD & Segurança de Supply Chain
- [ ] Padronizar build de imagens com **Jib** (todos os serviços).
- [ ] Publicar **SBOM** (CycloneDX) e rodar **OWASP Dependency-Check**.
- [ ] Habilitar **Dependabot** (Maven/GitHub Actions).
- [ ] **Quality Gate** com **Sonar** (coverage mínima, code smells).
- [ ] Assinar imagens (ex.: **cosign**) e verificar na implantação.
- [ ] Promover versões via **tags** e ambientes (dev → stage → prod) com gates.

## 10) Deploy & Operação
- [ ] Padronizar manifests com **Helm** ou **Kustomize** (se usar K8s).
- [ ] Definir **liveness** e **readiness** probes (Actuator).
- [ ] Parâmetros via **ConfigMaps/Secrets** (nunca em arquivos versionados).
- [ ] Estruturar **observabilidade** de produção: dashboards + alertas (SLOs/SLIs).
- [ ] Documentar **runbooks** (rotações de segredos, incidentes, DLT drains, aumento de partições).

---

## Roadmap sugerido (ordem de commits)
1. **segurança-urgente/**: remover segredos do repo, `.gitignore`, docs de rotação de tokens.
2. **auth-jwt-gateway/**: Spring Security + JWT (authn) e autorização por `projectId` (REST + WS handshake).
3. **cors-ws-lockdown/**: CORS e origins restritos; headers de segurança.
4. **resilience4j-cb/**: Circuit Breaker + retries + timeouts padronizados (Feign) com configs por cliente.
5. **kafka-dlt-retry/**: configurar DLTs, retry topics e error handler no `activity-service`.
6. **txn-outbox/**: transactional outbox no `task-service` (persistência + publisher confiável).
7. **observabilidade-core/**: OpenTelemetry, Micrometer, logs JSON, Actuator (health/readiness).
8. **db-migrations/**: Flyway/Liquibase + criação de índices.
9. **dominio-enum-status/**: `TaskStatus` forte + validações nos DTOs + migração de dados.
10. **tests-core/**: unit, WebMvc, Embedded Kafka, Contract, ArchUnit (coverage ≥70%).
11. **ws-performance/**: coalescência/debounce, limpeza de sessões, limites e testes de carga.
12. **supply-chain/**: SBOM, Dependency-Check, Dependabot, Sonar, assinatura de imagens.
13. **deploy-helm/**: charts/templates, probes, ConfigMaps/Secrets, promoção dev→stage→prod.
14. **runbooks-alertas/**: dashboards Grafana, alertas Prometheus, runbooks operacionais.

---

## Definições de pronto (DoD) por etapa
- **Config & Código**: PR com descrição, testes e exemplos de config.
- **Observabilidade**: dashboards e alertas **em produção**.
- **Segurança**: pentest básico / scanner CI sem achados críticos.
- **Confiabilidade**: DLT com monitoramento e playbook de reprocessamento.
- **Performance**: metas p95 documentadas e verificadas (teste de carga).

---

## Referências rápidas (para implementação)
- Spring Security OAuth2 Resource Server (JWT)
- Resilience4j (CB/Retry/TimeLimiter) com Spring Boot
- Spring for Apache Kafka (ErrorHandler, DLT)
- OpenTelemetry Java + Spring Boot Starter
- Micrometer + Prometheus + Grafana
- Flyway/Liquibase migrações
- Jib (container image build)

