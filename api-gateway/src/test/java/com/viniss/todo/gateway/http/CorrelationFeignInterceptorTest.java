package com.viniss.todo.gateway.http;

import feign.RequestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationFeignInterceptorTest {

    @Test
    @DisplayName("Quando MDC tem cid, adiciona X-Correlation-Id")
    void addsHeader() {
        try {
            MDC.put("cid", "cid-abc");
            var it = new CorrelationFeignInterceptor();
            var tpl = new RequestTemplate();
            it.apply(tpl);
            assertThat(tpl.headers().get(CorrelationFeignInterceptor.HEADER))
                    .containsExactly("cid-abc");
        } finally {
            MDC.clear();
        }
    }

    @Test
    @DisplayName("Sem cid, não adiciona header")
    void noHeader() {
        try {
            MDC.clear();
            var it = new CorrelationFeignInterceptor();
            var tpl = new RequestTemplate();
            it.apply(tpl);
            assertThat(tpl.headers()).doesNotContainKey(CorrelationFeignInterceptor.HEADER);
        } finally {
            MDC.clear();
        }
    }
}
