package com.viniss.todo.gateway.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class WsSessions {
  private final Map<String, Set<WebSocketSession>> byProject = new ConcurrentHashMap<>();

  public void subscribe(String projectId, WebSocketSession session) {
    byProject.computeIfAbsent(projectId, k -> new CopyOnWriteArraySet<>()).add(session);
  }

  public void unsubscribeAll(WebSocketSession session) {
    byProject.values().forEach(set -> set.remove(session));
  }

  public void broadcast(String projectId, String json) {
    Set<WebSocketSession> sessions = byProject.get(projectId);
    if (sessions == null) return;
    for (WebSocketSession s : sessions) {
      if (s.isOpen()) {
        try { s.sendMessage(new TextMessage(json)); } catch (IOException ignored) {}
      }
    }
  }
}
