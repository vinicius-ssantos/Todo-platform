package com.viniss.todo.gateway.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.viniss.todo.common.http.CorrelationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// >>> ajuste a classe do controller real, se o nome/pacote for diferente
@WebMvcTest(controllers = TaskController.class)
@Import(TaskControllerWebProxyTest.TestBeans.class)
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://allowed.test"
})
class TaskControllerWebProxyTest {

    @TestConfiguration
    static class TestBeans {
        @Bean CorrelationFilter correlationFilter() { return new CorrelationFilter(); }

        // Mock do Feign client real
        @Bean TaskClient taskClient() { return Mockito.mock(TaskClient.class); }

        // ObjectMapper com JSR-310 p/ evitar erros caso o DTO tenha datas
        @Bean ObjectMapper objectMapper() {
            return new ObjectMapper().registerModule(new JavaTimeModule());
        }
    }

    @org.springframework.beans.factory.annotation.Autowired MockMvc mvc;
    @org.springframework.beans.factory.annotation.Autowired ObjectMapper om;
    @org.springframework.beans.factory.annotation.Autowired TaskClient taskClient;

    private String json(Object o) throws Exception { return om.writeValueAsString(o); }

    // Helpers: criam *dublês simples* dos DTOs só com getters que o Jackson consiga serializar.
    // (Evita depender de construtores reais dos DTOs do módulo common.)
    private static class FakeTaskResponse extends com.viniss.todo.common.dto.TaskResponse {
        // se o seu TaskResponse for classe final sem no-args, remova o body do response do controller
        // ou ajuste para usar um builder real; aqui só precisamos que o Jackson serialize algo simples.
    }

    private com.viniss.todo.common.dto.CreateTaskRequest fakeCreateReq() {
        return new com.viniss.todo.common.dto.CreateTaskRequest(
                "Write tests", "Cobrir TaskAppService", "project-123", List.of("backend", "tests")
        );
    }

    private com.viniss.todo.common.dto.UpdateTaskRequest fakeUpdateReq() {
        return new com.viniss.todo.common.dto.UpdateTaskRequest(
                "Novo titulo", "DONE", "Nova desc", List.of("x", "y")
        );
    }

    @Test
    @DisplayName("POST /tasks → 201 quando backend cria")
    @WithMockUser
    void create_returns201() throws Exception {
        // retorna qualquer TaskResponse serializável (fake/real — não validamos o corpo aqui)
        given(taskClient.create(any())).willReturn(new FakeTaskResponse());

        mvc.perform(post("/tasks")
                        .header("X-Correlation-Id", "cid-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(fakeCreateReq())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /tasks/{id} → 200 quando backend atualiza")
    @WithMockUser
    void update_returns200() throws Exception {
        given(taskClient.update(eq("t-1"), any())).willReturn(new FakeTaskResponse());

        mvc.perform(put("/tasks/{id}", "t-1")
                        .header("X-Correlation-Id", "cid-456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(fakeUpdateReq())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /tasks/{id} → 200 quando backend aplica parcial")
    @WithMockUser
    void patch_returns200() throws Exception {
        given(taskClient.patch(eq("t-1"), any())).willReturn(new FakeTaskResponse());

        // parcial mínimo
        Map<String, Object> partial = Map.of("title", "Parcial");

        mvc.perform(patch("/tasks/{id}", "t-1")
                        .header("X-Correlation-Id", "cid-789")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(partial)))
                .andExpect(status().isOk());
    }
}
