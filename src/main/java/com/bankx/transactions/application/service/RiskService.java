package com.bankx.transactions.application.service;

import com.bankx.transactions.domain.model.RiskRule;
import com.bankx.transactions.domain.repository.RiskRuleRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Service for risk validation using JPA-based risk rules.
 * Wraps blocking JPA calls in reactive context using bounded elastic scheduler.
 */
@Service
@RequiredArgsConstructor
public class RiskService {

  private final RiskRuleRepository riskRepo;

  /**
   * Validates if a transaction is allowed based on risk rules.
   *
   * @param currency the transaction currency
   * @param type the transaction type (DEBIT or CREDIT)
   * @param amount the transaction amount
   * @return a Mono emitting true if allowed, false otherwise
   */
  public Mono<Boolean> isAllowed(String currency, String type, BigDecimal amount) {

    return Mono.fromCallable(() ->

            riskRepo.findFirstByCurrency(currency)
                .map(RiskRule::getMaxDebitPerTx)
                .orElse(new BigDecimal("0"))
        )
        .subscribeOn(Schedulers.boundedElastic())

        .map(maxDebit -> {
          if ("DEBIT".equalsIgnoreCase(type)) {
            return amount.compareTo(maxDebit) <= 0;
          }
          return true;
        });
  }
}
