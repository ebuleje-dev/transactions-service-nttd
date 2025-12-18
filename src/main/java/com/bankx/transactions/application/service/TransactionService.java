package com.bankx.transactions.application.service;

import com.bankx.transactions.application.dto.CreateTxRequest;
import com.bankx.transactions.domain.model.Account;
import com.bankx.transactions.domain.model.Transaction;
import com.bankx.transactions.domain.repository.AccountRepository;
import com.bankx.transactions.domain.repository.TransactionRepository;
import com.bankx.transactions.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    private final RiskService riskService;
    private final Sinks.Many<Transaction> txSink;


    public Mono<Transaction> create(CreateTxRequest req) {
        return accountRepo.findByNumber(req.getAccountNumber())

                .switchIfEmpty(Mono.error(new BusinessException("account_not_found")))

                .flatMap(account -> validateAndApply(account, req))

                .onErrorMap(IllegalStateException.class,
                        e -> new BusinessException(e.getMessage()));
    }

    /**
     * Transaction Validation
     */
    private Mono<Transaction> validateAndApply(Account acc, CreateTxRequest req) {

        String type = req.getType().toUpperCase();
        BigDecimal amount = req.getAmount();

        // Rules JPA
        return riskService.isAllowed(acc.getCurrency(), type, amount)

                .flatMap(allowed -> {

                    // Risk
                    if (!allowed) {
                        return Mono.error(new BusinessException("risk_rejected"));
                    }

                    // funds debit
                    if ("DEBIT".equals(type) && acc.getBalance().compareTo(amount) < 0) {
                        return Mono.error(new BusinessException("insufficient_funds"));
                    }

                    // Update Balance
                    // Error: Object Mutation 'acc' -> Race condition
                    // FIX: Crete new object (inmutabilidad reactiva)
                    BigDecimal newBalance = "DEBIT".equals(type)
                            ? acc.getBalance().subtract(amount)
                            : acc.getBalance().add(amount);

                    Account updatedAccount = Account.builder()
                            .id(acc.getId())
                            .number(acc.getNumber())
                            .holderName(acc.getHolderName())
                            .currency(acc.getCurrency())
                            .balance(newBalance)
                            .build();

                    // Save Account updated
                    return accountRepo.save(updatedAccount)

                            // Create transaction
                            .flatMap(savedAccount -> txRepo.save(
                                    Transaction.builder()
                                            .accountId(savedAccount.getId())
                                            .type(type)
                                            .amount(amount)
                                            .timestamp(Instant.now())
                                            .status("OK")
                                            .build()
                            ))

                            // Emit event SSE
                            .doOnNext(tx -> {
                                Sinks.EmitResult result = txSink.tryEmitNext(tx);
                                if (result.isFailure()) {
                                    log.warn("Failed to emit transaction event: {}", result);
                                }
                            });
                });
    }

    /**
     * List Transactions
     */
    public Flux<Transaction> byAccount(String accountNumber) {
        return accountRepo.findByNumber(accountNumber)
                .switchIfEmpty(Mono.error(new BusinessException("account_not_found")))
                .flatMapMany(acc ->
                        txRepo.findByAccountIdOrderByTimestampDesc(acc.getId())
                );
    }

    /**
     * Stream SSE
     */
    public Flux<ServerSentEvent<Transaction>> stream() {
        return txSink.asFlux()
                .map(tx -> ServerSentEvent.builder(tx)
                        .event("transaction")  // Event name
                        .build()
                );
    }
}