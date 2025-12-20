package com.bankx.transactions.application.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RiskRemoteClient {
    private final WebClient riskWebClient;

    @TimeLimiter(name = "riskClient")
    @Retry(name = "riskClient")
    @CircuitBreaker(name = "riskClient", fallbackMethod = "fallback")
    public Mono<Boolean> isAllowed(String currency, String type, BigDecimal
            amount) {
        return riskWebClient.get()
                .uri(uri -> uri.path("/allow")
                        .queryParam("currency", currency)
                        .queryParam("type", type)
                        .queryParam("amount", amount)
                        .queryParam("fail", false)
                        .queryParam("delayMs", 200) // Delay simulation
                        .build())
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    // Fallback → usa módulo legado JPA (bloqueante envuelto)
    public Mono<Boolean> fallback(String currency, String type, BigDecimal amount,
                                  Throwable ex) {
        return legacyAllowed(currency, type, amount);
    }

    @Autowired
    RiskService legacy; // JPA Risk
    private Mono<Boolean> legacyAllowed(String c, String t, BigDecimal a) {
        return legacy.isAllowed(c, t, a);
    }
}
