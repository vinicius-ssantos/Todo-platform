package com.viniss.todo.common.http;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiError(
    String code,                 // ex: "validation_error", "not_found", "invalid_argument"
    String message,              // mensagem humana
    Map<String, Object> details, // campos -> erro, ou extras de contexto
    String correlationId,        // do MDC
    OffsetDateTime timestamp     // ISO-8601
) {
  public static ApiError of(String code, String message, Map<String,Object> details, String cid) {
    return new ApiError(code, message, details == null ? Map.of() : details, cid, OffsetDateTime.now());
  }
}
