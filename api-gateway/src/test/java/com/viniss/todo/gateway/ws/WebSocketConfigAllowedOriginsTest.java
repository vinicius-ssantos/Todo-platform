package com.viniss.todo.gateway.ws;

import com.viniss.todo.gateway.WebSocketConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.Invocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Garante que WebSocketConfig registra o handler em "/ws" e aplica os allowed-origins
 * vindos da property app.cors.allowed-origins. Tolerante a chamada com único item "a,b".
 */
@SpringJUnitConfig(classes = {
        WebSocketConfig.class,
        WebSocketConfigAllowedOriginsTest.TestBeans.class
})
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://one.test,https://two.test"
})
class WebSocketConfigAllowedOriginsTest {

    @Configuration
    static class TestBeans {
        @Bean TaskWsHandler taskWsHandler() { return mock(TaskWsHandler.class); }
        @Bean WebSocketHandshakeInterceptor handshakeInterceptor() { return mock(WebSocketHandshakeInterceptor.class); }
    }

    @Autowired WebSocketConfig config;
    @Autowired TaskWsHandler taskWsHandler;
    @Autowired WebSocketHandshakeInterceptor handshakeInterceptor;

    @Test
    @DisplayName("registerWebSocketHandlers usa allowed-origins da property e registra /ws com interceptor")
    void allowedOriginsAndEndpoint() {
        WebSocketHandlerRegistry registry = mock(WebSocketHandlerRegistry.class);
        WebSocketHandlerRegistration registration = mock(WebSocketHandlerRegistration.class, RETURNS_SELF);
        when(registry.addHandler(any(), anyString())).thenReturn(registration);

        assertThatCode(() -> config.registerWebSocketHandlers(registry)).doesNotThrowAnyException();

        // /ws registrado com o handler injetado
        verify(registry).addHandler(taskWsHandler, "/ws");
        // interceptor aplicado
        verify(registration).addInterceptors(handshakeInterceptor);

        // Descobre a chamada a setAllowedOrigins(...) e normaliza os argumentos
        List<Invocation> invocs = new ArrayList<>(mockingDetails(registration).getInvocations());
        Invocation setAllowed = invocs.stream()
                .filter(i -> i.getMethod().getName().equals("setAllowedOrigins"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("setAllowedOrigins não foi chamado"));

        List<String> actual = new ArrayList<>();
        for (Object arg : setAllowed.getArguments()) {
            if (arg instanceof String s) {
                if (s.contains(",")) {
                    actual.addAll(Arrays.stream(s.split(",")).map(String::trim).toList());
                } else {
                    actual.add(s);
                }
            } else if (arg instanceof String[] arr) {
                actual.addAll(Arrays.asList(arr));
            }
        }

        assertThat(actual).containsExactly("http://one.test", "https://two.test");

        verifyNoMoreInteractions(registry, registration);
    }
}
