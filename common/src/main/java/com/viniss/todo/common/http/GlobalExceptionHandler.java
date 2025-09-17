package com.viniss.todo.common.http;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.slf4j.MDC;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeoutException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  record ApiError(String code, String message, Map<String,Object> details,
                  String correlationId, OffsetDateTime timestamp) {}

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
    Map<String,Object> details = new LinkedHashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(err ->
      details.put(err.getField(), err.getDefaultMessage()));
    return response("validation_error", "Payload inválido", details, HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
    return response("invalid_argument", ex.getMessage(), null, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex) {
    return response("not_found", ex.getMessage(), null, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex) {
    return response("internal_error", "Erro inesperado", Map.of("reason", ex.getClass().getSimpleName()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(CallNotPermittedException.class)
  public ResponseEntity<ApiError> handleCircuitOpen(CallNotPermittedException ex) {
    return response("circuit_open", "Circuit breaker aberto para o serviço downstream", 
                   Map.of(), HttpStatus.SERVICE_UNAVAILABLE);
  }

  @ExceptionHandler(TimeoutException.class)
  public ResponseEntity<ApiError> handleTimeout(TimeoutException ex) {
    return response("upstream_timeout", "Tempo limite ao chamar o serviço downstream", 
                   Map.of(), HttpStatus.GATEWAY_TIMEOUT);
  }

  private ResponseEntity<ApiError> response(String code, String msg, Map<String,Object> details, HttpStatus s) {
    var err = new ApiError(code, msg, details == null ? Map.of() : details, MDC.get("cid"), OffsetDateTime.now());
    return ResponseEntity.status(s).body(err);
  }
}