package com.bankx.transactions.domain.repository;

import com.bankx.transactions.domain.model.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * Reactive repository for managing Transaction entities in MongoDB.
 * Provides CRUD operations and custom query methods for transaction management.
 */
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {

  /**
   * Finds all transactions for a specific account, ordered by timestamp descending.
   *
   * @param accountId the account identifier to search for
   * @return a Flux emitting transactions for the account, newest first
   */
  Flux<Transaction> findByAccountIdOrderByTimestampDesc(String accountId);
}
