package com.viniss.todo.gateway.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.viniss.todo.common.dto.ActivityResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ActivityControllerWebTest {

    MockMvc mvc;
    ObjectMapper om;
    ActivityClient client;

    @BeforeEach
    void setup() {
        om = new ObjectMapper().registerModule(new JavaTimeModule());
        client = Mockito.mock(ActivityClient.class);

        mvc = MockMvcBuilders
                .standaloneSetup(new ActivityController(client))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(om))
                .build();
    }

    private ActivityResponse activity(long id, String taskId, String projectId, String title, String status) {
        ActivityResponse a = new ActivityResponse();
        a.setId(id);
        a.setTaskId(taskId);
        a.setProjectId(projectId);
        a.setType("TASK_EVENT");
        a.setAt(Instant.parse("2025-01-01T12:00:00Z"));
        a.setTitle(title);
        a.setStatus(status);
        return a;
    }

    @Test
    @DisplayName("GET /activities/project/{projectId} → 200 OK (proxy para ActivityClient.listByProject)")
    void byProject_returns200() throws Exception {
        String projectId = "proj-1";
        when(client.listByProject(eq(projectId)))
                .thenReturn(List.of(
                        activity(1L, "t-1", projectId, "Criada", "OPEN"),
                        activity(2L, "t-1", projectId, "Atualizada", "IN_PROGRESS")
                ));

        mvc.perform(get("/activities/project/{projectId}", projectId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].projectId").value(projectId))
                .andExpect(jsonPath("$[0].title").value("Criada"))
                .andExpect(jsonPath("$[1].status").value("IN_PROGRESS"));
    }
}
