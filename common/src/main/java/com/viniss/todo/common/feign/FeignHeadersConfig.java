package com.viniss.todo.common.feign;

import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignHeadersConfig {
  @Bean
  public RequestInterceptor correlationIdInterceptor() {
    return template -> {
      String cid = MDC.get("cid");
      if (cid != null) template.header("Correlation-Id", cid);
    };
  }
}
