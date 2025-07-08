package com.rabbithole.bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                        ReactiveJwtAuthenticationConverter jwtAuthConverter) {
                http
                                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                                .authorizeExchange(exchanges -> exchanges
                                                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .pathMatchers(HttpMethod.PUT, "/api/productos-personalizados/*/estado",
                                                                "/api/ordenes/*/estado")
                                                .hasRole("ADMIN")
                                                .pathMatchers(HttpMethod.POST, "/api/productos/**").hasRole("ADMIN")
                                                .pathMatchers(HttpMethod.PUT, "/api/productos/**").hasRole("ADMIN")
                                                .pathMatchers(HttpMethod.PATCH, "/api/productos/**").hasRole("ADMIN")
                                                .pathMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMIN")
                                                .pathMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
                                                .pathMatchers("/health", "/actuator/**", "/api/auth/**", "/public/**",
                                                                "/api/productos-personalizados/**", "/api/colores/**",
                                                                "/api/tallas/**",
                                                                "/api/fuentes/**", "/api/categorias/**",
                                                                "/api/usuarios/validate-token", "/api/usuarios/oid/**",
                                                                "/api/ordenes/anonima", "api/thumbnails/**")
                                                .permitAll()
                                                .pathMatchers(HttpMethod.POST, "/api/ordenes").authenticated()
                                                .pathMatchers(HttpMethod.GET, "/api/ordenes/usuario/**").authenticated()
                                                .pathMatchers(HttpMethod.GET, "/api/ordenes/*").permitAll()
                                                .pathMatchers(HttpMethod.GET, "/api/ordenes/**",
                                                                "/api/estados-orden/**", "/api/estados-diseno/**")
                                                .hasRole("ADMIN")
                                                .pathMatchers("/api/carritos/**")
                                                .authenticated()
                                                .anyExchange().denyAll())
                                .oauth2ResourceServer(oauth -> oauth
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));

                return http.build();
        }
}
