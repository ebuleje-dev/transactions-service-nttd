package com.bankx.transactions.domain.repository;

import com.bankx.transactions.domain.model.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveMongoRepository<Account, String> {

    Mono<Account> findByNumber(String number);
}
