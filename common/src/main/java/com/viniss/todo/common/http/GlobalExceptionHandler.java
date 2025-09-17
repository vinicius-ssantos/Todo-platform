package com.viniss.todo.common.http;

import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // 422 - Bean Validation no body (@Valid)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    Map<String,Object> details = new LinkedHashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(err -> details.put(err.getField(), err.getDefaultMessage()));
    ex.getBindingResult().getGlobalErrors().forEach(err -> details.put(err.getObjectName(), err.getDefaultMessage()));
    return response("validation_error", "Payload inválido", details, HttpStatus.UNPROCESSABLE_ENTITY);
  }

  // 400 - Bean Validation em params/path (@Validated)
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
    Map<String,Object> details = new LinkedHashMap<>();
    ex.getConstraintViolations().forEach(v -> details.put(v.getPropertyPath().toString(), v.getMessage()));
    return response("invalid_request", "Parâmetros inválidos", details, HttpStatus.BAD_REQUEST);
  }

  // 400 - JSON malformado / tipo errado
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex) {
    return response("malformed_json", "Corpo da requisição inválido ou malformado", Map.of(), HttpStatus.BAD_REQUEST);
  }

  // 400 - parâmetro obrigatório ausente
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex) {
    return response("missing_parameter", "Parâmetro obrigatório ausente", Map.of(ex.getParameterName(), "required"), HttpStatus.BAD_REQUEST);
  }

  // 400 - conversão de tipo em path/query
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    return response("type_mismatch", "Tipo inválido para parâmetro", Map.of(
        "name", ex.getName(),
        "value", String.valueOf(ex.getValue()),
        "requiredType", ex.getRequiredType() == null ? "unknown" : ex.getRequiredType().getSimpleName()
    ), HttpStatus.BAD_REQUEST);
  }

  // 404 - recurso não encontrado (use NoSuchElementException no serviço)
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex) {
    return response("not_found", ex.getMessage() == null ? "Recurso não encontrado" : ex.getMessage(), Map.of(), HttpStatus.NOT_FOUND);
  }

  // 409 - violações de unicidade/integridade
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
    return response("conflict", "Violação de integridade de dados", Map.of("reason", ex.getMostSpecificCause().getClass().getSimpleName()), HttpStatus.CONFLICT);
  }

  // 400 - uso indevido de argumentos
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
    return response("invalid_argument", ex.getMessage(), Map.of(), HttpStatus.BAD_REQUEST);
  }

  // 500 - fallback
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex) {
    return response("internal_error", "Erro inesperado", Map.of("error", ex.getClass().getSimpleName()), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<ApiError> response(String code, String msg, Map<String,Object> details, HttpStatus s) {
    var err = new ApiError(code, msg, details == null ? Map.of() : details, MDC.get("cid"), OffsetDateTime.now());
    return ResponseEntity.status(s).body(err);
  }
}