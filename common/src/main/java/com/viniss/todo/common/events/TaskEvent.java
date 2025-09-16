package com.viniss.todo.common.events;

import java.time.OffsetDateTime;

public sealed interface TaskEvent permits TaskCreated, TaskUpdated, TaskStatusChanged {
  String taskId();
  OffsetDateTime occurredAt();
}
