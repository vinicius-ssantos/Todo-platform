package com.viniss.todo.gateway.ws;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.socket.WebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WebSocketHandshakeInterceptorTest {

    private final WebSocketHandshakeInterceptor interceptor = new WebSocketHandshakeInterceptor();

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    private void setJwtWithClaims(Map<String, Object> claims) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claims(c -> c.putAll(claims))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    private ServerHttpRequest mockReq(String uri) {
        ServerHttpRequest req = mock(ServerHttpRequest.class, RETURNS_DEEP_STUBS);
        when(req.getURI()).thenReturn(URI.create(uri));
        return req;
    }

    private ServerHttpResponse mockRes() {
        return mock(ServerHttpResponse.class, RETURNS_DEEP_STUBS);
    }

    private WebSocketHandler mockHandler() {
        return mock(WebSocketHandler.class);
    }

    @Test
    @DisplayName("Permite quando JWT contém projects com o projectId da query")
    void allow_whenProjectsHasProjectId() throws Exception {
        setJwtWithClaims(Map.of("projects", List.of("p1", "p2")));
        var req = mockReq("ws://localhost/ws?projectId=p1");
        var res = mockRes();

        boolean ok = interceptor.beforeHandshake(req, res, mockHandler(), new HashMap<>());

        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("Permite quando scope inclui project:read (acesso amplo)")
    void allow_whenScopeProjectRead() throws Exception {
        setJwtWithClaims(Map.of("scope", "profile email project:read"));
        var req = mockReq("ws://localhost/ws?projectId=alpha");
        var res = mockRes();

        boolean ok = interceptor.beforeHandshake(req, res, mockHandler(), new HashMap<>());

        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("Nega quando não há acesso ao projectId")
    void deny_whenNoAccess() throws Exception {
        setJwtWithClaims(Map.of("projects", List.of("other")));
        var req = mockReq("ws://localhost/ws?projectId=pX");
        var res = mockRes();

        boolean ok = interceptor.beforeHandshake(req, res, mockHandler(), new HashMap<>());

        assertThat(ok).isFalse();
    }

    @Test
    @DisplayName("Nega quando não há projectId na query")
    void deny_whenMissingProjectIdParam() throws Exception {
        setJwtWithClaims(Map.of("projects", List.of("p1")));
        var req = mockReq("ws://localhost/ws");
        var res = mockRes();

        boolean ok = interceptor.beforeHandshake(req, res, mockHandler(), new HashMap<>());

        assertThat(ok).isFalse();
    }
}
