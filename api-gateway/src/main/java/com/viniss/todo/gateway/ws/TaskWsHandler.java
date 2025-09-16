package com.viniss.todo.gateway.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class TaskWsHandler extends TextWebSocketHandler {
  private final WsSessions sessions;
  private final ObjectMapper mapper = new ObjectMapper();

  public TaskWsHandler(WsSessions sessions) {
    this.sessions = sessions;
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    sessions.unsubscribeAll(session);
    super.afterConnectionClosed(session, status);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    JsonNode node = mapper.readTree(message.getPayload());
    if (node.has("type") && "subscribe".equals(node.get("type").asText())) {
      String projectId = node.path("projectId").asText(null);
      if (projectId != null) {
        sessions.subscribe(projectId, session);
        session.sendMessage(new TextMessage("{\"type\":\"subscribed\",\"projectId\":\""+projectId+"\"}"));
      }
    }
  }
}
