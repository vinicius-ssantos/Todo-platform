package com.viniss.todo.common.http;

import com.viniss.todo.common.http.ApiError;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** Testa diretamente o método do handler, evitando o construtor privado do Resilience4j. */
class GlobalExceptionHandlerCircuitTest {

    @Test
    @DisplayName("CallNotPermittedException → 503 circuit_open")
    void circuitOpen_returns503() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // NÃO use "new CallNotPermittedException(...)": construtor é privado.
        CallNotPermittedException ex = mock(CallNotPermittedException.class);

        ResponseEntity<ApiError> resp = handler.handleCircuitOpen(ex);

        assertThat(resp.getStatusCodeValue()).isEqualTo(503);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().code()).isEqualTo("circuit_open");
    }
}
