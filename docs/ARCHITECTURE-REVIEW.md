# Todo-platform – Revisão de Arquitetura (Ponto 1: Arquitetura e boas práticas)

Este documento revisa a separação de responsabilidades entre módulos, identifica acoplamentos desnecessários/risco de dependências circulares, sugere padrões (DDD, hexagonal, CQRS/event sourcing) e exemplifica uma reorganização por camadas (application vs domain vs adapters) aplicável incrementalmente.


## 1) Separação de responsabilidades entre módulos (estado atual)

- common
  - OK: concentra DTOs e modelos de eventos usados entre serviços.
  - Observação: contém também classes Spring (FeignHeadersConfig, filtros/interceptors HTTP). Isso cria acoplamento do módulo comum a Spring MVC/Feign.
- task-service
  - Exponde REST (TaskRestController), persiste entidade Task (JPA), publica eventos no Kafka (TaskEventProducer) e tem serviço de aplicação (TaskAppService).
- activity-service
  - Consome eventos do Kafka (TaskEventListener) e expõe endpoints de consulta (ActivityController) sobre Activity.
- api-gateway
  - Edge HTTP (Feign para task-service e activity-service) e WebSocket. Também possui um Kafka consumer (TaskEventConsumer) para retransmitir por WS.

Resumo: a separação macro por serviços está coerente. Há pequenos pontos de acoplamento que podem ser melhorados (ver seção 2).


## 2) Acoplamentos desnecessários e riscos de dependências circulares

- common -> Spring acoplamento
  - Problema: classes com dependência de Spring (FeignHeadersConfig, RequestLogging*) dentro de common forçam todos os consumidores a dependerem de infraestrutura web.
  - Risco: evolução do common fica travada por versões específicas de Spring e pode induzir dependências cruzadas futuras.
  - Ação sugerida: mover as configs específicas de Spring para cada serviço que as usa, ou para um novo módulo opcional (ex.: common-spring) dependente de common, nunca o contrário.

- task-service (aplicação -> adapter Kafka)
  - Problema: TaskAppService dependia diretamente de TaskEventProducer (Kafka). Isso liga a regra de aplicação a uma tecnologia específica.
  - Ação aplicada: introduzido o porto de saída TaskEventPublisher na aplicação e o adapter Kafka passou a implementá-lo (hexagonal). Ver seção 4.

- api-gateway como consumidor Kafka
  - Observação: o gateway consumindo Kafka cria dependência direta do edge em mensageria. É válido para uso de WS/broadcast, mas considere um canal dedicado (ex.: activity-service publicar eventos "activity" que o gateway consome) para evitar acoplamento direto ao domínio de tarefas.

- Dependências circulares
  - Não foram identificadas dependências circulares entre módulos Maven (common é apenas referenciado, não referencia serviços).
  - Atenção: mantenha a regra “common não depende de outros módulos”; serviços não devem depender entre si diretamente (via código), apenas via HTTP/Feign ou eventos.


## 3) Padrões aplicáveis

- DDD (Domain-Driven Design)
  - Bounded Contexts: task-service e activity-service são contextos distintos.
  - Linguagem ubíqua: status de Task deveria ser enum (já existe em common.dto.TaskStatus para DTO; considerar alinhar entidade JPA).
- Arquitetura Hexagonal (Ports & Adapters)
  - Application (caso de uso), Domain (entidades + regras), Adapters (HTTP, Kafka, JPA, Feign).
  - Benefício: permite testar lógica de aplicação sem infraestrutura e trocar adapters (ex.: Kafka->RabbitMQ) sem tocar o core.
- CQRS
  - Gateway pode adotar leitura via activity-service e escrita via task-service (já ocorre). Internamente, pode-se separar modelos de leitura e escrita se necessário (não obrigatório agora).
- Event Sourcing
  - Não necessário no estágio atual. Considere apenas se houver necessidade de auditoria completa e reconstrução de estado. Hoje, eventos de integração são suficientes.


## 4) Exemplo de reorganização (incremental) – application vs domain vs adapters

A seguinte estrutura pode ser adotada gradualmente, sem mudanças drásticas, iniciando pelo task-service:

- com.viniss.todo.task.domain
  - Task, TaskRepository (regras do domínio e portas primárias se necessário)
- com.viniss.todo.task.application
  - Serviços de aplicação (orquestram casos de uso): TaskAppService
  - Ports out (dependências externas): application.port.out.TaskEventPublisher
- com.viniss.todo.task.adapters
  - http: TaskRestController
  - kafka: TaskEventProducer (implementa TaskEventPublisher)
  - persistence/jpa: implementações de repositórios se usado o padrão de porta para persistência

Mudanças já aplicadas (exemplo prático):

- Criado o porto de saída:
  - task-service/src/main/java/com/viniss/todo/task/application/port/out/TaskEventPublisher.java
- Adapter Kafka passou a implementar a porta:
  - task-service/src/main/java/com/viniss/todo/task/kafka/TaskEventProducer.java implements TaskEventPublisher
- TaskAppService agora depende da porta (não do Kafka):
  - task-service/src/main/java/com/viniss/todo/task/service/TaskAppService.java (campo eventPublisher)

Isso reduz acoplamento da camada de aplicação a tecnologia de mensageria e serve como guia para outras dependências externas.

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


## 5) Roadmap incremental sugerido

1. Quick wins (baixo esforço, alto retorno)
   - Introduzir portas onde a aplicação toca infraestrutura diretamente (ex.: publicação de eventos, envio de emails, storage externo). Exemplificado em TaskEventPublisher. Referência: task-service/...TaskAppService.java, ...TaskEventProducer.java.
   - Documentar responsabilidades e regras em package-info.java de cada camada (domain/application/adapters). Referência: task-service/src/main/java/com/viniss/todo/task/... .
   - Garantir que common contenha apenas DTOs/eventos/utilitários e não componentes Spring. Mover FeignHeadersConfig e HTTP interceptors para api-gateway. Referências: common/.../feign, http.

2. Melhorias estruturais
   - Reorganizar pacotes para refletir hexagonal (application, domain, adapters) em todos os serviços. Começar por task-service e repetir em activity-service.
   - Extrair configurações Spring específicas do common para um novo módulo opcional (common-spring) ou para cada serviço consumidor.
   - Introduzir portas também para persistência se quiser trocar JPA/DB futuramente (ex.: DomainRepository + Adapter JPA implementando-o). Não obrigatório se o Spring Data já abstrai o suficiente para o projeto.

3. Evolução futura
   - Aplicar CQRS mais claro no gateway: controllers de leitura consultando activity-service, escrita indo para task-service, com modelos de resposta específicos.
   - Caso surja a necessidade de auditoria completa, avaliar Event Sourcing para Task, com stream de eventos versionado. Até lá, mantenha eventos de integração simples e versionados.


## 6) Critérios e boas práticas

- Mantenha módulos livres de dependências cruzadas (apenas common deve ser compartilhado e livre de frameworks de infraestrutura).
- Nomeie pacotes por papel (application/domain/adapters) e por tecnologia na borda (http, kafka, ws, jpa).
- Os serviços de aplicação orquestram casos de uso; domínio permanece pequeno e expressivo; adapters lidam com tecnologia.
- Faça mudanças incrementalmente para minimizar impacto e risco.
