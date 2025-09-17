package com.viniss.todo.gateway.http;

import feign.Feign;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ResilienceFeignConfig {

  @Bean
  public ScheduledExecutorService feignTimeLimiterScheduler() {
    // pequeno pool só para os TimeLimiters do Feign
    return Executors.newScheduledThreadPool(2);
  }

  @Bean
  public Feign.Builder feignBuilder(CircuitBreakerRegistry cbRegistry,
                                    RetryRegistry retryRegistry,
                                    TimeLimiterRegistry tlRegistry,
                                    ScheduledExecutorService scheduler) {

    var cb   = cbRegistry.circuitBreaker("task");
    var rty  = retryRegistry.retry("task");
    var tlim = tlRegistry.timeLimiter("task");

    FeignDecorators decorators = FeignDecorators.builder()
        .withCircuitBreaker(cb)
        .withRetry(rty)
        .withTimeLimiter(tlim, scheduler)
        .build();

    return Resilience4jFeign.builder(decorators);
  }
}
