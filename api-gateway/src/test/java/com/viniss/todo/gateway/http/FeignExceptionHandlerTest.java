package com.viniss.todo.gateway.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.viniss.todo.common.http.ApiError;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FeignExceptionHandlerTest {

    // 👇 ObjectMapper configurado para datas Java 8 (OffsetDateTime etc.)
    private static final ObjectMapper OM = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private FeignException errorWithBody(int status, ApiError body) throws Exception {
        byte[] json = OM.writeValueAsBytes(body);

        Request req = Request.create(
                Request.HttpMethod.GET,
                "http://example/test",
                Collections.emptyMap(),
                null, // corpo da requisição (não usamos)
                StandardCharsets.UTF_8,
                null
        );

        Response resp = Response.builder()
                .status(status)
                .reason("ERR")
                .request(req)
                .headers(Collections.singletonMap("Content-Type", Collections.singletonList("application/json")))
                .body(json) // corpo JSON do upstream simulando ApiError
                .build();

        return FeignException.errorStatus("GET http://example/test", resp);
    }

    private FeignException errorWithoutBody(int status) {
        Request req = Request.create(
                Request.HttpMethod.GET,
                "http://example/test",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                null
        );

        Response resp = Response.builder()
                .status(status)
                .reason("ERR")
                .request(req)
                .build();

        return FeignException.errorStatus("GET http://example/test", resp);
    }

    @Test
    @DisplayName("Quando upstream retorna ApiError JSON, propaga como está")
    void propagateApiErrorBody() throws Exception {
        FeignExceptionHandler handler = new FeignExceptionHandler();
        ApiError upstream = ApiError.of("not_found", "X", Map.of("k","v"), "cid-1");

        ResponseEntity<?> resp = handler.handleFeign(errorWithBody(404, upstream));

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        assertThat(resp.getBody()).isInstanceOf(ApiError.class);

        ApiError body = (ApiError) resp.getBody();
        assertThat(body.code()).isEqualTo("not_found");
        assertThat(body.details()).containsEntry("k","v");
        assertThat(body.correlationId()).isEqualTo("cid-1");
    }

    @Test
    @DisplayName("Sem corpo ApiError → retorna erro padrão com status propagado")
    void fallbackWhenNotApiError() {
        FeignExceptionHandler handler = new FeignExceptionHandler();

        ResponseEntity<?> resp = handler.handleFeign(errorWithoutBody(500));

        assertThat(resp.getStatusCode().value()).isEqualTo(500);
        assertThat(resp.getBody()).isInstanceOf(ApiError.class);

        ApiError body = (ApiError) resp.getBody();
        // Ajuste para o código real do seu handler no fallback:
        assertThat(body.code()).isIn("upstream_error", "internal_error");
    }
}
