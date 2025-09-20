# Testing Checklist (com progresso)

## Regra de uso
Cada tarefa concluída deve ser marcada com `[x]` (um X).  
Sempre que uma nova tarefa for finalizada, atualize este checklist.

---

## task-service
### Unit
- [x] **TaskAppService.create**: persiste, publica **TaskCreated** (verifica conteúdo do evento e correlação).
- [x] **TaskAppService.update**: atualiza campos, publica **TaskUpdated** (mantém imutáveis).
- [x] **TaskAppService.patch**: atualização parcial; ignora `null` e valida campos.
- [x] **TaskRepositoryJpaAdapter**: delega corretamente para `SpringDataTaskRepository` (save/find).
- [ ] **Mapper(s)**: request→entity e entity→response (null-safety; default values).
- [ ] **Validações de domínio**: título obrigatório, limites de tamanho, status permitido.

### Web (MockMvc / @WebMvcTest)
- [x] **POST /tasks** retorna **201** com Location/response.
- [x] **PUT /tasks/{id}** retorna **200** (conteúdo atualizado).
- [x] **PATCH /tasks/{id}** retorna **200** (somente campos presentes).
- [x] **Bean Validation**: título em branco, acima do limite → **400** com body de erro.
- [ ] **Headers obrigatórios**: `Correlation-Id` aceito/gerado; refletido na resposta/log.
- [ ] **Content-Type**: rejeita `text/plain` em endpoints JSON → **415**.

### JPA Slice (@DataJpaTest)
- [ ] **SpringDataTaskRepository**: `save`, `findById`, consultas por status/projeto (se existirem).
- [ ] **Constraints**: título `@Column(length=...)` respeitado (violação → exception).
- [ ] **Auditoria** (se houver): createdAt/updatedAt persistem automaticamente.

### Integration (@SpringBootTest + @AutoConfigureMockMvc + H2/Testcontainers)
- [ ] **Fluxo POST** persiste no H2 e retorna `TaskResponse` consistente.
- [ ] **PUT inexistente** retorna **404** com erro padronizado.
- [ ] **Idempotência** (se aplicável): repetição de mesma operação não duplica efeitos.
- [ ] **Kafka** (se houver publisher): evento enviado a tópico (usar `EmbeddedKafka`/Spy).

---

## activity-service
### Unit / Web / JPA / Integration
- [ ] **Service**: lógica principal (espelho do Task).
- [ ] **Controller**: retornos e validações análogos.
- [ ] **Repository**: save/find e constraints.
- [ ] **End-to-end**: criar e atualizar Activity com H2.

---

## api-gateway
### Web
- [ ] **TaskController** delega a **TaskClient** (mocks do client, timeout/erros).
- [ ] **ActivityController** delega a **ActivityClient**.

### Security
- [ ] **CORS/CSRF** conforme config.
- [ ] **WebSocket**: bloqueia origens não permitidas; handshake propaga `Correlation-Id`.
- [ ] **Auth** (se houver): rotas protegidas exigem credenciais válidas (401/403 corretos).

---

## common
### DTOs/Contracts
- [ ] **CreateTaskRequest**: validações (não nulo, tamanho).
- [ ] **UpdateTaskRequest**: validações e semântica (status válido).
- [ ] **TaskResponse**: mapeamento completo e estável.
- [ ] **Enum TaskStatus**: serialização/deserialização JSON robusta (case sensitivity).

---

## common-spring
### Infra
- [ ] **GlobalExceptionHandler**: mapeia IllegalArgumentException→**400**, NotFound→**404**, validação→**422/400** (conforme padrão).
- [ ] **RequestLoggingInterceptor**: registra/propaga headers (mascara sensíveis).
- [ ] **CorrelationFilter**: gera `Correlation-Id` se ausente e injeta no MDC/logs.

---

# Próximos testes sugeridos

1. **Destravar o ambiente de teste**: adicionar `spring-boot-starter-test`, migrar `@MockBean`→`@MockitoBean`.
2. **Cobrir happy paths Web do Task**: POST 201, PUT 200, PATCH 200.
3. **Validações e erros**: título vazio/limite → 400; Content-Type inválido → 415; PUT/PATCH inexistente → 404.
4. **Integração com H2**: POST persiste, PUT atualiza, PATCH parcial; Kafka se existir.
5. **JPA constraints**: save/find; violação de tamanho; auditoria createdAt/updatedAt.
6. **Observabilidade**: Correlation-Id gerado/propagado; logging MDC.
7. **Contratos**: DTOs serializam corretamente; enums case-insensitive.
8. **Resiliência gateway**: client falha → 502/504.
9. **Security e CORS**: OPTIONS preflight, tokens válidos/401/403.
10. **Edge cases**: PATCH vazio; PUT id conflitante; campos whitespace-only.
