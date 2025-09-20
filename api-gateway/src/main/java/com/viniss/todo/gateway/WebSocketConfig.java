package com.viniss.todo.gateway;

import com.viniss.todo.gateway.ws.TaskWsHandler;
import com.viniss.todo.gateway.ws.WebSocketHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.List;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TaskWsHandler taskWsHandler;
    private final WebSocketHandshakeInterceptor handshakeInterceptor;


    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private List<String> allowedOrigins;

    public WebSocketConfig(TaskWsHandler taskWsHandler,
                           WebSocketHandshakeInterceptor handshakeInterceptor) {
        this.taskWsHandler = taskWsHandler;
        this.handshakeInterceptor = handshakeInterceptor;
    }


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(taskWsHandler, "/ws")
                .setAllowedOrigins(allowedOrigins.toArray(new String[0]))
                .addInterceptors(handshakeInterceptor);
    }

}
