package com.viniss.todo.task.mapper;

import com.viniss.todo.common.dto.CreateTaskRequest;
import com.viniss.todo.common.dto.TaskResponse;
import com.viniss.todo.common.dto.TaskStatus;
import com.viniss.todo.common.dto.UpdateTaskRequest;
import com.viniss.todo.task.domain.Task;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TaskMapper {

    public Task toEntity(CreateTaskRequest req) {
        if (req == null) throw new IllegalArgumentException("CreateTaskRequest não pode ser nulo");
        String title = trimToNull(req.title());
        if (title == null) {
            throw new IllegalArgumentException("title é obrigatório");
        }

        Task entity = new Task();
        entity.setProjectId(trimToNull(req.projectId()));
        entity.setTitle(title);
        entity.setDescription(Optional.ofNullable(trim(req.description())).orElse(""));
        entity.setStatus(TaskStatus.TODO); // Status padrão, pois não existe no CreateTaskRequest
        entity.setLabels(req.labels() != null ? new ArrayList<>(req.labels()) : new ArrayList<>());
        
        return entity;
    }

    /** patch/update parcial: aplica apenas campos presentes (não nulos) */
    public void applyUpdate(Task entity, UpdateTaskRequest req) {
        if (entity == null) throw new IllegalArgumentException("entity não pode ser nula");
        if (req == null) return;

        if (req.title() != null) {
            String title = trimToNull(req.title());
            if (title == null) {
                throw new IllegalArgumentException("title não pode ser vazio/branco");
            }
            entity.setTitle(title);
        }

        if (req.description() != null) {
            String desc = trim(req.description());
            entity.setDescription(desc == null ? "" : desc);
        }

        if (req.status() != null) { // Corrigido: req.status() em vez de req.getStatus()
            try {
                TaskStatus status = TaskStatus.valueOf(req.status().toUpperCase());
                entity.setStatus(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status inválido: " + req.status());
            }
        }

        if (req.labels() != null) {
            entity.setLabels(new ArrayList<>(req.labels()));
        }
    }

    public TaskResponse toResponse(Task entity) {
        if (entity == null) return null;
        
        // Records são criados via construtor, não setters
        return new TaskResponse(
            entity.getId(),
            nvl(entity.getProjectId(), ""),
            nvl(entity.getTitle(), ""),
            nvl(entity.getDescription(), ""),
            entity.getStatus() != null ? entity.getStatus().name() : TaskStatus.TODO.name(),
            instantToOffsetDateTime(entity.getCreatedAt()),
            instantToOffsetDateTime(entity.getUpdatedAt()),
            entity.getLabels() != null ? new ArrayList<>(entity.getLabels()) : new ArrayList<>()
        );
    }

    // helpers
    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static <T> T nvl(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static OffsetDateTime instantToOffsetDateTime(java.time.Instant instant) {
        return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
    }
}