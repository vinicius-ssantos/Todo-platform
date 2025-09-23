package com.viniss.todo.gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigTest.PingController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://allowed.test"
})
class SecurityConfigTest {

    @Autowired
    MockMvc mvc;

    @RestController
    @RequestMapping("/test")
    static class PingController {
        @GetMapping("/ping")
        public String ping() { return "pong"; }
    }

    @Test
    @DisplayName("Headers de segurança são adicionados (CSP, X-Frame-Options, X-Content-Type-Options, Referrer-Policy)")
    void securityHeadersPresent() throws Exception {
        mvc.perform(get("/test/ping")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()) // autentica para não cair em 401
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("Referrer-Policy"));
    }

    @Test
    @DisplayName("CORS: preflight OPTIONS responde com origin permitido e métodos")
    void corsPreflight() throws Exception {
        mvc.perform(options("/test/ping")
                        .header("Origin", "http://allowed.test")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://allowed.test"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }
}
