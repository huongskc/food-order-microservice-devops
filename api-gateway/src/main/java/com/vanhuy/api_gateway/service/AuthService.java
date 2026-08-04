package com.vanhuy.api_gateway.service;

import com.vanhuy.api_gateway.dto.ValidTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String AUTH_SERVICE_URL = "http://user-service/api/v1/auth/validateToken";

    private final WebClient.Builder webClientBuilder;

    public AuthService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<ValidTokenResponse> validateToken(String token) {
        return webClientBuilder.build()
                .post()
                .uri(AUTH_SERVICE_URL + "?token={token}", token)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(ValidTokenResponse.class);
                    }
                    if (response.statusCode() == HttpStatus.UNAUTHORIZED) {
                        return Mono.just(new ValidTokenResponse(false));
                    }
                    return response.createException().flatMap(Mono::error);
                })
                .doOnSuccess(response -> logger.debug("Token validation completed"))
                .doOnError(error -> logger.error("Error communicating with auth service", error));
    }
}
