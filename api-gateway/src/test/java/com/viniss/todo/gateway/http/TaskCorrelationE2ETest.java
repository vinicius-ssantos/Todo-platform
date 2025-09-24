package com.viniss.todo.gateway.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E: garante que o gateway:
 *  - preserva o X-Correlation-Id enviado pelo cliente;
 *  - gera um novo UUID quando o header não é enviado;
 *  - propaga esse header para o serviço downstream (Feign → MockWebServer).
 *
 * IMPORTANTE:
 *  - Ajuste a propriedade "<feignName>.url" se o nome do seu @FeignClient não for "task-service".
 *    Ex.: se for @FeignClient(name = "todo-tasks"), troque para "todo-tasks.url" no DynamicPropertySource.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // Desabilita segurança somente nesta classe de teste (foco no Correlation-Id)
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",

                // Evita subir listener Kafka nos testes
                "spring.kafka.listener.auto-startup=false",
                "spring.kafka.bootstrap-servers=localhost:0"
        }
)
class TaskCorrelationE2ETest {

    static MockWebServer downstream;

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

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
        // Se o seu @FeignClient tiver outro name, ajuste aqui: "<name>.url"
        registry.add("task-service.url", () -> "http://localhost:" + downstream.getPort());
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

    @Test
    void preserves_and_propagates_client_correlation_id() throws Exception {
        downstream.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(sampleDownstreamJson()));

        String cid = "11111111-1111-1111-1111-111111111111";

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Correlation-Id", cid);

        var payload = Map.of(
                "title", "Nova",
                "description", "desc",
                "projectId", "proj-1",
                "labels", List.of("a", "b")
        );

        var resp = rest.postForEntity("http://localhost:" + port + "/tasks",
                new HttpEntity<>(payload, headers), Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Header deve estar presente na resposta do gateway
        var returned = firstNonNull(resp.getHeaders().getFirst("X-Correlation-Id"),
                resp.getHeaders().getFirst("Correlation-Id"));
        assertThat(returned).isEqualTo(cid);

        // Validar propagação no request enviado ao downstream
        var recorded = downstream.takeRequest();
        String forwarded = firstNonNull(recorded.getHeader("X-Correlation-Id"),
                recorded.getHeader("Correlation-Id"));
        assertThat(forwarded).isEqualTo(cid);
    }

    @Test
    void generates_and_propagates_when_missing() throws Exception {
        downstream.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(sampleDownstreamJson()));

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var payload = Map.of(
                "title", "Nova",
                "description", "desc",
                "projectId", "proj-1",
                "labels", List.of("a", "b")
        );

        var resp = rest.postForEntity("http://localhost:" + port + "/tasks",
                new HttpEntity<>(payload, headers), Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Gateway deve gerar um UUID válido
        var returned = firstNonNull(resp.getHeaders().getFirst("X-Correlation-Id"),
                resp.getHeaders().getFirst("Correlation-Id"));
        assertThat(returned).isNotBlank();
        UUID.fromString(returned);

        // E deve propagar ao downstream
        var recorded = downstream.takeRequest();
        String forwarded = firstNonNull(recorded.getHeader("X-Correlation-Id"),
                recorded.getHeader("Correlation-Id"));
        assertThat(forwarded).isEqualTo(returned);
    }

    private static String firstNonNull(String a, String b) {
        return a != null ? a : b;
    }
}
