package com.bankx.transactions.infrastructure.config;

import com.bankx.transactions.domain.model.Transaction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

/**
 * Configuration for reactive Sinks.
 * Provides Sink beans for event broadcasting in reactive flows.
 */
@Configuration
public class SinkConfig {

  /**
   * Creates a multicast Sink for broadcasting transaction events.
   *
   * @return a Sink for Transaction events with backpressure buffering
   */
  @Bean
  public Sinks.Many<Transaction> txSink() {
    return Sinks.many()
        .multicast()
        .onBackpressureBuffer();
  }
}
