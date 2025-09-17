# Todo-platform – Revisão de Arquitetura (Checklist vivo)

Este documento foi reescrito em formato de checklist. INSTRUÇÃO: sempre que houver uma mudança no código ou na arquitetura referente a algum item, marque [x] quando concluído ou desmarque [ ] quando voltar a ficar pendente/em revisão. Opcionalmente, adicione uma breve nota e data ao lado do item alterado.

Última atualização: 2025-09-17 15:50

---

## 1) Separação de responsabilidades entre módulos (estado atual)

- [x] common concentra DTOs e modelos de eventos usados entre serviços (validado).
- [x] common continha classes Spring (FeignHeadersConfig, filtros/interceptors HTTP); isso acoplava o módulo a Spring MVC/Feign. Ação aplicada: movidas para o módulo opcional common-spring. (2025-09-17 15:23) Referência: seção 2.
- [x] task-service expõe REST (TaskRestController), persiste Task (JPA), publica eventos no Kafka (TaskEventProducer) e possui serviço de aplicação (TaskAppService).
- [x] activity-service consome eventos do Kafka (TaskEventListener) e expõe endpoints de consulta (ActivityController) sobre Activity.
- [x] api-gateway atua como Edge HTTP (Feign para task-service e activity-service) e WebSocket; possui consumidor Kafka (TaskEventConsumer) para retransmitir por WS.
- [x] Separação macro por serviços coerente, com pequenos pontos de acoplamento a melhorar (ver seção 2).

---

## 2) Acoplamentos desnecessários e riscos de dependências circulares

- common -> Spring acoplamento
  - [x] Problema (resolvido): existiam classes dependentes de Spring dentro de common (FeignHeadersConfig, RequestLogging*), forçando consumidores a dependerem de infraestrutura web.
  - [x] Ação aplicada: configurações Spring movidas para o módulo opcional common-spring; common permanece livre de frameworks de infraestrutura. (2025-09-17 15:23)

- task-service (aplicação -> adapter Kafka)
  - [x] Problema anterior: TaskAppService dependia diretamente de TaskEventProducer (Kafka), acoplando regra de aplicação a tecnologia.
  - [x] Ação aplicada: introduzido o porto de saída TaskEventPublisher na aplicação e o adapter Kafka passou a implementá-lo (hexagonal). Ver seção 4.

- api-gateway como consumidor Kafka
  - [ ] Observação: o gateway consumindo Kafka cria dependência direta do edge em mensageria. Avaliar um canal dedicado (ex.: activity-service publicar eventos "activity" para o gateway) para evitar acoplamento direto ao domínio de tarefas.

- Dependências circulares
  - [x] Não foram identificadas dependências circulares entre módulos Maven (common é apenas referenciado, não referencia serviços).
  - [x] Regra a manter: “common não depende de outros módulos”; serviços não devem depender entre si diretamente (via código), apenas via HTTP/Feign ou eventos.

---

## 3) Padrões aplicáveis

- DDD (Domain-Driven Design)
  - [x] Bounded Contexts distintos: task-service e activity-service.
  - [x] Linguagem ubíqua: entidade JPA de Task alinhada para usar enum de status equivalente ao DTO (common.dto.TaskStatus). (2025-09-17 15:27)

- Arquitetura Hexagonal (Ports & Adapters)
  - [x] Núcleo: Application (casos de uso), Domain (entidades + regras), Adapters (HTTP, Kafka, JPA, Feign). Prática já iniciada no task-service com porta de eventos.
  - [ ] Expandir a organização hexagonal (application/domain/adapters) para todos os serviços.

- CQRS
  - [x] Gateway já pratica leitura via activity-service e escrita via task-service.
  - [ ] Separar modelos de leitura e escrita internamente se necessário (não obrigatório agora).

- Event Sourcing
  - [x] Não necessário no estágio atual. Manter eventos de integração simples e versionados.

---

## 4) Exemplo de reorganização (incremental) – application vs domain vs adapters

Estrutura alvo a ser adotada gradualmente (começando pelo task-service):

- com.viniss.todo.task.domain
  - [x] Task, TaskRepository (regras do domínio e portas primárias, se necessário) organizados conforme domínio. (2025-09-17 15:30)
- com.viniss.todo.task.application
  - [x] Serviços de aplicação (orquestram casos de uso): TaskAppService. (2025-09-17 15:38) Pacote renomeado para com.viniss.todo.task.application.
  - [x] Ports out (dependências externas): application.port.out.TaskEventPublisher (introduzido).
