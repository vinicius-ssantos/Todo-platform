package com.viniss.todo.task.application.port.out;

import com.viniss.todo.common.events.TaskCreated;
import com.viniss.todo.common.events.TaskUpdated;

/**
 * Outbound port for publishing task domain events.
 *
 * This abstraction allows the application layer to remain independent
 * from any specific messaging technology (Kafka, RabbitMQ, etc.).
 */
public interface TaskEventPublisher {
    void publishCreated(TaskCreated event);
    void publishUpdated(TaskUpdated event);
}
