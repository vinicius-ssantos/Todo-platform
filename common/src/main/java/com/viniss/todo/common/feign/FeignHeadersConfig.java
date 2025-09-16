package com.viniss.todo.common.feign;

import com.viniss.todo.common.http.CorrelationFilter;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignHeadersConfig {
  @Bean
  public RequestInterceptor correlationIdInterceptor() {
    return (RequestTemplate template) -> {
      String cid = MDC.get(CorrelationFilter.CID_KEY);
      if (cid != null && !cid.isBlank()) {
        template.header(CorrelationFilter.CID_HEADER, cid);
      }
    };
  }
}
