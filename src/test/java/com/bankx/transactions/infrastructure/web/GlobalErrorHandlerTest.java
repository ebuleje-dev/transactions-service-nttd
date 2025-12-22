package com.bankx.transactions.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;

import com.bankx.transactions.application.dto.CreateTxRequest;
import com.bankx.transactions.application.service.RiskRemoteClient;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Integration tests for GlobalErrorHandler.
 * Tests error handling for business exceptions, validation errors, and generic exceptions.
 */
@SpringBootTest
@AutoConfigureWebTestClient
class GlobalErrorHandlerTest {

  @Autowired
  private WebTestClient webTestClient;

  @MockitoBean
  private RiskRemoteClient riskRemoteClient;

  @Test
  void handleBusinessExceptionAccountNotFound() {
    Mockito.when(riskRemoteClient.isAllowed(any(), any(), any()))
        .thenReturn(Mono.just(true));

    CreateTxRequest request = new CreateTxRequest();
    request.setAccountNumber("999-9999");
    request.setType("DEBIT");
    request.setAmount(new BigDecimal("100"));

    webTestClient.post()
        .uri("/api/transactions")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.error").isEqualTo("account_not_found");
  }

  @Test
  void handleBusinessExceptionInsufficientFunds() {
    Mockito.when(riskRemoteClient.isAllowed(any(), any(), any()))
        .thenReturn(Mono.just(true));

    CreateTxRequest request = new CreateTxRequest();
    request.setAccountNumber("001-0002");
    request.setType("DEBIT");
    request.setAmount(new BigDecimal("1000"));

    webTestClient.post()
        .uri("/api/transactions")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.error").isEqualTo("insufficient_funds");
  }

  @Test
  void handleBusinessExceptionRiskRejected() {
    Mockito.when(riskRemoteClient.isAllowed(any(), any(), any()))
        .thenReturn(Mono.error(new RuntimeException("Remote service unavailable")));

    CreateTxRequest request = new CreateTxRequest();
    request.setAccountNumber("001-0001");
    request.setType("DEBIT");
    request.setAmount(new BigDecimal("2000"));

    webTestClient.post()
        .uri("/api/transactions")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.error").isEqualTo("risk_rejected");
  }

  @Test
  void handleValidationExceptionInvalidRequest() {
    CreateTxRequest request = new CreateTxRequest();
    // Missing required fields

    webTestClient.post()
        .uri("/api/transactions")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.errors").exists();
  }

  @Test
  void handleBusinessExceptionAccountNotFoundInList() {
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/api/transactions")
            .queryParam("accountNumber", "999-9999")
            .build())
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.error").isEqualTo("account_not_found");
  }

  @Test
  void createMultipleTransactions() {
    Mockito.when(riskRemoteClient.isAllowed(any(), any(), any()))
        .thenReturn(Mono.just(true));

    // Create multiple transactions to increase coverage
    for (int i = 0; i < 3; i++) {
      CreateTxRequest request = new CreateTxRequest();
      request.setAccountNumber("001-0003");
      request.setType(i % 2 == 0 ? "CREDIT" : "DEBIT");
      request.setAmount(new BigDecimal("50"));

      webTestClient.post()
          .uri("/api/transactions")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus().isCreated();
    }
  }

  @Test
  void handleGenericException() {
    GlobalErrorHandler handler = new GlobalErrorHandler();
    Exception exception = new Exception("Unexpected error occurred");

    handler.handleGenericException(exception)
        .as(StepVerifier::create)
        .assertNext(response -> {
          org.assertj.core.api.Assertions.assertThat(response.getStatusCode().value())
              .isEqualTo(500);
          org.assertj.core.api.Assertions.assertThat(response.getBody())
              .isNotNull()
              .containsEntry("error", "internal_error")
              .containsEntry("details", "Unexpected error occurred");
        })
        .verifyComplete();
  }
}
