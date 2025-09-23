package com.viniss.todo.gateway.ws;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Testa o gerenciamento de sessões por projeto e broadcast de mensagens.
 * ATENÇÃO: WsSessions.subscribe(String projectId, WebSocketSession session)
 */
class WsSessionsTest {

    @Test
    @DisplayName("subscribe + broadcast: envia só para inscritos do mesmo projectId")
    void subscribe_and_broadcast() throws Exception {
        WsSessions sessions = new WsSessions();

        WebSocketSession sA1 = mock(WebSocketSession.class);
        WebSocketSession sA2 = mock(WebSocketSession.class);
        WebSocketSession sB1 = mock(WebSocketSession.class);

        when(sA1.isOpen()).thenReturn(true);
        when(sA2.isOpen()).thenReturn(true);
        when(sB1.isOpen()).thenReturn(true);

        // ordem correta: (projectId, session)
        sessions.subscribe("proj-A", sA1);
        sessions.subscribe("proj-A", sA2);
        sessions.subscribe("proj-B", sB1);

        String json = "{\"event\":\"task-updated\"}";
        sessions.broadcast("proj-A", json);

        // Proj-A recebe
        verify(sA1).sendMessage(argThat((TextMessage m) -> json.equals(m.getPayload())));
        verify(sA2).sendMessage(argThat((TextMessage m) -> json.equals(m.getPayload())));
        // Proj-B não recebe
        verify(sB1, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("unsubscribeAll: remove a sessão de todos os projetos")
    void unsubscribeAll_removes_from_all_projects() throws Exception {
        WsSessions sessions = new WsSessions();

        WebSocketSession s = mock(WebSocketSession.class);
        WebSocketSession other = mock(WebSocketSession.class);
        when(s.isOpen()).thenReturn(true);
        when(other.isOpen()).thenReturn(true);

        sessions.subscribe("proj-A", s);
        sessions.subscribe("proj-B", s);
        sessions.subscribe("proj-B", other);

        sessions.unsubscribeAll(s);

        String jsonA = "{\"a\":1}";
        String jsonB = "{\"b\":2}";
        sessions.broadcast("proj-A", jsonA);
        sessions.broadcast("proj-B", jsonB);

        // s não recebe mais nada
        verify(s, never()).sendMessage(any(TextMessage.class));
        // other segue recebendo em proj-B
        verify(other).sendMessage(argThat((TextMessage m) -> jsonB.equals(m.getPayload())));
    }

    @Test
    @DisplayName("broadcast ignora sessões fechadas (isOpen = false)")
    void broadcast_ignores_closed_sessions() throws Exception {
        WsSessions sessions = new WsSessions();

        WebSocketSession open = mock(WebSocketSession.class);
        WebSocketSession closed = mock(WebSocketSession.class);
        when(open.isOpen()).thenReturn(true);
        when(closed.isOpen()).thenReturn(false);

        sessions.subscribe("proj-X", open);
        sessions.subscribe("proj-X", closed);

        sessions.broadcast("proj-X", "{\"msg\":true}");

        verify(open).sendMessage(any(TextMessage.class));
        verify(closed, never()).sendMessage(any(TextMessage.class));
    }
}
