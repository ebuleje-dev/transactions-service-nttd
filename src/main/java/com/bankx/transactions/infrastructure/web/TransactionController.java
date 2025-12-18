package com.bankx.transactions.infrastructure.web;

import com.bankx.transactions.application.dto.CreateTxRequest;
import com.bankx.transactions.application.service.TransactionService;
import com.bankx.transactions.domain.model.Transaction;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping("/transactions")
    public Mono<ResponseEntity<Transaction>> create(
            @Valid @RequestBody CreateTxRequest req) {

        log.info("Creating transaction: account={}, type={}, amount={}",
                req.getAccountNumber(), req.getType(), req.getAmount());

        return service.create(req)
                .map(tx -> {
                    log.info("Transaction created: id={}, status={}",
                            tx.getId(), tx.getStatus());
                    return ResponseEntity.status(HttpStatus.CREATED).body(tx);
                });
    }

    @GetMapping("/transactions")
    public Flux<Transaction> list(@RequestParam String accountNumber) {

        log.info("Listing transactions for account: {}", accountNumber);

        return service.byAccount(accountNumber)
                .doOnComplete(() -> log.info("Transaction list completed"));
    }

    @GetMapping(value = "/stream/transactions",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Transaction>> stream() {

        log.info("New SSE client connected");

        return service.stream()
                .doOnCancel(() -> log.info("SSE client disconnected"))
                .doOnComplete(() -> log.info("SSE stream completed"));
    }
}
