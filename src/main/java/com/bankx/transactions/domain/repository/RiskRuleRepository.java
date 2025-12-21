package com.bankx.transactions.domain.repository;

import com.bankx.transactions.domain.model.RiskRule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for managing RiskRule entities in H2 database.
 * Provides CRUD operations and custom query methods for risk rule validation.
 */
public interface RiskRuleRepository extends JpaRepository<RiskRule, Long> {

  /**
   * Finds the first risk rule for a specific currency.
   *
   * @param currency the currency code to search for
   * @return an Optional containing the risk rule if found, or empty if not found
   */
  Optional<RiskRule> findFirstByCurrency(String currency);
}
