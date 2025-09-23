package com.viniss.todo.task.kafka;

import com.viniss.todo.common.events.TaskCreated;
import com.viniss.todo.common.events.TaskUpdated;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskEventProducer.
 */
class TaskEventProducerTest {

    @Test
    @DisplayName("publishCreated: envia no tópico task.events com key=taskId")
    void publishCreated_sendsToKafka() {
        KafkaTemplate<String, Object> kafka = mock(KafkaTemplate.class);
        TaskEventProducer producer = new TaskEventProducer(kafka);

        TaskCreated evt = new TaskCreated("t-1", "p-1", "Title", "TODO", OffsetDateTime.now(), List.of("a"));

        producer.publishCreated(evt);

        ArgumentCaptor<String> topic = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);

        verify(kafka).send(topic.capture(), key.capture(), payload.capture());
        assertThat(topic.getValue()).isEqualTo("task.events");
        assertThat(key.getValue()).isEqualTo("t-1");
        assertThat(payload.getValue()).isEqualTo(evt);
    }

    @Test
    @DisplayName("publishUpdated: envia no tópico task.events com key=taskId")
    void publishUpdated_sendsToKafka() {
        KafkaTemplate<String, Object> kafka = mock(KafkaTemplate.class);
        TaskEventProducer producer = new TaskEventProducer(kafka);

        TaskUpdated evt = new TaskUpdated("t-1", "p-1", "Title", "DONE", OffsetDateTime.now(), List.of());

        producer.publishUpdated(evt);

        ArgumentCaptor<String> topic = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);

        verify(kafka).send(topic.capture(), key.capture(), payload.capture());
        assertThat(topic.getValue()).isEqualTo("task.events");
        assertThat(key.getValue()).isEqualTo("t-1");
        assertThat(payload.getValue()).isEqualTo(evt);
    }
}
