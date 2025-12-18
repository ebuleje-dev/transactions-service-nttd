package com.bankx.transactions.application.service;

import com.bankx.transactions.domain.model.RiskRule;
import com.bankx.transactions.domain.repository.RiskRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RiskService {

    private final RiskRuleRepository riskRepo;

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
