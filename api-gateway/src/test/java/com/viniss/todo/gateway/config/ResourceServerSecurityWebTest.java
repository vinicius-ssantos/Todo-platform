package com.viniss.todo.gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que a SecurityConfig está como Resource Server (JWT):
 * - 401 sem JWT
 * - 200 com JWT
 */
@WebMvcTest(controllers = TestSecureController.class)
@Import({ SecurityConfig.class, ResourceServerSecurityWebTest.JwtStubConfig.class })
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://allowed.test"
})
class ResourceServerSecurityWebTest {

    @Autowired MockMvc mvc;

    @Test
    @DisplayName("Sem JWT → 401 Unauthorized")
    void unauthenticated_returns401() throws Exception {
        mvc.perform(get("/secure/ping")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Com JWT → 200 OK")
    void withJwt_returns200() throws Exception {
        mvc.perform(get("/secure/ping")
                        .with(jwt()) // autentica
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /** Stub de JwtDecoder para satisfazer oauth2ResourceServer().jwt() na SecurityConfig */
    @TestConfiguration
    static class JwtStubConfig {
        @Bean
        JwtDecoder jwtDecoder() {
            return token -> org.springframework.security.oauth2.jwt.Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "tester")
                    .build();
        }
    }
}

/** Controller mínimo protegido pela chain real da SecurityConfig */
@RestController
@RequestMapping("/secure")
class TestSecureController {
    @GetMapping(value = "/ping", produces = MediaType.APPLICATION_JSON_VALUE)
    public String ping() { return "{\"ok\":true}"; }
}
