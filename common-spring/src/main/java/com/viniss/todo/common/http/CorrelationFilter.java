package com.viniss.todo.common.http;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;



@Component
public class CorrelationFilter implements Filter {
  public static final String CID_KEY = "cid";
  public static final String CID_HEADER = "Correlation-Id";
  public static final String CID_HEADER_ALT = "X-Correlation-Id";

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
          throws IOException, ServletException {

    HttpServletRequest r = (HttpServletRequest) req;
    HttpServletResponse resp = (HttpServletResponse) res;

    String cid = firstNonBlank(r.getHeader(CID_HEADER_ALT), r.getHeader(CID_HEADER));
    if (cid == null || cid.isBlank()) {
      cid = UUID.randomUUID().toString();
    }

    MDC.put(CID_KEY, cid);
    try {
      // **Devolve nos dois cabeçalhos para compatibilidade**
      resp.setHeader(CID_HEADER, cid);
      resp.setHeader(CID_HEADER_ALT, cid);
      chain.doFilter(req, res);
    } finally {
      MDC.remove(CID_KEY);
    }
  }
  private static String firstNonBlank(String a, String b) {
    return (a != null && !a.isBlank()) ? a : b;
  }
}
