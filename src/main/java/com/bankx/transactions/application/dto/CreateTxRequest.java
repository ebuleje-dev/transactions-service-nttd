package com.bankx.transactions.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new transaction.
 * Contains account number, transaction type, and amount with validation constraints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTxRequest {

  @NotBlank(message = "Account number is required")
  private String accountNumber;

  @NotBlank(message = "Transaction type is required")
  private String type;  // "CREDIT" o "DEBIT"

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
  private BigDecimal amount;
}
