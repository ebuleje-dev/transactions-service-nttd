package com.bankx.transactions.application.service;

import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Integration tests for RiskRemoteClient.
 * Tests remote risk validation with circuit breaker and fallback mechanisms.
 */
@SpringBootTest
class RiskRemoteClientTest {

  @Autowired
  private RiskRemoteClient riskRemoteClient;

  @MockitoBean
  private WebClient riskWebClient;

  @MockitoBean
  private RiskService riskService;

  @Test
  void isAllowedSuccess() {
    // Mock WebClient behavior
    WebClient.RequestHeadersUriSpec requestHeadersUriSpec =
        Mockito.mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.RequestHeadersSpec requestHeadersSpec =
        Mockito.mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

    Mockito.when(riskWebClient.get()).thenReturn(requestHeadersUriSpec);
    Mockito.when(requestHeadersUriSpec.uri(any(java.util.function.Function.class)))
        .thenReturn(requestHeadersSpec);
    Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    Mockito.when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.just(true));

    StepVerifier.create(riskRemoteClient.isAllowed("PEN", "DEBIT", new BigDecimal("100")))
        .expectNext(true)
        .verifyComplete();
  }

  @Test
  void isAllowedReturningFalse() {
    // Mock WebClient behavior
    WebClient.RequestHeadersUriSpec requestHeadersUriSpec =
        Mockito.mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.RequestHeadersSpec requestHeadersSpec =
        Mockito.mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

    Mockito.when(riskWebClient.get()).thenReturn(requestHeadersUriSpec);
    Mockito.when(requestHeadersUriSpec.uri(any(java.util.function.Function.class)))
        .thenReturn(requestHeadersSpec);
    Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    Mockito.when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.just(false));

    StepVerifier.create(riskRemoteClient.isAllowed("USD", "DEBIT", new BigDecimal("1000")))
        .expectNext(false)
        .verifyComplete();
  }

  @Test
  void fallbackMethodDirectly() {
    // Mock RiskService for fallback
    Mockito.when(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("200")))
        .thenReturn(Mono.just(true));

    // Call fallback method directly
    Mono<Boolean> result = riskRemoteClient.fallback("PEN", "DEBIT", new BigDecimal("200"),
        new RuntimeException("Test exception"));

    StepVerifier.create(result)
        .expectNext(true)
        .verifyComplete();
  }

  @Test
  void fallbackReturnsFalse() {
    // Mock RiskService for fallback returning false
    Mockito.when(riskService.isAllowed("USD", "DEBIT", new BigDecimal("1000")))
        .thenReturn(Mono.just(false));

    // Call fallback method directly
    Mono<Boolean> result = riskRemoteClient.fallback("USD", "DEBIT", new BigDecimal("1000"),
        new RuntimeException("Test exception"));

    StepVerifier.create(result)
        .expectNext(false)
        .verifyComplete();
  }
}
