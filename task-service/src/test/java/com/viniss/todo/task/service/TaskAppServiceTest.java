package com.viniss.todo.task.service;

import com.viniss.todo.common.dto.CreateTaskRequest;
import com.viniss.todo.common.dto.UpdateTaskRequest;
import com.viniss.todo.common.dto.TaskStatus;
import com.viniss.todo.common.events.TaskCreated;
import com.viniss.todo.common.events.TaskUpdated;
import com.viniss.todo.task.application.port.out.TaskEventPublisher;
import com.viniss.todo.task.domain.Task;
import com.viniss.todo.task.domain.TaskRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários de TaskAppService (JUnit 5 + Mockito + AssertJ).
 * Foco: comportamento do serviço (mapeamento, defaults, chamadas a repo/publisher).
 */
@ExtendWith(MockitoExtension.class)
class TaskAppServiceTest {

    @Mock TaskRepository repository;
    @Mock TaskEventPublisher eventPublisher;

    @InjectMocks TaskAppService service;

    @Test
    @DisplayName("create(): persiste com defaults e publica TaskCreated")
    void create_persisteDefaults_publicaEvento() {
        // Arrange
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateTaskRequest req = new CreateTaskRequest(
                "Write tests",
                "Cobrir TaskAppService",
                "project-123",
                null // labels nulo -> deve virar lista vazia
        );

        // Act
        var resp = service.create(req);

        // Assert: salvou
        verify(repository, times(1)).save(any(Task.class));
        assertThat(resp.id()).isNotBlank();
        assertThat(resp.projectId()).isEqualTo("project-123");
        assertThat(resp.title()).isEqualTo("Write tests");
        assertThat(resp.description()).isEqualTo("Cobrir TaskAppService");
        assertThat(resp.status()).isEqualTo(TaskStatus.TODO.name()); // default
        assertThat(resp.labels()).isEmpty();
        assertThat(resp.createdAt()).isNotNull();
        assertThat(resp.updatedAt()).isNotNull();

        // Assert: publicou TaskCreated com campos espelhados
        ArgumentCaptor<TaskCreated> cap = ArgumentCaptor.forClass(TaskCreated.class);
        verify(eventPublisher).publishCreated(cap.capture());
        TaskCreated evt = cap.getValue();
        assertThat(evt.taskId()).isEqualTo(resp.id());
        assertThat(evt.projectId()).isEqualTo(resp.projectId());
        assertThat(evt.title()).isEqualTo(resp.title());
        assertThat(evt.status()).isEqualTo(resp.status());
        assertThat(evt.labels()).isEmpty();
        assertThat(evt.occurredAt()).isInstanceOf(OffsetDateTime.class);
    }

    @Test
    @DisplayName("update(): aplica alterações, atualiza updatedAt e publica TaskUpdated")
    void update_aplicaMudancas_publicaEvento() {
        // Arrange
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task existing = new Task();
        existing.setId(UUID.randomUUID().toString());
        existing.setProjectId("p1");
        existing.setTitle("Old title");
        existing.setDescription("Old desc");
        existing.setStatus(TaskStatus.TODO);
        existing.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        existing.setUpdatedAt(existing.getCreatedAt());
        existing.setLabels(List.of("a"));

        when(repository.findById(existing.getId())).thenReturn(Optional.of(existing));

        UpdateTaskRequest req = new UpdateTaskRequest(
                "New title",
                "New desc",
                TaskStatus.DONE.name(),
                List.of("x", "y")
        );

        // Act
        var resp = service.update(existing.getId(), req);

        // Assert
        verify(repository).findById(existing.getId());
        verify(repository).save(any(Task.class));

        assertThat(resp.title()).isEqualTo("New title");
        assertThat(resp.description()).isEqualTo("New desc");
        assertThat(resp.status()).isEqualTo(TaskStatus.DONE.name());
        assertThat(resp.labels()).containsExactly("x","y");
        assertThat(resp.createdAt()).isEqualTo(OffsetDateTime.ofInstant(existing.getCreatedAt(), ZoneOffset.UTC));
        assertThat(resp.updatedAt()).isNotNull();

        ArgumentCaptor<TaskUpdated> cap = ArgumentCaptor.forClass(TaskUpdated.class);
        verify(eventPublisher).publishUpdated(cap.capture());
        TaskUpdated evt = cap.getValue();
        assertThat(evt.taskId()).isEqualTo(existing.getId());
        assertThat(evt.projectId()).isEqualTo("p1");
        assertThat(evt.title()).isEqualTo("New title");
        assertThat(evt.status()).isEqualTo(TaskStatus.DONE.name());
        assertThat(evt.labels()).containsExactly("x","y");
        assertThat(evt.occurredAt()).isNotNull();
    }

    @Test
    @DisplayName("update(): quando não encontra, lança exceção e não publica")
    void update_quandoNaoEncontra_lanca() {
        // Arrange
        when(repository.findById("nope")).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.update("nope", new UpdateTaskRequest(null,null,null,null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task not found");

        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishUpdated(any());
    }

    @Test
    @DisplayName("patch(): permite atualização parcial (mantém valores antigos quando null)")
    void patch_parcial_mantemValoresNaoInformados() {
        // Arrange
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task existing = new Task();
        existing.setId("t-1");
        existing.setProjectId("p1");
        existing.setTitle("T");
        existing.setDescription("D");
        existing.setStatus(TaskStatus.TODO);
        existing.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        existing.setUpdatedAt(existing.getCreatedAt());
        existing.setLabels(List.of("a"));

        when(repository.findById("t-1")).thenReturn(Optional.of(existing));

        UpdateTaskRequest parcial = new UpdateTaskRequest("New", null, null, null);

        // Act
        var resp = service.patch("t-1", parcial);

        // Assert
        verify(repository).save(any(Task.class));
        verify(eventPublisher).publishUpdated(any(TaskUpdated.class));

        assertThat(resp.title()).isEqualTo("New");
        assertThat(resp.description()).isEqualTo("D");                // inalterado
        assertThat(resp.status()).isEqualTo(TaskStatus.TODO.name());  // inalterado
        assertThat(resp.labels()).containsExactly("a");               // inalterado
    }
}