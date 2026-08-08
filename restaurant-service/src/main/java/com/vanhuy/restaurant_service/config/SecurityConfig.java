package com.vanhuy.restaurant_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {
    private static final String[] RESTAURANT_ENDPOINTS = {
            "/api/v1/restaurants/**",
            "/api/v1/menu-items/**"
    };

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs",
                                "/api-docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, RESTAURANT_ENDPOINTS).permitAll()
                        .requestMatchers(RESTAURANT_ENDPOINTS).hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        byte[] key = Base64.getDecoder().decode(secretKey);
        SecretKeySpec secret = new SecretKeySpec(key, "HmacSHA256");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secret).build();
        decoder.setJwtValidator(JwtValidators.createDefault());
        return decoder;
    }

    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            Object rolesClaim = jwt.getClaims().get("roles");
            Collection<?> roles = rolesClaim instanceof Collection<?> values ? values : List.of();
            Collection<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(this::extractAuthority)
                    .filter(value -> value != null && !value.isBlank())
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        };
    }

    private String extractAuthority(Object role) {
        if (role instanceof Map<?, ?> roleMap) {
            Object authority = roleMap.get("authority");
            return authority instanceof String value ? value : null;
        }
        return role instanceof String value ? value : null;
    }
}
