package com.viniss.todo.task.domain;

import java.util.Optional;

/**
 * Domain port for task persistence.
 *
 * This interface must not depend on Spring Data or any persistence technology.
 * Adapters (e.g., JPA) should implement this contract.
 */
public interface TaskRepository {
  Task save(Task task);
  Optional<Task> findById(String id);
}
