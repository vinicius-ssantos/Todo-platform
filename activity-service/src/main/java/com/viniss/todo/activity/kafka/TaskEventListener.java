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

  @KafkaListener(topics = "task.events", groupId = "activity")
  public void onEvent(ConsumerRecord<String, Object> record) {
    Object v = record.value();
    if (v instanceof TaskCreated) {
      TaskCreated tc = (TaskCreated) v;
      save("created", tc.taskId(), tc.projectId(), tc.title(), tc.status(), tc.occurredAt().toInstant());
    } else if (v instanceof TaskUpdated) {
      TaskUpdated tu = (TaskUpdated) v;
      save("updated", tu.taskId(), tu.projectId(), tu.title(), tu.status(), tu.occurredAt().toInstant());
    } else if (v instanceof Map) {
      Map<?,?> map = (Map<?,?>) v;
      Object maybeType = map.get("@type");
      String type = maybeType != null ? maybeType.toString() : "event";
      String id = String.valueOf(map.get("id"));
      String projectId = String.valueOf(map.get("projectId"));
      String title = String.valueOf(map.get("title"));
      String status = String.valueOf(map.get("status"));
      Instant at = Instant.now();
      save(type, id, projectId, title, status, at);
    }
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