package com.viniss.todo.gateway.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AutoConfigureMockMvc
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // Evita subir Kafka/consumidores nos testes
                "spring.kafka.listener.auto-startup=false",
                "spring.kafka.bootstrap-servers=localhost:0"
        }
)
class TaskCorrelationE2ETest {

    static MockWebServer downstream;

    @Autowired
    MockMvc mvc;

    static final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeAll
    static void startServer() throws IOException {
        downstream = new MockWebServer();
        downstream.start();
    }

    @AfterAll
    static void stopServer() throws IOException {
        downstream.shutdown();
    }

    @DynamicPropertySource
    static void overrideFeignUrl(DynamicPropertyRegistry registry) {
        // ATENÇÃO: ajuste a chave "<name>.url" para o "name" usado no seu @FeignClient
        // Ex.: se for @FeignClient(name="todo-tasks") => "todo-tasks.url"
        registry.add("clients.task.url", () -> "http://localhost:" + downstream.getPort());

    }

    private static String sampleDownstreamJson() throws Exception {
        var now = OffsetDateTime.parse("2025-01-01T00:00:00Z");
        var body = Map.of(
                "id", "t-9",
                "projectId", "proj-1",
                "title", "Nova",
                "description", "desc",
                "status", "OPEN",
                "createdAt", now.toString(),
                "updatedAt", now.toString(),
                "labels", List.of("a", "b")
        );
        return om.writeValueAsString(body);
    }

    private static String payloadJson() throws Exception {
        var payload = Map.of(
                "title", "Nova",
                "description", "desc",
                "projectId", "proj-1",
                "labels", List.of("a", "b")
        );
        return om.writeValueAsString(payload);
    }

    /** Post-processor que injeta um JWT com escopo/autoridade aceitos pela sua SecurityConfig. */
    private static RequestPostProcessor jwtWrite() {
        return jwt().jwt(j -> {
            j.claim("scope", "todo.write");
            j.claim("scp", java.util.List.of("todo.write"));
            j.subject("tester");
        }).authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("SCOPE_todo.write"));
    }

    @Test
    void preserves_and_propagates_client_correlation_id() throws Exception {
        downstream.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(sampleDownstreamJson()));

        String cid = "11111111-1111-1111-1111-111111111111";

        MvcResult res = mvc.perform(post("/tasks")
                        .with(jwtWrite())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-Id", cid)
                        .content(payloadJson()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Correlation-Id deve estar no response
        String returned = firstNonNull(
                res.getResponse().getHeader("X-Correlation-Id"),
                res.getResponse().getHeader("Correlation-Id")
        );
        assertThat(returned).isEqualTo(cid);

        // E deve ser propagado ao downstream (Feign -> MockWebServer)
        var recorded = downstream.takeRequest();
        String forwarded = firstNonNull(
                recorded.getHeader("X-Correlation-Id"),
                recorded.getHeader("Correlation-Id")
        );
        assertThat(forwarded).isEqualTo(cid);
    }

    @Test
    void generates_and_propagates_when_missing() throws Exception {
        downstream.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(sampleDownstreamJson()));

        MvcResult res = mvc.perform(post("/tasks")
                        .with(jwtWrite())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadJson()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Deve gerar um UUID no response
        String returned = firstNonNull(
                res.getResponse().getHeader("X-Correlation-Id"),
                res.getResponse().getHeader("Correlation-Id")
        );
        assertThat(returned).isNotBlank();
        UUID.fromString(returned);

        // E propagar ao downstream
        var recorded = downstream.takeRequest();
        String forwarded = firstNonNull(
                recorded.getHeader("X-Correlation-Id"),
                recorded.getHeader("Correlation-Id")
        );
        assertThat(forwarded).isEqualTo(returned);
    }

    private static String firstNonNull(String a, String b) {
        return a != null ? a : b;
    }
}
