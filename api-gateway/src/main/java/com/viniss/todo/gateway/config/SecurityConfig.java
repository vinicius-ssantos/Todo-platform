package com.viniss.todo.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuração de segurança para o API Gateway.
 * 
 * Funcionalidades:
 * - JWT Resource Server (OAuth2)
 * - CORS restritivo
 * - Headers de segurança (CSP, X-Frame-Options, etc.)
 * - Sessões stateless
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private List<String> allowedOrigins;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String jwtIssuerUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // Desabilitar CSRF para APIs stateless
            .csrf(csrf -> csrf.disable())
            
            // Configuração de sessões stateless
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configurar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Headers de segurança (configuração modernizada para Spring Security 6.1+)
            .headers(headers -> headers
                // Substituir frameOptions() por frameOptions(frameOptions -> ...)
                .frameOptions(frameOptions -> frameOptions.deny())
                
                // Substituir contentTypeOptions() por contentTypeOptions(contentTypeOptions -> ...)
                .contentTypeOptions(contentTypeOptions -> {})
                
                // HSTS com método correto (includeSubDomains ao invés de includeSubdomains)
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true))
                
                // Substituir referrerPolicy() direto por referrerPolicy(referrerPolicy -> ...)
                .referrerPolicy(referrerPolicy -> 
                    referrerPolicy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                
                // Adicionar headers customizados (CSP e Permissions Policy)
                .addHeaderWriter((request, response) -> {
                    // Content Security Policy
                    response.setHeader("Content-Security-Policy", 
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "connect-src 'self' ws: wss:; " +
                        "font-src 'self'; " +
                        "object-src 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self'");
                    
                    // Permissions Policy (Feature Policy replacement)
                    response.setHeader("Permissions-Policy", 
                        "camera=(), microphone=(), geolocation=(), payment=()");
                })
            )
            
            // Configuração de autorização
            .authorizeHttpRequests(authz -> authz
                // Endpoints públicos
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/ws/**").permitAll() // WebSocket público por enquanto
                .requestMatchers("/api/v1/auth/**").permitAll() // Endpoints de auth se existirem
                
                // Todos os outros endpoints requerem autenticação
                .anyRequest().authenticated()
            )
            
            // Configurar JWT Resource Server
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwkSetUri(jwtIssuerUri + "/protocol/openid_connect/certs") // Para Keycloak
                    // Para outros provedores, usar: .issuerUri(jwtIssuerUri)
                )
            )
            
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origens permitidas (restritivo)
        configuration.setAllowedOrigins(allowedOrigins);
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "X-Correlation-ID",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Headers expostos para o cliente
        configuration.setExposedHeaders(List.of(
            "X-Correlation-ID",
            "X-Total-Count"
        ));
        
        // Permitir credenciais
        configuration.setAllowCredentials(true);
        
        // Cache de preflight
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}