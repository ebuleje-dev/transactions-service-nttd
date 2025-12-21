package com.bankx.transactions.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

import com.bankx.transactions.application.dto.CreateTxRequest;
import com.bankx.transactions.exception.BusinessException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Integration tests for TransactionService.
 * Tests transaction creation, validation, querying, and SSE streaming.
 */
@SpringBootTest
public class TransactionServiceTest {
  @Autowired
  TransactionService service;

  @MockitoBean
  RiskRemoteClient riskRemoteClient;

  @Test
  void debitOk() {
    // Risk Mock remote
    Mockito.when(
            riskRemoteClient.isAllowed(any(), any(), any())
    ).thenReturn(Mono.just(true));

    CreateTxRequest req = new CreateTxRequest();
    req.setAccountNumber("001-0001");
    req.setType("DEBIT");
    req.setAmount(new BigDecimal("100"));

    StepVerifier.create(service.create(req))
        .assertNext(tx -> assertEquals("OK", tx.getStatus()))
        .verifyComplete();
  }

  @Test
  void creditOk() {
    Mockito.when(
            riskRemoteClient.isAllowed(any(), any(), any())
    ).thenReturn(Mono.just(true));

    CreateTxRequest req = new CreateTxRequest();
    req.setAccountNumber("001-0002");
    req.setType("CREDIT");
    req.setAmount(new BigDecimal("500"));

    StepVerifier.create(service.create(req))
        .assertNext(tx -> {
          assertEquals("OK", tx.getStatus());
          assertEquals("CREDIT", tx.getType());
        })
        .verifyComplete();
  }

  @Test
  void accountNotFound() {
    Mockito.when(
            riskRemoteClient.isAllowed(any(), any(), any())
    ).thenReturn(Mono.just(true));

    CreateTxRequest req = new CreateTxRequest();
    req.setAccountNumber("999-9999");
    req.setType("DEBIT");
    req.setAmount(new BigDecimal("100"));

    StepVerifier.create(service.create(req))
        .expectErrorMatches(e -> e instanceof BusinessException
            && e.getMessage().equals("account_not_found"))
        .verify();
  }

  @Test
  void insufficientFunds() {
    Mockito.when(
            riskRemoteClient.isAllowed(any(), any(), any())
    ).thenReturn(Mono.just(true));

    CreateTxRequest req = new CreateTxRequest();
    req.setAccountNumber("001-0002");
    req.setType("DEBIT");
    req.setAmount(new BigDecimal("1000"));

    StepVerifier.create(service.create(req))
        .expectErrorMatches(e -> e instanceof BusinessException
            && e.getMessage().equals("insufficient_funds"))
        .verify();
  }

  @Test
  void riskRejected() {
    Mockito.when(
            riskRemoteClient.isAllowed(any(), any(), any())
    ).thenReturn(Mono.error(new RuntimeException("Remote service unavailable")));

    CreateTxRequest req = new CreateTxRequest();
    req.setAccountNumber("001-0001");
    req.setType("DEBIT");
    req.setAmount(new BigDecimal("2000"));

    StepVerifier.create(service.create(req))
        .expectErrorMatches(e -> e instanceof BusinessException
            && e.getMessage().equals("risk_rejected"))
        .verify();
  }

  @Test
  void byAccountOk() {
    Mockito.when(
            riskRemoteClient.isAllowed(any(), any(), any())
    ).thenReturn(Mono.just(true));

    // First create a transaction
    CreateTxRequest req = new CreateTxRequest();
    req.setAccountNumber("001-0001");
    req.setType("DEBIT");
    req.setAmount(new BigDecimal("50"));

    StepVerifier.create(service.create(req))
        .assertNext(tx -> assertEquals("OK", tx.getStatus()))
        .verifyComplete();

    // Then query transactions for that account
    StepVerifier.create(service.byAccount("001-0001"))
        .expectNextMatches(tx -> tx.getStatus().equals("OK"))
        .thenConsumeWhile(tx -> true)
        .verifyComplete();
  }

  @Test
  void byAccountNotFound() {
    StepVerifier.create(service.byAccount("999-9999"))
        .expectErrorMatches(e -> e instanceof BusinessException
            && e.getMessage().equals("account_not_found"))
        .verify();
  }

  @Test
  void streamTest() {
    Mockito.when(
            riskRemoteClient.isAllowed(any(), any(), any())
    ).thenReturn(Mono.just(true));

    // Create a transaction to trigger SSE event
    CreateTxRequest req = new CreateTxRequest();
    req.setAccountNumber("001-0001");
    req.setType("CREDIT");
    req.setAmount(new BigDecimal("25"));

    // Subscribe to stream
    StepVerifier streamVerifier = StepVerifier.create(service.stream().take(1))
        .expectNextMatches(sse -> {
          assertNotNull(sse.data());
          assertEquals("transaction", sse.event());
          return true;
        })
        .thenCancel()
        .verifyLater();

    StepVerifier.create(service.create(req))
        .assertNext(tx -> assertEquals("OK", tx.getStatus()))
        .verifyComplete();

    // Verify stream received the event
    streamVerifier.verify();
  }

  @Test
  void debitCaseInsensitive() {
    Mockito.when(
            riskRemoteClient.isAllowed(any(), any(), any())
    ).thenReturn(Mono.just(true));

    CreateTxRequest req = new CreateTxRequest();
    req.setAccountNumber("001-0001");
    req.setType("debit");
    req.setAmount(new BigDecimal("150"));

    StepVerifier.create(service.create(req))
        .assertNext(tx -> {
          assertEquals("OK", tx.getStatus());
          assertEquals("DEBIT", tx.getType());
        })
        .verifyComplete();
  }

  @Test
  void creditToUsdAccount() {
    Mockito.when(
            riskRemoteClient.isAllowed(any(), any(), any())
    ).thenReturn(Mono.just(true));

    CreateTxRequest req = new CreateTxRequest();
    req.setAccountNumber("001-0003");
    req.setType("CREDIT");
    req.setAmount(new BigDecimal("300"));

    StepVerifier.create(service.create(req))
        .assertNext(tx -> {
          assertEquals("OK", tx.getStatus());
          assertEquals("CREDIT", tx.getType());
        })
        .verifyComplete();
  }

  @Test
  void debitUsdAccountOk() {
    Mockito.when(
            riskRemoteClient.isAllowed(any(), any(), any())
    ).thenReturn(Mono.just(true));

    CreateTxRequest req = new CreateTxRequest();
    req.setAccountNumber("001-0003");
    req.setType("DEBIT");
    req.setAmount(new BigDecimal("200"));

    StepVerifier.create(service.create(req))
        .assertNext(tx -> {
          assertEquals("OK", tx.getStatus());
          assertEquals("DEBIT", tx.getType());
        })
        .verifyComplete();
  }

  @Test
  void multipleTransactionsSameAccount() {
    Mockito.when(
            riskRemoteClient.isAllowed(any(), any(), any())
    ).thenReturn(Mono.just(true));

    // Create multiple transactions
    for (int i = 0; i < 5; i++) {
      CreateTxRequest req = new CreateTxRequest();
      req.setAccountNumber("001-0002");
      req.setType("CREDIT");
      req.setAmount(new BigDecimal("10"));

      StepVerifier.create(service.create(req))
          .assertNext(tx -> assertEquals("OK", tx.getStatus()))
          .verifyComplete();
    }

    // Verify all transactions are in the list
    StepVerifier.create(service.byAccount("001-0002").count())
        .assertNext(count -> {
          assert count >= 5;
        })
        .verifyComplete();
  }
}
