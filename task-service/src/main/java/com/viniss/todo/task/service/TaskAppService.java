package com.viniss.todo.task.service;

import com.viniss.todo.common.dto.CreateTaskRequest;
import com.viniss.todo.common.dto.TaskResponse;
import com.viniss.todo.common.dto.UpdateTaskRequest;
import com.viniss.todo.common.dto.TaskStatus;
import com.viniss.todo.common.events.TaskCreated;
import com.viniss.todo.common.events.TaskUpdated;
import com.viniss.todo.task.application.port.out.TaskEventPublisher;
import com.viniss.todo.task.domain.Task;
import com.viniss.todo.task.domain.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class TaskAppService {
  private final TaskRepository repository;
  private final TaskEventPublisher eventPublisher;

  public TaskAppService(TaskRepository repository, TaskEventPublisher eventPublisher) {
    this.repository = repository;
    this.eventPublisher = eventPublisher;
  }

  public TaskResponse create(CreateTaskRequest req) {
    Task t = new Task();
    t.setId(UUID.randomUUID().toString());
    t.setProjectId(req.projectId());
    t.setTitle(req.title());
    t.setDescription(req.description());
    t.setStatus(TaskStatus.TODO); // default inicial
    t.setCreatedAt(Instant.now());
    t.setUpdatedAt(t.getCreatedAt());
    t.setLabels(req.labels() == null ? new ArrayList<>() : new ArrayList<>(req.labels()));

    Task saved = repository.save(t);

    // Evento de integração (agora com taskId e OffsetDateTime)
    eventPublisher.publishCreated(new TaskCreated(
            saved.getId(), // taskId
            saved.getProjectId(),
            saved.getTitle(),
            saved.getStatus().name(),
            saved.getCreatedAt().atOffset(ZoneOffset.UTC), // OffsetDateTime
            saved.getLabels()
    ));

    return toResponse(saved);
  }

  public TaskResponse update(String id, UpdateTaskRequest req) {
    Task t = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));

    if (req.title() != null) t.setTitle(req.title());
    if (req.description() != null) t.setDescription(req.description());
    if (req.status() != null) t.setStatus(TaskStatus.valueOf(req.status()));
    if (req.labels() != null) t.setLabels(new ArrayList<>(req.labels()));
    t.setUpdatedAt(Instant.now());

    Task saved = repository.save(t);

    eventPublisher.publishUpdated(new TaskUpdated(
            saved.getId(), // taskId
            saved.getProjectId(),
            saved.getTitle(),
            saved.getStatus().name(),
            saved.getUpdatedAt().atOffset(ZoneOffset.UTC), // OffsetDateTime
            saved.getLabels()
    ));

    return toResponse(saved);
  }

  // PATCH parcial (mesma DTO com campos opcionais)
  public TaskResponse patch(String id, UpdateTaskRequest req) {
    return update(id, req);
  }

  private static TaskResponse toResponse(Task t) {
    return new TaskResponse(
            t.getId(),
            t.getProjectId(),
            t.getTitle(),
            t.getDescription(),
            t.getStatus() == null ? null : t.getStatus().name(),
            t.getCreatedAt() == null ? null : t.getCreatedAt().atOffset(ZoneOffset.UTC),
            t.getUpdatedAt() == null ? null : t.getUpdatedAt().atOffset(ZoneOffset.UTC),
            t.getLabels()
    );
  }
}