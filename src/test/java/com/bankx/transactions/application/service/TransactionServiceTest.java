package com.bankx.transactions.application.service;

import com.bankx.transactions.application.dto.CreateTxRequest;
import com.bankx.transactions.exception.BusinessException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for TransactionService using StepVerifier.
 * Validates complete transaction creation flow including risk validation and balance checks.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Test
    void shouldCreateDebitTransactionSuccessfully() {
        
        CreateTxRequest req = new CreateTxRequest();
        req.setAccountNumber("001-0001");
        req.setType("DEBIT");
        req.setAmount(new BigDecimal("100"));

        
        StepVerifier.create(transactionService.create(req))
                .assertNext(tx -> {
                    assertEquals("OK", tx.getStatus());
                    assertEquals("DEBIT", tx.getType());
                    assertEquals(new BigDecimal("100"), tx.getAmount());
                })
                .verifyComplete();
    }

    @Test
    void shouldCreateCreditTransactionSuccessfully() {
        
        CreateTxRequest req = new CreateTxRequest();
        req.setAccountNumber("001-0002");
        req.setType("CREDIT");
        req.setAmount(new BigDecimal("500"));

        StepVerifier.create(transactionService.create(req))
                .assertNext(tx -> {
                    assertEquals("OK", tx.getStatus());
                    assertEquals("CREDIT", tx.getType());
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectTransactionByRisk() {
        
        CreateTxRequest req = new CreateTxRequest();
        req.setAccountNumber("001-0001");
        req.setType("DEBIT");
        req.setAmount(new BigDecimal("2000"));

        StepVerifier.create(transactionService.create(req))
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException
                                && throwable.getMessage().equals("risk_rejected"))
                .verify();
    }

    @Test
    void shouldRejectTransactionByInsufficientFunds() {
        
        CreateTxRequest req = new CreateTxRequest();
        req.setAccountNumber("001-0002");
        req.setType("DEBIT");
        req.setAmount(new BigDecimal("1000"));

        
        StepVerifier.create(transactionService.create(req))
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException
                                && throwable.getMessage().equals("insufficient_funds"))
                .verify();
    }

    @Test
    void shouldRejectTransactionForNonExistentAccount() {
        
        CreateTxRequest req = new CreateTxRequest();
        req.setAccountNumber("999-9999");
        req.setType("DEBIT");
        req.setAmount(new BigDecimal("50"));

        
        StepVerifier.create(transactionService.create(req))
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException
                                && throwable.getMessage().equals("account_not_found"))
                .verify();
    }

    @Test
    void shouldListTransactionsByAccount() {
        String accountNumber = "001-0001";

        StepVerifier.create(transactionService.byAccount(accountNumber))
                .expectNextMatches(tx -> accountNumber.equals(tx.getAccountId()))
                .expectComplete()
                .verify();
    }
}
