package com.viniss.todo.task.persistence.jpa;

import com.viniss.todo.common.dto.TaskStatus;
import com.viniss.todo.task.domain.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests para TaskRepositoryJpaAdapter.
 * Foco: garantir que ele DELEGA corretamente ao SpringDataTaskRepository.
 */
@ExtendWith(MockitoExtension.class)
class TaskRepositoryJpaAdapterTest {

    @Mock
    SpringDataTaskRepository springData;

    @InjectMocks
    TaskRepositoryJpaAdapter adapter;

    private Task newTask() {
        Task t = new Task();
        t.setId(UUID.randomUUID().toString());
        t.setProjectId("p1");
        t.setTitle("T");
        t.setDescription("D");
        t.setStatus(TaskStatus.TODO);
        t.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        t.setUpdatedAt(Instant.parse("2024-01-02T00:00:00Z"));
        t.setLabels(List.of("a", "b"));
        return t;
    }

    @Test
    @DisplayName("save(): deve delegar para SpringDataTaskRepository.save e retornar resultado")
    void save_delegatesAndReturns() {
        // Arrange
        Task toSave = newTask();
        Task saved = newTask();
        saved.setId("saved-123");

        when(springData.save(any(Task.class))).thenReturn(saved);

        // Act
        Task returned = adapter.save(toSave);

        // Assert
        verify(springData, times(1)).save(toSave);
        verifyNoMoreInteractions(springData);
        assertThat(returned).isSameAs(saved);
        assertThat(returned.getId()).isEqualTo("saved-123");
    }

    @Test
    @DisplayName("findById(): deve delegar para SpringDataTaskRepository.findById e retornar Optional")
    void findById_delegatesAndReturns() {
        // Arrange
        Task found = newTask();
        found.setId("abc");
        when(springData.findById("abc")).thenReturn(Optional.of(found));

        // Act
        Optional<Task> result = adapter.findById("abc");

        // Assert
        verify(springData, times(1)).findById("abc");
        verifyNoMoreInteractions(springData);
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("abc");
    }

    @Test
    @DisplayName("findById(): deve retornar empty quando SpringData retorna empty")
    void findById_returnsEmptyWhenNotFound() {
        when(springData.findById("missing")).thenReturn(Optional.empty());

        Optional<Task> result = adapter.findById("missing");

        verify(springData).findById("missing");
        verifyNoMoreInteractions(springData);
        assertThat(result).isEmpty();
    }
}
