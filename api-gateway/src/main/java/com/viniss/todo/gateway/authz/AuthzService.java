package com.viniss.todo.gateway.authz;// package com.viniss.todo.gateway.authz;

import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

@Component("authz")
public class AuthzService {

    private final TaskQueryPort taskQuery;

    public AuthzService(Optional<TaskQueryPort> taskQuery) {
        // Permite funcionar mesmo sem implementação (só desabilita checagem por taskId)
        this.taskQuery = taskQuery.orElse(null);
    }

    /** Checa se o JWT dá acesso ao projectId informado. */
    public boolean hasProjectAccess(Authentication authentication, String projectId) {
        if (projectId == null || projectId.isBlank()) return false;
        if (!(authentication instanceof JwtAuthenticationToken token)) return false;
        Jwt jwt = token.getToken();

        // 1) admin "global"
        if (containsIgnoreCase(claimAsList(jwt, "roles"), "admin")) return true;

        // 2) lista de projetos no claim "projects": ["project-123", ...]
        if (claimAsList(jwt, "projects").contains(projectId)) return true;

        // 3) escopos do tipo "project:read" ou "project:{id}:read/write"
        String scope = jwt.getClaimAsString("scope");
        if (scope != null) {
            Set<String> scopes = new HashSet<>(Arrays.asList(scope.split("\\s+")));
            if (scopes.contains("project:read")) return true; // permissao ampla
            if (scopes.contains("project:" + projectId + ":read")) return true;
            if (scopes.contains("project:" + projectId + ":write")) return true;
        }

        // 4) map/objeto "project_roles": { "project-123": ["OWNER","WRITER"] }
        Object pr = jwt.getClaims().get("project_roles");
        if (pr instanceof Map<?,?> map) {
            Object roles = map.get(projectId);
            if (roles instanceof Collection<?> c && !c.isEmpty()) return true;
        }

        return false;
    }

    /** Resolve projectId a partir da taskId e aplica a mesma checagem. */
    public boolean canAccessTask(Authentication authentication, String taskId) {
        if (taskQuery == null) return false; // sem implementação disponível
        String projectId = taskQuery.findProjectIdByTaskId(taskId).orElse(null);
        return hasProjectAccess(authentication, projectId);
    }

    // Helpers
    private List<String> claimAsList(Jwt jwt, String name) {
        Object v = jwt.getClaims().get(name);
        if (v instanceof Collection<?> col) {
            List<String> out = new ArrayList<>();
            for (Object o : col) if (o != null) out.add(String.valueOf(o));
            return out;
        }
        if (v instanceof String s) return Arrays.asList(s.split("\\s*,\\s*"));
        return Collections.emptyList();
    }

    private boolean containsIgnoreCase(Collection<String> col, String value) {
        for (String s : col) if (s.equalsIgnoreCase(value)) return true;
        return false;
    }
}
