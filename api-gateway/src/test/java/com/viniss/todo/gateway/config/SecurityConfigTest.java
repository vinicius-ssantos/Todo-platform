package com.viniss.todo.gateway.config;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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

    @Autowired MockMvc mvc;

    /** Controller mínimo só para este teste (evita 404) */
    @RestController
    @RequestMapping("/test")
    static class PingController {
        @GetMapping(value = "/ping", produces = MediaType.APPLICATION_JSON_VALUE)
        public String ping() { return "{\"ok\":true}"; }
    }

    @Disabled
    @Test
    @DisplayName("Headers de segurança presentes")
    void securityHeadersPresent() throws Exception {
        mvc.perform(get("/test/ping")
                        .with(jwt())                       // autentica para não cair em 401
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("Referrer-Policy"));
    }

    @Test
    @DisplayName("CORS preflight OK para origin permitido")
    void corsPreflight() throws Exception {
        mvc.perform(options("/test/ping")
                        .header("Origin", "http://allowed.test")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://allowed.test"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }
}
