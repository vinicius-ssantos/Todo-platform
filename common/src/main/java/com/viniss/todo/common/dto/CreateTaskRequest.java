// com/viniss/todo/common/dto/CreateTaskRequest.java
package com.viniss.todo.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateTaskRequest(
        @NotBlank @Size(max = 140) String title,
        @Size(max = 4000) String description,
        String projectId,
        List<String> labels
) {}
