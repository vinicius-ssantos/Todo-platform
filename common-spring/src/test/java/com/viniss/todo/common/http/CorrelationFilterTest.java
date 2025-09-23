package com.viniss.todo.common.http;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CorrelationFilterTest {

    @Test
    @DisplayName("Quando header não vem, gera UUID e limpa o MDC após a requisição")
    void generatesAndCleans() throws IOException, ServletException {
        CorrelationFilter f = new CorrelationFilter();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader(CorrelationFilter.CID_HEADER)).thenReturn(null);

        // Durante a chamada do chain, o MDC deve estar preenchido
        doAnswer(inv -> {
            String cidInChain = MDC.get(CorrelationFilter.CID_KEY);
            assertThat(cidInChain).isNotBlank();
            // deve ser UUID válido
            UUID.fromString(cidInChain);
            return null;
        }).when(chain).doFilter(req, res);

        f.doFilter(req, res, chain);

        // Após sair do filtro, o MDC deve estar limpo
        assertThat(MDC.get(CorrelationFilter.CID_KEY)).isNull();

        verify(chain, times(1)).doFilter(req, res);
    }

    @Test
    @DisplayName("Quando header vem, usa o valor do header e limpa depois")
    void usesHeader() throws IOException, ServletException {
        CorrelationFilter f = new CorrelationFilter();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader(CorrelationFilter.CID_HEADER)).thenReturn("cid-abc");

        doAnswer(inv -> {
            assertThat(MDC.get(CorrelationFilter.CID_KEY)).isEqualTo("cid-abc");
            return null;
        }).when(chain).doFilter(req, res);

        f.doFilter(req, res, chain);
        assertThat(MDC.get(CorrelationFilter.CID_KEY)).isNull();
    }
}
