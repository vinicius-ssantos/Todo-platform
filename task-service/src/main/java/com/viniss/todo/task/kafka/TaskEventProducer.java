package com.viniss.todo.task.kafka;

import com.viniss.todo.common.events.TaskCreated;
import com.viniss.todo.common.events.TaskUpdated;
import com.viniss.todo.task.application.port.out.TaskEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TaskEventProducer implements TaskEventPublisher {
  private static final String TOPIC = "task.events";
  private final KafkaTemplate<String, Object> kafkaTemplate;

  public TaskEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public void publishCreated(TaskCreated event) {
    kafkaTemplate.send(TOPIC, event.taskId(), event);
  }

  @Override
  public void publishUpdated(TaskUpdated event) {
    kafkaTemplate.send(TOPIC, event.taskId(), event);
  }
}
