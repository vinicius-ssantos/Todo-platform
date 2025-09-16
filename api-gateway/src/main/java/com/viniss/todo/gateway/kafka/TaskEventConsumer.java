package com.viniss.todo.gateway.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viniss.todo.common.events.TaskCreated;
import com.viniss.todo.common.events.TaskUpdated;
import com.viniss.todo.gateway.ws.WsSessions;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TaskEventConsumer {
  private final WsSessions sessions;
  private final ObjectMapper mapper;

  public TaskEventConsumer(WsSessions sessions, ObjectMapper mapper) {
    this.sessions = sessions;
    this.mapper = mapper;
  }

  @KafkaListener(topics = "task.events", groupId = "gateway")
  public void consume(ConsumerRecord<String, Object> record) throws JsonProcessingException {
    Object value = record.value();
    String projectId = null;
    String type = null;
    if (value instanceof TaskCreated) {
      TaskCreated tc = (TaskCreated) value;
      projectId = tc.projectId();
      type = "task.created";
    } else if (value instanceof TaskUpdated) {
      TaskUpdated tu = (TaskUpdated) value;
      projectId = tu.projectId();
      type = "task.updated";
    } else if (value instanceof Map) {
      Map<?,?> map = (Map<?,?>) value;
      Object pid = map.get("projectId");
      projectId = pid != null ? pid.toString() : null;
      Object maybeType = map.get("@type");
      type = maybeType != null ? maybeType.toString() : "task.event";
    }
    if (projectId != null) {
      Map<String, Object> msg = new HashMap<>();
      msg.put("type", type);
      msg.put("event", value);
      String json = mapper.writeValueAsString(msg);
      sessions.broadcast(projectId, json);
    }
  }
}
