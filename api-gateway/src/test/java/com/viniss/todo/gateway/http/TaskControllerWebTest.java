package com.viniss.todo.gateway.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.viniss.todo.common.dto.CreateTaskRequest;
import com.viniss.todo.common.dto.TaskResponse;
import com.viniss.todo.common.dto.UpdateTaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskControllerWebTest {

    MockMvc mvc;
    ObjectMapper om;
    TaskClient client;

    @BeforeEach
    void setup() {
        om = new ObjectMapper().registerModule(new JavaTimeModule());
        client = Mockito.mock(TaskClient.class);

        mvc = MockMvcBuilders
                .standaloneSetup(new TaskController(client))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(om))
                .build();
    }

    private TaskResponse sampleResponse(String id, String title) {
        var now = OffsetDateTime.now();
        return new TaskResponse(
                id,
                "proj-1",
                title,
                "desc",
                "OPEN",
                now,
                now,
                List.of("a", "b")
        );
    }

    @Test
    @DisplayName("POST /tasks → 200 OK (proxy para TaskClient.create)")
    void create_returns200() throws Exception {
        when(client.create(any())).thenReturn(sampleResponse("t-1", "Nova"));

        var body = new CreateTaskRequest("Nova", "desc", "proj-1", List.of("a","b"));

        mvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("t-1"))
                .andExpect(jsonPath("$.title").value("Nova"))
                .andExpect(jsonPath("$.projectId").value("proj-1"))
                .andExpect(jsonPath("$.labels[0]").value("a"));
    }

    @Test
    @DisplayName("PUT /tasks/{id} → 200 OK (proxy para TaskClient.update)")
    void update_returns200() throws Exception {
        when(client.update(eq("t-2"), any())).thenReturn(sampleResponse("t-2", "Atualizada"));

        var body = new UpdateTaskRequest("Atualizada", "desc", "OPEN", List.of("x"));

        mvc.perform(put("/tasks/{id}", "t-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("t-2"))
                .andExpect(jsonPath("$.title").value("Atualizada"));
    }

    @Test
    @DisplayName("PATCH /tasks/{id} → 200 OK (proxy para TaskClient.patch)")
    void patch_returns200() throws Exception {
        when(client.patch(eq("t-3"), any())).thenReturn(sampleResponse("t-3", "Parcial"));

        var body = new UpdateTaskRequest("Parcial", null, null, null);

        mvc.perform(patch("/tasks/{id}", "t-3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("t-3"))
                .andExpect(jsonPath("$.title").value("Parcial"));
    }
}
