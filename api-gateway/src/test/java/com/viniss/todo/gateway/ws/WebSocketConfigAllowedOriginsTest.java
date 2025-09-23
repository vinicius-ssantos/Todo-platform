package com.viniss.todo.gateway.ws;

import com.viniss.todo.gateway.WebSocketConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Garante que WebSocketConfig registra o handler em "/ws" e aplica os allowed-origins
 * vindos da property app.cors.allowed-origins.
 */
@SpringJUnitConfig(classes = {
        WebSocketConfig.class,
        WebSocketConfigAllowedOriginsTest.TestBeans.class
})
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://one.test,https://two.test" // <-- chave correta
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

        verify(registry).addHandler(taskWsHandler, "/ws");
        verify(registration).setAllowedOrigins("http://one.test", "https://two.test");
        verify(registration).addInterceptors(handshakeInterceptor);
        verifyNoMoreInteractions(registry, registration);
    }
}
