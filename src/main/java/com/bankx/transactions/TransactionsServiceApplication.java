package com.bankx.transactions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the banking transactions service.
 * Provides reactive transaction processing with MongoDB and H2 dual database architecture.
 */
@SpringBootApplication
public class TransactionsServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(TransactionsServiceApplication.class, args);
  }

}
