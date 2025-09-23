package com.viniss.todo.activity.kafka;

import com.viniss.todo.activity.domain.Activity;
import com.viniss.todo.activity.domain.ActivityRepository;
import com.viniss.todo.common.events.TaskCreated;
import com.viniss.todo.common.events.TaskUpdated;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class TaskEventListener {
  private final ActivityRepository repository;

  public TaskEventListener(ActivityRepository repository) {
    this.repository = repository;
  }

  @KafkaListener(topics = "task.events", groupId = "activity-service")
  public void onMessage(ConsumerRecord<String, ?> record) {
    Object payload = record.value();

    if (payload == null) {
      // opcional: log.warn("Ignoring null payload ...");
      return; // NÃO salva
    }

    if (payload instanceof TaskCreated e) {
      // Somente quando o payload é um TaskCreated válido
      save("TaskCreated", e.taskId(), e.projectId(), e.title(), e.status(), e.occurredAt().toInstant());
      return;
    }

    if (payload instanceof TaskUpdated e) {
      // Somente quando o payload é um TaskUpdated válido
      save("TaskUpdated", e.taskId(), e.projectId(), e.title(), e.status(), e.occurredAt().toInstant());
      return;
    }

    if (payload instanceof Map<?, ?> m) {
      Object type = m.get("type");
      if ("TaskCreated".equals(type)) {
        // extrai campos e chama save(...)
        return;
      }
      if ("TaskUpdated".equals(type)) {
        // extrai campos e chama save(...)
        return;
      }
      return; // Map sem type conhecido → ignora
    }

    // Demais tipos desconhecidos → ignora
    // opcional: log.warn("Ignoring unexpected payload type: {}", payload.getClass().getName());
  }

  private void save(String type, String taskId, String projectId, String title, String status, Instant at) {
    Activity a = new Activity();
    a.setTaskId(taskId);
    a.setProjectId(projectId);
    a.setType(type);
    a.setTitle(title);
    a.setStatus(status);
    a.setAt(at != null ? at : Instant.now());
    repository.save(a);
  }
}