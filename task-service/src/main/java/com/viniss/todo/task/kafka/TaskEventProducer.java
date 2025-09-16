package com.viniss.todo.task.kafka;

import com.viniss.todo.common.events.TaskCreated;
import com.viniss.todo.common.events.TaskUpdated;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TaskEventProducer {
  private static final String TOPIC = "task.events";
  private final KafkaTemplate<String, Object> kafkaTemplate;

  public TaskEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publishCreated(TaskCreated event) {
    kafkaTemplate.send(TOPIC, event.taskId(), event);
  }

  public void publishUpdated(TaskUpdated event) {
    kafkaTemplate.send(TOPIC, event.taskId(), event);
  }
}
