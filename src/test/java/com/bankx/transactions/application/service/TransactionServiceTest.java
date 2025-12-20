package com.bankx.transactions.application.service;

import com.bankx.transactions.application.dto.CreateTxRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.ArgumentMatchers.any;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        var req = new CreateTxRequest();
        req.setAccountNumber("001-0001");
        req.setType("DEBIT");
        req.setAmount(new BigDecimal("100"));

        StepVerifier.create(service.create(req))
                .assertNext(tx -> assertEquals("OK", tx.getStatus()))
                .verifyComplete();
    }
}
