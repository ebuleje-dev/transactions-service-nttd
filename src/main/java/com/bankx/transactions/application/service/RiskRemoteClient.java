package com.bankx.transactions.application.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Remote client for risk validation with resilience patterns.
 * Implements circuit breaker and retry mechanisms with fallback to legacy JPA service.
 */
@Service
@RequiredArgsConstructor
public class RiskRemoteClient {
  private final WebClient riskWebClient;

  /**
   * Validates if a transaction is allowed using remote risk service.
   *
   * @param currency the transaction currency
   * @param type the transaction type (DEBIT or CREDIT)
   * @param amount the transaction amount
   * @return a Mono emitting true if allowed, false otherwise
   */
  // @TimeLimiter(name = "riskClient") // Don't use in Mono - Resilience4j limit
  @Retry(name = "riskClient")
  @CircuitBreaker(name = "riskClient", fallbackMethod = "fallback")
  public Mono<Boolean> isAllowed(String currency, String type, BigDecimal amount) {
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

  /**
   * Fallback method when remote risk service fails.
   * Uses legacy JPA-based risk validation.
   *
   * @param currency the transaction currency
   * @param type the transaction type
   * @param amount the transaction amount
   * @param ex the exception that triggered the fallback
   * @return a Mono emitting validation result from legacy service
   */
  // Fallback → usa módulo legado JPA (bloqueante envuelto)
  public Mono<Boolean> fallback(String currency, String type, BigDecimal amount,
                                Throwable ex) {
    return legacyAllowed(currency, type, amount);
  }

  @Autowired
  RiskService legacy; // JPA Risk

  /**
   * Delegates to legacy JPA-based risk service.
   *
   * @param c the currency
   * @param t the transaction type
   * @param a the amount
   * @return a Mono emitting validation result
   */
  private Mono<Boolean> legacyAllowed(String c, String t, BigDecimal a) {
    return legacy.isAllowed(c, t, a);
  }
}
