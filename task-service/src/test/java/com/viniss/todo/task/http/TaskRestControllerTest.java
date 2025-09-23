package com.viniss.todo.task.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viniss.todo.task.service.TaskAppService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = TaskRestController.class)
@Import(TaskRestControllerWebTest.TestSecurityConfig.class) // <- libera tudo e desabilita CSRF no teste
class TaskRestControllerWebTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean TaskAppService service;

    private String json(Object o) throws Exception { return om.writeValueAsString(o); }

    @Test @DisplayName("POST /tasks -> 201 Created (payload válido)")
    void create_returns201() throws Exception {
        Mockito.when(service.create(any())).thenReturn(null);

        var body = Map.of(
                "title","Write tests",
                "description","Cobrir TaskAppService",
                "projectId","project-123",
                "labels", List.of("backend","tests")
        );

        mvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated());
    }

    @Test @DisplayName("PUT /tasks/{id} -> 200 OK (payload válido)")
    void update_returns200() throws Exception {
        Mockito.when(service.update(anyString(), any())).thenReturn(null);

        var body = Map.of(
                "title","Novo título",
                "description","Nova descrição",
                "status","DONE",
                "labels", List.of("x","y")
        );

        mvc.perform(put("/tasks/{id}", "t-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("PATCH /tasks/{id} -> 200 OK (parcial)")
    void patch_returns200() throws Exception {
        Mockito.when(service.patch(anyString(), any())).thenReturn(null);

        var body = Map.of("title","Parcial");

        mvc.perform(patch("/tasks/{id}", "t-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("POST /tasks -> 400 quando título em branco")
    void create_returns400_whenTitleBlank() throws Exception {
        var body = Map.of(
                "title","   ",
                "description","desc",
                "projectId","project-123"
        );

        mvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test @DisplayName("POST /tasks -> 400 quando título ultrapassa limite")
    void create_returns400_whenTitleTooLong() throws Exception {
        int MAX_TITLE = 120; // ajuste para o @Size real do seu CreateTaskRequest
        String over = "X".repeat(MAX_TITLE + 1);

        var body = Map.of(
                "title", over,
                "description", "desc",
                "projectId", "project-123"
        );

        mvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}
