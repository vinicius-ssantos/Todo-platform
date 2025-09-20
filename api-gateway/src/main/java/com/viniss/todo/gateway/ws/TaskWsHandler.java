
package com.viniss.todo.gateway.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class TaskWsHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(TaskWsHandler.class);

    private final WsSessions sessions;
    private final ObjectMapper mapper = new ObjectMapper();

    public TaskWsHandler(WsSessions sessions) {
        this.sessions = sessions;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Dados já validados pelo interceptor
        String userId = (String) session.getAttributes().get("userId");
        String projectId = (String) session.getAttributes().get("projectId");

        log.info("WebSocket conectado - userId: {}, projectId: {}", userId, projectId);

        // Auto-subscrever ao projeto validado no handshake
        sessions.subscribe(projectId, session);

        // Confirmar conexão
        String confirmMessage = String.format(
                "{\"type\":\"connected\",\"projectId\":\"%s\",\"userId\":\"%s\"}",
                projectId, userId
        );
        session.sendMessage(new TextMessage(confirmMessage));

        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String projectId = (String) session.getAttributes().get("projectId");

        log.info("WebSocket desconectado - userId: {}, projectId: {}", userId, projectId);

        sessions.unsubscribeAll(session);
        super.afterConnectionClosed(session, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Usuário já está autenticado e autorizado
        String userId = (String) session.getAttributes().get("userId");
        String authorizedProjectId = (String) session.getAttributes().get("projectId");

        JsonNode node = mapper.readTree(message.getPayload());
        String messageType = node.path("type").asText();

        switch (messageType) {
            case "ping":
                session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
                break;

            case "subscribe":
                // Permitir apenas subscrição ao projeto autorizado
                String requestedProjectId = node.path("projectId").asText();
                if (authorizedProjectId.equals(requestedProjectId)) {
                    sessions.subscribe(requestedProjectId, session);
                    session.sendMessage(new TextMessage(
                            String.format("{\"type\":\"subscribed\",\"projectId\":\"%s\"}", requestedProjectId)
                    ));
                } else {
                    session.sendMessage(new TextMessage(
                            "{\"type\":\"error\",\"message\":\"Acesso negado ao projeto solicitado\"}"
                    ));
                }
                break;

            default:
                session.sendMessage(new TextMessage(
                        "{\"type\":\"error\",\"message\":\"Tipo de mensagem não reconhecido\"}"
                ));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Erro no transporte WebSocket para sessão {}: {}",
                session.getId(), exception.getMessage(), exception);
        super.handleTransportError(session, exception);
    }
}