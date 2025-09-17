package com.viniss.todo.gateway.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viniss.todo.common.http.ApiError;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import feign.FeignException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestControllerAdvice
public class FeignExceptionHandler {
  private final ObjectMapper om = new ObjectMapper();

  @ExceptionHandler(FeignException.class)
  public ResponseEntity<?> handleFeign(FeignException ex) {
    int status = ex.status() <= 0 ? 502 : ex.status();

    // Tenta desserializar o corpo como ApiError
    try {
      if (ex.responseBody().isPresent()) {
        var buf = ex.responseBody().get().array();
        var json = new String(buf, StandardCharsets.UTF_8);
        var apiError = om.readValue(json, ApiError.class);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(apiError);
      }
    } catch (Exception ignore) { /* corpo não é ApiError */ }

    var body = ApiError.of("upstream_error",
        "Erro ao chamar serviço interno",
        Map.of("service", "task-service", "status", status),
        MDC.get("cid"));
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
  }
}
