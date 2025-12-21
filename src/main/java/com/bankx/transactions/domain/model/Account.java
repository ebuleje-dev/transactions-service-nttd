package com.bankx.transactions.domain.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a bank account in the system.
 * Stores account details including number, holder name, currency, and current balance.
 */
@Document(collection = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
  @Id
  private String id;

  private String number;
  private String holderName;
  private String currency;
  private BigDecimal balance;
}
