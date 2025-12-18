package com.bankx.transactions.domain.repository;

import com.bankx.transactions.domain.model.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {

    Flux<Transaction> findByAccountIdOrderByTimestampDesc(String accountId);
}
