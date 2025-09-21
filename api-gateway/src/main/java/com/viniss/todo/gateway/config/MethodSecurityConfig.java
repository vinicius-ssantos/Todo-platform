package com.viniss.todo.gateway.config;// package com.viniss.todo.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity // << habilita @PreAuthorize
public class MethodSecurityConfig {
  // vazio mesmo; apenas habilita as anotações
}
