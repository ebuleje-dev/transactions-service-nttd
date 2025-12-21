package com.bankx.transactions.domain.repository;

import com.bankx.transactions.domain.model.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for managing Account entities in MongoDB.
 * Provides CRUD operations and custom query methods for account management.
 */
public interface AccountRepository extends ReactiveMongoRepository<Account, String> {

  /**
   * Finds an account by its account number.
   *
   * @param number the account number to search for
   * @return a Mono emitting the account if found, or empty if not found
   */
  Mono<Account> findByNumber(String number);
}
