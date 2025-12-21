package com.bankx.transactions.infrastructure.config;

import com.bankx.transactions.domain.model.Account;
import com.bankx.transactions.domain.model.RiskRule;
import com.bankx.transactions.domain.repository.AccountRepository;
import com.bankx.transactions.domain.repository.RiskRuleRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Seeds initial data for risk rules and accounts.
 * Executes on application startup to populate test data.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final RiskRuleRepository riskRepo;
  private final AccountRepository accountRepo;

  @Override
  public void run(String... args) {
    seedRiskRules();
    seedAccounts();
  }

  private void seedRiskRules() {
    log.info("Seeding risk rules (H2)...");

    RiskRule penRule = riskRepo.save(
        RiskRule.builder()
            .currency("PEN")
            .maxDebitPerTx(new BigDecimal("1500"))
            .build()
    );
    log.info("Risk rule created: {} - Max DEBIT: {}",
        penRule.getCurrency(), penRule.getMaxDebitPerTx());

    RiskRule usdRule = riskRepo.save(
        RiskRule.builder()
            .currency("USD")
            .maxDebitPerTx(new BigDecimal("500"))
            .build()
    );
    log.info("Risk rule created: {} - Max DEBIT: {}",
        usdRule.getCurrency(), usdRule.getMaxDebitPerTx());
  }

  private void seedAccounts() {
    log.info("Seeding accounts (MongoDB)...");

    accountRepo.deleteAll()
        .thenMany(Flux.just(
            Account.builder()
                .number("001-0001")
                .holderName("Ana Peru")
                .currency("PEN")
                .balance(new BigDecimal("2000"))
                .build(),

            Account.builder()
                .number("001-0002")
                .holderName("Luis Alvarado")
                .currency("PEN")
                .balance(new BigDecimal("800"))
                .build(),

            Account.builder()
                .number("001-0003")
                .holderName("Maria Lopez")
                .currency("USD")
                .balance(new BigDecimal("1000"))
                .build()
        ))
        .flatMap(accountRepo::save)
        .doOnNext(acc -> log.info("Account created: {} - {} - Balance: {}",
            acc.getNumber(), acc.getHolderName(), acc.getBalance()))
        .blockLast();

    log.info("Data seeding completed!");
  }
}
