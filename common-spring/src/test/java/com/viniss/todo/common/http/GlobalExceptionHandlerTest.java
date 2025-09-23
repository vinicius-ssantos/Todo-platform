package com.viniss.todo.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeoutException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    MockMvc mvc;
    ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ---------- DTO & Controller de teste ----------
    static class CreateDto {
        @NotBlank
        @Size(max = 10)
        public String title;
    }

    @RestController
    @RequestMapping("/test-ex")
    @Validated
    static class TestController {

        @PostMapping("/bean")
        public String beanValidation(@RequestBody @Valid CreateDto dto) {
            return "ok";
        }

        @GetMapping("/param")
        public String paramValidation(@RequestParam @NotBlank @Size(max = 5) String q) {
            return "q=" + q;
        }

        @PostMapping("/json")
        public String expectsJson(@RequestBody CreateDto dto) {
            return "ok";
        }

        @GetMapping("/type")
        public String typeMismatch(@RequestParam Long id) {
            return "id=" + id;
        }

        @GetMapping("/409")
        public String conflict() {
            throw new DataIntegrityViolationException("dup");
        }

        @GetMapping("/timeout")
        public String timeout() throws Exception {
            throw new TimeoutException("slow");
        }
    }

    // ---------- Testes ----------
    @Test
    @DisplayName("Bean Validation (body) → 400 validation_error")
    void beanValidation_returns400() throws Exception {
        var body = new CreateDto();
        body.title = " ".repeat(3); // em branco

        mvc.perform(post("/test-ex/bean")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));
    }

    @Test
    @DisplayName("ConstraintViolation (param) → 400 invalid_request")
    void paramValidation_returns400() throws Exception {
        mvc.perform(get("/test-ex/param")
                        .param("q", "      ")) // em branco
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid_request"));
    }

    @Test
    @DisplayName("JSON malformado → 400 malformed_json")
    void jsonMalformed_returns400() throws Exception {
        mvc.perform(post("/test-ex/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("malformed_json"));
    }

    @Test
    @DisplayName("Type mismatch → 400 type_mismatch")
    void typeMismatch_returns400() throws Exception {
        mvc.perform(get("/test-ex/type")
                        .param("id", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("type_mismatch"));
    }

    @Test
    @DisplayName("DataIntegrityViolation → 409 conflict")
    void conflict_returns409() throws Exception {
        mvc.perform(get("/test-ex/409"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("conflict"));
    }

    @Test
    @DisplayName("TimeoutException → 504 upstream_timeout")
    void timeout_returns504() throws Exception {
        mvc.perform(get("/test-ex/timeout"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.code").value("upstream_timeout"));
    }
}
