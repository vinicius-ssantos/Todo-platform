package com.viniss.todo.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

/**
 * Customizações adicionais de segurança web.
 * Ativo apenas quando necessário.
 */
@Configuration
@ConditionalOnProperty(name = "app.security.web-customizer.enabled", havingValue = "true")
public class WebSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer
{

    /**
     * Ignora completamente determinados paths da cadeia de filtros de segurança.
     * Use com cuidado - apenas para recursos estáticos ou endpoints de debug.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
            .requestMatchers("/favicon.ico", "/robots.txt")
            .requestMatchers("/static/**", "/assets/**");
    }

    @Override
    protected boolean sameOriginDisabled() {
        // Permitir origens diferentes (já controlado pelo CORS no WebSocketConfig)
        return true;
    }

}