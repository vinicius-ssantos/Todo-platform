package com.viniss.todo.common.http;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class RequestLoggingInterceptorInfoTest {

    private MockMvc mvc;
    private ListAppender<ILoggingEvent> appender;
    private Logger logger;

    @RestController
    static class OkController {
        @GetMapping(value = "/ok", produces = MediaType.TEXT_PLAIN_VALUE)
        public String ok() { return "ok"; }
    }

    @BeforeEach
    void setup() {
        var interceptor = new RequestLoggingInterceptor(); // ajuste se o construtor exigir deps
        mvc = MockMvcBuilders.standaloneSetup(new OkController())
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
    @DisplayName("2xx → log em nível INFO")
    void logsInfoOn2xx() throws Exception {
        mvc.perform(get("/ok")).andReturn();

        boolean hasInfo = appender.list.stream()
                .anyMatch(e -> e.getLevel() == Level.INFO);
        assertTrue(hasInfo, "Esperava log em nível INFO no RequestLoggingInterceptor");
    }
}
