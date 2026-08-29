package com.vanhuy.api_gateway.service;

import com.vanhuy.api_gateway.dto.ValidTokenResponse;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final JwtParser jwtParser;

    public AuthService(@Value("${jwt.secretKey}") String secretKey) {
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .build();
    }

    public Mono<ValidTokenResponse> validateToken(String token) {
        return Mono.fromSupplier(() -> {
            try {
                jwtParser.parseClaimsJws(token);
                logger.debug("Token validation completed");
                return new ValidTokenResponse(true);
            } catch (JwtException | IllegalArgumentException exception) {
                logger.debug("Token validation failed: {}", exception.getMessage());
                return new ValidTokenResponse(false);
            }
        });
    }
}
