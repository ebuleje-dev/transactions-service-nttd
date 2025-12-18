package com.bankx.transactions.application.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@RequiredArgsConstructor
public class RiskRemoteClient {

    private final WebClient riskWebClient;
    private final RiskService riskService;

    @TimeLimiter(name = "riskClient")
    @Retry(name = "riskClient")
    @CircuitBreaker(name = "riskClient", fallbackMethod = "fallbackToLegacy")
    public Mono<Boolean> isAllowed(final String currency, final String type,
                                     final BigDecimal amount) {
        log.debug("Calling remote risk service: currency={}, type={}, amount={}",
                currency, type, amount);

        return riskWebClient.get()
                .uri(uri -> uri.path("/allow")
                        .queryParam("currency", currency)
                        .queryParam("type", type)
                        .queryParam("amount", amount)
                        .queryParam("fail", false)  // Cambiar a true para forzar fallback
                        .queryParam("delayMs", 200)
                        .build())
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnSuccess(allowed -> log.debug("Remote risk decision: allowed={}", allowed))
                .doOnError(e -> log.warn("Remote risk service error: {}", e.getMessage()));
    }

    public Mono<Boolean> fallbackToLegacy(final String currency, final String type,
                                           final BigDecimal amount, final Throwable ex) {
        log.warn("Circuit Breaker activated! Falling back to legacy risk service. Reason: {}",
                ex.getMessage());

        return riskService.isAllowed(currency, type, amount)
                .doOnSuccess(allowed -> log.info("Legacy risk decision: allowed={}", allowed));
    }
}
