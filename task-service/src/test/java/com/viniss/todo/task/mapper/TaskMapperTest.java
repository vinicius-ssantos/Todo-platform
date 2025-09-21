package com.viniss.todo.task.mapper;


import com.viniss.todo.common.dto.CreateTaskRequest;
import com.viniss.todo.common.dto.TaskResponse;
import com.viniss.todo.common.dto.TaskStatus;
import com.viniss.todo.common.dto.UpdateTaskRequest;
import com.viniss.todo.task.domain.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class TaskMapperTest {
    private final TaskMapper mapper = new TaskMapper();

    @Test
    void toEntity_aplicaDefaults_eTrim() {
        // Records são criados via construtor
        CreateTaskRequest req = new CreateTaskRequest(
                "  Título  ",
                "  desc  ",
                "  project-123  ",
                List.of("label1")
        );

        Task e = mapper.toEntity(req);

        assertThat(e.getProjectId()).isEqualTo("project-123");
        assertThat(e.getTitle()).isEqualTo("Título");
        assertThat(e.getDescription()).isEqualTo("desc");
        assertThat(e.getStatus()).isEqualTo(TaskStatus.TODO); // default
        assertThat(e.getLabels()).containsExactly("label1");
    }

    @Test
    void toEntity_titleObrigatorio_quandoNuloOuBranco() {
        CreateTaskRequest req = new CreateTaskRequest(
                "   ", // título em branco
                "desc",
                "project-123",
                null
        );
        
        assertThatThrownBy(() -> mapper.toEntity(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title é obrigatório");
    }

    @Test
    void applyUpdate_patchSomenteCamposPresentes() {
        Task entity = new Task();
        entity.setTitle("Old");
        entity.setDescription("old");
        entity.setStatus(TaskStatus.TODO);

        // UpdateTaskRequest com String para status
        UpdateTaskRequest req = new UpdateTaskRequest(
                "  New  ",          // aplica
                null,               // ignora
                "DONE",             // aplica (String, não enum)
                null                // ignora
        );

        mapper.applyUpdate(entity, req);

        assertThat(entity.getTitle()).isEqualTo("New");
        assertThat(entity.getDescription()).isEqualTo("old");
        assertThat(entity.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void applyUpdate_titleBrancoDisparaErro() {
        Task entity = new Task();
        entity.setTitle("Old");

        UpdateTaskRequest req = new UpdateTaskRequest(
                "   ",    // título em branco
                null,
                null,
                null
        );

        assertThatThrownBy(() -> mapper.applyUpdate(entity, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title não pode ser vazio/branco");
    }

    @Test
    void toResponse_evitaNulls_eMantemDefaults() {
        Task e = new Task();
        e.setId("t-1");
        e.setProjectId(null);
        e.setTitle(null);
        e.setDescription(null);
        e.setStatus(null);

        TaskResponse r = mapper.toResponse(e);

        assertThat(r.id()).isEqualTo("t-1");
        assertThat(r.projectId()).isEqualTo("");
        assertThat(r.title()).isEqualTo("");
        assertThat(r.description()).isEqualTo("");
        // TaskResponse.status() retorna String, não enum
        assertThat(r.status()).isEqualTo("TODO");
        assertThat(r.createdAt()).isNull();
        assertThat(r.updatedAt()).isNull();
    }
}