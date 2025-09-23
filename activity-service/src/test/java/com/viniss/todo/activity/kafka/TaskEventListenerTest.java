package com.viniss.todo.activity.kafka;

import com.viniss.todo.activity.domain.Activity;
import com.viniss.todo.activity.domain.ActivityRepository;
import com.viniss.todo.common.events.TaskCreated;
import com.viniss.todo.common.events.TaskUpdated;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.annotation.KafkaListener;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskEventListener using reflection to invoke the @KafkaListener method.
 * This avoids coupling to the exact method name/signature while asserting the mapping.
 */
class TaskEventListenerTest {

    private Method listenerMethod() {
        // pick the first method annotated with @KafkaListener
        return Arrays.stream(TaskEventListener.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(KafkaListener.class))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No @KafkaListener method found in TaskEventListener"));
    }

    @Test
    @DisplayName("TaskCreated → salva Activity com type=created e campos do evento")
    void created_mapsAndSavesActivity() throws Exception {
        ActivityRepository repo = mock(ActivityRepository.class);
        TaskEventListener listener = new TaskEventListener(repo);

        var occurred = OffsetDateTime.parse("2025-01-02T12:00:00Z");
        TaskCreated evt = new TaskCreated("t-1", "p-1", "New task", "TODO", occurred, List.of("a","b"));

        ConsumerRecord<String, Object> record = new ConsumerRecord<>("task.events", 0, 0L, "t-1", evt);

        Method m = listenerMethod();
        m.setAccessible(true);
        m.invoke(listener, record);

        ArgumentCaptor<Activity> cap = ArgumentCaptor.forClass(Activity.class);
        verify(repo).save(cap.capture());

        Activity a = cap.getValue();
        assertThat(a.getTaskId()).isEqualTo("t-1");
        assertThat(a.getProjectId()).isEqualTo("p-1");
        assertThat(a.getTitle()).isEqualTo("New task");
        assertThat(a.getStatus()).isEqualTo("TODO");
        assertThat(a.getType()).isEqualTo("created"); // conforme comentário na entidade
        assertThat(a.getAt()).isEqualTo(occurred.toInstant());
    }

    @Test
    @DisplayName("TaskUpdated → salva Activity com type=updated e campos do evento")
    void updated_mapsAndSavesActivity() throws Exception {
        ActivityRepository repo = mock(ActivityRepository.class);
        TaskEventListener listener = new TaskEventListener(repo);

        var occurred = OffsetDateTime.parse("2025-03-04T00:00:00Z");
        TaskUpdated evt = new TaskUpdated("t-9", "p-9", "Edited", "DONE", occurred, List.of());

        ConsumerRecord<String, Object> record = new ConsumerRecord<>("task.events", 0, 1L, "t-9", evt);

        Method m = listenerMethod();
        m.setAccessible(true);
        m.invoke(listener, record);

        ArgumentCaptor<Activity> cap = ArgumentCaptor.forClass(Activity.class);
        verify(repo).save(cap.capture());

        Activity a = cap.getValue();
        assertThat(a.getTaskId()).isEqualTo("t-9");
        assertThat(a.getProjectId()).isEqualTo("p-9");
        assertThat(a.getTitle()).isEqualTo("Edited");
        assertThat(a.getStatus()).isEqualTo("DONE");
        assertThat(a.getType()).isEqualTo("updated"); // conforme comentário na entidade
        assertThat(a.getAt()).isEqualTo(occurred.toInstant());
    }
}
