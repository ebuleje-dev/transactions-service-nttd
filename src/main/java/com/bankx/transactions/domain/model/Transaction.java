package com.bankx.transactions.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a financial transaction in the system.
 * Stores transaction details including amount, type, timestamp, and processing status.
 */
@Document(collection = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
  @Id
  private String id;

  private String accountId;
  private String type;
  private BigDecimal amount;
  private Instant timestamp;
  private String status;
  private String reason;
}
