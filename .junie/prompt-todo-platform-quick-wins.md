# Prompt – Quick Wins (Todo-platform)

Atue como revisor técnico sênior e produza APENAS uma lista de Quick Wins (baixo esforço, alto retorno) para o projeto Todo-platform.

Contexto:
- Multi-módulo: common, api-gateway, task-service, activity-service
- Stack: Java 21, Spring Boot 3.x, Kafka, Postgres, Feign, Docker Compose
- Extras: virtual threads, MDC/Correlation-Id
- Observações: Kafka com spring.json.trusted.packages: "*" em alguns consumers; JPA ddl-auto: update; ausência de suíte de testes sólida; arquivo .env presente

Entregável esperado:
- Lista priorizada de até 15 itens, cada um contendo: Problema, Por quê (risco/impacto), Como corrigir (passo-a-passo curto, com snippet de config/código se necessário), Custo (P, M, G), Benefício (P, M, G).
- Foque em ações aplicáveis em até 1–2 dias de trabalho e que não exijam grande refatoração.

Âncoras recomendadas (não exaustivas):
- Segurança: restringir trusted.packages do Kafka, remover segredos do repo, .env.example sem credenciais, profiles separados por ambiente
- Confiabilidade: timeouts/retentativas Feign/Resilience4j com backoff, OkHttp client configurado
- Observabilidade: habilitar Actuator básico, healthchecks, logs estruturados com correlationId, métricas padrão
- Dados: substituir ddl-auto por Flyway inicial (baseline) sem quebra, adicionar índices simples (ex.: projectId)
- DevEx/CI: cache Maven no build local/CI, Jib nos serviços com template comum, healthchecks no docker-compose
- WebSocket: allowed-origins, limites de payload, ping/pong

Formato de saída (exemplo de um item):
- [Prioridade: Alta] Kafka trusted.packages muito amplo
  - Problema: consumers com spring.json.trusted.packages: "*" (api-gateway/application.yml; activity-service/application.yml)
  - Por quê: risco de desserialização insegura/RCE e acoplamento desnecessário
  - Como corrigir: definir para "com.viniss.todo.common.events"; exemplo:
    spring.kafka.consumer.properties.spring.json.trusted.packages: "com.viniss.todo.common.events"
  - Custo: P | Benefício: G

Se faltar contexto, faça até 3 perguntas breves antes de responder. Seja direto e acionável.