- com.viniss.todo.task.adapters
  - [x] http: TaskRestController (adapter HTTP já existente em com.viniss.todo.task.http). (2025-09-17 15:30)
  - [x] kafka: TaskEventProducer implementa TaskEventPublisher.
  - [x] persistence/jpa: implementações de repositórios se for adotado padrão de porta para persistência. (2025-09-17 15:41) Implementado no task-service: porto de domínio TaskRepository (sem Spring) e adapter JPA (TaskRepositoryJpaAdapter + SpringDataTaskRepository).

Mudanças já aplicadas (exemplo prático):

- [x] Criado o porto de saída: task-service/src/main/java/com/viniss/todo/task/application/port/out/TaskEventPublisher.java
- [x] Adapter Kafka implementa a porta: task-service/src/main/java/com/viniss/todo/task/kafka/TaskEventProducer.java
- [x] TaskAppService agora depende da porta (não do Kafka): task-service/src/main/java/com/viniss/todo/task/service/TaskAppService.java (campo eventPublisher)

Isso reduz acoplamento da camada de aplicação à tecnologia de mensageria e serve de guia para outras dependências externas.

Snippet (porta):

```java
public interface TaskEventPublisher {
    void publishCreated(TaskCreated event);
    void publishUpdated(TaskUpdated event);
}
```

Uso na aplicação:

```java
@Service
public class TaskAppService {
  private final TaskRepository repository;
  private final TaskEventPublisher eventPublisher; // depende da porta
  // ...
}
```

Adapter Kafka implementa a porta:

```java
@Component
public class TaskEventProducer implements TaskEventPublisher {
  // ...
}
```

---

## 5) Roadmap incremental sugerido (checklist)

1. Quick wins (baixo esforço, alto retorno)
   - [x] Introduzir portas onde a aplicação toca infraestrutura diretamente para publicação de eventos (TaskEventPublisher no task-service). Referência: TaskAppService, TaskEventProducer.
   - [ ] Introduzir portas para outras integrações externas (ex.: envio de emails, storage externo), se/quando surgirem.
   - [x] Documentar responsabilidades e regras em package-info.java de cada camada (domain/application/adapters) nos serviços. (2025-09-17 15:30) Adicionados package-info no task-service (domain, service, application.port.out, http, kafka). (2025-09-17 15:35) Adicionados package-info no activity-service (domain, http, kafka) e no api-gateway (http, kafka, ws). (2025-09-17 15:50) Adicionado package-info no activity-service (persistence/jpa).
   - [x] Garantir que common contenha apenas DTOs/eventos/utilitários e não componentes Spring. Feito: FeignHeadersConfig e HTTP interceptors movidos para o módulo opcional (common-spring). (2025-09-17 15:23)
   - [x] Expor PATCH no api-gateway para encaminhar updates parciais ao task-service (Feign + Controller). (2025-09-17 15:50)

2. Melhorias estruturais
   - [ ] Reorganizar pacotes para refletir hexagonal (application, domain, adapters) em todos os serviços (começar por task-service e repetir em activity-service).
   - [x] Extrair configurações Spring específicas do common para um novo módulo opcional (common-spring) ou para cada serviço consumidor, mantendo common sem dependências de Spring. (2025-09-17 15:23) Concluído: ver seção 2 e módulo common-spring.
   - [x] Considerar portas também para persistência se for desejável trocar JPA/DB futuramente (ex.: DomainRepository + Adapter JPA). Aplicado no task-service e activity-service (2025-09-17 15:44). Não obrigatório se o Spring Data já abstrai o suficiente.

3. Evolução futura
   - [ ] Aplicar CQRS mais claro no gateway: controllers de leitura consultando activity-service, escrita indo para task-service, com modelos de resposta específicos.
   - [ ] Caso surja a necessidade de auditoria completa, avaliar Event Sourcing para Task, com stream de eventos versionado. Até lá, manter eventos de integração simples e versionados.

---

## 6) Critérios e boas práticas (checklist de conformidade)

- [x] Módulos livres de dependências cruzadas (apenas common é compartilhado e livre de frameworks de infraestrutura). (2025-09-17 15:30) Mantido; gateway depende de common-spring (opcional) e não há dependências cruzadas entre serviços.
- [x] Nomear pacotes por papel (application/domain/adapters) e por tecnologia na borda (http, kafka, ws, jpa). (2025-09-17 15:38) Parcial: task-service ajustado; demais serviços pendentes.
- [ ] Serviços de aplicação orquestram casos de uso; domínio permanece pequeno e expressivo; adapters lidam com tecnologia.
- [ ] Realizar mudanças incrementalmente para minimizar impacto e risco.

---

Lembrete: sempre que houver uma mudança, marque ou desmarque o check do(s) item(ns) afetado(s) e, se útil, inclua uma breve observação com data para histórico.
