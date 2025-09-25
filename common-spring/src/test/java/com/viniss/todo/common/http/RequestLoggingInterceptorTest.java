package com.viniss.todo.common.http;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testa logs do RequestLoggingInterceptor, com CorrelationFilter e GlobalExceptionHandler. */
class RequestLoggingInterceptorTest {

    MockMvc mvc;
    ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setup() {
        // MockMvc standalone com controller de teste, interceptor e filtro de correlação
        var mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mvc = MockMvcBuilders
                .standaloneSetup(new TestCtrl())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .addInterceptors(new RequestLoggingInterceptor())
                .addFilters(new CorrelationFilter())
                .build();

        // Captura de logs do interceptor
        Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);
    }

    @RestController
    @RequestMapping("/logtest")
    static class TestCtrl {
        @GetMapping("/ok")
        public String ok() { return "ok"; }

        @GetMapping("/boom")
        public String boom() { throw new RuntimeException("boom"); }
    }

    @Test
    @DisplayName("preHandle e afterCompletion logam request_start e request_end com CID")
    void logs_ok_flow() throws Exception {
        mvc.perform(get("/logtest/ok").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Deve ter pelo menos 2 eventos: start e end
        assertThat(appender.list).hasSizeGreaterThanOrEqualTo(2);

        var msgConcat = appender.list.stream().map(ILoggingEvent::getFormattedMessage).reduce("", (a,b) -> a + " | " + b);
        // Verifica conteúdo das mensagens
        assertThat(msgConcat).contains("request_start");
        assertThat(msgConcat).contains("request_end");
        assertThat(msgConcat).contains("method=GET");
        assertThat(msgConcat).contains("path=/logtest/ok");

        // Verifica presença de CID no MDC
        boolean hasCidInAll = appender.list.stream().allMatch(e -> e.getMDCPropertyMap().get("cid") != null);
        assertThat(hasCidInAll).isTrue();
    }

    @Test
    @DisplayName("Mesmo com exceção, afterCompletion loga request_end e handler retorna 500")
    void logs_on_exception() throws Exception {
        // Captura os logs do interceptor
        Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            // Dispara a rota que lança exceção e é tratada pelo @ControllerAdvice de teste (status 500)
            mvc.perform(get("/logtest/boom"))
                    .andExpect(status().isInternalServerError())
                    .andReturn();

            // Consolida as mensagens para facilitar asserts “contains”
            String all = appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .reduce("", (a, b) -> a + " | " + b);

            // ✅ Agora esperamos request_error (e NÃO request_end) em caso de 500/exception
            assertThat(all).contains("request_start");
            assertThat(all).contains("request_error");
            assertThat(all).contains("status=500");
            assertThat(all).doesNotContain("request_end");

            // (opcional) garante nível ERROR em pelo menos um evento
            assertThat(appender.list)
                    .anySatisfy(e -> {
                        assertThat(e.getLevel()).isEqualTo(Level.ERROR);
                        assertThat(e.getFormattedMessage()).contains("request_error");
                    });

        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
