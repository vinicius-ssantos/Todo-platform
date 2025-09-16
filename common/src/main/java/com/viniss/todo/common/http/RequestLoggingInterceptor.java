package com.viniss.todo.common.http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
  private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
  private static final String START_TS = "reqStart";

  @Override
  public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
    req.setAttribute(START_TS, System.nanoTime());
    log.info("request_start method={} path={} cid={}",
        req.getMethod(), req.getRequestURI(), MDC.get(CorrelationFilter.CID_KEY));
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, @Nullable Exception ex) {
    Long start = (Long) req.getAttribute(START_TS);
    long tookMs = start == null ? -1 : Math.round((System.nanoTime() - start) / 1_000_000.0);
    if (ex == null) {
      log.info("request_end method={} path={} status={} took_ms={} cid={}",
          req.getMethod(), req.getRequestURI(), res.getStatus(), tookMs, MDC.get(CorrelationFilter.CID_KEY));
    } else {
      log.warn("request_error method={} path={} status={} took_ms={} error={} cid={}",
          req.getMethod(), req.getRequestURI(), res.getStatus(), tookMs, ex.getClass().getSimpleName(),
          MDC.get(CorrelationFilter.CID_KEY));
    }
  }
}
