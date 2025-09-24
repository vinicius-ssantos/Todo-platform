package com.viniss.todo.gateway.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.viniss.todo.common.dto.CreateTaskRequest;
import com.viniss.todo.common.dto.TaskResponse;
import com.viniss.todo.common.http.CorrelationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TaskController.class)
class TaskControllerCorrelationIdTest {

    @Autowired MockMvc mvc;

    @MockBean TaskClient client;

    @Autowired ObjectMapper om;

    @TestConfiguration
    static class Cfg {
        @Bean ObjectMapper objectMapper() {
            return new ObjectMapper().registerModule(new JavaTimeModule());
        }
        // Ajuste o pacote/classe do seu filtro real:
        @Bean
        CorrelationFilter correlationFilter() {
            return new CorrelationFilter();
        }
    }

    private TaskResponse sample() {
        return new TaskResponse("t-1", "proj-1", "Nova", "desc", "OPEN", null, null, List.of());
    }

    @Test
    @DisplayName("Preserva Correlation-Id enviado pelo cliente")
    void preservesIncomingCorrelationId() throws Exception {
        when(client.create(any())).thenReturn(sample());

        String headerName = existingHeaderNameOrDefault(null); // "X-Correlation-Id" ou "Correlation-Id"
        String cid = "11111111-1111-1111-1111-111111111111";

        mvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(headerName, cid)
                        .content(om.writeValueAsString(new CreateTaskRequest("Nova","desc","proj-1", List.of()))))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    var h1 = result.getResponse().getHeader("X-Correlation-Id");
                    var h2 = result.getResponse().getHeader("Correlation-Id");
                    String found = h1 != null ? h1 : h2;
                    if (found == null) throw new AssertionError("Correlation-Id header ausente");
                    if (!cid.equals(found)) throw new AssertionError("Correlation-Id não preservado");
                });
    }

    @Test
    @DisplayName("Gera Correlation-Id quando ausente")
    void generatesCorrelationIdWhenMissing() throws Exception {
        when(client.create(any())).thenReturn(sample());

        mvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new CreateTaskRequest("Nova","desc","proj-1", List.of()))))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    var h1 = result.getResponse().getHeader("X-Correlation-Id");
                    var h2 = result.getResponse().getHeader("Correlation-Id");
                    String found = h1 != null ? h1 : h2;
                    if (found == null) throw new AssertionError("Correlation-Id header ausente");
                    // precisa ser um UUID válido
                    UUID.fromString(found);
                });
    }

    // Utilitário: se você já sabe o header correto do seu projeto, troque para um literal.
    private static String existingHeaderNameOrDefault(@Nullable String configured) {
        return configured != null ? configured : "X-Correlation-Id";
    }
}
