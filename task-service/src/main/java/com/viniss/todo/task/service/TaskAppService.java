package com.viniss.todo.task.service;

import com.viniss.todo.common.dto.CreateTaskRequest;
import com.viniss.todo.common.dto.TaskResponse;
import com.viniss.todo.common.dto.UpdateTaskRequest;
import com.viniss.todo.common.events.TaskCreated;
import com.viniss.todo.common.events.TaskUpdated;
import com.viniss.todo.task.domain.Task;
import com.viniss.todo.task.domain.TaskRepository;
import com.viniss.todo.task.kafka.TaskEventProducer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class TaskAppService {
  private final TaskRepository repository;
  private final TaskEventProducer producer;

  public TaskAppService(TaskRepository repository, TaskEventProducer producer) {
    this.repository = repository;
    this.producer = producer;
  }

  public TaskResponse create(CreateTaskRequest req) {
    Task t = new Task();
    t.setId(UUID.randomUUID().toString());
    t.setProjectId(req.projectId());
    t.setTitle(req.title());
    t.setDescription(req.description());
    t.setStatus("TODO"); // default inicial (mantive String p/ bater com sua entidade)
    t.setCreatedAt(Instant.now());
    t.setUpdatedAt(t.getCreatedAt());
    t.setLabels(req.labels() == null ? new ArrayList<>() : new ArrayList<>(req.labels()));

    Task saved = repository.save(t);

    // Evento de integração (agora com taskId e OffsetDateTime)
    producer.publishCreated(new TaskCreated(
            saved.getId(), // taskId
            saved.getProjectId(),
            saved.getTitle(),
            saved.getStatus(),
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
    if (req.status() != null) t.setStatus(req.status());
    if (req.labels() != null) t.setLabels(new ArrayList<>(req.labels()));
    t.setUpdatedAt(Instant.now());

    Task saved = repository.save(t);

    producer.publishUpdated(new TaskUpdated(
            saved.getId(), // taskId
            saved.getProjectId(),
            saved.getTitle(),
            saved.getStatus(),
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
            t.getStatus(),
            t.getCreatedAt() == null ? null : t.getCreatedAt().atOffset(ZoneOffset.UTC),
            t.getUpdatedAt() == null ? null : t.getUpdatedAt().atOffset(ZoneOffset.UTC),
            t.getLabels()
    );
  }
}