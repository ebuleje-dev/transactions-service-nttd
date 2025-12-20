package com.bankx.transactions.infrastructure.mock;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

@RestController
@RequestMapping("/mock/risk")
public class MockRiskController {

    @GetMapping("/allow")
    public Mono<Boolean> allow(@RequestParam String currency,
                               @RequestParam String type,
                               @RequestParam BigDecimal amount,
                               @RequestParam(defaultValue = "false") boolean fail,
                               @RequestParam(defaultValue = "0") long delayMs) {
        if (fail) return Mono.error(new RuntimeException("risk_down"));
        return Mono.just(("DEBIT".equalsIgnoreCase(type) && amount.compareTo(new
                        BigDecimal("1200")) > 0) ? false : true)
                .delayElement(Duration.ofMillis(delayMs));
    }
}
