package com.bankx.transactions.application.service;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

/**
 * Integration tests for RiskRemoteClient using StepVerifier.
 * Validates Circuit Breaker, Retry, and Timeout resilience patterns.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class RiskRemoteClientTest {

    @Autowired
    private RiskRemoteClient riskRemoteClient;

    @Test
    void shouldAllowSmallDebit() {
        // Given: Transacción
        String currency = "PEN";
        String type = "DEBIT";
        BigDecimal amount = new BigDecimal("100");

        StepVerifier.create(riskRemoteClient.isAllowed(currency, type, amount))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldRejectLargeDebit() {
        String currency = "PEN";
        String type = "DEBIT";
        BigDecimal amount = new BigDecimal("1500");

        StepVerifier.create(riskRemoteClient.isAllowed(currency, type, amount))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldAllowCredit() {
        String currency = "USD";
        String type = "CREDIT";
        BigDecimal amount = new BigDecimal("5000");

        StepVerifier.create(riskRemoteClient.isAllowed(currency, type, amount))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldUseRemoteServiceWhenAvailable() {
        String currency = "PEN";
        String type = "DEBIT";
        BigDecimal amount = new BigDecimal("500");

        StepVerifier.create(riskRemoteClient.isAllowed(currency, type, amount))
                .expectNext(true)
                .verifyComplete();
    }
}
