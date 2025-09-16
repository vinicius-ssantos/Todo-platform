package com.viniss.todo.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskResponse(
        String id,
        String projectId,
        String title,
        String description,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<String> labels
) {}