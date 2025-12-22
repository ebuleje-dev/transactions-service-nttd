package com.bankx.transactions.infrastructure.mock;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

/**
 * Integration tests for MockRiskController.
 * Tests all scenarios including success, failure, delays, and business logic.
 */
@SpringBootTest
@AutoConfigureWebTestClient
class MockRiskControllerTest {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private MockRiskController mockRiskController;

  @Test
  void allowReturnsTrue_whenAmountIsLessThan1200() {
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/mock/risk/allow")
            .queryParam("currency", "PEN")
            .queryParam("type", "DEBIT")
            .queryParam("amount", "1000")
            .queryParam("fail", "false")
            .queryParam("delayMs", "0")
            .build())
        .exchange()
        .expectStatus().isOk()
        .expectBody(Boolean.class)
        .isEqualTo(true);
  }

  @Test
  void allowReturnsFalse_whenAmountIsGreaterThan1200() {
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/mock/risk/allow")
            .queryParam("currency", "USD")
            .queryParam("type", "DEBIT")
            .queryParam("amount", "1500")
            .queryParam("fail", "false")
            .queryParam("delayMs", "0")
            .build())
        .exchange()
        .expectStatus().isOk()
        .expectBody(Boolean.class)
        .isEqualTo(false);
  }

  @Test
  void allowReturnsTrue_whenAmountEquals1200() {
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/mock/risk/allow")
            .queryParam("currency", "PEN")
            .queryParam("type", "DEBIT")
            .queryParam("amount", "1200")
            .queryParam("fail", "false")
            .queryParam("delayMs", "0")
            .build())
        .exchange()
        .expectStatus().isOk()
        .expectBody(Boolean.class)
        .isEqualTo(true);
  }

  @Test
  void allowReturnsTrue_whenTypeIsCredit() {
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/mock/risk/allow")
            .queryParam("currency", "USD")
            .queryParam("type", "CREDIT")
            .queryParam("amount", "5000")
            .queryParam("fail", "false")
            .queryParam("delayMs", "0")
            .build())
        .exchange()
        .expectStatus().isOk()
        .expectBody(Boolean.class)
        .isEqualTo(true);
  }

  @Test
  void allowReturnsError_whenFailIsTrue() {
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/mock/risk/allow")
            .queryParam("currency", "PEN")
            .queryParam("type", "DEBIT")
            .queryParam("amount", "100")
            .queryParam("fail", "true")
            .queryParam("delayMs", "0")
            .build())
        .exchange()
        .expectStatus().is5xxServerError();
  }

  @Test
  void allowWithDelay_completesAfterDelay() {
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/mock/risk/allow")
            .queryParam("currency", "PEN")
            .queryParam("type", "DEBIT")
            .queryParam("amount", "100")
            .queryParam("fail", "false")
            .queryParam("delayMs", "100")
            .build())
        .exchange()
        .expectStatus().isOk()
        .expectBody(Boolean.class)
        .isEqualTo(true);
  }

  @Test
  void allowDirectly_whenAmountIsLow() {
    StepVerifier.create(mockRiskController.allow(
            "PEN", "DEBIT", new BigDecimal("500"), false, 0))
        .expectNext(true)
        .verifyComplete();
  }

  @Test
  void allowDirectly_whenAmountIsHigh() {
    StepVerifier.create(mockRiskController.allow(
            "USD", "DEBIT", new BigDecimal("2000"), false, 0))
        .expectNext(false)
        .verifyComplete();
  }

  @Test
  void allowDirectly_withFailTrue() {
    StepVerifier.create(mockRiskController.allow(
            "PEN", "DEBIT", new BigDecimal("100"), true, 0))
        .expectErrorMessage("risk_down")
        .verify();
  }

  @Test
  void allowDirectly_withDelay() {
    StepVerifier.create(mockRiskController.allow(
            "PEN", "CREDIT", new BigDecimal("100"), false, 50))
        .expectNext(true)
        .verifyComplete();
  }

  @Test
  void allowWithDefaultParameters() {
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/mock/risk/allow")
            .queryParam("currency", "PEN")
            .queryParam("type", "DEBIT")
            .queryParam("amount", "100")
            .build())
        .exchange()
        .expectStatus().isOk()
        .expectBody(Boolean.class)
        .isEqualTo(true);
  }
}
