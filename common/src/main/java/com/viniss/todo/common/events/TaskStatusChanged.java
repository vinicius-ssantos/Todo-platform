// domain/events/TaskStatusChanged.java
package com.viniss.todo.common.events;

import com.viniss.todo.common.dto.TaskStatus;
import java.time.OffsetDateTime;

public record TaskStatusChanged(
    String taskId,
    TaskStatus oldStatus,
    TaskStatus newStatus,
    OffsetDateTime occurredAt
) implements TaskEvent {}
