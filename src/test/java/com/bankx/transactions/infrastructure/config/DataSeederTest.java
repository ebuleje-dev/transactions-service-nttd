package com.bankx.transactions.infrastructure.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bankx.transactions.domain.model.Account;
import com.bankx.transactions.domain.model.RiskRule;
import com.bankx.transactions.domain.repository.AccountRepository;
import com.bankx.transactions.domain.repository.RiskRuleRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

/**
 * Unit tests for DataSeeder.
 * Tests the seeding of risk rules and accounts on application startup.
 */
class DataSeederTest {

  @Mock
  private RiskRuleRepository riskRuleRepository;

  @Mock
  private AccountRepository accountRepository;

  @InjectMocks
  private DataSeeder dataSeeder;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void runSeedsRiskRulesAndAccounts() {
    // Arrange - Mock RiskRule saving
    RiskRule penRule = RiskRule.builder()
        .id(1L)
        .currency("PEN")
        .maxDebitPerTx(new BigDecimal("1500"))
        .build();

    RiskRule usdRule = RiskRule.builder()
        .id(2L)
        .currency("USD")
        .maxDebitPerTx(new BigDecimal("500"))
        .build();

    when(riskRuleRepository.save(any(RiskRule.class)))
        .thenReturn(penRule)
        .thenReturn(usdRule);

    // Arrange - Mock Account operations
    Account account1 = Account.builder()
        .number("001-0001")
        .holderName("Ana Peru")
        .currency("PEN")
        .balance(new BigDecimal("2000"))
        .build();

    Account account2 = Account.builder()
        .number("001-0002")
        .holderName("Luis Alvarado")
        .currency("PEN")
        .balance(new BigDecimal("800"))
        .build();

    Account account3 = Account.builder()
        .number("001-0003")
        .holderName("Maria Lopez")
        .currency("USD")
        .balance(new BigDecimal("1000"))
        .build();

    when(accountRepository.deleteAll()).thenReturn(Mono.empty());
    when(accountRepository.save(any(Account.class)))
        .thenReturn(Mono.just(account1))
        .thenReturn(Mono.just(account2))
        .thenReturn(Mono.just(account3));

    // Act
    dataSeeder.run();

    // Assert - Verify risk rules were saved
    verify(riskRuleRepository, times(2)).save(any(RiskRule.class));

    // Assert - Verify accounts were deleted and saved
    verify(accountRepository, times(1)).deleteAll();
    verify(accountRepository, times(3)).save(any(Account.class));
  }

  @Test
  void runWithEmptyArguments() {
    // Arrange
    RiskRule penRule = RiskRule.builder()
        .id(1L)
        .currency("PEN")
        .maxDebitPerTx(new BigDecimal("1500"))
        .build();

    when(riskRuleRepository.save(any(RiskRule.class))).thenReturn(penRule);
    when(accountRepository.deleteAll()).thenReturn(Mono.empty());
    when(accountRepository.save(any(Account.class)))
        .thenReturn(Mono.just(Account.builder().build()));

    // Act - Run with empty args
    dataSeeder.run(new String[]{});

    // Assert - Should still execute seeding
    verify(riskRuleRepository, times(2)).save(any(RiskRule.class));
    verify(accountRepository, times(1)).deleteAll();
  }

  @Test
  void runWithNullArguments() {
    // Arrange
    RiskRule penRule = RiskRule.builder()
        .id(1L)
        .currency("PEN")
        .maxDebitPerTx(new BigDecimal("1500"))
        .build();

    when(riskRuleRepository.save(any(RiskRule.class))).thenReturn(penRule);
    when(accountRepository.deleteAll()).thenReturn(Mono.empty());
    when(accountRepository.save(any(Account.class)))
        .thenReturn(Mono.just(Account.builder().build()));

    // Act - Run with null args (varargs can be null)
    dataSeeder.run((String[]) null);

    // Assert - Should still execute seeding
    verify(riskRuleRepository, times(2)).save(any(RiskRule.class));
    verify(accountRepository, times(1)).deleteAll();
  }
}
