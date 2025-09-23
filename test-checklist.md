# Plano de Testes — Todo-platform

_Marquei com [x] o que já existe no repositório. Os demais ficam como próximos passos._


## task-service

- [x] **TaskAppService (unit)**
  - [x] create: defaults, evento TaskCreated
  - [x] update: happy path, not found, evento TaskUpdated
  - [x] patch: parcial mantém campos não enviados
- [x] **TaskRestController (web)**
  - [x] POST /tasks 201 + validação Bean Validation (título blank/limite)
  - [x] PUT /tasks/{id} 200 (válido)
  - [x] PATCH /tasks/{id} 200 (parcial)
  - [ ] AuthZ por projectId (se aplicável)
- [x] **TaskMapper (unit)**
  - [x] request→entity (null-safety; defaults)
  - [x] entity→response (conversão Instant→OffsetDateTime; status enum→string)
- [x] **TaskRepositoryJpaAdapter (unit)**
  - [x] save delega ao SpringData
  - [x] findById delega e propaga Optional
- [x] **TaskEventProducer (unit)**
  - [x] publishCreated envia para tópico com key=taskId
  - [x] publishUpdated envia para tópico com key=taskId

## activity-service

- [x] **TaskEventListener (unit)**
  - [x] TaskCreated → mapeia e salva Activity
  - [x] TaskUpdated → mapeia e salva Activity
  - [ ] Ignora mensagens inválidas/corpo inesperado
- [ ] **ActivityRepositoryJpaAdapter (unit)**
  - [ ] save delega
  - [ ] consultas específicas (se existirem)
- [ ] **ActivityController (web)**
  - [ ] GET endpoints respondem 200 e shape esperado
  - [ ] Paginação/ordenação (se existir)

## api-gateway

- [ ] **TaskController (web)**
  - [ ] Rotas proxy OK 200/201
  - [ ] Propagação do Correlation-Id
  - [ ] AuthZ por projectId (se aplicável)
- [ ] **ActivityController (web)**
  - [ ] Rotas proxy OK 200
- [x] **FeignExceptionHandler (unit)**
  - [x] Corpo ApiError é repassado
  - [x] Sem corpo ApiError → upstream_error
- [x] **WebSocketHandshakeInterceptor (unit)**
  - [x] Permite quando JWT tem acesso ao projectId
  - [x] Nega quando não tem
  - [x] Extrai projectId da query string
- [ ] **WebSocketConfig (unit)**
  - [ ] setAllowedOrigins usa app.websocket.allowed-origins
- [x] **SecurityConfig (web)**
  - [x] Headers de segurança presentes (CSP, X-Frame-Options, X-Content-Type-Options, Referrer-Policy)
  - [x] CORS responde conforme allowed-origins
  - [ ] Resource Server JWT protege endpoints

## common / common-spring

- [x] **CorrelationFilter (unit)**
  - [x] Sem header → gera UUID e limpa MDC
  - [x] Com header → usa valor e limpa MDC
- [ ] **RequestLoggingInterceptor (unit)**
  - [ ] preHandle define início; afterCompletion loga com status e CID
  - [ ] Log de erro quando Exception presente
- [x] **GlobalExceptionHandler (web)**
  - [x] Bean Validation (400) → ApiError 'validation_error'
  - [x] ConstraintViolation (400) → ApiError 'invalid_request'
  - [x] HttpMessageNotReadable (400)
  - [x] MethodArgumentTypeMismatch (400)
  - [x] DataIntegrityViolation (409)
  - [ ] CallNotPermitted (503)
  - [x] Timeout (504)