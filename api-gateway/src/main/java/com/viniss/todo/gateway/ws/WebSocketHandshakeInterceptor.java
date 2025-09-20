
package com.viniss.todo.gateway.ws;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        // 1. Verificar se há autenticação JWT válida
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext.getAuthentication() == null || 
            !(securityContext.getAuthentication() instanceof JwtAuthenticationToken)) {
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) securityContext.getAuthentication();
        Jwt jwt = jwtAuth.getToken();

        // 2. Extrair projectId da query string
        String query = request.getURI().getQuery();
        String projectId = null;
        if (query != null) {
            Map<String, String> queryParams = UriComponentsBuilder.fromUriString("?" + query)
                    .build()
                    .getQueryParams()
                    .toSingleValueMap();
            projectId = queryParams.get("projectId");
        }

        // 3. Validar se projectId foi fornecido
        if (projectId == null || projectId.trim().isEmpty()) {
            response.setStatusCode(org.springframework.http.HttpStatus.BAD_REQUEST);
            return false;
        }

        // 4. Validar acesso ao projeto (verificar se o usuário tem permissão)
        if (!hasAccessToProject(jwt, projectId)) {
            response.setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
            return false;
        }

        // 5. Armazenar dados na sessão para uso posterior
        attributes.put("userId", jwt.getSubject());
        attributes.put("projectId", projectId);
        attributes.put("jwt", jwt);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Implementação opcional para logging ou limpeza
    }

    /**
     * Valida se o usuário tem acesso ao projeto específico
     */
    private boolean hasAccessToProject(Jwt jwt, String projectId) {
        // Opção 1: Verificar claims do JWT (se o projectId estiver no token)
        Object projectsClaim = jwt.getClaims().get("projects");
        if (projectsClaim instanceof java.util.List<?> projects) {
            return projects.contains(projectId);
        }

        // Opção 2: Verificar roles/scopes para acesso geral
        Object scopesClaim = jwt.getClaims().get("scope");
        if (scopesClaim instanceof String scopes) {
            return scopes.contains("project:read") || scopes.contains("admin");
        }

        // Opção 3: Validação customizada (ex: consultar serviço de autorização)
        // return authorizationService.hasProjectAccess(jwt.getSubject(), projectId);

        // Por padrão, negar acesso se não houver validação específica
        return false;
    }
}