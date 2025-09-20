                                                              
# Test Plan — Todo-platform

## Escopo por módulo
- **common**: tests de DTOs (validação) e mapeamentos simples.
- **common-spring**: tests de `GlobalExceptionHandler`, `RequestLoggingInterceptor`, `CorrelationFilter`.
- **task-service**:
  - Unit: `TaskAppService` (create/update/patch; eventos publicados; validação de dados nulos).
  - Unit: `TaskRepositoryJpaAdapter` (delegação ao Spring Data).
  - Web: `TaskRestController` via `@WebMvcTest` (201/200, validação, JSON).
  - JPA slice: `SpringDataTaskRepository` via `@DataJpaTest` (persist e findById).
  - Integration: `@SpringBootTest(webEnvironment=RANDOM_PORT)` com H2 (criar + atualizar).
- **activity-service**: (análogos aos do `task-service`).
- **api-gateway**:
  - Web: `TaskController` e `ActivityController` via `@WebMvcTest`, mockando Feign clients.
  - Security: smoke test de `WebSecurityConfig` (endereços públicos/privados).

## Dependências de teste (adicionar a cada submódulo)
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.mockito</groupId>
  <artifactId>mockito-junit-jupiter</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <scope>test</scope>
</dependency>
```
> Observação: Para tests `@DataJpaTest` e de integração sem Postgres/Kafka, use H2 e desative Kafka auto-config em testes (`spring.autoconfigure.exclude=...KafkaAutoConfiguration`).

## Ordem de implementação sugerida (ganho rápido → cobertura alta)
1. **Unit — TaskAppService**
2. **Web — TaskRestController (@WebMvcTest)** 
3. **JPA Slice — SpringDataTaskRepository (@DataJpaTest)**
4. **API Gateway — TaskController (@WebMvcTest)**
5. **Integration — Task-service end-to-end (H2)**
6. Common/common-spring utilitários e handlers
7. Repetir o padrão para `activity-service`

## Comandos úteis
- `mvn -q -Dtest=*TaskAppServiceTest test -pl task-service`
- `mvn -q -Dtest=*WebTest test -pl task-service`
- `mvn -q -Dtest=*JpaTest test -pl task-service`
- `mvn -q -Dtest=*Gateway* test -pl api-gateway`
