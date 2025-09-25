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
  public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
    long tookMs = 0L;
    Object start = req.getAttribute("req.startNanos");
    if (start instanceof Long s) {
      tookMs = (System.nanoTime() - s) / 1_000_000;
    }

    int status = res.getStatus();
    boolean isError = (ex != null) || status >= 500;

    if (isError) {
      if (ex != null) {
        // último argumento Throwable vira stacktrace no logback/slf4j
        log.error("request_error method={} path={} status={} took_ms={} error={} cid={}",
                req.getMethod(), req.getRequestURI(), status, tookMs,
                ex.getClass().getSimpleName(), MDC.get("cid"), ex);
      } else {
        log.error("request_error method={} path={} status={} took_ms={} error={} cid={}",
                req.getMethod(), req.getRequestURI(), status, tookMs,
                "Http" + status, MDC.get("cid"));
      }
    } else {
      log.info("request_end method={} path={} status={} took_ms={} cid={}",
              req.getMethod(), req.getRequestURI(), status, tookMs, MDC.get("cid"));
    }
  }

}
