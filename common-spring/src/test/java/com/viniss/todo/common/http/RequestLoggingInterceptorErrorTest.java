package com.viniss.todo.common.http;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RequestLoggingInterceptorErrorTest {

    private MockMvc mvc;
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        // Captura logs do interceptor
        logger = (Logger) LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        // Sobe apenas o necessário para o teste
        mvc = MockMvcBuilders
                .standaloneSetup(new BoomController())
                .setControllerAdvice(new TestExceptionHandler()) // 👈 trata exceções → 500
                .addInterceptors(new RequestLoggingInterceptor())
                .build();
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @RestController
    static class BoomController {
        @GetMapping("/boom")
        String boom() {
            throw new IllegalStateException("boom");
        }
    }

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(Exception.class)
        ResponseEntity<Map<String, Object>> handle(Exception ex) {
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", ex.getClass().getSimpleName()));
        }
    }

    @Test
    @DisplayName("Exceção → log em nível ERROR")
    void logsErrorOnException() throws Exception {
        mvc.perform(get("/boom"))
                .andExpect(status().isInternalServerError()) // ✅ não deixa o teste explodir
                .andReturn();

        boolean hasError = appender.list.stream()
                .anyMatch(e -> e.getLevel() == Level.ERROR);
        assertTrue(hasError, "Esperava log em nível ERROR no RequestLoggingInterceptor");
    }
}
