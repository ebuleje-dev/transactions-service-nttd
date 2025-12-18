package com.bankx.transactions.infrastructure.web;

import java.math.BigDecimal;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/mock/risk")
public class MockRiskController {

    @GetMapping("/allow")
    public Mono<Boolean> allow(
            @RequestParam final String currency,
            @RequestParam final String type,
            @RequestParam final BigDecimal amount,
            @RequestParam(defaultValue = "false") final boolean fail,
            @RequestParam(defaultValue = "0") final long delayMs) {

        log.debug("Mock risk check: currency={}, type={}, amount={}, fail={}, delayMs={}",
                currency, type, amount, fail, delayMs);

        if (fail) {
            log.warn("Mock risk service: simulating failure");
            return Mono.error(new RuntimeException("mock_risk_service_down"));
        }

        boolean allowed = !("DEBIT".equalsIgnoreCase(type)
                && amount.compareTo(new BigDecimal("1200")) > 0);

        log.debug("Mock risk decision: allowed={}", allowed);

        return Mono.just(allowed)
                .delayElement(Duration.ofMillis(delayMs));
    }
}
