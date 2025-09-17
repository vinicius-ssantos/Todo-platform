package com.viniss.todo.task.persistence.jpa;

import com.viniss.todo.task.domain.Task;
import com.viniss.todo.task.domain.TaskRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA adapter that implements the domain TaskRepository using Spring Data.
 */
@Component
public class TaskRepositoryJpaAdapter implements TaskRepository {
  private final SpringDataTaskRepository springData;

  public TaskRepositoryJpaAdapter(SpringDataTaskRepository springData) {
    this.springData = springData;
  }

  @Override
  public Task save(Task task) {
    return springData.save(task);
  }

  @Override
  public Optional<Task> findById(String id) {
    return springData.findById(id);
  }
}
