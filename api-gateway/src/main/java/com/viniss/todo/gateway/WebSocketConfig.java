package com.viniss.todo.gateway;

import com.viniss.todo.gateway.ws.TaskWsHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
  private final TaskWsHandler taskWsHandler;

  public WebSocketConfig(TaskWsHandler taskWsHandler) {
    this.taskWsHandler = taskWsHandler;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(taskWsHandler, "/ws").setAllowedOrigins("*");
  }
}
