# Plano de Testes — Todo-platform

_Marquei com [x] o que já existe no repositório. Os demais ficam como próximos passos._


## task-service

- [x] **TaskAppService (unit)**
  - [ ] create: defaults, evento TaskCreated
  - [ ] update: happy path, not found, evento TaskUpdated
  - [ ] patch: parcial mantém campos não enviados
- [x] **TaskRestController (web)**
  - [ ] POST /tasks 201 + validação Bean Validation (título blank/limite)
  - [ ] PUT /tasks/{id} 200 (válido)
  - [ ] PATCH /tasks/{id} 200 (parcial)
  - [ ] AuthZ por projectId (se aplicável)
- [x] **TaskMapper (unit)**
  - [ ] request→entity (null-safety; defaults)
  - [ ] entity→response (conversão Instant→OffsetDateTime; status enum→string)
- [x] **TaskRepositoryJpaAdapter (unit)**
  - [ ] save delega ao SpringData
  - [ ] findById delega e propaga Optional
- [ ] **TaskEventProducer (unit)**
  - [ ] publishCreated envia para tópico com key=taskId
  - [ ] publishUpdated envia para tópico com key=taskId

## activity-service

- [ ] **TaskEventListener (unit)**
  - [ ] TaskCreated → mapeia e salva Activity
  - [ ] TaskUpdated → mapeia e salva Activity
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
- [ ] **FeignExceptionHandler (unit)**
  - [ ] Corpo ApiError é repassado
  - [ ] Sem corpo ApiError → upstream_error
- [ ] **WebSocketHandshakeInterceptor (unit)**
  - [ ] Permite quando JWT tem acesso ao projectId
  - [ ] Nega quando não tem
  - [ ] Extrai projectId da query string
- [ ] **WebSocketConfig (unit)**
  - [ ] setAllowedOrigins usa app.websocket.allowed-origins
- [ ] **SecurityConfig (web)**
  - [ ] Headers de segurança presentes (CSP, X-Frame-Options, X-Content-Type-Options, Referrer-Policy)
  - [ ] CORS responde conforme allowed-origins
  - [ ] Resource Server JWT protege endpoints

## common / common-spring

- [ ] **CorrelationFilter (unit)**
  - [ ] Sem header → gera UUID e limpa MDC
  - [ ] Com header → usa valor e limpa MDC
- [ ] **RequestLoggingInterceptor (unit)**
  - [ ] preHandle define início; afterCompletion loga com status e CID
  - [ ] Log de erro quando Exception presente
- [ ] **GlobalExceptionHandler (web)**
  - [ ] Bean Validation (400) → ApiError 'validation_error'
  - [ ] ConstraintViolation (400) → ApiError 'invalid_request'
  - [ ] HttpMessageNotReadable (400)
  - [ ] MethodArgumentTypeMismatch (400)
  - [ ] DataIntegrityViolation (409)
  - [ ] CallNotPermitted (503)
  - [ ] Timeout (504)