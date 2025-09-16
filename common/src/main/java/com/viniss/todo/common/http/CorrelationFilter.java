package com.viniss.todo.common.http;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationFilter implements Filter {
  public static final String CID_KEY = "cid";
  public static final String CID_HEADER = "Correlation-Id";

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest r = (HttpServletRequest) req;
    String cid = r.getHeader(CID_HEADER);
    if (cid == null || cid.isBlank()) {
      cid = UUID.randomUUID().toString();
    }
    MDC.put(CID_KEY, cid);
    try {
      chain.doFilter(req, res);
    } finally {
      MDC.remove(CID_KEY);
    }
  }
}
