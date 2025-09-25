package com.viniss.todo.gateway.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viniss.todo.common.http.ApiError;
import feign.FeignException;
import feign.RetryableException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.ByteBuffer;
import java.util.Map;

@RestControllerAdvice
public class FeignExceptionHandler {

  private final ObjectMapper om;

  public FeignExceptionHandler(ObjectMapper om) {
    this.om = om;
  }

  @ExceptionHandler(FeignException.class)
  public ResponseEntity<?> handleFeign(FeignException ex) {
    int status = ex.status();

    // RetryableException ou qualquer status inválido → 503
    if (ex instanceof RetryableException || status < 100 || status > 999) {
      status = HttpStatus.SERVICE_UNAVAILABLE.value();
    }


    // Feign 12+: responseBody() -> Optional<ByteBuffer>
    byte[] bodyBytes = null;
    try {
      var opt = ex.responseBody(); // Optional<ByteBuffer>
      if (opt.isPresent()) {
        ByteBuffer buf = opt.get().asReadOnlyBuffer();
        bodyBytes = new byte[buf.remaining()];
        buf.get(bodyBytes);
      }
    } catch (Throwable ignore) {
      // versões antigas/variações: se precisar, dá para tentar ex.content()
      // if (bodyBytes == null && ex.content() != null) bodyBytes = ex.content();
    }

    // Tenta propagar ApiError do upstream
    if (bodyBytes != null && bodyBytes.length > 0) {
      try {
        ApiError upstream = om.readValue(bodyBytes, ApiError.class);
        return ResponseEntity.status(status).body(upstream);
      } catch (Exception ignore) {
        // corpo não era ApiError - cai no fallback
      }
    }

    // Fallback padrão
    ApiError fallback = ApiError.of(
            "upstream_error",
            ex.getMessage(),
            Map.of("upstreamStatus", status),
            MDC.get("cid")
    );
    return ResponseEntity.status(status).body(fallback);
  }
}
