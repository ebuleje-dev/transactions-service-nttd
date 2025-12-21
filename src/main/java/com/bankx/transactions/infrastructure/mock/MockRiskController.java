package com.bankx.transactions.infrastructure.mock;

import java.math.BigDecimal;
import java.time.Duration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Mock controller for simulating risk service responses.
 * Used for testing resilience patterns without external dependencies.
 */
@RestController
@RequestMapping("/mock/risk")
public class MockRiskController {

  /**
   * Simulates risk validation with configurable delay and failure.
   *
   * @param currency the transaction currency
   * @param type the transaction type
   * @param amount the transaction amount
   * @param fail whether to simulate a failure
   * @param delayMs delay in milliseconds before responding
   * @return a Mono emitting true if allowed, false otherwise
   */
  @GetMapping("/allow")
  public Mono<Boolean> allow(@RequestParam String currency,
                             @RequestParam String type,
                             @RequestParam BigDecimal amount,
                             @RequestParam(defaultValue = "false") boolean fail,
                             @RequestParam(defaultValue = "0") long delayMs) {
    if (fail) {
      return Mono.error(new RuntimeException("risk_down"));
    }
    return Mono.just(("DEBIT".equalsIgnoreCase(type)
            && amount.compareTo(new BigDecimal("1200")) > 0) ? false : true)
        .delayElement(Duration.ofMillis(delayMs));
  }
}
