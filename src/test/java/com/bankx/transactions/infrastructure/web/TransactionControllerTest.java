package com.bankx.transactions.infrastructure.web;

import com.bankx.transactions.application.dto.CreateTxRequest;
import com.bankx.transactions.application.service.RiskRemoteClient;
import com.bankx.transactions.application.service.TransactionService;
import com.bankx.transactions.domain.model.Transaction;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@AutoConfigureWebTestClient
class TransactionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TransactionService transactionService;

    @MockitoBean
    private RiskRemoteClient riskRemoteClient;

    @Test
    void createTransactionOk() {
        Mockito.when(riskRemoteClient.isAllowed(any(), any(), any()))
                .thenReturn(Mono.just(true));

        CreateTxRequest request = new CreateTxRequest();
        request.setAccountNumber("001-0001");
        request.setType("DEBIT");
        request.setAmount(new BigDecimal("100"));

        webTestClient.post()
                .uri("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Transaction.class)
                .value(tx -> {
                    assert tx.getStatus().equals("OK");
                    assert tx.getType().equals("DEBIT");
                });
    }

    @Test
    void createTransactionCreditOk() {
        Mockito.when(riskRemoteClient.isAllowed(any(), any(), any()))
                .thenReturn(Mono.just(true));

        CreateTxRequest request = new CreateTxRequest();
        request.setAccountNumber("001-0002");
        request.setType("CREDIT");
        request.setAmount(new BigDecimal("200"));

        webTestClient.post()
                .uri("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Transaction.class)
                .value(tx -> {
                    assert tx.getStatus().equals("OK");
                    assert tx.getType().equals("CREDIT");
                });
    }

    @Test
    void listTransactionsOk() {
        Mockito.when(riskRemoteClient.isAllowed(any(), any(), any()))
                .thenReturn(Mono.just(true));

        // First create a transaction
        CreateTxRequest request = new CreateTxRequest();
        request.setAccountNumber("001-0001");
        request.setType("DEBIT");
        request.setAmount(new BigDecimal("50"));

        transactionService.create(request).block();

        // Then list transactions
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/transactions")
                        .queryParam("accountNumber", "001-0001")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Transaction.class)
                .value(list -> {
                    assert !list.isEmpty();
                });
    }

    @Test
    void streamTransactionsOk() {
        Mockito.when(riskRemoteClient.isAllowed(any(), any(), any()))
                .thenReturn(Mono.just(true));

        webTestClient.get()
                .uri("/api/stream/transactions")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM);
    }

    @Test
    void streamTransactionsWithEvents() {
        Mockito.when(riskRemoteClient.isAllowed(any(), any(), any()))
                .thenReturn(Mono.just(true));

        // Create transaction to trigger event
        CreateTxRequest request = new CreateTxRequest();
        request.setAccountNumber("001-0001");
        request.setType("CREDIT");
        request.setAmount(new BigDecimal("75"));

        // Subscribe to stream and take 1 event then cancel
        webTestClient.get()
                .uri("/api/stream/transactions")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody()
                .take(1)
                .subscribe();

        // Create a transaction
        transactionService.create(request).block();
    }
}
