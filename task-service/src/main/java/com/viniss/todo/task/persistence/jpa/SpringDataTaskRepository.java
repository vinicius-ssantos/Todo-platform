package com.viniss.todo.task.persistence.jpa;

import com.viniss.todo.task.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataTaskRepository extends JpaRepository<Task, String> {
}
