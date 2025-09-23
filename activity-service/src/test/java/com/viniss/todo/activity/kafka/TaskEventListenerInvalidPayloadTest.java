package com.viniss.todo.activity.kafka;

import com.viniss.todo.activity.domain.ActivityRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.annotation.KafkaListener;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Garante que payloads inesperados não geram Activity.
 */
class TaskEventListenerInvalidPayloadTest {

    private Method listenerMethod() {
        return Arrays.stream(TaskEventListener.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(KafkaListener.class))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No @KafkaListener method found"));
    }

    @Test
    @DisplayName("Ignora quando value é Map (payload genérico JSON)")
    void ignore_whenMapPayload() throws Exception {
        ActivityRepository repo = mock(ActivityRepository.class);
        TaskEventListener listener = new TaskEventListener(repo);

        ConsumerRecord<String, Object> record =
                new ConsumerRecord<>("task.events", 0, 0L, "t-1", Map.of("unexpected", "value"));

        Method m = listenerMethod();
        m.setAccessible(true);
        m.invoke(listener, record);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("Ignora quando value é null")
    void ignore_whenNull() throws Exception {
        ActivityRepository repo = mock(ActivityRepository.class);
        TaskEventListener listener = new TaskEventListener(repo);

        ConsumerRecord<String, Object> record =
                new ConsumerRecord<>("task.events", 0, 1L, "t-2", null);

        Method m = listenerMethod();
        m.setAccessible(true);
        m.invoke(listener, record);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("Ignora quando value é de tipo desconhecido")
    void ignore_whenUnknownType() throws Exception {
        ActivityRepository repo = mock(ActivityRepository.class);
        TaskEventListener listener = new TaskEventListener(repo);

        ConsumerRecord<String, Object> record =
                new ConsumerRecord<>("task.events", 0, 2L, "t-3", new Object());

        Method m = listenerMethod();
        m.setAccessible(true);
        m.invoke(listener, record);

        verify(repo, never()).save(any());
        verifyNoMoreInteractions(repo);
    }
}
