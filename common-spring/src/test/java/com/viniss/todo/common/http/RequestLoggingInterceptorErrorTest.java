package com.viniss.todo.common.http;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class RequestLoggingInterceptorErrorTest {

    private MockMvc mvc;
    private ListAppender<ILoggingEvent> appender;
    private Logger logger;

    @RestController
    static class BoomController {
        @GetMapping(value = "/boom", produces = MediaType.TEXT_PLAIN_VALUE)
        public String boom() {
            throw new IllegalStateException("boom");
        }
    }

    @BeforeEach
    void setup() {
        var interceptor = new RequestLoggingInterceptor(); // ajuste se necessário
        mvc = MockMvcBuilders.standaloneSetup(new BoomController())
                .addInterceptors(interceptor)
                .build();

        logger = (Logger) org.slf4j.LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    @DisplayName("Exceção → log em nível ERROR")
    void logsErrorOnException() throws Exception {
        mvc.perform(get("/boom")).andReturn();

        boolean hasError = appender.list.stream()
                .anyMatch(e -> e.getLevel() == Level.ERROR);
        assertTrue(hasError, "Esperava log em nível ERROR no RequestLoggingInterceptor");
    }
}
