package com.bankx.transactions.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "risk_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String currency;
    private BigDecimal maxDebitPerTx;
}
