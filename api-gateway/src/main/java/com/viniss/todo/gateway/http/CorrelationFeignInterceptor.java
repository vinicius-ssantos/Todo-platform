package com.viniss.todo.gateway.http;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;

/** Propaga X-Correlation-Id do MDC para as chamadas Feign. */
public class CorrelationFeignInterceptor implements RequestInterceptor {
    public static final String HEADER = "X-Correlation-Id";

    @Override
    public void apply(RequestTemplate template) {
        String cid = MDC.get("cid");
        if (cid != null && !cid.isBlank()) {
            template.header(HEADER, cid);
        }
    }
}
